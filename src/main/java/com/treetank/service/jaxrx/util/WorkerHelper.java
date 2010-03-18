package com.treetank.service.jaxrx.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.jaxrx.implementation.TreeTank;
import com.treetank.service.xml.XMLSerializer;
import com.treetank.service.xml.XMLShredder;

/**
 * This class contains methods that are respectively used by ths worker classes
 * (RIDWorker.java and DBWorker.java)
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */

public class WorkerHelper {

    /**
     * Shreds a given InputStream
     * 
     * @param wtx
     *            current write transaction reference
     * @param value
     *            InputStream to be shred
     */
    public void shredInputStream(final IWriteTransaction wtx,
            final InputStream value, final boolean child) {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        XMLStreamReader parser;
        try {
            parser = factory.createXMLStreamReader(value);
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
     * @return <code>true</code> when the file exists and is not empty.
     *         <code>false</code> otherwise.
     */
    public boolean checkExistingResource(final File resource) {
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
     * @param rtx
     *            IReadTransaction
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
    public XMLSerializer serializeXML(final IReadTransaction rtx,
            final OutputStream out, final boolean serializeXMLDec,
            final boolean serializeRest) {
        return new XMLSerializer(rtx, out, serializeXMLDec, serializeRest);
    }

    /**
     * This method creates a new TreeTank reference
     * 
     * @return new Treetank reference
     */
    public TreeTank createTreeTrankObject() {
        return new TreeTank();
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
     *            <code>true</code> if the transaction has to be aborted,
     *            <code>false</code> otherwise.
     * @param wtx
     *            IWriteTransaction to be closed
     * @param ses
     *            ISession to be closed
     * @param dbase
     *            IDatabase to be closed
     * @throws TreetankException
     */
    public synchronized void closeWTX(
            final boolean abortTransaction, // NOPMD due bugfixing with
                                            // Sebastion Graf
            final IWriteTransaction wtx, final ISession ses,
            final IDatabase dbase) throws TreetankException {
        if (abortTransaction) {
            wtx.abort();
        }
        dbase.close();
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
    public synchronized void closeRTX(final IReadTransaction rtx, // NOPMD due
                                                                  // bugfixing
                                                                  // with
                                                                  // Sebastion
                                                                  // Graf
            final ISession ses, final IDatabase dbase) throws TreetankException {
        dbase.close();

    }

}
