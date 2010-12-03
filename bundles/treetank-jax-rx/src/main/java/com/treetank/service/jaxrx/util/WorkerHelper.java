package com.treetank.service.jaxrx.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.enums.EIdAccessType;
import com.treetank.service.jaxrx.implementation.DatabaseRepresentation;
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;
import com.treetank.service.xml.serialize.XMLSerializerProperties;
import com.treetank.service.xml.shredder.XMLShredder;

/**
 * This class contains methods that are respectively used by this worker classes
 * (NodeIdRepresentation.java and DatabaseRepresentation.java)
 * 
 * @author Patrick Lang, Lukas Lewandowski, University of Konstanz
 * 
 */

public final class WorkerHelper {

    /**
     * The map containing the available access types.
     */
    private final transient Map<String, EIdAccessType> typeList;

    /**
     * This constructor initializes the {@link EIdAccessType}s.
     */
    private WorkerHelper() {
        typeList = new HashMap<String, EIdAccessType>();
        typeList.put("FIRSTCHILD()", EIdAccessType.FIRSTCHILD);
        typeList.put("LASTCHILD()", EIdAccessType.LASTCHILD);
        typeList.put("RIGHTSIBLING()", EIdAccessType.RIGHTSIBLING);
        typeList.put("LEFTSIBLING()", EIdAccessType.LEFTSIBLING);
    }

    /**
     * The instance variable for singleton.
     */
    private static final transient WorkerHelper INSTANCE = new WorkerHelper();

    /**
     * Shreds a given InputStream
     * 
     * @param wtx
     *            current write transaction reference
     * @param value
     *            InputStream to be shred
     */
    public static void shredInputStream(final IWriteTransaction wtx, final InputStream value,
        final boolean child) {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLEventReader parser;
        try {
            parser = factory.createXMLEventReader(value);
        } catch (final XMLStreamException xmlse) {
            throw new WebApplicationException(xmlse);
        }

        try {
            final XMLShredder shredder = new XMLShredder(wtx, parser, child);
            shredder.call();
        } catch (final Exception exce) {
            throw new WebApplicationException(exce);
        }
    }

    /**
     * This method checks if the file is available and not empty.
     * 
     * @param resource
     *            The file that will be checked.
     * @return <code>true</code> when the file exists and is not empty. <code>false</code> otherwise.
     */
    public static boolean checkExistingResource(final File resource) {
        boolean isExisting;
        if (resource.getAbsoluteFile().getTotalSpace() > 0) {
            isExisting = true;
        } else {
            isExisting = false;
        }
        return isExisting;
    }

    /**
     * This method creates a new XMLSerializer reference
     * 
     * @param session
     *            Associated session.
     * @param out
     *            OutputStream
     * 
     * @param serializeXMLDec
     *            specifies whether XML declaration should be shown
     * @param serializeRest
     *            specifies whether node id should be shown
     * 
     * @return new XMLSerializer reference
     */
    public static XMLSerializer serializeXML(final ISession session, final OutputStream out,
        final boolean serializeXMLDec, final boolean serializeRest, final Long revision) {
        final XMLSerializerBuilder builder;
        if (revision == null)
            builder = new XMLSerializerBuilder(session, out);
        else
            builder = new XMLSerializerBuilder(session, out, revision);
        builder.setREST(serializeRest);
        builder.setID(serializeRest);
        builder.setDeclaration(serializeXMLDec);
        final XMLSerializer serializer = builder.build();
        return serializer;
    }

    /**
     * This method creates a new XMLSerializer reference
     * 
     * @param session
     *            Associated session.
     * @param out
     *            OutputStream
     * 
     * @param serializeXMLDec
     *            specifies whether XML declaration should be shown
     * @param serializeRest
     *            specifies whether node id should be shown
     * 
     * @return new XMLSerializer reference
     */
    public static XMLSerializer serializeXML(final ISession session, final OutputStream out,
        final boolean serializeXMLDec, final boolean serializeRest, final Long nodekey, final Long revision) {
        final XMLSerializerProperties props = new XMLSerializerProperties();
        final XMLSerializerBuilder builder;
        if (revision == null && nodekey == null)
            builder = new XMLSerializerBuilder(session, out);
        else if (revision != null && nodekey == null)
            builder = new XMLSerializerBuilder(session, out, revision);
        else if (revision == null && nodekey != null)
            builder = new XMLSerializerBuilder(session, nodekey, out, props);
        else
            builder = new XMLSerializerBuilder(session, nodekey, out, props, revision);
        builder.setREST(serializeRest);
        builder.setID(serializeRest);
        builder.setDeclaration(serializeXMLDec);
        builder.setIndend(false);
        final XMLSerializer serializer = builder.build();
        return serializer;
    }

    /**
     * This method creates a new TreeTank reference
     * 
     * @return new Treetank reference
     */
    public DatabaseRepresentation createTreeTrankObject() {
        return new DatabaseRepresentation();
    }

    /**
     * This method creates a new StringBuilder reference
     * 
     * @return new StringBuilder reference
     */
    public StringBuilder createStringBuilderObject() {
        return new StringBuilder();
    }

    /**
     * This method closes all open treetank connections concerning a
     * WriteTransaction.
     * 
     * @param abortTransaction
     *            <code>true</code> if the transaction has to be aborted, <code>false</code> otherwise.
     * @param wtx
     *            IWriteTransaction to be closed
     * @param ses
     *            ISession to be closed
     * @param dbase
     *            IDatabase to be closed
     * @throws TreetankException
     */
    public static void closeWTX(final boolean abortTransaction, final IWriteTransaction wtx,
        final ISession ses, final IDatabase dbase) throws TreetankException {
        synchronized (dbase) {
            if (abortTransaction) {
                wtx.abort();
            }
            dbase.close();
        }
    }

    /**
     * This method closes all open treetank connections concerning a
     * ReadTransaction.
     * 
     * @param rtx
     *            IReadTransaction to be closed
     * @param ses
     *            ISession to be closed
     * @param dbase
     *            IDatabase to be closed
     * @throws TreetankException
     */
    public static void closeRTX(final IReadTransaction rtx, final ISession ses, final IDatabase dbase)
        throws TreetankException {
        synchronized (dbase) {
            dbase.close();
        }
    }

    /**
     * This method checks the variable URL path after the node id resource (e.g.
     * http://.../factbook/3/[ACCESSTYPE]) for the available access type to
     * identify a node. The access types are defined in {@link EIdAccesType}.
     * 
     * @param accessType
     *            The access type as String value encoded in the URL request.
     * @return The valid access type or null otherwise.
     */
    public EIdAccessType validateAccessType(final String accessType) {
        return typeList.get(accessType.toUpperCase(Locale.US));
    }

    /**
     * This method return the singleton instance.
     * 
     * @return The single instance.
     */
    public static WorkerHelper getInstance() {
        return INSTANCE;
    }
}
