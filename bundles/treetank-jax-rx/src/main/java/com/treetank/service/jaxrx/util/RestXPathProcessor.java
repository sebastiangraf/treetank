/**
 * 
 */
package com.treetank.service.jaxrx.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.treetank.access.Database;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.xpath.XPathAxis;

/**
 * This class is responsible to offer XPath processing functions for REST.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz.
 * 
 */
public class RestXPathProcessor {

    /**
     * This field the begin result element of a XQuery or XPath expression.
     */
    private static transient String beginResult = "<jaxrx:result xmlns:jaxrx=\"http://jaxrx.org/\">";

    /**
     * This field the end result element of a XQuery or XPath expression.
     */
    private static transient String endResult = "</jaxrx:result>";

    /**
     * Getting part of the XML based on a XPath query
     * 
     * @param resourceName
     *            where the content should be extracted
     * @param xpath
     *            contains XPath query
     * @param nodeid
     *            To response the resource with a restid for each node ( <code>true</code>) or without (
     *            <code>false</code>).
     * @param revision
     *            The revision of the requested resource. If <code>null</code>,
     *            than response the latest revision.
     * @param output
     *            The OutputStream reference which have to be modified and
     *            returned
     * @return the queried XML fragment
     * @throws IOException
     *             The exception occurred.
     * @throws TreetankException
     */
    public OutputStream getXpathResource(final String resourceName, final String xpath, final boolean nodeid,
        final Long revision, final OutputStream output, final boolean wrapResult) throws IOException,
        TreetankException {

        // work around because of query root char '/'
        String qQuery = xpath;
        if (xpath.charAt(0) == '/')
            qQuery = ".".concat(xpath);
        if (resourceName.endsWith(RESTProps.COLEND)) {
            final List<File> tnks = checkColForTnks(resourceName);
            // Serializing the XML
            if (wrapResult) {
                output.write(beginResult.getBytes());
                for (final File aTNK : tnks) {
                    doXPathRes(aTNK, revision, output, nodeid, qQuery);
                }
                output.write(endResult.getBytes());
            } else {
                for (final File aTNK : tnks) {
                    doXPathRes(aTNK, revision, output, nodeid, qQuery);
                }
            }

        } else {
            final String tnkFile =
                RESTProps.STOREDBPATH + File.separatorChar + resourceName + RESTProps.TNKEND;
            final File dbFile = new File(tnkFile);
            if (WorkerHelper.checkExistingResource(dbFile)) {

                if (wrapResult) {
                    output.write(beginResult.getBytes());
                    doXPathRes(dbFile, revision, output, nodeid, qQuery);
                    output.write(endResult.getBytes());
                } else {
                    doXPathRes(dbFile, revision, output, nodeid, qQuery);
                }

            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        }
        return output;
    }

    /**
     * Getting part of the XML based on a XPath query
     * 
     * @param dbFile
     *            where the content should be extracted
     * 
     * @param query
     *            contains XPath query
     * @param rId
     *            To response the resource with a restid for each node ( <code>true</code>) or without (
     *            <code>false</code>).
     * @param doRevision
     *            The revision of the requested resource. If <code>null</code>,
     *            than response the latest revision.
     * @param output
     *            The OutputStream reference which have to be modified and
     *            returned
     * @param doNodeId
     *            specifies whether node id should be shown
     * @param doWrap
     *            output of result elements
     * @throws TreetankException
     */
    public void getXpathResource(final File dbFile, final long rId, final String query,
        final boolean doNodeId, final Long doRevision, final OutputStream output, final boolean doWrap)
        throws TreetankException {

        // work around because of query root char '/'
        String qQuery = query;
        if (query.charAt(0) == '/')
            qQuery = ".".concat(query);

        IDatabase database = null;
        ISession session = null;
        IReadTransaction rtx = null;
        try {
            database = Database.openDatabase(dbFile);
            // Creating a new session
            session = database.getSession();
            // Creating a transaction

            if (doRevision == null) {
                rtx = session.beginReadTransaction();
            } else {
                rtx = session.beginReadTransaction(doRevision);
            }

            final boolean exist = rtx.moveTo(rId);
            if (exist) {
                final IAxis axis = new XPathAxis(rtx, qQuery);
                if (doWrap) {
                    output.write(beginResult.getBytes());
                    for (final long key : axis) {
                        if (key >= 0) {
                            WorkerHelper.serializeXML(session, output, false, doNodeId, key, doRevision)
                                .call();
                        } else {
                            output.write(rtx.getNode().getRawValue());
                        }
                    }

                    output.write(endResult.getBytes());
                } else {
                    for (final long key : axis) {
                        if (key >= 0) {
                            WorkerHelper.serializeXML(session, output, false, doNodeId, key, doRevision)
                                .call();
                        } else {
                            output.write(rtx.getNode().getRawValue());
                        }
                    }

                }
            } else {
                throw new WebApplicationException(404);
            }

        } catch (final TreetankException ttExcep) {
            throw new WebApplicationException(ttExcep, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final IOException ioExcep) {
            throw new WebApplicationException(ioExcep, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final WebApplicationException exce) { // NOPMD framework need
            // WebapplicationException
            throw exce;
        } catch (final Exception globExcep) {
            throw new WebApplicationException(globExcep, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            WorkerHelper.closeRTX(rtx, session, database);
        }
    }

    /**
     * This method performs an XPath evaluation and writes it to a given output
     * stream.
     * 
     * @param dbFile
     *            The existing tnk file.
     * @param revision
     *            The revision of the requested document.
     * @param output
     *            The output stream where the results are written.
     * @param nodeid
     *            <code>true</code> if node id's have to be delivered. <code>false</code> otherwise.
     * @param xpath
     *            The XPath expression.
     * @throws TreetankException
     */
    private void doXPathRes(final File dbFile, final Long revision, final OutputStream output,
        final boolean nodeid, final String xpath) throws TreetankException {
        // Database connection to treetank
        IDatabase database = null;
        ISession session = null;
        IReadTransaction rtx = null;
        try {
            database = Database.openDatabase(dbFile);
            // Creating a new session
            session = database.getSession();
            // Creating a transaction
            if (revision == null) {
                rtx = session.beginReadTransaction();
            } else {
                rtx = session.beginReadTransaction(revision);
            }

            final IAxis axis = new XPathAxis(rtx, xpath);
            for (final long key : axis) {
                if (key >= 0) {
                    WorkerHelper.serializeXML(session, output, false, nodeid, key, revision).call();
                    final String book = new String(((ByteArrayOutputStream)output).toByteArray());
                    System.out.print(book);
                } else {
                    output.write(rtx.getNode().getRawValue());
                }
            }

        } catch (final TreetankException ttExcep) {
            throw new WebApplicationException(ttExcep, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final IOException ioExcep) {
            throw new WebApplicationException(ioExcep, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (final Exception globExcep) {
            throw new WebApplicationException(globExcep, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            rtx.moveToDocumentRoot();

            WorkerHelper.closeRTX(rtx, session, database);

            // rtx.close();
            // session.close();
            // database.close();
        }
    }

    /**
     * This method checks a collection for included tnk files.
     * 
     * @param resource
     *            The name of the collection.
     * @return A list of available tnk files.
     */
    private List<File> checkColForTnks(final String resource) {
        List<File> tnks;
        final File colFile = new File(RESTProps.STOREDBPATH + File.separatorChar + resource);
        if (colFile.exists()) {
            tnks = new ArrayList<File>();
            final File[] files = colFile.listFiles();
            for (final File file : files) {
                if (file.getName().endsWith(RESTProps.TNKEND)) {
                    tnks.add(file);
                }
            }
            return tnks;
        } else {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

    }

}
