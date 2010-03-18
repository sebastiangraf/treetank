package com.treetank.service.jaxrx.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.implementation.TreeTank;

/**
 * This class is responsible to test the {@link WorkerHelper} class.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public class WorkerHelperTest {
    /**
     * The WorkerHelper reference.
     */
    private transient static WorkerHelper workerHelper;
    /**
     * The Treetank reference.
     */
    private transient static TreeTank treeTank;
    /**
     * The resource name.
     */
    private static final transient String RESOURCENAME = "factyTest";
    /**
     * The test file that has to be saved on the server.
     */
    private final static File DBFILE = new File(RESTProps.STOREDBPATH
            + File.separatorChar + RESOURCENAME + ".tnk");

    /**
     * The test file that has to be saved on the server.
     */
    private static final transient InputStream INPUTFILE = WorkerHelperTest.class
            .getClass().getResourceAsStream("/factbook.xml");

    /**
     * A simple set up.
     * 
     * @throws FileNotFoundException
     */
    @BeforeClass
    public static void setUp() throws FileNotFoundException, TreetankException {
        workerHelper = new WorkerHelper();
        treeTank = new TreeTank();
        treeTank.shred(INPUTFILE, RESOURCENAME);
    }

    /**
     * A simple tear down.
     */
    // @AfterClass
    // public void tearDown() {
    // treeTank.deleteResource(resourceName);
    // }

    /**
     * This method tests {@link WorkerHelper#checkExistingResource(File)}
     */
    @Test
    public void testCheckExistingResource() {
        assertEquals("test check existing resource", true, workerHelper
                .checkExistingResource(DBFILE));
    }

    /**
     * This method tests {@link WorkerHelper#createTreeTrankObject()}
     */
    @Test
    public void testCreateTreeTankObject() {
        assertNotNull("test create treetank object", workerHelper
                .createTreeTrankObject());
    }

    /**
     * This method tests {@link WorkerHelper#createStringBuilderObject()}
     */
    @Test
    public void testCreateStringBuilderObject() {
        assertNotNull("test create string builder object", workerHelper
                .createStringBuilderObject());
    }

    /**
     * This method tests
     * {@link WorkerHelper#serializeXML(IReadTransaction, OutputStream, boolean, boolean)}
     */
    @Test
    public void testSerializeXML() throws TreetankException, IOException {
        final IDatabase database = Database.openDatabase(DBFILE);
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        final OutputStream out = new ByteArrayOutputStream();

        assertNotNull("test serialize xml", workerHelper.serializeXML(rtx, out,
                true, true));
        rtx.close();
        session.close();
        database.close();
        out.close();
    }

    /**
     * This method tests
     * {@link WorkerHelper#shredInputStream(IWriteTransaction, InputStream, boolean)}
     */
    @Test
    public void testShredInputStream() throws TreetankException, IOException {

        long lastRevision = treeTank.getLastRevision(RESOURCENAME);

        final IDatabase database = Database.openDatabase(DBFILE);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        final InputStream inputStream = new ByteArrayInputStream("<testNode/>"
                .getBytes());

        workerHelper.shredInputStream(wtx, inputStream, true);

        assertEquals("test shred input stream", treeTank
                .getLastRevision(RESOURCENAME), ++lastRevision);
        wtx.close();
        session.close();
        database.close();
        inputStream.close();
    }

    /**
     * This method tests
     * {@link WorkerHelper#close(IWriteTransaction, IReadTransaction, ISession, IDatabase)}
     */
    @Test(expected = IllegalStateException.class)
    public void testClose() throws TreetankException {
        IDatabase database = Database.openDatabase(DBFILE);
        ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        workerHelper.closeWTX(false, wtx, session, database);

        wtx.commit();

        database = Database.openDatabase(DBFILE);
        session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        workerHelper.closeRTX(rtx, session, database);

        rtx.moveTo(11);

    }

}
