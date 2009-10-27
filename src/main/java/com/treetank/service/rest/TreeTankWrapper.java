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

import java.io.IOException;
import java.io.OutputStream;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankFrameworkException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankRestException;
import com.treetank.service.xml.XMLSerializer;
import com.treetank.service.xml.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.session.Session;

/**
 * This class wraps a treetank instance to make it accessable with the help of
 * REST.
 * 
 * Bound to a given path, this class encapsulates all Treetank-Commands to make
 * it access for the Rest.
 * 
 * @author Georgios Gianakarras, University of Konstanz
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
     * @param path
     *            to be bound to.
     */
    public TreeTankWrapper(final String path) {
        session = Session.beginSession(path);
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
            revNumber = XMLShredder.shred(id, value, session);
        } catch (final TreetankFrameworkException exc) {
            throw new TreetankRestException(500, exc.getMessage(), exc);
        }

        return revNumber;
    }

    /**
     * Putting some text in the structure.
     * 
     * @param id
     *            of the node where the value should be changed
     * @param value
     *            to be changed
     * @return the new revision number
     * @throws TreetankRestException
     *             of anything weird occurs
     */
    public long putText(final long id, final String value)
            throws TreetankRestException {
        IWriteTransaction wtx = null;
        try {
            wtx = session.beginWriteTransaction();
            final long revision = wtx.getRevisionNumber();
            if (wtx.moveTo(id)) {
                wtx.setValue(value);
                wtx.commit();
            } else {
                throw new TreetankRestException(404, new StringBuilder(
                        "Node with id=").append(id).append(" not found.")
                        .toString());
            }
            return revision;
        } catch (final TreetankRestException te) {
            try {
                wtx.abort();
            } catch (final TreetankIOException exc) {
                throw new TreetankRestException(exc);
            }
            throw te;
        } catch (final TreetankIOException exc) {
            throw new TreetankRestException(exc);
        } finally {
            if (wtx != null) {
                wtx.close();
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
            } catch (final TreetankIOException exc) {
                throw new TreetankRestException(exc);
            }
            throw te;
        } catch (final TreetankIOException exc) {
            throw new TreetankRestException(exc);
        } finally {
            if (wtx != null) {
                wtx.close();
            }
        }
    }

    /**
     * Getting the last revision. This call is NOT threadsafe!
     * 
     * @return getting the last revision call;
     */
    public long getLastRevision() {
        IReadTransaction rtx = null;
        long lastRevision = 0;
        try {
            rtx = session.beginReadTransaction();
            lastRevision = rtx.getRevisionNumber();
        } finally {
            if (rtx != null) {
                rtx.close();
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
                new XMLSerializer(rtx, out, false, true).run();
                out.write(END_REST_ITEM);
            } else {
                throw new TreetankRestException(404, "Node with id=" + id
                        + " not found.");
            }
        } catch (final TreetankRestException te) {
            throw te;
        } catch (final IOException ie) {
            throw new TreetankRestException(500, ie.getMessage(), ie);
        } finally {
            if (rtx != null) {
                rtx.close();
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
                        new XMLSerializer(rtx, out, false, true).run();
                    } else {
                        out.write(rtx.getNode().getRawValue());
                    }
                    out.write(END_REST_ITEM);
                }
            } else {
                throw new TreetankRestException(404, "Node with id=" + id
                        + " not found.");
            }
        } catch (TreetankRestException te) {
            throw te;
        } catch (IOException ie) {
            throw new TreetankRestException(500, ie.getMessage(), ie);
        } finally {
            if (rtx != null) {
                rtx.close();
            }
        }
    }

    /**
     * Closing the wrapper
     */
    public void close() {
        session.close();
    }

}
