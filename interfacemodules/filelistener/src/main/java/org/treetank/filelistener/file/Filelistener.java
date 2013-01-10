package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.treetank.filelistener.api.file.IWatchCallback;

public class Filelistener {

	private final WatchService watcher;
	private final Map<WatchKey, Path> keyPaths = new ConcurrentHashMap();
	
	private static Map<String, String> filelistenerToPaths;

	/**
	 * This thread is used, so the program does not get blocked by the
	 * watchservice.
	 */
	private volatile Thread processingThread;

	/**
	 * This callback is used for upper layers to be able to react on changes in
	 * the filesystem without having to implement such methods as below.
	 */
	private final IWatchCallback callback;

	public Filelistener(IWatchCallback callback) throws IOException {
		this.callback = callback;
		this.watcher = FileSystems.getDefault().newWatchService();
	}

	public void watchDir(File dir) throws IOException {
		Path p = dir.toPath();
		WatchKey key = p.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
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
			for (WatchEvent evt : key.pollEvents()) {
				WatchEvent.Kind eventType = evt.kind();
				if (eventType == OVERFLOW)
					continue;
				Object o = evt.context();
				if (o instanceof Path) {
					Path path = (Path) o;
					process(dir, path, eventType);
				}
			}
			key.reset();
		}
	}

	private void process(Path dir, Path file, WatchEvent.Kind evtType) {
		callback.processFileSystemChanges(dir, file, evtType);
	}
	
	public static boolean addFilelistener(){
		if(filelistenerToPaths == null){
			
		}
		
		return false;
	}

}
