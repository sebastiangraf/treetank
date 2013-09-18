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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.FilelistenerWriteTrx;
import org.treetank.api.IFilelistenerWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.ResourceNotExistingException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Andreas Rain
 */
public class Filelistener {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Filelistener.class);

	/** The watchservice from java to watch the different paths. */
	private final WatchService mWatcher;
	/** A map consisting of the paths that are being watched. */
	private final Map<WatchKey, Path> mKeyPaths = new ConcurrentHashMap<WatchKey, Path>();

	/**
	 * A map that consists of the storageName as the key and the watched folder
	 * path for the storage.
	 */
	private static Map<String, String> mFilelistenerToPaths;
	/**
	 * A map that consists of the storageName as the key and the session from
	 * treetank for the storage.
	 */
	private Map<String, ISession> mSessions;
	/**
	 * A map that consists of the storageName as the key and the page
	 * transaction from treetank for the storage.
	 */
	private Map<String, IFilelistenerWriteTrx> mTrx;
	/**
	 * This map holds all subdirectories and the registration with the
	 * watchservice
	 */
	private Map<String, List<String>> mSubDirectories;

	/**
	 * A map consisting of the services that handle tasks for different
	 * resources parallel
	 */
	private Map<String, ExecutorService> mExecutorMap;

	/** Map to keep track of filesystemnotifications */
	private Map<String, FilesystemNotification> mLockedFiles;
	/** Map to put FSN inside if a fsn is still locked in mLockedFiles */
	private Map<String, FilesystemNotification> mFsnOnHold;
	/** Observer class to be notified when a notification has been processed */
	private FilesystemNotificationObserver mObserver;

	/**
	 * This thread is used, so the program does not get blocked by the
	 * watchservice.
	 */
	private volatile Thread mProcessingThread;

	/**
	 * @throws IOException
	 */
	public Filelistener() throws IOException {
		this.mWatcher = FileSystems.getDefault().newWatchService();
		mSubDirectories = new HashMap<String, List<String>>();
		mExecutorMap = new HashMap<String, ExecutorService>();
		mLockedFiles = new HashMap<>();
		mFsnOnHold = new HashMap<>();
	}

	/**
	 * Get the observer for this filelistener
	 * 
	 * @return observer
	 */
	public FilesystemNotificationObserver getObserver() {
		return mObserver;
	}

	/**
	 * Set the observer for this filelistener
	 */
	public void setObserver(FilesystemNotificationObserver pObserver) {
		this.mObserver = pObserver;
	}

	/**
	 * @param dir
	 * @throws IOException
	 */
	public void watchDir(File dir) throws IOException {
		Path p = dir.toPath();
		WatchKey key = p.register(mWatcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		mKeyPaths.put(key, p);
	}

	/**
	 * Start listening to the defined folders.
	 * 
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ResourceNotExistingException
	 * @throws TTException
	 */
	public void startListening() throws FileNotFoundException,
			ClassNotFoundException, IOException, ResourceNotExistingException,
			TTException {
		mProcessingThread = new Thread() {
			public void run() {
				try {
					processFileNotifications();
				} catch (InterruptedException | TTException | IOException e) {
				}
			}
		};
		mProcessingThread.start();

		initSessions();
	}

	/**
	 * This method is used to initialize a session with treetank for every
	 * storage configuration thats in the database.
	 * 
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ResourceNotExistingException
	 * @throws TTException
	 */
	private void initSessions() throws FileNotFoundException,
			ClassNotFoundException, IOException, ResourceNotExistingException,
			TTException {
		Map<String, String> filelisteners = getFilelisteners();
		mSessions = new HashMap<String, ISession>();
		mTrx = new HashMap<String, IFilelistenerWriteTrx>();

		if (filelisteners.isEmpty()) {
			return;
		}

		for (Entry<String, String> e : filelisteners.entrySet()) {
			mSessions.put(e.getKey(), StorageManager.getSession(e.getKey()));
			mTrx.put(e.getKey(),
					new FilelistenerWriteTrx(mSessions.get(e.getKey())
							.beginBucketWtx(), mSessions.get(e.getKey())));
			mSubDirectories.put(e.getValue(), new ArrayList<String>());
			mExecutorMap.put(e.getValue(), Executors.newSingleThreadExecutor());

			List<String> subDirs = mSubDirectories.get(e.getValue());

			for (String s : mTrx.get(e.getKey()).getFilePaths()) {
				String fullFilePath = new StringBuilder().append(e.getValue())
						.append(File.separator).append(s).toString();
				subDirs.add(fullFilePath);

				Path p = Paths.get(fullFilePath);

				watchParents(p, e.getValue());
			}
		}
	}

	/**
	 * Watch parent folders of this file until the root listener path has been
	 * reached.
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
	private void releaseSessions() throws TTException {
		if (mSessions == null) {
			return;
		}

		// Closing all transactions.
		try {
			for (IFilelistenerWriteTrx trx : mTrx.values()) {
				trx.close();
			}
		} catch (IllegalStateException ise) {
			ise.printStackTrace();
		}

		// Closing all storages aswell.
		for (ISession s : mSessions.values()) {
			s.close();
		}
	}

	/**
	 * Shutdown listening to the defined folders and release all bonds to
	 * Treetank.
	 * 
	 * @throws TTException
	 * @throws IOException
	 */
	public void shutDownListener() throws TTException, IOException {
		for (ExecutorService s : mExecutorMap.values()) {
			s.shutdown();

			while (!s.isTerminated()) {
				// Do nothing.
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOGGER.error(e.getStackTrace().toString());
				}
			}
		}

		Thread thr = mProcessingThread;
		if (thr != null) {
			thr.interrupt();
		}

		mWatcher.close();

		releaseSessions();
	}

	/**
	 * In this method the notifications of the filesystem if anything changed in
	 * a folder that the system is listening to are being extracted and
	 * processed.
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TTException
	 */
	private void processFileNotifications() throws InterruptedException,
			TTException, IOException {
		while (true) {
			WatchKey key = mWatcher.take();
			Path dir = mKeyPaths.get(key);
			for (WatchEvent<?> evt : key.pollEvents()) {
				WatchEvent.Kind<?> eventType = evt.kind();
				if (eventType == OVERFLOW)
					continue;
				Object o = evt.context();
				if (o instanceof Path) {
					Path path = dir.resolve((Path) evt.context());
					process(dir, path, eventType);
				}
			}
			key.reset();

			processFsnOnHold();
		}
	}

	private void processFsnOnHold() {
		// Maybe lockedfiles are finished
		// synchronized (mFsnOnHold) {
		// for (String s : mFsnOnHold
		// .keySet()) {
		// if (mLockedFiles.get(s) != null
		// && mLockedFiles.get(s)
		// .isFinished()) {
		// mLockedFiles.remove(s);
		//
		// System.out.println("fsnonhold "
		// + s);
		// ExecutorService service = mExecutorMap.get(s);
		// if (service != null && !service.isShutdown()) {
		// mLockedFiles.put(s, mFsnOnHold.get(s));
		// mFsnOnHold.remove(s);
		// service.submit(mLockedFiles.get(s));
		// }
		// }
		// }
		// }

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
	private void process(Path dir, Path file, WatchEvent.Kind<?> evtType)
			throws TTException, IOException, InterruptedException {
//		LOGGER.info("Processing " + file.getFileName() + " with event "
//				+ evtType);
		IFilelistenerWriteTrx trx = null;
		String rootPath = getListenerRootPath(dir);

		String relativePath = file.toFile().getAbsolutePath();
		relativePath = relativePath.substring(
				getListenerRootPath(dir).length(), relativePath.length());

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
//			if (mLockedFiles.get(rootPath + File.separator
//					+ file.toFile().getName()) != null) {
//				if (mLockedFiles.get(
//						rootPath + File.separator + file.toFile().getName())
//						.isFinished()) {
//					ExecutorService s = mExecutorMap
//							.get(getListenerRootPath(dir));
//					if (s != null && !s.isShutdown()) {
//
//						FilesystemNotification n = new FilesystemNotification(
//								file.toFile(), relativePath, rootPath, evtType,
//								trx);
//						if (mObserver != null) {
//							n.addObserver(mObserver);
//						}
//						mFsnOnHold.remove(rootPath + File.separator
//								+ file.toFile().getName());
//						mLockedFiles.put(rootPath + File.separator
//								+ file.toFile().getName(), n);
//						s.submit(n);
//					}
//				} else {
//					FilesystemNotification n = new FilesystemNotification(
//							file.toFile(), relativePath, rootPath, evtType, trx);
//					if (mObserver != null) {
//						n.addObserver(mObserver);
//					}
//					mFsnOnHold.put(rootPath + File.separator
//							+ file.toFile().getName(), n);
//				}
//			} else {
				ExecutorService s = mExecutorMap.get(getListenerRootPath(dir));
				if (s != null && !s.isShutdown()) {
					FilesystemNotification n = new FilesystemNotification(
							file.toFile(), relativePath, rootPath, evtType, trx);
					if (mObserver != null) {
						n.addObserver(mObserver);
					}
//					mLockedFiles.put(rootPath + File.separator
//							+ file.toFile().getName(), n);

					s.submit(n);
//				}
			}
		}

	}

	/**
	 * In this method a subdirectory is being added to the system and watched.
	 * 
	 * This is necessary since the {@link WatchService} doesn't support watching
	 * a folder with higher depths than 1.
	 * 
	 * @param root
	 * @param filePath
	 * @throws IOException
	 */
	private void addSubDirectory(Path root, Path filePath) throws IOException {
		String listener = getListenerRootPath(root);

		List<String> listeners = mSubDirectories.get(listener);

		if (listeners != null) {
			if (mSubDirectories.get(listener).contains(
					filePath.toAbsolutePath())) {
				return;
			} else {
				mSubDirectories.get(listener).add(filePath.toString());
			}

			try {
				watchDir(filePath.toFile());
			} catch (IOException e) {
				throw new IOException("Could not watch the subdirectories.", e);
			}
		}
	}

	/**
	 * This utility method allows you to get the root path for a subdirectory.
	 * 
	 * The root path is a directory that has been explicitly listened to and not
	 * just recursively.
	 * 
	 * @param root
	 * @return returns the root path as a String
	 */
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
	 * A utility method to get all filelisteners that are already defined and
	 * stored.
	 * 
	 * @return returns a map of relative paths to the folders as the keyset and
	 *         the resourcenames that point to the configurations in the
	 *         valueset.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Map<String, String> getFilelisteners()
			throws FileNotFoundException, IOException, ClassNotFoundException {

		mFilelistenerToPaths = new HashMap<String, String>();

		File listenerFilePaths = new File(StorageManager.ROOT_PATH
				+ File.separator + "mapping.data");

		getFileListenersFromSystem(listenerFilePaths);

		return mFilelistenerToPaths;
	}

	/**
	 * Add a new filelistener to the system.
	 * 
	 * @param pResourcename
	 * @param pListenerPath
	 * @return return true if there has been a success.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean addFilelistener(String pResourcename,
			String pListenerPath) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		mFilelistenerToPaths = new HashMap<String, String>();

		File listenerFilePaths = new File(StorageManager.ROOT_PATH
				+ File.separator + "mapping.data");

		getFileListenersFromSystem(listenerFilePaths);

		mFilelistenerToPaths.put(pResourcename, pListenerPath);

		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		for (Entry<String, String> e : mFilelistenerToPaths.entrySet()) {
			output.write((e.getKey() + "\n").getBytes());
			output.write((e.getValue() + "\n").getBytes());
		}

		java.nio.file.Files.write(listenerFilePaths.toPath(),
				output.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING);

		return true;
	}

	/**
	 * You can remove a filelistener from the system identifying it with it's
	 * Storagename.
	 * 
	 * The listeners have to be shutdown to do this task.
	 * 
	 * @param pResourcename
	 * @return true if resource could be removed
	 * @throws IOException
	 * @throws TTException
	 * @throws ResourceNotExistingException
	 */
	public boolean removeFilelistener(String pResourcename) throws IOException,
			TTException, ResourceNotExistingException {
		mFilelistenerToPaths = new HashMap<String, String>();

		File listenerFilePaths = new File(StorageManager.ROOT_PATH
				+ File.separator + "mapping.data");

		getFileListenersFromSystem(listenerFilePaths);

		mFilelistenerToPaths.remove(pResourcename);

		StorageManager.removeResource(pResourcename);

		ByteArrayDataOutput output = ByteStreams.newDataOutput();
		for (Entry<String, String> e : mFilelistenerToPaths.entrySet()) {
			output.write((e.getKey() + "\n").getBytes());
			output.write((e.getValue() + "\n").getBytes());
		}

		java.nio.file.Files.write(listenerFilePaths.toPath(),
				output.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING);

		return true;
	}

	/**
	 * This is a helper method that let's you initialize mFilelistenerToPaths
	 * with all the filelisteners that have been stored in the filesystem.
	 * 
	 * @param pListenerFilePaths
	 * @throws IOException
	 */
	private static void getFileListenersFromSystem(File pListenerFilePaths)
			throws IOException {
		if (!pListenerFilePaths.exists()) {
			java.nio.file.Files.createFile(pListenerFilePaths.toPath());
		} else {
			byte[] bytes = java.nio.file.Files.readAllBytes(pListenerFilePaths
					.toPath());

			ByteArrayDataInput input = ByteStreams.newDataInput(bytes);

			String key;
			while ((key = input.readLine()) != null) {
				String val = input.readLine();

				mFilelistenerToPaths.put(key, val);
			}
		}
	}

	/**
	 * Get the desired transaction. Primarily used by the workers to operate.
	 * 
	 * @param key
	 * @return the transaction for the given resource key
	 */
	public synchronized IFilelistenerWriteTrx getTrx(String key) {
		return mTrx.get(key);
	}

}
