package com.treetank.service.jaxrx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;
import com.treetank.service.jaxrx.implementation.DatabaseRepresentation;
import com.treetank.service.xml.shredder.EShredderInsert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private transient static DatabaseRepresentation treeTank;
    /**
     * The resource name.
     */
    private static final transient String RESOURCENAME = "factyTest";
    /**
     * The test file that has to be saved on the server.
     */
    private final static File DBFILE = new File(RESTProps.STOREDBPATH + File.separatorChar + RESOURCENAME
        + ".tnk");

    /**
     * The test file that has to be saved on the server.
     */
    private final transient InputStream INPUTFILE = WorkerHelperTest.class.getClass().getResourceAsStream(
        "/factbook.xml");

    /**
     * A simple set up.
     * 
     * @throws FileNotFoundException
     */
    @Before
    public void setUp() throws FileNotFoundException, AbsTTException {
        workerHelper = WorkerHelper.getInstance();
        treeTank = new DatabaseRepresentation();
        treeTank.shred(INPUTFILE, RESOURCENAME);
    }

    /**
     * A simple tear down.
     */
    @After
    public void tearDown() {
        treeTank.deleteResource(RESOURCENAME);
    }

    /**
     * This method tests {@link WorkerHelper#checkExistingResource(File)}
     */
    @Test
    public void testCheckExistingResource() {
        assertEquals("test check existing resource", true, WorkerHelper.checkExistingResource(DBFILE));
    }

    /**
     * This method tests {@link WorkerHelper#createTreeTrankObject()}
     */
    @Test
    public void testCreateTreeTankObject() {
        assertNotNull("test create treetank object", workerHelper.createTreeTrankObject());
    }

    /**
     * This method tests {@link WorkerHelper#createStringBuilderObject()}
     */
    @Test
    public void testCreateStringBuilderObject() {
        assertNotNull("test create string builder object", workerHelper.createStringBuilderObject());
    }

    /**
     * This method tests {@link WorkerHelper#serializeXML(IReadTransaction, OutputStream, boolean, boolean)}
     */
    @Test
    public void testSerializeXML() throws AbsTTException, IOException {
        final IDatabase database = Database.openDatabase(DBFILE);
        final ISession session = database.getSession();
        final OutputStream out = new ByteArrayOutputStream();

        assertNotNull("test serialize xml", WorkerHelper.serializeXML(session, out, true, true, null));
        session.close();
        database.close();
        out.close();
    }

    /**
     * This method tests {@link WorkerHelper#shredInputStream(IWriteTransaction, InputStream, boolean)}
     */
    @Test
    public void testShredInputStream() throws AbsTTException, IOException {

        long lastRevision = treeTank.getLastRevision(RESOURCENAME);

        final IDatabase database = Database.openDatabase(DBFILE);
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        final InputStream inputStream = new ByteArrayInputStream("<testNode/>".getBytes());

        WorkerHelper.shredInputStream(wtx, inputStream, EShredderInsert.ADDASFIRSTCHILD);

        assertEquals("test shred input stream", treeTank.getLastRevision(RESOURCENAME), ++lastRevision);
        wtx.close();
        session.close();
        database.close();
        inputStream.close();
    }

    /**
     * This method tests {@link WorkerHelper#close(IWriteTransaction, IReadTransaction, ISession, IDatabase)}
     */
    @Test(expected = IllegalStateException.class)
    public void testClose() throws AbsTTException {
        IDatabase database = Database.openDatabase(DBFILE);
        ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        WorkerHelper.closeWTX(false, wtx, session, database);

        wtx.commit();

        database = Database.openDatabase(DBFILE);
        session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        WorkerHelper.closeRTX(rtx, session, database);

        rtx.moveTo(11);

    }

}
