package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.treetank.filelistener.api.file.IWatchCallback;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

public class Filelistener {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keyPaths = new ConcurrentHashMap<WatchKey, Path>();

    private static Map<String, String> filelistenerToPaths;

    /**
     * This thread is used, so the program does not get blocked by the
     * watchservice.
     */
    private volatile Thread processingThread;

    public Filelistener() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
    }

    public void watchDir(File dir) throws IOException {
        Path p = dir.toPath();
        WatchKey key = p.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keyPaths.put(key, p);
    }

    public void startListening() {
        processingThread = new Thread() {
            public void run() {
                try {
                    processFileNotifications();
                } catch (InterruptedException ex) {

                }
            }
        };
        processingThread.start();
    }

    public void shutDownListener() {
        Thread thr = processingThread;
        if (thr != null) {
            thr.interrupt();
        }
    }

    private void processFileNotifications() throws InterruptedException {
        while (true) {
            WatchKey key = watcher.take();
            Path dir = keyPaths.get(key);
            for (WatchEvent<?> evt : key.pollEvents()) {
                WatchEvent.Kind eventType = evt.kind();
                if (eventType == OVERFLOW)
                    continue;
                Object o = evt.context();
                if (o instanceof Path) {
                    Path path = (Path)o;
                    process(dir, path, eventType);
                }
            }
            key.reset();
        }
    }

    private void process(Path dir, Path file, WatchEvent.Kind evtType) {
        
    }

    public static Map<String, String> getFilelisteners() throws FileNotFoundException, IOException,
        ClassNotFoundException {
        
        filelistenerToPaths = new HashMap<String, String>();

        File listenerFilePaths = new File(StorageManager.ROOT_PATH + File.separator + "mapping.data");

        if (!listenerFilePaths.exists()) {
            java.nio.file.Files.createFile(listenerFilePaths.toPath());
        } else {
            byte[] bytes = java.nio.file.Files.readAllBytes(listenerFilePaths.toPath());

            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            String key;
            while ((key = input.readLine()) != null) {
                String val = input.readLine();

                filelistenerToPaths.put(key, val);
            }
        }

        return filelistenerToPaths;
    }

    public static boolean addFilelistener(String storageName, String listenerPath)
        throws FileNotFoundException, IOException, ClassNotFoundException {
        filelistenerToPaths = new HashMap<String, String>();

        File listenerFilePaths = new File(StorageManager.ROOT_PATH + File.separator + "mapping.data");

        if (!listenerFilePaths.exists()) {
            java.nio.file.Files.createFile(listenerFilePaths.toPath());

            filelistenerToPaths.put(storageName, listenerPath);
        } else {
            byte[] bytes = java.nio.file.Files.readAllBytes(listenerFilePaths.toPath());

            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

            String key;
            while ((key = input.readLine()) != null) {
                String val = input.readLine();

                filelistenerToPaths.put(key, val);
            }

            filelistenerToPaths.put(storageName, listenerPath);
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        for (Entry<String, String> e : filelistenerToPaths.entrySet()) {
            output.write((e.getKey() + "\n").getBytes());
            output.write((e.getValue() + "\n").getBytes());
        }

        java.nio.file.Files.write(listenerFilePaths.toPath(), output.toByteArray(),
            StandardOpenOption.TRUNCATE_EXISTING);
        
        return true;
    }

}
