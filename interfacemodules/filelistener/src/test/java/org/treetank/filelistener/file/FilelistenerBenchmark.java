package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import org.testng.annotations.Test;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.ResourceNotExistingException;
import org.treetank.filelistener.file.data.FileDataFactory;
import org.treetank.filelistener.file.data.FilelistenerMetaDataFactory;
import org.treetank.io.IOUtils;

import com.google.common.io.Files;

/**
 * This bench suit tests reads/writes on the filelistener for different
 * filesizes;
 * 
 * @author Andreas Rain
 * 
 */
public class FilelistenerBenchmark implements FilesystemNotificationObserver {

	private String benchFile = "target" + File.separator + "FileBench";

	/** Filelistener resource */
	private static final String RESOURCE_1 = "RESOURCE_1";
	private static final String BUCKETFOLDER = StorageManager.ROOT_PATH
			+ File.separator + "storage" + File.separator + "resources"
			+ File.separator + RESOURCE_1 + File.separator + "data"
			+ File.separator + "RESOURCE_1";

	/** Filelistener for the benchmark */
	private Filelistener filelistener;
	/** long array to track start time of file reads/writes */
	long[] starts;
	/** long array to track end time of file reads/writes */
	long[] ends;
	/** long array to track bucket counts */
	long[] bucketCount;
	/** Filename corresponding to array position */
	Map<String, Integer> fileMap;
	/** Still running */
	boolean finishedBench;
	/** BlockingQueue */
	private LinkedBlockingQueue<FilesystemNotification> notifications;

	public void setUp() throws Exception {
		IOUtils.recursiveDelete(new File(StorageManager.ROOT_PATH));
		StorageManager.createResource(RESOURCE_1,
				new ModuleSetter().setDataFacClass(FileDataFactory.class)
						.setMetaFacClass(FilelistenerMetaDataFactory.class)
						.createModule());
		filelistener = new Filelistener();
		filelistener.setObserver(this);
		fileMap = new HashMap<String, Integer>();
		finishedBench = false;
		notifications = new LinkedBlockingQueue<>();
	}

	public void tearDown() throws Exception {
		filelistener.removeFilelistener(RESOURCE_1);
	}

	private final static int FILES = 100;

	@Test
	public void realBench() throws Exception {
		int[] toExecute = new int[] {/* 262144, 524288, 1048576, 2097152,
				4194304, 8388608, */16777216, 33554432 };

		for (int i = 0; i < toExecute.length; i++) {
			setUp();
			bench(toExecute[i]);
			tearDown();
		}

	}

	private void bench(int filebenchSize) throws FileNotFoundException,
			ClassNotFoundException, IOException, ResourceNotExistingException,
			TTException, InterruptedException {
		// Listening to the target folder
//		Assert.assertNotNull(new File(BUCKETFOLDER).list());
		final File tmpdir = Files.createTempDir();
		Filelistener.addFilelistener(RESOURCE_1, tmpdir.getAbsolutePath());
		filelistener.watchDir(tmpdir);
		filelistener.startListening();

		// Benching creation of files on the filesystem and awaiting
		// finalization in treetank.
		starts = new long[FILES];
		ends = new long[FILES];
		bucketCount = new long[FILES];
		for (int i = 0; i < FILES; i++) {
			String filename = tmpdir + File.separator + "file" + (i + 1)
					+ ".data";
			fileMap.put(File.separator + "file" + (i + 1) + ".data", i);
			
			byte[] fileBytes = new byte[filebenchSize];
                        Random rand = new Random(42 * (i + 1));
                        rand.nextBytes(fileBytes);
			Files.write(fileBytes, new File(filename));
			
		}

		while (!finishedBench) {
			FilesystemNotification n = notifications.poll();
			if (n == null || n.getEvtType() == ENTRY_CREATE) {
				if (n != null)
					starts[fileMap.get(n.getRelativePath())] = System
							.currentTimeMillis();
				continue;
			}
			if (n.getRelativePath() != null) {
				ends[fileMap.get(n.getRelativePath())] = System
						.currentTimeMillis();
				bucketCount[fileMap.get(n.getRelativePath())] = bucketCount();
				if (fileMap.get(n.getRelativePath()) == FILES-1)
					finishedBench = true;
			}
		}
		if (finishedBench) {
			// Do something, analyze
			printBench(filebenchSize+"");
		} else {
			fail("Bench was not finished but notified that it would be finished.");
		}

		filelistener.shutDownListener();
		IOUtils.recursiveDelete(tmpdir);
	}

	private void printBench(String string) throws IOException {
		System.out.println("######################################");
		System.out.println("Finished bench with " + string + ".");
		System.out.println("######################################");
		for (int i = 0; i < starts.length; i++) {
			System.out.print("Run " + i + "\t");
		}
		System.out.println();
		String s = "";
		for (int i = 0; i < starts.length; i++) {
			s += (ends[i] - starts[i]) + ",";
		}
		String s2 = "";
		for (int i = 0; i < starts.length; i++) {
			s2 += bucketCount[i] + ",";
		}
		System.out.println(s);
		System.out.println(s2);

		Files.append(s + "\n", new File(benchFile + "_" + string + "time.csv"),
				Charset.forName("UTF-8"));
		Files.append(s2, new File(benchFile + "_" + string + "buckets.csv"),
				Charset.forName("UTF-8"));
		System.out.println();

	}

	private long bucketCount() {
	    File file = new File(BUCKETFOLDER);
	    if(file.list() != null){
	        return file.list().length;
	    }
	    
	    return 0;
	}

	@Override
	public synchronized LinkedBlockingQueue<FilesystemNotification> getBlockingQueue() {
		return notifications;
	}

}
