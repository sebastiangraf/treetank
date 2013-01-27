package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.treetank.access.FilelistenerWriteTrx;
import org.treetank.api.IFilelistenerWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageNotExistingException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Andreas Rain
 */
public class Filelistener {

    /** The watchservice from java to watch the different paths. */
    private final WatchService mWatcher;
    /** A map consisting of the paths that are being watched. */
    private final Map<WatchKey, Path> mKeyPaths = new ConcurrentHashMap<WatchKey, Path>();

    /** A map that consists of the storageName as the key and the watched folder path for the storage. */
    private static Map<String, String> mFilelistenerToPaths;
    /** A map that consists of the storageName as the key and the session from treetank for the storage. */
    private Map<String, ISession> mSessions;
    /**
     * A map that consists of the storageName as the key and the page transaction from treetank for the
     * storage.
     */
    private Map<String, IFilelistenerWriteTrx> mTrx;
    /** This map holds all subdirectories and the registration with the watchservice */
    private Map<String, List<String>> mSubDirectories;
    /** A map consisting of all the workers that work on the different folders */
    private Map<String, FilesystemNotificationQueueWorker> mWorkers;
    /** ExecutorService for the queue workers */
    private ExecutorService mExecutor;

    /**
     * This thread is used, so the program does not get blocked by the
     * watchservice.
     */
    private volatile Thread mProcessingThread;

    public Filelistener() throws IOException {
        this.mWatcher = FileSystems.getDefault().newWatchService();
        mSubDirectories = new HashMap<String, List<String>>();
        mWorkers = new HashMap<String, FilesystemNotificationQueueWorker>();
        mExecutor = Executors.newCachedThreadPool();
    }

    public void watchDir(File dir) throws IOException {
        Path p = dir.toPath();
        WatchKey key = p.register(mWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        mKeyPaths.put(key, p);
    }

    /**
     * Start listening to the defined folders.
     * 
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws StorageNotExistingException
     * @throws TTException
     */
    public void startListening() throws FileNotFoundException, ClassNotFoundException, IOException,
        StorageNotExistingException, TTException {
        mProcessingThread = new Thread() {
            public void run() {
                try {
                    processFileNotifications();
                } catch (InterruptedException ex) {
                    mProcessingThread = null;
                } catch (TTException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        mProcessingThread.start();

        initSessions();
    }

    /**
     * This method is used to initialize a session with treetank
     * for every storage configuration thats in the database.
     * 
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws StorageNotExistingException
     * @throws TTException
     */
    private void initSessions() throws FileNotFoundException, ClassNotFoundException, IOException,
        StorageNotExistingException, TTException {
        Map<String, String> filelisteners = getFilelisteners();
        mSessions = new HashMap<String, ISession>();
        mTrx = new HashMap<String, IFilelistenerWriteTrx>();

        if (filelisteners.isEmpty()) {
            return;
        }

        for (Entry<String, String> e : filelisteners.entrySet()) {
            mSessions.put(e.getKey(), StorageManager.getSession(e.getKey()));
            mTrx.put(e.getKey(), new FilelistenerWriteTrx(mSessions.get(e.getKey())
                .beginPageWriteTransaction(), mSessions.get(e.getKey())));
            mSubDirectories.put(e.getValue(), new ArrayList<String>());
            mWorkers.put(e.getValue(), new FilesystemNotificationQueueWorker(this));

            List<String> subDirs = mSubDirectories.get(e.getValue());

            for (String s : mTrx.get(e.getKey()).getFilePaths()) {
                String fullFilePath =
                    new StringBuilder().append(e.getValue()).append(File.separator).append(s).toString();
                subDirs.add(fullFilePath);

                Path p = Paths.get(fullFilePath);

                watchParents(p, e.getValue());
            }
        }

        for (FilesystemNotificationQueueWorker worker : mWorkers.values()) {
            mExecutor.submit(worker);
        }
    }

    /**
     * Watch parent folders of this file until the
     * root listener path has been reached.
     * 
     * @param p
     * @param until
     * @throws IOException
     */
    private void watchParents(Path p, String until) throws IOException {
        if (p.getParent() != null && !until.equals(p.getParent().toString())) {
            watchDir(p.getParent().toFile());
            watchParents(p.getParent(), until);
        }
    }

    /**
     * Release transactions and session from treetank.
     * 
     * @throws TTException
     */
    public void releaseSessions() throws TTException {
        if (mSessions == null) {
            return;
        }

        // Closing all transactions.
        for (IFilelistenerWriteTrx trx : mTrx.values()) {
            trx.close();
        }

        // Closing all storages aswell.
        for (ISession s : mSessions.values()) {
            s.close();
        }
    }

    /**
     * Shutdown listening to the defined folders and release all
     * bonds to Treetank.
     */
    public void shutDownListener() throws TTException {
        mExecutor.shutdown();

        Thread thr = mProcessingThread;
        if (thr != null) {
            thr.interrupt();
        }

        releaseSessions();
    }

    /**
     * In this method the notifications of the filesystem
     * if anything changed in a folder that the system is listening to
     * are being extracted and processed.
     * 
     * @throws InterruptedException
     * @throws IOException
     * @throws TTException
     */
    private void processFileNotifications() throws InterruptedException, TTException, IOException {
        while (true) {
            WatchKey key = mWatcher.take();
            Path dir = mKeyPaths.get(key);
            for (WatchEvent<?> evt : key.pollEvents()) {
                WatchEvent.Kind<?> eventType = evt.kind();
                if (eventType == OVERFLOW)
                    continue;
                Object o = evt.context();
                if (o instanceof Path) {
                    Path path = dir.resolve((Path)evt.context());

                    process(dir, path, eventType);
                }
            }
            key.reset();
        }
    }

    /**
     * This method is used to process the file system modifications.
     * 
     * @param dir
     * @param file
     * @param evtType
     * @throws IOException
     * @throws TTException
     * @throws InterruptedException
     */
    private void process(Path dir, Path file, WatchEvent.Kind<?> evtType) throws TTException, IOException,
        InterruptedException {

        IFilelistenerWriteTrx trx = null;

        String relativePath = file.toFile().getAbsolutePath();
        relativePath = relativePath.substring(getListenerRootPath(dir).length(), relativePath.length());

        for (Entry<String, String> e : mFilelistenerToPaths.entrySet()) {
            if (e.getValue().equals(getListenerRootPath(dir))) {
                trx = mTrx.get(e.getKey());
            }
        }

        if (file.toFile().isDirectory()) {
            if (evtType == ENTRY_CREATE) {
                addSubDirectory(dir, file);
                return;
            } else if (evtType == ENTRY_DELETE) {
                for (String s : trx.getFilePaths()) {
                    if (s.contains(relativePath)) {
                        trx.removeFile(s);
                    }
                }
            }
        } else {
            FilesystemNotificationQueueWorker worker = mWorkers.get(getListenerRootPath(dir));
            if (worker != null) {
                synchronized (worker) {
                    FilesystemNotification n =
                        new FilesystemNotification(file.toFile(), relativePath, getListenerRootPath(dir),
                            evtType);
                    worker.getQueue().offer(n);

                    while (!worker.getQueue().contains(n))
                        ;
                    worker.notify();
                }
            }
        }

    }

    private void addSubDirectory(Path root, Path filePath) {
        String listener = getListenerRootPath(root);

        List<String> listeners = mSubDirectories.get(listener);

        if (listeners != null) {
            if (mSubDirectories.get(listener).contains(filePath.toAbsolutePath())) {
                return;
            } else {
                mSubDirectories.get(listener).add(filePath.toString());
            }

            try {
                watchDir(filePath.toFile());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private String getListenerRootPath(Path root) {
        String listener = "";

        for (String s : mFilelistenerToPaths.values()) {
            if (root.toString().contains(s)) {
                listener = s;
            }
        }

        return listener;
    }

    /**
     * A utility method to get all filelisteners that are already defined and stored.
     * 
     * @return returns a map of relative paths to the folders as the keyset and
     *         the storagenames that point to the configurations in the valueset.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Map<String, String> getFilelisteners() throws FileNotFoundException, IOException,
        ClassNotFoundException {

        mFilelistenerToPaths = new HashMap<String, String>();

        File listenerFilePaths = new File(StorageManager.ROOT_PATH + File.separator + "mapping.data");

        if (!listenerFilePaths.exists()) {
            java.nio.file.Files.createFile(listenerFilePaths.toPath());
        } else {
            byte[] bytes = java.nio.file.Files.readAllBytes(listenerFilePaths.toPath());

            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            String key;
            while ((key = input.readLine()) != null) {
                String val = input.readLine();

                mFilelistenerToPaths.put(key, val);
            }
        }

        return mFilelistenerToPaths;
    }

    /**
     * Determine whether or not the system still operates on the directory.
     * 
     * @param dir
     * @return
     */
    public boolean isWorking(Path dir) {
        synchronized (mWorkers.get(getListenerRootPath(dir))) {
            if (mWorkers.get(getListenerRootPath(dir)).isWorking()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Add a new filelistener to the system.
     * 
     * @param storageName
     * @param listenerPath
     * @return return true if there has been a success.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static boolean addFilelistener(String storageName, String listenerPath)
        throws FileNotFoundException, IOException, ClassNotFoundException {
        mFilelistenerToPaths = new HashMap<String, String>();

        File listenerFilePaths = new File(StorageManager.ROOT_PATH + File.separator + "mapping.data");

        if (!listenerFilePaths.exists()) {
            java.nio.file.Files.createFile(listenerFilePaths.toPath());

            mFilelistenerToPaths.put(storageName, listenerPath);
        } else {
            byte[] bytes = java.nio.file.Files.readAllBytes(listenerFilePaths.toPath());

            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            String key;
            while ((key = input.readLine()) != null) {
                String val = input.readLine();

                mFilelistenerToPaths.put(key, val);
            }

            mFilelistenerToPaths.put(storageName, listenerPath);
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        for (Entry<String, String> e : mFilelistenerToPaths.entrySet()) {
            output.write((e.getKey() + "\n").getBytes());
            output.write((e.getValue() + "\n").getBytes());
        }

        java.nio.file.Files.write(listenerFilePaths.toPath(), output.toByteArray(),
            StandardOpenOption.TRUNCATE_EXISTING);

        return true;
    }

    /**
     * Get the desired transaction. Primarily used by the workers to
     * operate.
     * 
     * @param key
     * @return
     */
    public synchronized IFilelistenerWriteTrx getTrx(String key) {
        return mTrx.get(key);
    }

}
