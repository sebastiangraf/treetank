/**
 * 
 */
package org.treetank.filelistener.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.ResourceNotExistingException;
import org.treetank.filelistener.file.node.FileNodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;
import org.treetank.io.IOUtils;

import com.google.common.io.Files;

/**
 * @author Andreas Rain
 * 
 */
public class FilelistenerTest {

    private static final String RESOURCE_1 = "RESOURCE_1";
    private static final String RESOURCE_2 = "RESOURCE_2";

    private static final File TMPDIR_1 = Files.createTempDir();
    private static final File TMPDIR_2 = Files.createTempDir();

    private Filelistener filelistener;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        IOUtils.recursiveDelete(new File(StorageManager.ROOT_PATH));
        
        StorageManager.createResource(RESOURCE_1, new ModuleSetter().setNodeFacClass(FileNodeFactory.class)
            .setMetaFacClass(FilelistenerMetaPageFactory.class).createModule());
        StorageManager.createResource(RESOURCE_2, new ModuleSetter().setNodeFacClass(FileNodeFactory.class)
            .setMetaFacClass(FilelistenerMetaPageFactory.class).createModule());
        filelistener = new Filelistener();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
//        StorageManager.removeResource(RESOURCE_1); Not necessary since Filelistener.removeFileListener(String resourceName) already deletes the resource.
//        StorageManager.removeResource(RESOURCE_2); Not necessary since Filelistener.removeFileListener(String resourceName) already deletes the resource.

        filelistener.removeFilelistener(RESOURCE_1);
        filelistener.removeFilelistener(RESOURCE_2);
        TMPDIR_1.delete();
        TMPDIR_2.delete();
    }

    /**
     * Test method for {@link org.treetank.filelistener.file.Filelistener}.
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws FileNotFoundException
     * @throws TTException
     * @throws ResourceNotExistingException
     */
    @Test
    public void testFilelistener() throws FileNotFoundException, ClassNotFoundException, IOException,
        ResourceNotExistingException, TTException {
        // Testing adding of Filelisteners
        Filelistener.addFilelistener(RESOURCE_1, TMPDIR_1.getAbsolutePath());
        Filelistener.addFilelistener(RESOURCE_2, TMPDIR_2.getAbsolutePath());

        filelistener.watchDir(TMPDIR_1);
        filelistener.watchDir(TMPDIR_2);

        filelistener.startListening();

        // Checking if transactions have been created
        assertNotNull(filelistener.getTrx(RESOURCE_1));
        assertNotNull(filelistener.getTrx(RESOURCE_2));

        // Checking if listeners are in the system.
        assertEquals(2, Filelistener.getFilelisteners().size());

        // Creating bytes to write into file
        byte[] bytesToWrite = new byte[1024 * 1024 * 2];
        Random rand = new Random(42);
        rand.nextBytes(bytesToWrite);

        Files.write(bytesToWrite, new File(TMPDIR_1 + File.separator + "file1.txt"));
        Files.write(bytesToWrite, new File(TMPDIR_2 + File.separator + "file1.txt"));
        Files.write(bytesToWrite, new File(TMPDIR_1 + File.separator + "file2.txt"));
        Files.write(bytesToWrite, new File(TMPDIR_2 + File.separator + "file2.txt"));

        filelistener.shutDownListener();

    }

}
