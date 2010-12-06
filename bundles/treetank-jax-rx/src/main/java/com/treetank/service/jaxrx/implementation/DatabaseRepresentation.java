package com.treetank.service.jaxrx.implementation; // NOPMD we need all these imports, declaring with * is

// pointless

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.util.RESTProps;
import com.treetank.service.jaxrx.util.RESTResponseHelper;
import com.treetank.service.jaxrx.util.RESTXMLShredder;
import com.treetank.service.jaxrx.util.RestXPathProcessor;
import com.treetank.service.jaxrx.util.WorkerHelper;
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.settings.EDatabaseSetting;

import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.QueryParameter;

/**
 * This class is the TreeTank DB connection for RESTful Web Services processing.
 * When a RESTful WS database request occurs it will be forwarded to TreeTank to
 * manage the request. Here XML files can be shredded and serialized to build
 * the client response. Further more it supports XPath queries.
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */
public class DatabaseRepresentation {

    /**
     * This field the begin result element of a XQuery or XPath expression.
     */
    private static transient String beginResult = "<jaxrx:result xmlns:jaxrx=\"http://jaxrx.org/\">";

    /**
     * This field the end result element of a XQuery or XPath expression.
     */
    private static transient String endResult = "</jaxrx:result>";

    /**
     * Often used 'yes' {@link String}.
     */
    private static final transient String YESSTRING = "yes";

    /**
     * This method is responsible to create a new database.
     * 
     * @param inputStream
     *            The stream containing the XML document that has to be stored.
     * @param resourceName
     *            The name of the new database.
     * @throws JaxRxException
     *             The exception occurred.
     */
    public void createResource(final InputStream inputStream, final String resourceName)
        throws JaxRxException {
        synchronized (resourceName) {
            if (inputStream == null) {
                throw new JaxRxException(400, "Bad user request");
            } else {
                try {
                    shred(inputStream, resourceName);
                } catch (final TreetankException exce) {
                    throw new JaxRxException(exce);
                }
            }
        }
    }

    /**
     * This method is responsible to deliver the whole database. Additional
     * parameters can be set (wrap, revision, output) which change the response
     * view.
     * 
     * @param resourceName
     *            The name of the requested database.
     * @param queryParams
     *            The optional query parameters.
     * @return The XML database resource, depending on the query parameters.
     * @throws JaxRxException
     *             The exception occurred.
     */
    public StreamingOutput getResource(final String resourceName,
        final Map<QueryParameter, String> queryParams) throws JaxRxException {
        final StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, JaxRxException {
                final String revision = queryParams.get(QueryParameter.REVISION);
                final String wrap = queryParams.get(QueryParameter.WRAP);
                final String nodeId = queryParams.get(QueryParameter.OUTPUT);
                final boolean wrapResult = (wrap == null) ? false : wrap.equalsIgnoreCase(YESSTRING);
                final boolean nodeid = (nodeId == null) ? false : nodeId.equalsIgnoreCase(YESSTRING);
                try {
                    if (revision == null) {
                        serialize(resourceName, null, nodeid, output, wrapResult);
                    } else {
                        // pattern which have to match against the input
                        final Pattern pattern = Pattern.compile("[0-9]+[-]{1}[1-9]+");

                        final Matcher matcher = pattern.matcher(revision);

                        if (matcher.matches()) {
                            getModificHistory(resourceName, revision, nodeid, output, wrapResult);
                        } else {
                            serialize(resourceName, Long.valueOf(revision), nodeid, output, wrapResult);
                        }
                    }
                } catch (final NumberFormatException exce) {
                    throw new JaxRxException(400, exce.getMessage());
                } catch (final TreetankException exce) {
                    throw new JaxRxException(exce);
                }
            }
        };
        return streamingOutput;
    }

    /**
     * This method is responsible to perform queries on a special database.
     * (XPath queries).
     * 
     * @param resource
     *            The name of the database instance.
     * @param query
     *            The XPath expression.
     * @param otherParams
     *            Further query parameters (output, wrap, revision) which change
     *            the response.
     * @return The result of the XPath query expression.
     */
    public StreamingOutput performQueryOnResource(final String resource, final String query,
        final Map<QueryParameter, String> otherParams) {
        final StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, JaxRxException {
                final String revision = otherParams.get(QueryParameter.REVISION);
                final String wrap = otherParams.get(QueryParameter.WRAP);
                final String nodeId = otherParams.get(QueryParameter.OUTPUT);
                final boolean wrapResult = (wrap == null) ? true : wrap.equalsIgnoreCase(YESSTRING);
                final boolean nodeid = (nodeId == null) ? false : nodeId.equalsIgnoreCase(YESSTRING);
                final Long rev = revision == null ? null : Long.valueOf(revision);
                final RestXPathProcessor xpathProcessor = new RestXPathProcessor();
                try {
                    xpathProcessor.getXpathResource(resource, query, nodeid, rev, output, wrapResult);
                } catch (final TreetankException exce) {
                    throw new JaxRxException(exce);
                }
            }
        };
        return streamingOutput;
    }

    /**
     * This method is responsible to deliver a list of available resources and
     * collections supported by TreeTank's REST implementation.
     * 
     * @return The list of available databases and collections wrapped in an XML
     *         document.
     * @throws JaxRxException
     *             The exception occurred.
     */
    public StreamingOutput getResourcesNames() throws JaxRxException {
        final Map<String, String> availResources = new HashMap<String, String>();
        final File resourcesDir = new File(RESTProps.STOREDBPATH);
        final File[] files = resourcesDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    final String dirName = file.getAbsoluteFile().getName();
                    if (dirName.endsWith(RESTProps.TNKEND)) {
                        availResources.put(dirName, "resource");
                    } else if (dirName.endsWith(RESTProps.COLEND)) {
                        availResources.put(dirName, "collection");
                    }
                }
            }
        }

        return RESTResponseHelper.buildResponseOfDomLR(availResources);
    }

    /**
     * This method is responsible to add a new XML document to a collection.
     * 
     * @param input
     *            The new XML document packed in an {@link InputStream}.
     * @param resource
     *            The name of the collection.
     * @throws JaxRxException
     *             The exception occurred.
     */
    public void add(final InputStream input, final String resource) throws JaxRxException {
        synchronized (resource) {
            try {
                if (resource.endsWith(RESTProps.COLEND)) {
                    final String saveName = resource + File.separatorChar + (new Date().getTime());
                    shred(input, saveName);
                } else {
                    final File tnkFile =
                        new File(RESTProps.STOREDBPATH + File.separatorChar + resource + RESTProps.TNKEND);
                    if (tnkFile.exists()) {
                        final File newCol =
                            new File(RESTProps.STOREDBPATH + File.separatorChar + resource + RESTProps.COLEND);
                        newCol.mkdir();
                        tnkFile.renameTo(new File(newCol, tnkFile.getName()));
                        final String saveName =
                            newCol.getName() + File.separatorChar + (new Date().getTime());
                        shred(input, saveName);
                    }
                }
            } catch (final TreetankException exce) {
                throw new JaxRxException(exce);
            }
        }

    }

    /**
     * This method is responsible to delete an existing database.
     * 
     * @param resourceName
     *            The name of the database.
     * @throws JaxRxException
     *             The exception occurred.
     */
    public void deleteResource(final String resourceName) throws WebApplicationException {
        boolean isDeleted;
        synchronized (resourceName) {
            if (resourceName.endsWith(RESTProps.COLEND)) {
                isDeleted =
                    deleteCompleteDirectory(new File(RESTProps.STOREDBPATH + File.separatorChar
                        + resourceName));
                if (!isDeleted) {
                    throw new JaxRxException(404, "Resource not found");
                }
            } else {
                final File resource =
                    new File(RESTProps.STOREDBPATH + File.separatorChar + resourceName + RESTProps.TNKEND);
                // isDeleted = deleteCompleteDirectory(resource);
                isDeleted = Database.truncateDatabase(resource);
                if (!isDeleted) {
                    throw new JaxRxException(500, "Deletion could not be performed");
                }
            }
        }
    }

    /**
     * This method is responsible to save the XML file, which is in an {@link InputStream}, as a TreeTank
     * object.
     * 
     * @param xmlInput
     *            The XML file in an {@link InputStream}.
     * @param resource
     *            The name of the resource.
     * @return <code>true</code> when the shredding process has been successful. <code>false</code> otherwise.
     * @throws TreetankException
     */
    public final boolean shred(final InputStream xmlInput, final String resource) throws TreetankException {
        boolean allOk;
        IWriteTransaction wtx = null;
        IDatabase database = null;
        ISession session = null;
        boolean abort = false;
        try {
            final StringBuilder tnkFileName =
                new StringBuilder(RESTProps.STOREDBPATH + File.separatorChar + resource);
            tnkFileName.append(RESTProps.TNKEND);
            final File tnk = new File(tnkFileName.toString());

            // Shredding the database to the file as XML
            final Properties dbProps = new Properties();
            dbProps.setProperty(EDatabaseSetting.REVISION_TO_RESTORE.name(), "1");
            final DatabaseConfiguration conf = new DatabaseConfiguration(tnk, dbProps);

            Database.createDatabase(conf);
            database = Database.openDatabase(tnk);
            session = database.getSession();
            wtx = session.beginWriteTransaction();
            wtx.moveToDocumentRoot();
            final boolean exist = wtx.moveToFirstChild();
            if (exist) {
                wtx.remove();
                wtx.commit();
            }
            final XMLShredder shredder =
                new XMLShredder(wtx, RESTXMLShredder.createReader(xmlInput), EShredderInsert.ADDASFIRSTCHILD);
            shredder.call();
            allOk = true;
        } catch (final Exception exce) {
            abort = true;
            throw new JaxRxException(exce);
        } finally {
            WorkerHelper.closeWTX(abort, wtx, session, database);
        }
        return allOk;
    }

    /**
     * This method is responsible to build an {@link OutputStream} containing an
     * XML file out of the TreeTank file.
     * 
     * @param resource
     *            The name of the resource that will be offered as XML.
     * @param nodeid
     *            To response the resource with a restid for each node ( <code>true</code>) or without (
     *            <code>false</code>).
     * @param revision
     *            The revision of the requested resource. If <code>null</code>,
     *            than response the latest revision.
     * @return The {@link OutputStream} containing the serialized XML file.
     * @throws IOException
     * @throws TreetankException
     * @throws WebApplicationException
     */
    private final OutputStream serialize(final String resource, final Long revision, final boolean nodeid,
        final OutputStream output, final boolean wrapResult) throws IOException, JaxRxException,
        TreetankException {

        if (resource.endsWith(RESTProps.COLEND)) {
            final List<File> tnks = checkColForTnks(resource);
            // Serializing the XML
            if (wrapResult) {
                output.write(beginResult.getBytes());
                for (final File aTNK : tnks) {
                    serializIt(aTNK, null, output, nodeid);
                }
                output.write(endResult.getBytes());
            } else {
                for (final File aTNK : tnks) {
                    serializIt(aTNK, null, output, nodeid);
                }
            }

        } else {
            final String tnkFile = RESTProps.STOREDBPATH + File.separatorChar + resource + RESTProps.TNKEND;
            final File dbFile = new File(tnkFile);
            if (WorkerHelper.checkExistingResource(dbFile)) {
                try {
                    if (wrapResult) {
                        output.write(beginResult.getBytes());
                        serializIt(dbFile, revision, output, nodeid);
                        output.write(endResult.getBytes());
                    } else {
                        serializIt(dbFile, revision, output, nodeid);
                    }
                } catch (final Exception exce) {
                    throw new JaxRxException(exce);
                }
            } else {
                throw new JaxRxException(404, "Not found");
            }
        }

        return output;
    }

    /**
     * This method deletes a (not empty) directory.
     * 
     * @param path
     *            The {@link File} directory that has to be deleted.
     * @return <code>true</code> if the delete process has been successful. <code>false</code> otherwise.
     */
    private boolean deleteCompleteDirectory(final File path) {
        if (path.exists()) {
            final File[] files = path.listFiles();
            for (final File file : files) {
                // if (file.isDirectory()) {
                // deleteCompleteDirectory(file);
                // } else {
                // file.delete();
                // }
                if (file.isDirectory())
                    Database.truncateDatabase(file);
            }
        }
        return path.delete();

    }

    /**
     * This method reads the existing database, and offers the last revision id
     * of the database
     * 
     * @param resourceName
     *            The name of the existing database.
     * @return The {@link OutputStream} containing the result
     * @throws WebApplicationException
     *             The Exception occurred.
     * @throws TreetankException
     */
    public long getLastRevision(final String resourceName) throws JaxRxException, TreetankException {
        final String tnkFile = RESTProps.STOREDBPATH + File.separatorChar + resourceName + RESTProps.TNKEND;
        final File dbFile = new File(tnkFile);
        long lastRevision;
        if (WorkerHelper.checkExistingResource(dbFile)) {

            IDatabase database = null;
            IReadTransaction rtx = null;
            ISession session = null;
            try {
                database = Database.openDatabase(dbFile);
                session = database.getSession();
                rtx = session.beginReadTransaction();
                lastRevision = rtx.getRevisionNumber();

            } catch (final TreetankException ttExcep) {
                throw new JaxRxException(ttExcep);
            } catch (final Exception globExcep) {
                throw new JaxRxException(globExcep);
            } finally {
                WorkerHelper.closeRTX(rtx, session, database);
            }
        } else {
            throw new JaxRxException(404, "Resource not found");
        }
        return lastRevision;

    }

    /**
     * This method reads the existing database, and offers all modifications of
     * the two given revisions
     * 
     * @param resourceName
     *            The name of the existing database.
     * @param revisionRange
     *            Contains the range of revisions
     * @param nodeid
     *            To response the resource with a restid for each node ( <code>true</code>) or without (
     *            <code>false</code>).
     * @param output
     *            The OutputStream reference which have to be modified and
     *            returned
     * @param wrap
     *            <code>true</code> if the results have to be wrapped. <code>false</code> otherwise.
     * @return The {@link OutputStream} containing the result
     * @throws TreetankException
     * @throws WebApplicationException
     * @throws WebApplicationException
     *             The Exception occurred.
     */
    public OutputStream getModificHistory(final String resourceName, // NOPMD this method needs alls these
        // functions
        final String revisionRange, final boolean nodeid, final OutputStream output, final boolean wrap)
        throws JaxRxException, TreetankException {

        // extract both revision from given String value
        final StringTokenizer tokenizer = new StringTokenizer(revisionRange, "-");
        final long revision1 = Long.valueOf(tokenizer.nextToken());
        final long revision2 = Long.valueOf(tokenizer.nextToken());

        if (revision1 < revision2 && revision2 <= getLastRevision(resourceName)) {
            final String tnkFile =
                RESTProps.STOREDBPATH + File.separatorChar + resourceName + RESTProps.TNKEND;
            final File dbFile = new File(tnkFile);

            // variables for highest rest-id in respectively revision
            long maxRestidRev1 = 0;
            long maxRestidRev2 = 0;

            // Connection to treetank, creating a session
            IDatabase database = null;
            IAxis axis = null;
            IReadTransaction rtx = null;
            ISession session = null;
            // List for all restIds of modifications
            final List<Long> modificRestids = new LinkedList<Long>();

            // List of all restIds of revision 1
            final List<Long> restIdsRev1 = new LinkedList<Long>();

            try {
                database = Database.openDatabase(dbFile);
                session = database.getSession();

                // get highest rest-id from given revision 1
                rtx = session.beginReadTransaction(revision1);
                axis = new XPathAxis(rtx, ".//*");

                while (axis.hasNext()) {
                    if (rtx.getNode().getNodeKey() > maxRestidRev1) {
                        maxRestidRev1 = rtx.getNode().getNodeKey();
                    }
                    // stores all restids from revision 1 into a list
                    restIdsRev1.add(rtx.getNode().getNodeKey());
                }
                rtx.moveToDocumentRoot();
                rtx.close();

                // get highest rest-id from given revision 2
                rtx = session.beginReadTransaction(revision2);
                axis = new XPathAxis(rtx, ".//*");

                while (axis.hasNext()) {
                    final Long nodeKey = rtx.getNode().getNodeKey();
                    if (nodeKey > maxRestidRev2) {
                        maxRestidRev2 = rtx.getNode().getNodeKey();
                    }
                    if (nodeKey > maxRestidRev1) {
                        /*
                         * writes all restids of revision 2 higher than the
                         * highest restid of revision 1 into the list
                         */
                        modificRestids.add(nodeKey);
                    }
                    /*
                     * removes all restids from restIdsRev1 that appears in
                     * revision 2 all remaining restids in the list can be seen
                     * as deleted nodes
                     */
                    restIdsRev1.remove(nodeKey);
                }
                rtx.moveToDocumentRoot();
                rtx.close();

                rtx = session.beginReadTransaction(revision1);

                // linked list for holding unique restids from revision 1
                final List<Long> restIdsRev1New = new LinkedList<Long>();

                /*
                 * Checks if a deleted node has a parent node that was deleted
                 * too. If so, only the parent node is stored in new list to
                 * avoid double print of node modification
                 */
                for (Long nodeKey : restIdsRev1) {
                    rtx.moveTo(nodeKey);
                    final long parentKey = rtx.getNode().getParentKey();
                    if (!restIdsRev1.contains(parentKey)) {
                        restIdsRev1New.add(nodeKey);
                    }
                }
                rtx.moveToDocumentRoot();
                rtx.close();

                if (wrap) {
                    output.write(beginResult.getBytes());
                }
                /*
                 * Shred modified restids from revision 2 to xml fragment Just
                 * modifications done by post commands
                 */
                rtx = session.beginReadTransaction(revision2);

                for (Long nodeKey : modificRestids) {
                    rtx.moveTo(nodeKey);
                    WorkerHelper.serializeXML(session, output, false, nodeid, nodeKey, revision2).call();
                }
                rtx.moveToDocumentRoot();
                rtx.close();

                /*
                 * Shred modified restids from revision 1 to xml fragment Just
                 * modifications done by put and deletes
                 */
                rtx = session.beginReadTransaction(revision1);
                for (Long nodeKey : restIdsRev1New) {
                    rtx.moveTo(nodeKey);
                    WorkerHelper.serializeXML(session, output, false, nodeid, nodeKey, revision1).call();
                }
                if (wrap) {
                    output.write(endResult.getBytes());
                }

                rtx.moveToDocumentRoot();

            } catch (final Exception globExcep) {
                throw new JaxRxException(globExcep);
            } finally {
                WorkerHelper.closeRTX(rtx, session, database);
            }
        } else {
            throw new JaxRxException(400, "Bad user request");
        }

        return output;
    }

    /**
     * This method checks a collection for included tnk files.
     * 
     * @param resource
     *            The name of the collection.
     * @return A list of available tnk files.
     */
    private List<File> checkColForTnks(final String resource) {
        final List<File> tnks = new ArrayList<File>();
        final File colFile = new File(RESTProps.STOREDBPATH + File.separatorChar + resource);
        if (colFile.exists()) {
            final File[] files = colFile.listFiles();
            for (final File file : files) {
                if (file.getName().endsWith(RESTProps.TNKEND)) {
                    tnks.add(file);
                }
            }
        } else {
            throw new JaxRxException(404, "Collection not found");
        }

        return tnks;
    }

    /**
     * The XML serializer to a given tnk file.
     * 
     * @param aTNK
     *            The tnk that has to be serialized.
     * @param revision
     *            The revision of the document.
     * @param output
     *            The output stream where we write the XML file.
     * @param nodeid
     *            <code>true</code> when you want the result nodes with node
     *            id's. <code>false</code> otherwise.
     * @throws WebApplicationException
     *             The exception occurred.
     * @throws TreetankException
     */
    private void serializIt(final File aTNK, final Long revision, final OutputStream output,
        final boolean nodeid) throws JaxRxException, TreetankException {
        // Connection to treetank, creating a session
        IDatabase database = null;
        ISession session = null;
        // IReadTransaction rtx = null;
        try {
            database = Database.openDatabase(aTNK);
            session = database.getSession();
            // and creating a transaction
            // if (revision == null) {
            // rtx = session.beginReadTransaction();
            // } else {
            // rtx = session.beginReadTransaction(revision);
            // }
            final XMLSerializerBuilder builder;
            if (revision == null)
                builder = new XMLSerializerBuilder(session, output);
            else
                builder = new XMLSerializerBuilder(session, output, revision);
            builder.setREST(nodeid);
            builder.setID(nodeid);
            builder.setDeclaration(false);
            final XMLSerializer serializer = builder.build();
            serializer.call();
        } catch (final Exception exce) {
            throw new JaxRxException(exce);
        } finally {
            // closing the treetank storage
            WorkerHelper.closeRTX(null, session, database);
        }
    }

    /**
     * This method reverts the latest revision data to the requested.
     * 
     * @param resourceName
     *            The name of the XML resource.
     * @param backToRevision
     *            The revision value, which has to be set as the latest.
     * @throws WebApplicationException
     * @throws TreetankException
     */
    public void revertToRevision(final String resourceName, final long backToRevision) throws JaxRxException,
        TreetankException {
        final StringBuilder tnkFileName =
            new StringBuilder(RESTProps.STOREDBPATH + File.separatorChar + resourceName);
        tnkFileName.append(RESTProps.TNKEND);
        final File tnk = new File(tnkFileName.toString());
        IDatabase database = null;
        ISession session = null;
        IWriteTransaction wtx = null;
        boolean abort = false;
        try {
            database = Database.openDatabase(tnk);
            session = database.getSession();
            wtx = session.beginWriteTransaction();
            wtx.revertTo(backToRevision);
            wtx.commit();
        } catch (final TreetankException exce) {
            abort = true;
            throw new JaxRxException(exce);
        } finally {
            WorkerHelper.closeWTX(abort, wtx, session, database);
        }
    }

}
