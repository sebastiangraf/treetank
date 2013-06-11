package org.treetank.filelistener.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.CoreTestHelper.Holder;
import org.treetank.access.FilelistenerWriteTrx;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.api.IFilelistenerWriteTrx;

import com.google.common.io.Files;
import com.google.inject.Inject;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Andreas Rain
 *
 */
@Guice(moduleFactory = ModuleFactory.class)
public class FilesystemNotificationTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private Holder mHolder;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();
        mHolder = CoreTestHelper.Holder.generateStorage();
        final ResourceConfiguration config =
            mResourceConfig.create(StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile()
                .getAbsolutePath(), CoreTestHelper.RESOURCENAME));
        CoreTestHelper.Holder.generateSession(mHolder, config);
    }
    
    /**
     * @throws Exception
     */
    @Test
    public void testDifferentEvents() throws Exception{
        IFilelistenerWriteTrx trx = new FilelistenerWriteTrx(mHolder.getSession().beginBucketWtx(), mHolder.getSession());
        File tmp = Files.createTempDir();
        
        String relativePath = File.separator+"file.txt";
        File file = new File(tmp.getAbsolutePath() + File.separator+"file.txt");
        
        byte[] bytes = new byte[1024*8];
        
        Random rand = new Random(42);
        rand.nextBytes(bytes);
        
        Files.write(bytes, file);
        
        List<FilesystemNotification> notifications = new ArrayList<FilesystemNotification>();
        
        notifications.add(new FilesystemNotification(file, relativePath, tmp.getAbsolutePath(), ENTRY_CREATE, trx));
        notifications.add(new FilesystemNotification(file, relativePath, tmp.getAbsolutePath(), ENTRY_MODIFY, trx));
        notifications.add(new FilesystemNotification(file, relativePath, tmp.getAbsolutePath(), ENTRY_DELETE, trx));
        
        for(FilesystemNotification s : notifications){
            s.call();
        }
        
    }
    
    /**
     * 
     */
    @Test
    public void testEquals(){
        File f = new File("");
        
        FilesystemNotification fsn1 = new FilesystemNotification(f, "", "", ENTRY_CREATE, null);
        FilesystemNotification fsn2 = new FilesystemNotification(f, "", "", ENTRY_CREATE, null);
        FilesystemNotification fsn3 = new FilesystemNotification(f, "", "", ENTRY_MODIFY, null);
        FilesystemNotification fsn4 = new FilesystemNotification(f, "a", "", ENTRY_CREATE, null);
        FilesystemNotification fsn5 = new FilesystemNotification(f, "", "b", ENTRY_CREATE, null);
        FilesystemNotification fsn6 = new FilesystemNotification(null, "", "", ENTRY_CREATE, null);
        FilesystemNotification fsn7 = new FilesystemNotification(null, "a", "b", ENTRY_CREATE, null);
        FilesystemNotification fsn8 = new FilesystemNotification(null, "", "b", ENTRY_CREATE, null);
        
        assertEquals(fsn1, fsn2);
        assertTrue(!fsn1.equals(fsn3));
        assertTrue(!fsn1.equals(fsn4));
        assertTrue(!fsn1.equals(fsn5));
        assertTrue(!fsn1.equals(fsn6));
        assertTrue(!fsn6.equals(fsn7));
        assertTrue(!fsn7.equals(fsn8));
    }
    
    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.deleteEverything();
    }


}
