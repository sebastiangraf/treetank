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
import org.treetank.api.IFilelistenerWriteTrx;
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

    private static final String createBenchFile = "target" + File.separator + "F";

    /** Filelistener resource */
    public static final String RESOURCE_1 = "bench53473ResourcegraveISCSI9283";

    /** Filelistener for the benchmark */
    private Filelistener filelistener;
    /** long array to track end time of file writes */
    long[] createEnds;
    /** long array to track end time of file reads */
    long[] readEnds;
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
        StorageManager.createResource(RESOURCE_1, new ModuleSetter().setDataFacClass(FileDataFactory.class)
            .setMetaFacClass(FilelistenerMetaDataFactory.class).createModule());
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
        int[] toExecute = new int[] {
            262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432
        };

        for (int i = 0; i < toExecute.length; i++) {
            setUp();
            bench(toExecute[i]);
            tearDown();
        }

    }

    private void bench(int filebenchSize) throws FileNotFoundException, ClassNotFoundException, IOException,
        ResourceNotExistingException, TTException, InterruptedException {
        // Listening to the target folder
        // Assert.assertNotNull(new File(BUCKETFOLDER).list());
        final File tmpdir = Files.createTempDir();
        Filelistener.addFilelistener(RESOURCE_1, tmpdir.getAbsolutePath());
        filelistener.watchDir(tmpdir);
        filelistener.startListening();

        // Benching creation of files on the filesystem and awaiting
        // finalization in treetank.

        createEnds = new long[FILES];
        readEnds = new long[FILES];
        bucketCount = new long[FILES];
        for (int i = 0; i < FILES; i++) {
            String filename = tmpdir + File.separator + "file" + (i + 1) + ".data";
            fileMap.put(File.separator + "file" + (i + 1) + ".data", i);

            byte[] fileBytes = new byte[filebenchSize];
            Random rand = new Random(42 * (i + 1));
            rand.nextBytes(fileBytes);
            Files.write(fileBytes, new File(filename));

        }

        while (!finishedBench || !notifications.isEmpty()) {
            FilesystemNotification n = notifications.poll();
            if (n == null || n.getEvtType() == ENTRY_CREATE) {
                continue;
            }
            if (n.getRelativePath() != null) {
                createEnds[fileMap.get(n.getRelativePath())] = n.getTime();
                bucketCount[fileMap.get(n.getRelativePath())] = n.getBucketAmount();
                // System.out.println("Run finished.");
                if (fileMap.get(n.getRelativePath()) == FILES - 1)
                    finishedBench = true;
            }
        }
        
        if (finishedBench) {
            //All files in storage, time to check retrieval time
            IFilelistenerWriteTrx trx = filelistener.getTrx(RESOURCE_1);
            File file;
            long time;
            String filename;
            for (int i = 0; i < FILES; i++) {
                filename = File.separator + "file" + (i + 1) + ".data";
                time = System.currentTimeMillis();
                file = trx.getFullFile(filename);
                time = System.currentTimeMillis() - time;
                readEnds[i] = time;

            }
            
            // Do something, analyze
            printBench(filebenchSize + "");
        } else {
            fail("Bench was not finished but notified that it would be finished.");
        }

        filelistener.shutDownListener();
        IOUtils.recursiveDelete(tmpdir);
    }

    private void printBench(String string) throws IOException {
        while (string.length() < 8) {
            string = "0" + string;
        }
        String s = "";
        String s2 = "";
        String s3 = "";
        for (int i = 0; i < createEnds.length - 1; i++) {
            s   += createEnds[i] + ",";
            s2  += bucketCount[i] + ",";
            s3  += readEnds[i] + ",";

        }
        s       +=  createEnds[createEnds.length - 1];
        s2      += bucketCount[bucketCount.length - 1];
        s3      +=    readEnds[readEnds.length - 1];

        Files.write(s   + "\n", new File(createBenchFile + string + "T.csv"), Charset.forName("UTF-8"));
        Files.write(s2  + "\n", new File(createBenchFile + string + "B.csv"), Charset.forName("UTF-8"));
        Files.write(s3  + "\n", new File(createBenchFile + string + "R.csv"), Charset.forName("UTF-8"));
    }

    @Override
    public synchronized LinkedBlockingQueue<FilesystemNotification> getBlockingQueue() {
        return notifications;
    }

    @Override
    public synchronized void addNotification(FilesystemNotification n) {
        notifications.add(n);
    }

}
