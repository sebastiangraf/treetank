package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.treetank.filelistener.api.file.IWatchCallback;

import com.google.common.io.Files;

public class FilelistenerTest implements IWatchCallback {

    private Filelistener listener;

    private File tmpDir;

    private int createCounter;
    private int modCounter;
    private int deleteCounter;

    @BeforeClass
    public void setUp() {
        tmpDir = Files.createTempDir();

        try {
            //TODO check this, must set a parameter in the constructor
            listener = new Filelistener(null);
            listener.watchDir(tmpDir);
            listener.startListening();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @AfterClass
    public void destroy() {
        listener.shutDownListener();
    }

    @Test
    public void testMonitoring() {
        try {

            assertEquals(createCounter, 0);
            assertEquals(deleteCounter, 0);

            System.out.println(tmpDir.getAbsolutePath());
            File file1 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test1.txt").toString());
            file1.createNewFile();

            File file2 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test2.txt").toString());
            file2.createNewFile();

            File file3 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test3.txt").toString());
            file3.createNewFile();

            assertTrue(file2.delete());

            File file4 =
                new File(new StringBuilder().append(tmpDir.getAbsolutePath()).append(File.separator).append(
                    "test4.txt").toString());
            file4.createNewFile();

            while (createCounter != 4) {

            }

        } catch (IOException e) {
            assertFalse(true);
            e.printStackTrace();
        }
    }

    @Override
    public void processFileSystemChanges(Path dir, Path file, Kind evtType) {
        assertTrue(dir.toFile().isDirectory(), "Is a directory");

        if (evtType == ENTRY_CREATE) {
            assertFile(file);
            System.out.println("Fired create");

            createCounter++;
        } else if (evtType == ENTRY_DELETE) {
            assertFile(file);
            System.out.println("Fired delete");

            deleteCounter--;
        } else if (evtType == ENTRY_MODIFY) {
            assertFile(file);
        }
    }

    private void assertFile(Path file) {
        assertTrue(file.toFile().getName().contains("test"));
        assertTrue(file.toFile().getName().contains(".txt"));
    }

}
