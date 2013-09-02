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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.exception.TTByteHandleException;
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

    /** Filelistener resource */
    private static final String RESOURCE_1 = "RESOURCE_1";
    private static final String BUCKETFOLDER = StorageManager.ROOT_PATH + File.separator + "storage"
        + File.separator + "resources" + File.separator + RESOURCE_1 + File.separator + "data";

    private final static Random rand = new Random(42);

    /** Temp dir for the test files */
    private final File TMPDIR_1 = Files.createTempDir();
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

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
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

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        filelistener.removeFilelistener(RESOURCE_1);
        TMPDIR_1.delete();
    }

    private final static int FILES = 10;

    @Test(dataProvider = "getFiles")
    public void bench(Class<Integer> clazz, Integer[] numbers) throws FileNotFoundException,
        ClassNotFoundException, IOException, ResourceNotExistingException, TTException, InterruptedException {
        // Listening to the target folder
        Assert.assertNotNull(new File(BUCKETFOLDER).list());
        Filelistener.addFilelistener(RESOURCE_1, TMPDIR_1.getAbsolutePath());
        filelistener.watchDir(TMPDIR_1);
        filelistener.startListening();

        final byte[][] fileBytes = new byte[FILES][];
        final int startFactor = numbers[0];
        final int numberOfFactor = numbers[1];

        // setting size for Files
        for (int i = 0; i < numberOfFactor; i++) {
            fileBytes[i] = new byte[startFactor * (i + 1)];
        }

        for (int j = 0; j < numberOfFactor; j++) {

            for (int i = 0; i < FILES; i++) {
                rand.nextBytes(fileBytes[i]);
            }

            // Benching creation of files on the filesystem and awaiting
            // finalization in treetank.
            starts = new long[FILES];
            ends = new long[FILES];
            bucketCount = new long[FILES];
            for (int i = 0; i < fileBytes.length; i++) {
                String filename = TMPDIR_1 + File.separator + "file" + (i + 1) + ".data";
                fileMap.put(File.separator + "file" + (i + 1) + ".data", i);
                Files.write(fileBytes[i], new File(filename));
            }

            while (!finishedBench) {
                FilesystemNotification n = notifications.poll();
                if (n == null || n.getEvtType() == ENTRY_CREATE) {
                    if (n != null)
                        starts[fileMap.get(n.getRelativePath())] = System.currentTimeMillis();
                    continue;
                }
                if (n.getRelativePath() != null) {
                    ends[fileMap.get(n.getRelativePath())] = System.currentTimeMillis();
                    bucketCount[fileMap.get(n.getRelativePath())] = bucketCount();
                    if (fileMap.get(n.getRelativePath()) == 99)
                        finishedBench = true;
                }
            }
            if (finishedBench) {
                // Do something, analyze
                printTimeBench((startFactor * (j + 1)) + "Time");
                printBucketBench((startFactor * (j + 1)) + "Buckets");
            } else {
                fail("Bench was not finished but notified that it would be finished.");
            }

            filelistener.shutDownListener();
        }
    }

    @DataProvider(name = "sizes")
    public Object[][] sizes() throws TTByteHandleException {
        Integer[] factorAndSizes = {
            262144, 8
        };

        Object[][] returnVal = {
            {
                Integer.class, factorAndSizes
            }
        };
        return returnVal;
    }

    private void printBucketBench(String string) throws IOException {
        System.out.println("######################################");
        System.out.println("Finished bench with " + string + ", buckets.");
        System.out.println("######################################");
        String s = "";
        for (int i = 0; i < starts.length; i++) {
            s += bucketCount[i] + ",";
        }
        System.out.println(s);
        Files.append(s, new File(string + ".csv"), Charset.forName("UTF-8"));
    }

    private void printTimeBench(String string) throws IOException {
        System.out.println("######################################");
        System.out.println("Finished bench with " + string + ", time.");
        System.out.println("######################################");
        String s = "";
        for (int i = 0; i < starts.length; i++) {
            s += (ends[i] - starts[i]) + ",";
        }

        System.out.println(s);

        Files.append(s + "\n", new File(string + ".csv"), Charset.forName("UTF-8"));

        System.out.println();

    }

    private long bucketCount() {
        return new File(BUCKETFOLDER).list().length;
    }

    @Override
    public synchronized LinkedBlockingQueue<FilesystemNotification> getBlockingQueue() {
        return notifications;
    }

}
