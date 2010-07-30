/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.saxon.wrapper;

import net.sf.saxon.om.MutableDocumentInfo;

import com.treetank.api.IDatabase;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;

/**
 * <h1>MutableDocumentWrapper</h1>
 * 
 * <p>
 * Main entry point for creating a modifiable tree in Saxon in concunction with the implementation of Saxon's
 * MutableNodeInfo core interface. Represents a document node.
 * </p>
 * 
 * <p>
 * <strong>Currently not used.</strong> For use with XQuery Update and requires a "commercial" Saxon license.
 * Furthermore as of now not stable and doesn't support third party applications.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class MutableDocumentWrapper extends MutableNodeWrapper implements MutableDocumentInfo {

    protected MutableDocumentWrapper(IDatabase database, IWriteTransaction wtx) throws TreetankException {
        super(database, wtx);
    }

    @Override
    public void resetIndexes() {
        // TODO Auto-generated method stub

    }

}
