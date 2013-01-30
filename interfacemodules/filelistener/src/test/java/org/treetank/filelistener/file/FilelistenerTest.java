package org.treetank.filelistener.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.Properties;
import java.util.Random;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.filelistener.file.node.FileNode;

import com.google.common.io.Files;
import com.google.inject.Inject;

public class FilelistenerTest {
    
    private Filelistener listener;

    private File tmpDir;

    private int createCounter;
    private int deleteCounter;

    @BeforeMethod
    public void setUp() throws Exception {
        tmpDir = Files.createTempDir();

        listener = new Filelistener();
        listener.watchDir(tmpDir);

        StorageManager.createStorage("test" + tmpDir.getName(), StorageManager.BACKEND_INDEX_JCLOUDS);
        Filelistener.addFilelistener("test" + tmpDir.getName(), tmpDir.toString());

        listener.startListening();
    }

    @AfterMethod
    public void destroy() throws Exception {
        listener.shutDownListener();
        listener.removeFilelistener("test" + tmpDir.getName());
    }

    @Test
    public void testMonitoring() throws Exception {
        try {
            assertEquals(createCounter, 0);
            assertEquals(deleteCounter, 0);

            // Creating random bytes.
            byte[] randomBytes = new byte[512 * 4096];
            Random rand = new Random(42);
            rand.nextBytes(randomBytes);

            File file1 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test1.txt").toString());
            file1.createNewFile();

            Files.write(randomBytes, file1);

            File file2 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test2.txt").toString());
            file2.createNewFile();

            rand = new Random(42+1);
            rand.nextBytes(randomBytes);
            Files.write(randomBytes, file2);

            File file3 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test3.txt").toString());
            file3.createNewFile();

            rand = new Random(42+2);
            rand.nextBytes(randomBytes);
            Files.write(randomBytes, file3);

            assertTrue(file2.delete());

            File file4 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test4.txt").toString());
            file4.createNewFile();

            rand = new Random(42+3);
            rand.nextBytes(randomBytes);
            Files.write(randomBytes, file4);

            synchronized (listener) {
                while (listener.isWorking(tmpDir.toPath())) {
                    this.wait();
                }
            }

            File file1Tmp = null;

            while (file1Tmp == null) {
                try {
                    if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                        file1Tmp = listener.getTrx("test" + tmpDir.getName()).getFullFile("\\test1.txt");
                    } else {
                        file1Tmp = listener.getTrx("test" + tmpDir.getName()).getFullFile("/test1.txt");
                    }
                    
                    if(Files.toByteArray(file1Tmp) != Files.toByteArray(file1)){
                        file1Tmp = null;
                    }
                } catch (NullPointerException e) {
                    Thread.sleep(2500);
                }
            }

            BufferedInputStream file1InputStream = Files.asByteSource(file1).openBufferedStream();

            byte[] file1bytes = new byte[FileNode.FILENODESIZE];
            file1InputStream.read(file1bytes);

            BufferedInputStream file1TmpInputStream = Files.asByteSource(file1).openBufferedStream();

            byte[] file1Tmpbytes = new byte[FileNode.FILENODESIZE];
            file1TmpInputStream.read(file1Tmpbytes);

            assertEquals(file1bytes, file1Tmpbytes);

            File file3Tmp = null;

            while (file3Tmp == null) {
                try {
                    if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                        file3Tmp = listener.getTrx("test" + tmpDir.getName()).getFullFile("\\test3.txt");
                    } else {
                        file3Tmp = listener.getTrx("test" + tmpDir.getName()).getFullFile("/test3.txt");
                    }
                    
                    if(Files.toByteArray(file3Tmp) != Files.toByteArray(file3)){
                        file3Tmp = null;
                    }
                } catch (NullPointerException e) {
                    Thread.sleep(2500);
                }
            }

            BufferedInputStream file3InputStream = Files.asByteSource(file3).openBufferedStream();

            byte[] file3bytes = new byte[FileNode.FILENODESIZE];
            file3InputStream.read(file3bytes);

            BufferedInputStream file3TmpInputStream = Files.asByteSource(file3).openBufferedStream();

            byte[] file3Tmpbytes = new byte[FileNode.FILENODESIZE];
            file3TmpInputStream.read(file3Tmpbytes);

            assertEquals(file3bytes, file3Tmpbytes);

            File file4Tmp = null;

            while (file4Tmp == null) {
                try {
                    if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                        file4Tmp = listener.getTrx("test" + tmpDir.getName()).getFullFile("\\test4.txt");
                    } else {
                        file4Tmp = listener.getTrx("test" + tmpDir.getName()).getFullFile("/test4.txt");
                    }
                    
                    if(Files.toByteArray(file4Tmp) != Files.toByteArray(file1)){
                        file4Tmp = null;
                    }
                } catch (NullPointerException e) {
                    Thread.sleep(2500);
                }
            }

            BufferedInputStream file4InputStream = Files.asByteSource(file4).openBufferedStream();

            byte[] file4bytes = new byte[FileNode.FILENODESIZE];
            file4InputStream.read(file4bytes);
            BufferedInputStream file4TmpInputStream = Files.asByteSource(file4).openBufferedStream();

            byte[] file4Tmpbytes = new byte[FileNode.FILENODESIZE];
            file4TmpInputStream.read(file4Tmpbytes);
            
            assertEquals(file4bytes, file4Tmpbytes);
        } catch (Exception exc) {
            destroy();
            throw exc;
        }
    }
    /*
     * @Override
     * public void processFileSystemChanges(Path dir, Path file, Kind evtType) {
     * assertTrue(dir.toFile().isDirectory(), "Is a directory");
     * 
     * if (evtType == ENTRY_CREATE) {
     * assertTrue(file.toFile().getName().contains("test"));
     * assertTrue(file.toFile().getName().contains(".txt"));
     * 
     * System.out.println("Fired create");
     * 
     * createCounter++;
     * } else if (evtType == ENTRY_DELETE) {
     * assertTrue(file.toFile().getName().contains("test"));
     * assertTrue(file.toFile().getName().contains(".txt"));
     * 
     * System.out.println("Fired delete");
     * 
     * deleteCounter--;
     * } else if (evtType == ENTRY_MODIFY) {
     * 
     * }
     * }
     */

}
