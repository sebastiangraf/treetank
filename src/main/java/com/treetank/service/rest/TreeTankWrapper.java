/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: TreeTankWrapper.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.service.rest;

import java.io.File;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.treetank.access.Database;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankRestException;
import com.treetank.service.xml.XMLSerializer;
import com.treetank.service.xml.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;

/**
 * This class wraps a treetank instance to make it accessable with the help of
 * REST.
 * 
 * Bound to a given path, this class encapsulates all Treetank-Commands to make
 * it access for the Rest.
 * 
 * @author Georgios Giannakaras, University of Konstanz
 * 
 */
public final class TreeTankWrapper {

    /** Byte representation for the begin-rest item */
    private final static byte[] BEGIN_REST_ITEM = { 60, 114, 101, 115, 116, 58,
            105, 116, 101, 109, 62 };

    /** Byte representation for the end-rest item */
    private final static byte[] END_REST_ITEM = { 60, 47, 114, 101, 115, 116,
            58, 105, 116, 101, 109, 62 };

    /** Session to be bound to */
    private final ISession session;

    /**
     * Constructor, just binding the treetank to this service.
     * 
     * @param file
     *            to be bound to.
     */
    public TreeTankWrapper(final File file) throws TreetankRestException {

        try {
            final IDatabase db = Database.openDatabase(file);
            session = db.getSession();
        } catch (final TreetankException exc) {
            throw new TreetankRestException(exc);
        }
    }

    /**
     * Posting content as a subtree for the node with the given id
     * 
     * @param id
     *            where the new content should be located for subtree
     * @param value
     *            to be posted, can be a complete xml-structure.
     * @return new revision numbers
     * @throws TreetankRestException
     *             of anything weird occurs
     */
    public long post(final long id, final String value)
            throws TreetankRestException {
        long revNumber = -1;
        try {
            final IWriteTransaction wtx = session.beginWriteTransaction();
            wtx.moveTo(id);
            shredString(wtx, value);
        } catch (final Exception exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }

        return revNumber;
    }

    private long shredString(final IWriteTransaction wtx, final String value)
            throws Exception {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        final XMLStreamReader parser = factory
                .createXMLStreamReader(new StringReader(value));
        final XMLShredder shredder = new XMLShredder(wtx, parser);
        return shredder.call();
    }

    /**
     * Putting some data in the structure. This results in deleting the old
     * content and putting in the new one.
     * 
     * @param id
     *            of the node where the value should be changed
     * @param value
     *            to be changed
     * @return the new revision number
     * @throws TreetankRestException
     *             of anything weird occurs
     */
    public long put(final long id, final String value)
            throws TreetankRestException {
        IWriteTransaction wtx = null;
        try {
            wtx = session.beginWriteTransaction();
            wtx.moveTo(id);

            if (wtx.moveToFirstChild()) {
                do {
                    wtx.remove();
                } while (wtx.getNode().getNodeKey() != id);
            }
            final long attCount = wtx.getNode().getAttributeCount();
            if (attCount > 0) {
                for (int i = 0; i < attCount; i++) {
                    wtx.moveToAttribute(i);
                    wtx.remove();
                }
            }
            final long namespaceCount = wtx.getNode().getNamespaceCount();
            if (namespaceCount > 0) {
                for (int i = 0; i < namespaceCount; i++) {
                    wtx.moveToNamespace(i);
                    wtx.remove();
                }
            }

            final long revision = shredString(wtx, value);
            wtx.commit();
            wtx.close();
            return revision;
        } catch (final Exception exc) {
            throw new TreetankRestException(exc);
        } finally {
            if (wtx != null) {
                try {
                    wtx.close();
                } catch (final TreetankException exc) {
                    throw new TreetankRestException(exc);
                }
            }
        }
    }

    /**
     * Deleting a node with the given id.
     * 
     * @param id
     *            to be deleted.
     * @return the new revision number
     * @throws TreetankRestException
     */
    public long delete(final long id) throws TreetankRestException {
        IWriteTransaction wtx = null;
        long revision = 0;
        try {
            wtx = session.beginWriteTransaction();
            revision = wtx.getRevisionNumber();
            if (wtx.moveTo(id)) {
                wtx.remove();
                wtx.commit();
            } else {
                throw new TreetankRestException(404, "Node with id=" + id
                        + " not found.");
            }
            return revision;
        } catch (final TreetankRestException te) {
            try {
                wtx.abort();
            } catch (final TreetankException exc) {
                throw new TreetankRestException(exc);
            }
            throw te;
        } catch (final TreetankException exc) {
            throw new TreetankRestException(exc);
        } finally {
            if (wtx != null) {
                try {
                    wtx.close();
                } catch (final TreetankException exc) {
                    throw new TreetankRestException(exc);
                }
            }
        }
    }

    /**
     * Getting the last revision. This call is NOT threadsafe!
     * 
     * @return getting the last revision call;
     */
    public long getLastRevision() throws TreetankRestException {
        IReadTransaction rtx = null;
        long lastRevision = 0;
        try {
            rtx = session.beginReadTransaction();
            lastRevision = rtx.getRevisionNumber();
        } catch (final TreetankException exc) {
            throw new TreetankRestException(exc);
        } finally {
            if (rtx != null) {
                try {
                    rtx.close();
                } catch (final TreetankException exc) {
                    throw new TreetankRestException(exc);
                }
            }
        }
        return lastRevision;
    }

    /**
     * Getting part of the xml as OutputStream.
     * 
     * @param out
     *            to retrieve from treetank-storage
     * @param revision
     *            to be retrieved
     * @param id
     *            as the start node of the tree
     * @throws TreetankRestException
     */
    public void get(final OutputStream out, final long revision, final long id)
            throws TreetankRestException {
        IReadTransaction rtx = null;
        try {
            rtx = session.beginReadTransaction(revision);
            if (rtx.moveTo(id)) {
                out.write(BEGIN_REST_ITEM);
                new XMLSerializer(rtx, out, false, true).call();
                out.write(END_REST_ITEM);
            } else {
                throw new TreetankRestException(404, "Node with id=" + id
                        + " not found.");
            }

        } catch (final Exception ie) {
            throw new TreetankRestException(500, ie.getMessage(), ie);
        } finally {
            if (rtx != null) {
                try {
                    rtx.close();
                } catch (final TreetankException exc) {
                    throw new TreetankRestException(exc);
                }
            }
        }
    }

    /**
     * Getting part of the xml based on an XML-Expression
     * 
     * @param out
     *            where the content should be streamed
     * @param revision
     *            which should be read
     * @param id
     *            of the node starting with the content
     * @param expression
     *            XPath-Expression to be evaluated
     * @throws TreetankRestException
     */
    public void get(final OutputStream out, final long revision, final long id,
            final String expression) throws TreetankRestException {
        IReadTransaction rtx = null;
        try {
            rtx = session.beginReadTransaction(revision);
            if (rtx.moveTo(id)) {
                final IAxis axis = new XPathAxis(rtx, expression);
                for (final long key : axis) {
                    out.write(BEGIN_REST_ITEM);
                    if (key >= 0) {
                        new XMLSerializer(rtx, out, false, true).call();
                    } else {
                        out.write(rtx.getNode().getRawValue());
                    }
                    out.write(END_REST_ITEM);
                }
            } else {
                throw new TreetankRestException(404, "Node with id=" + id
                        + " not found.");
            }
        } catch (Exception ie) {
            throw new TreetankRestException(500, ie.getMessage(), ie);
        } finally {
            if (rtx != null) {
                try {
                    rtx.close();
                } catch (final TreetankException exc) {
                    throw new TreetankRestException(exc);
                }
            }
        }
    }

    /**
     * Closing the wrapper
     */
    public void close() throws TreetankRestException {
        try {
            session.close();
        } catch (final TreetankException exc) {
            throw new TreetankRestException(exc);
        }

    }

}
