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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.ResourceNotExistingException;
import org.treetank.filelistener.file.node.FileNodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;
import org.treetank.io.IOUtils;

import com.google.common.io.Files;

/**
 * This bench suit tests reads/writes on the filelistener for different filesizes;
 * 
 * @author Andreas Rain
 * 
 */
public class FilelistenerBenchmark implements FilesystemNotificationObserver {

    private File benchFile = new File("target" + File.separator + "FileBench.csv");
    
    /** Filelistener resource */
    private static final String RESOURCE_1 = "RESOURCE_1";
    /** Temp dir for the test files */
    private final File TMPDIR_1 = Files.createTempDir();
    /** Filelistener for the benchmark */
    private Filelistener filelistener;
    /** Two-dimensional byte array for mltiple bytes */
    private byte[][] fileBytes;
    /** long array to track start time of file reads/writes */
    long[] starts;
    /** long array to track end time of file reads/writes */
    long[] ends;
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
        StorageManager.createResource(RESOURCE_1, new ModuleSetter().setDataFacClass(FileNodeFactory.class)
            .setMetaFacClass(FilelistenerMetaPageFactory.class).createModule());
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

    private final static int FILES = 100;
    
    /**
     * This test case benches one megabyte file reads / writes on the filelistener.
     * @throws TTException 
     * @throws ResourceNotExistingException 
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws FileNotFoundException 
     * @throws InterruptedException 
     */
    @Parameters({"filebench-size"})
    @Test
    public void bench(int filebenchSize) throws FileNotFoundException, ClassNotFoundException, IOException, ResourceNotExistingException, TTException, InterruptedException {
        // Listening to the target folder
        Filelistener.addFilelistener(RESOURCE_1, TMPDIR_1.getAbsolutePath());
        filelistener.watchDir(TMPDIR_1);
        filelistener.startListening();

        // Setting up file data for 10 files
        fileBytes = new byte[FILES][filebenchSize];
        
        for (int i = 0; i < fileBytes.length; i++) {
            // Using random seed of (i+1) * 42
            Random rand = new Random(42*(i+1));
            rand.nextBytes(fileBytes[i]);
        }

        // Benching creation of files on the filesystem and awaiting finalization in treetank.
        starts = new long[FILES];
        ends = new long[FILES];
        for (int i = 0; i < fileBytes.length; i++) {
            String filename = TMPDIR_1 + File.separator + "file" + (i+1) + ".data";
            fileMap.put(File.separator + "file" + (i+1) + ".data", i);
            Files.write(fileBytes[i], new File(filename));
        }
        
        while(!finishedBench){
            FilesystemNotification n = notifications.poll();
            if(n == null || n.getEvtType() == ENTRY_CREATE) {
                if (n != null) starts[fileMap.get(n.getRelativePath())] = System.currentTimeMillis();
                continue;
            }
            if(n.getRelativePath() != null){
                ends[fileMap.get(n.getRelativePath())] = System.currentTimeMillis();
                
                if(fileMap.get(n.getRelativePath()) == 99) finishedBench = true;
            }
        }
        if(finishedBench){
            // Do something, analyze
            printBench( filebenchSize + " bytes");
        }
        else{
            fail("Bench was not finished but notified that it would be finished.");
        }

        filelistener.shutDownListener();
    }

    private void printBench(String string) throws IOException {
        System.out.println("######################################");
        System.out.println("Finished bench with " + string +".");
        System.out.println("######################################");
        for (int i = 0; i < starts.length; i++) {
            System.out.print("Run " + i + "\t");
        }
        System.out.println();
        String s = "";
        for (int i = 0; i < starts.length; i++) {
            System.out.print( (ends[i] - starts[i]) + "ms \t");
            s += (ends[i] - starts[i]) + ",";
        }
        Files.append(s+"\n", benchFile, Charset.forName("UTF-8"));
        System.out.println();
        
    }

    @Override
    public synchronized LinkedBlockingQueue<FilesystemNotification> getBlockingQueue() {
        return notifications;
    }

}
