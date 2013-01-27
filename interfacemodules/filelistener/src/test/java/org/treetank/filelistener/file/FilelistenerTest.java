package org.treetank.filelistener.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.exceptions.StorageNotExistingException;

import com.google.common.io.Files;

public class FilelistenerTest {

    private Filelistener listener;

    private File tmpDir;

    private int createCounter;
    private int deleteCounter;

    @BeforeClass
    public void setUp() throws ClassNotFoundException, StorageNotExistingException, TTException, StorageAlreadyExistsException {
        tmpDir = Files.createTempDir();
        System.out.println(tmpDir.getAbsolutePath());
        try {
            
            listener = new Filelistener();
            listener.watchDir(tmpDir);
            
            Filelistener.addFilelistener("test"+tmpDir.getName(), tmpDir.toString());
            StorageManager.createStorage("test"+tmpDir.getName(), StorageManager.BACKEND_INDEX_JCLOUDS);
                
            listener.startListening();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @AfterClass
    public void destroy() throws TTException {
        listener.shutDownListener();
    }

    @Test
    public void testMonitoring() throws TTIOException, InterruptedException {
        try {

            assertEquals(createCounter, 0);
            assertEquals(deleteCounter, 0);

            File file1 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test1.txt").toString());
            file1.createNewFile();
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }
            
            Files.write(new byte[512*512], file1);
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }

            File file2 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test2.txt").toString());
            file2.createNewFile();
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }
            
            Files.write(new byte[512*512], file2);
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }

            File file3 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test3.txt").toString());
            file3.createNewFile();
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }
            
            Files.write(new byte[512*512], file3);
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }

            assertTrue(file2.delete());
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }

            File file4 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test4.txt").toString());
            file4.createNewFile();
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }
            
            Files.write(new byte[512*512], file4);
            Thread.sleep(2500);
            
            while(listener.isWorking(tmpDir.toPath())){
                synchronized(this){
                    wait();
                }
            }
            
            File file1Tmp = null;
            
            while(file1Tmp == null){
                try{
                    file1Tmp = listener.getTrx("test"+tmpDir.getName()).getFullFile("\\test1.txt");
                }
                catch(NullPointerException e){
                    Thread.sleep(2500);
                }
            }
            
            byte[] file1bytes = Files.toByteArray(file1);
            byte[] file1Tmpbytes = Files.toByteArray(file1Tmp);
            
            assertEquals(file1bytes, file1Tmpbytes);

        } catch (IOException e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    /*@Override
    public void processFileSystemChanges(Path dir, Path file, Kind evtType) {
        assertTrue(dir.toFile().isDirectory(), "Is a directory");

        if (evtType == ENTRY_CREATE) {
            assertTrue(file.toFile().getName().contains("test"));
            assertTrue(file.toFile().getName().contains(".txt"));

            System.out.println("Fired create");

            createCounter++;
        } else if (evtType == ENTRY_DELETE) {
            assertTrue(file.toFile().getName().contains("test"));
            assertTrue(file.toFile().getName().contains(".txt"));

            System.out.println("Fired delete");

            deleteCounter--;
        } else if (evtType == ENTRY_MODIFY) {

        }
    }*/

}
