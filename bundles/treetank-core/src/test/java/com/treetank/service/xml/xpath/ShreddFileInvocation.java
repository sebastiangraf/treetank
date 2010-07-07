/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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
 * $Id: ShreddFileInvocation.java 4410 2008-08-27 13:42:43Z kramis $
 */

package com.treetank.service.xml.xpath;

import static org.junit.Assert.fail;

import java.io.File;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.XMLShredder;

public class ShreddFileInvocation {

    public static final String XML = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "content.xml";

    public static void main(String[] args) {
        try {
            TestHelper.deleteEverything();
            // Setup parsed session.
            XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());

            // Verify.
            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1
                    .getFile());
            final ISession session = database.getSession();
            final IReadTransaction rtx = session.beginReadTransaction();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            rtx.moveToDocumentRoot();

            final IAxis xpath = new XPathAxis(wtx,
                    "/office:document-content/office:body/office:text/text:p");
            for (long node : xpath) {
                System.out.println(node);
                wtx.moveTo(node);
                wtx.remove();
            }
            wtx.close();
            session.close();
            database.close();
            TestHelper.closeEverything();
        } catch (final Exception exc) {
            fail(exc.toString());
        }

    }

}
