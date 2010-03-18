package com.treetank.service.jaxrx.implementation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Inherited;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jaxrx.constants.EURLParameter;
import org.w3c.dom.Document;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.enums.EIdPostType;
import com.treetank.service.jaxrx.interfaces.INodeID;
import com.treetank.service.jaxrx.util.PostQueryExtractor;
import com.treetank.service.jaxrx.util.RESTProps;
import com.treetank.service.jaxrx.util.RestXPathProcessor;
import com.treetank.service.jaxrx.util.WorkerHelper;
import com.treetank.service.xml.XMLSerializer;

/**
 * This class is responsible to work with database specific XML node id's. It
 * allows to access a resource by a node id, modify an existing resource by node
 * id, delete an existing resource by node id and to append a new resource to an
 * existing XML elementi identified by a node id.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public class RIDWorker implements INodeID {

    /**
     * The folder where the tnk files will be saved.
     */
    private static final transient String RESPATH = RESTProps.STOREDBPATH;
    /**
     * The tnk file ending.
     */
    private static final transient String TNKEND = ".tnk";

    /**
     * This field specifies the begin result element of the request.
     */
    private static transient byte[] beginResult = "<jaxrx:result xmlns:jaxrx=\"http://jaxrx.org/\">"
            .getBytes();

    /**
     * This field specifies the end result element of the request.
     */
    private static transient byte[] endResult = "</jaxrx:result>".getBytes();

    /** Reference to get access to worker help classes */
    private transient final WorkerHelper workerHelper = new WorkerHelper();

    /**
     * {@link Inherited}
     */
    @Override
    public Set<EURLParameter> getAvaliableParams() {
        final Set<EURLParameter> avParams = new HashSet<EURLParameter>();
        avParams.add(EURLParameter.WRAP);
        avParams.add(EURLParameter.OUTPUT);
        avParams.add(EURLParameter.QUERY);
        return avParams;
    }

    /**
     * {@link Inherited}
     */
    @Override
    public StreamingOutput getResource(final String resourceName,
            final long nodeId, final Map<EURLParameter, String> queryParams)
            throws WebApplicationException {
        final StreamingOutput sOutput = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException,
                    WebApplicationException {

                final String tnkFile = RESPATH + File.separatorChar
                        + resourceName + TNKEND;
                final File dbFile = new File(tnkFile);

                final String xPath = queryParams.get(EURLParameter.QUERY);
                final String revision = queryParams.get(EURLParameter.REVISION);
                final String wrap = queryParams.get(EURLParameter.WRAP);
                final String doNodeId = queryParams.get(EURLParameter.OUTPUT);
                final boolean wrapResult = (wrap == null) ? false : Boolean
                        .parseBoolean(wrap);
                final boolean nodeid = (doNodeId == null) ? false : Boolean
                        .parseBoolean(doNodeId);
                RestXPathProcessor xpathProcessor;
                try {
                    if (xPath == null && revision == null) {
                        serialize(dbFile, nodeId, null, nodeid, output,
                                wrapResult);

                    } else if (xPath != null && revision == null) {
                        xpathProcessor = new RestXPathProcessor(workerHelper);
                        xpathProcessor.getXpathResource(dbFile, nodeId, xPath,
                                nodeid, null, output, wrapResult);
                    } else if (xPath == null && revision != null) {
                        serialize(dbFile, nodeId, Long.valueOf(revision),
                                nodeid, output, wrapResult);
                    } else if (xPath != null && revision != null) {
                        xpathProcessor = new RestXPathProcessor(workerHelper);
                        xpathProcessor.getXpathResource(dbFile, nodeId, xPath,
                                nodeid, Long.valueOf(revision), output,
                                wrapResult);
                    }
                } catch (final TreetankException exce) {
                    throw new WebApplicationException(exce);
                }

            }
        };

        return sOutput;
    }

    /**
     * {@link Inherited}
     */
    @Override
    public boolean deleteResource(final String resourceName, final long rId)
            throws WebApplicationException {
        boolean deleted = false;
        synchronized (resourceName) {
            ISession session = null;
            IDatabase database = null;
            IWriteTransaction wtx = null;
            final String tnkFile = RESPATH + File.separatorChar + resourceName
                    + TNKEND;
            final File dbFile = new File(tnkFile);
            boolean abort = false;
            if (workerHelper.checkExistingResource(dbFile)) {
                try {
                    database = Database.openDatabase(dbFile);
                    // Creating a new session
                    session = database.getSession();
                    // Creating a write transaction
                    wtx = session.beginWriteTransaction();
                    // move to node with given rest id and deletes it
                    if (wtx.moveTo(rId)) {
                        wtx.remove();
                        wtx.commit();
                        deleted = true;
                    } else {
                        // workerHelper.closeWTX(abort, wtx, session, database);
                        throw new WebApplicationException(404);
                    }
                } catch (final TreetankException exce) {
                    abort = true;
                    throw new WebApplicationException(exce);
                } finally {
                    try {
                        workerHelper.closeWTX(abort, wtx, session, database);
                    } catch (final TreetankException exce) {
                        throw new WebApplicationException(exce);
                    }
                }
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
        return deleted;
    }

    /**
     * {@link Inherited}
     */
    @Override
    public void modifyResource(final String resourceName, final long rId,
            final InputStream newValue) throws WebApplicationException {
        synchronized (resourceName) {
            ISession session = null;
            IDatabase database = null;
            IWriteTransaction wtx = null;
            final String tnkFile = RESPATH + File.separatorChar + resourceName
                    + TNKEND;
            final File dbFile = new File(tnkFile);
            boolean abort = false;
            if (workerHelper.checkExistingResource(dbFile)) {
                try {
                    database = Database.openDatabase(dbFile);
                    // Creating a new session
                    session = database.getSession();
                    // Creating a write transaction
                    wtx = session.beginWriteTransaction();

                    if (wtx.moveTo(rId)) {
                        final long parentKey = wtx.getNode().getParentKey();
                        wtx.remove();
                        wtx.moveTo(parentKey);
                        workerHelper.shredInputStream(wtx, newValue, true);

                    } else {
                        // workerHelper.closeWTX(abort, wtx, session, database);
                        throw new WebApplicationException(404);
                    }

                } catch (final TreetankException exc) {
                    abort = true;
                    throw new WebApplicationException(exc);
                } finally {
                    try {
                        workerHelper.closeWTX(abort, wtx, session, database);
                    } catch (final TreetankException exce) {
                        throw new WebApplicationException(exce);
                    }
                }
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
    }

    /**
     * {@link Inherited}
     */
    @Override
    public Response postResource(final String resourceName, final long rId,
            final Object input, final EIdPostType type)
            throws WebApplicationException {
        ISession session = null;
        IDatabase database = null;
        IWriteTransaction wtx = null;
        final String tnkFile = RESPATH + File.separatorChar + resourceName
                + TNKEND;
        final File dbFile = new File(tnkFile);
        Response responseBuild = null;
        StreamingOutput sOutput = null;
        if (type == EIdPostType.PERFORMQUERY) {
            final Document pvDoc = (Document) input;
            sOutput = new StreamingOutput() {
                @Override
                public void write(final OutputStream output)
                        throws IOException, WebApplicationException {
                    final Map<String, String> postQuery = PostQueryExtractor
                            .getQueryOutOfXML(pvDoc);
                    final String query = postQuery.get("query");
                    final String wrap = postQuery.get("wrap");
                    final String nodeid = postQuery.get("output");
                    final String revision = postQuery.get("revision");

                    final boolean doWrap = (wrap == null) ? false : Boolean
                            .parseBoolean(wrap);
                    final boolean doNodeId = (nodeid == null) ? false : Boolean
                            .parseBoolean(nodeid);
                    final Long doRevision = (revision == null) ? null : Long
                            .valueOf(revision);
                    try {
                        new RestXPathProcessor(workerHelper).getXpathResource(
                                dbFile, rId, query, doNodeId, doRevision,
                                output, doWrap);

                    } catch (final TreetankException exce) {
                        throw new WebApplicationException(exce);
                    }
                }
            };
            responseBuild = Response.ok(sOutput).build();
        } else {
            synchronized (resourceName) {
                boolean abort;
                if (workerHelper.checkExistingResource(dbFile)) {
                    abort = false;
                    try {

                        database = Database.openDatabase(dbFile);
                        // Creating a new session
                        session = database.getSession();
                        // Creating a write transaction
                        wtx = session.beginWriteTransaction();
                        final boolean exist = wtx.moveTo(rId);
                        if (exist) {
                            if (type == EIdPostType.APPENDCHILD) {
                                workerHelper.shredInputStream(wtx,
                                        (InputStream) input, true);
                                responseBuild = Response.status(
                                        Response.Status.CREATED).build();
                            } else if (type == EIdPostType.APPENDSIBLING) {
                                workerHelper.shredInputStream(wtx,
                                        (InputStream) input, false);
                                responseBuild = Response.status(
                                        Response.Status.CREATED).build();
                            }
                        } else {
                            throw new WebApplicationException(404);
                        }
                    } catch (final WebApplicationException exce) { // NOPMD due
                        // to
                        // different
                        // exception
                        // types
                        abort = true;
                        throw exce;
                    } catch (final Exception exce) {
                        abort = true;
                        throw new WebApplicationException(exce);
                    } finally {
                        try {
                            workerHelper
                                    .closeWTX(abort, wtx, session, database);
                        } catch (final TreetankException exce) {
                            throw new WebApplicationException(exce);
                        }
                    }
                }
            }
        }
        return responseBuild;

    }

    /**
     * This method serializes requested resource
     * 
     * @param dbFile
     *            The requested XML resource as tnk file.
     * @param nodeId
     *            The node id of the requested resource.
     * @param revision
     *            The revision of the requested resource.
     * @param doNodeId
     *            Specifies whether the node id's have to be shown in the
     *            result.
     * @param output
     *            The output stream to be written.
     * @param wrapResult
     *            Specifies whether the result has to be wrapped with a result
     *            element.
     */
    private void serialize(final File dbFile, final long nodeId,
            final Long revision, final boolean doNodeId,
            final OutputStream output, final boolean wrapResult) {
        if (workerHelper.checkExistingResource(dbFile)) {
            ISession session = null;
            IDatabase database = null;
            IReadTransaction rtx = null;
            try {
                database = Database.openDatabase(dbFile);
                session = database.getSession();
                if (revision == null) {
                    rtx = session.beginReadTransaction();
                } else {
                    rtx = session.beginReadTransaction(revision);
                }

                // move to node with given id and read it
                if (wrapResult) {
                    output.write(beginResult);
                    if (rtx.moveTo(nodeId)) {
                        new XMLSerializer(rtx, output, false, doNodeId).call();
                    } else {
                        // workerHelper.close(null, rtx, session, database);
                        throw new WebApplicationException(404);
                    }
                    output.write(endResult);
                } else {
                    if (rtx.moveTo(nodeId)) {
                        new XMLSerializer(rtx, output, false, doNodeId).call();
                    } else {
                        // workerHelper.close(null, rtx, session, database);
                        throw new WebApplicationException(404);
                    }

                }
            } catch (final TreetankException ttExcep) {
                throw new WebApplicationException(ttExcep,
                        Response.Status.INTERNAL_SERVER_ERROR);
            } catch (final IOException ioExcep) {
                throw new WebApplicationException(ioExcep,
                        Response.Status.INTERNAL_SERVER_ERROR);
            } catch (final Exception globExcep) {
                if (globExcep instanceof WebApplicationException) { // NOPMD due
                    // to
                    // different
                    // exception
                    // types
                    throw (WebApplicationException) globExcep;
                } else {
                    throw new WebApplicationException(globExcep,
                            Response.Status.INTERNAL_SERVER_ERROR);
                }
            } finally {
                try {
                    workerHelper.closeRTX(rtx, session, database);
                } catch (final TreetankException exce) {
                    throw new WebApplicationException(exce,
                            Response.Status.INTERNAL_SERVER_ERROR);
                }
            }

        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    /*
     * This method is responsible to revert a resource to an older revision. It
     * creates a new revision with the same content of the requested revert to
     * value revision.
     * 
     * @param resourceName The name of the requested database.
     * 
     * @param backToRevision The {@link String} value of the revision that has
     * to be the latest one.
     * 
     * @throws IOException
     * 
     * @throws TreetankException
     * 
     * private void revertToOlderRevision(final String resourceName, final
     * String backToRevision) throws WebApplicationException, IOException,
     * TreetankException {
     * 
     * final String tnkFile = RESPATH + File.separatorChar + resourceName +
     * TNKEND; final File dbFile = new File(tnkFile);
     * 
     * PipedInputStream pipedInput = new PipedInputStream(); final
     * PipedOutputStream pipedOutput = new PipedOutputStream(pipedInput);
     * 
     * if (workerHelper.checkExistingResource(dbFile)) { database =
     * Database.openDatabase(dbFile); new Thread(new Runnable() { public void
     * run() { try { // Creating a new session final ISession session =
     * database.getSession();
     * 
     * // Creating a read transaction for older revision rtx =
     * session.beginReadTransaction(Long.valueOf( backToRevision).longValue());
     * rtx.moveToDocumentRoot(); final long childNodesNum = rtx.getNode()
     * .getChildCount(); final long firstChildKey = rtx.getNode()
     * .getFirstChildKey();
     * 
     * for (long i = 0; i < childNodesNum; i++) { if (i == 0) {
     * rtx.moveTo(firstChildKey); } else { final long rightSiblingKey =
     * rtx.getNode() .getRightSiblingKey(); rtx.moveTo(rightSiblingKey); }
     * 
     * System.out.println("kkk"); workerHelper.serializeXML2(rtx, pipedOutput,
     * false, false).call(); }
     * 
     * } catch (final Exception exc) { throw new WebApplicationException(exc); }
     * } }).start();
     * 
     * final ISession session = database.getSession();
     * 
     * // Creating a write transaction for last revision wtx =
     * session.beginWriteTransaction(); // move to document root node and remove
     * it (removes whole doc)
     * 
     * wtx.moveToDocumentRoot(); final long childNodesNum =
     * wtx.getNode().getChildCount(); final long firstChildKey =
     * wtx.getNode().getFirstChildKey(); wtx.moveTo(firstChildKey);
     * 
     * for (long i = 0; i < childNodesNum; i++) { if (i == 0) {
     * wtx.moveTo(firstChildKey); } else { final long rightSiblingKey =
     * wtx.getNode() .getRightSiblingKey(); wtx.moveTo(rightSiblingKey); }
     * wtx.remove(); }
     * 
     * workerHelper.shredInputStream(wtx, pipedInput, true);
     * 
     * wtx.commit(); workerHelper.close(wtx, rtx, session, database);
     * 
     * }
     * 
     * }
     */

}
