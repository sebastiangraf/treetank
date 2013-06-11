package org.treetank.access;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.ModuleFactory;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;

import com.google.common.io.Files;
import com.google.inject.Inject;

/**
 * 
 * 
 * @author Andreas Rain
 * 
 */
@Guice(moduleFactory = ModuleFactory.class)
public class TransactionTest {

    @Inject
    private IResourceConfigurationFactory mResourceConfig;

    private ResourceConfiguration mResource;

    /** Holder for the storage */
    private CoreTestHelper.Holder mHolder;

    /** One storage for the whole test.. */
    private ISession mSession;

    /** One transaction for the whole test.. */
    private FilelistenerWriteTrx mTrx;

    /** One temporary directory for the created files. */
    private File tmpDir;

    /** Some predefined relative paths */

    private final String file1RelativePath = "1.txt";
    private final String file2RelativePath = "2.txt";

    // Setting up the storage we want to operate on.
    @BeforeMethod
    public void setUp() throws TTException {
        CoreTestHelper.deleteEverything();
        mHolder = CoreTestHelper.Holder.generateStorage();
        Properties props =
            StandardSettings.getProps(CoreTestHelper.PATHS.PATH1.getFile().getAbsolutePath(),
                CoreTestHelper.RESOURCENAME);
        mResource = mResourceConfig.create(props);
        CoreTestHelper.Holder.generateSession(mHolder, mResource);
        mSession = mHolder.getSession();
        mTrx = new FilelistenerWriteTrx(mSession.beginBucketWtx(), mSession);
        tmpDir = Files.createTempDir();
    }

    @AfterMethod
    public void tearDown() throws TTException {
        mTrx.commit();
        mTrx.close();
        mSession.close();
        CoreTestHelper.deleteEverything();
    }

    @Test
    public void testTransactions() throws TTException, IOException {

        // First file is empty.
        addFileToTmpDir(file1RelativePath);

        mTrx.addEmptyFile(file1RelativePath);

        // Second file gets a random amount bytes written to it.
        File file2 = addFileToTmpDir(file2RelativePath);

        mTrx.addEmptyFile(file2RelativePath);

        // Creating random byte array
        byte[] randomBytes = new byte[1024 * 1024 * 32];
        Random rand = new Random(42);
        rand.nextBytes(randomBytes);

        if (!file2.exists()) {
            file2.createNewFile();
        }

        Files.write(randomBytes, file2);

        // Adding the file to the treetank backend
        mTrx.addFile(file2, file2RelativePath);

        // Getting the created files from the backend now

        File f = mTrx.getFullFile(file1RelativePath);
        byte[] bytes = Files.toByteArray(f);

        assertEquals(bytes, new byte[0]);

        f = mTrx.getFullFile(file2RelativePath);
        bytes = Files.toByteArray(f);

        assertEquals(bytes, randomBytes);
    }

    private File addFileToTmpDir(String pRelativePath) {
        return new File(tmpDir.getAbsolutePath() + File.separator + pRelativePath);
    }

}
