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
 * $Id: XPathAxisWideTest.java 4433 2008-08-28 14:26:02Z scherer $
 */

package com.treetank.service.xml.xpath;

import java.io.File;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.AbsAxis;
import com.treetank.axis.IAxisTest;
import com.treetank.exception.TTException;
import com.treetank.service.xml.shredder.XMLShredder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XPathAxisWideTest {

    public static final String XML =
        "src" + File.separator + "test" + File.separator + "resources" + File.separator + "factbook.xml";

    public static final String XML2 =
        "src" + File.separator + "test" + File.separator + "resources" + File.separator + "shakespeare.xml";

    @Before
    public void setUp() throws TTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws TTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testIterateFactbook() throws Exception {
        // Setup parsed session.

        XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());

        // Verify.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IReadTransaction rtx = session.beginReadTransaction();
        rtx.moveToDocumentRoot();

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/mondial/continent[@id]"), new long[] {
            2L, 5L, 8L, 11L, 14L
        });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "mondial/continent[@name]"), new long[] {
            2L, 5L, 8L, 11L, 14L
        });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "mondial/continent[@id=\"f0_119\"]"), new long[] {
            2L
        });

        IAxisTest.testIAxisConventions(new XPathAxis(rtx, "/mondial/continent[@name = \"Africa\"]"),
            new long[] {
                14L
            });

        final AbsAxis axis5 = new XPathAxis(rtx, "mondial/lake/node()");
        for (int i = 0; i < 61; i++) {
            assertEquals(true, axis5.hasNext());
        }
        // assertEquals(29891L, axis5.next());
        assertEquals(false, axis5.hasNext());

        final AbsAxis axis6 = new XPathAxis(rtx, "mondial/country/religions/node()");
        for (int i = 0; i < 446; i++) {
            assertEquals(true, axis6.hasNext());
            axis6.next();
        }
        assertEquals(false, axis6.hasNext());

        final AbsAxis axis7 = new XPathAxis(rtx, "child::mondial/child::lake/child::node()");
        for (int i = 0; i < 60; i++) {
            assertEquals(true, axis7.hasNext());
            axis7.next();
        }
        assertEquals(true, axis7.hasNext());
        // assertEquals(29891L, axis7.next());
        assertEquals(false, axis7.hasNext());

        final AbsAxis axis8 = new XPathAxis(rtx, "//*[@id]");
        for (int i = 0; i < 5562; i++) {
            assertEquals(true, axis8.hasNext());
            axis8.next();
        }
        assertEquals(false, axis8.hasNext());

        final AbsAxis axis9 = new XPathAxis(rtx, "/mondial/country/attribute::car_code");
        for (int i = 0; i < 194; i++) {
            assertEquals(true, axis9.hasNext());
            axis9.next();
        }
        assertEquals(false, axis9.hasNext());

        final AbsAxis axis10 = new XPathAxis(rtx, "//country[@*]");
        for (int i = 0; i < 231; i++) {
            assertEquals(true, axis10.hasNext());
            axis10.next();
        }
        assertEquals(false, axis10.hasNext());

        rtx.close();
        session.close();
    }
    //
    // // lasts too long
    // @Test
    // public void testIterateShakespreare() throws IOException,
    // XMLStreamException {
    // // Setup parsed session.
    // XMLShredder.shred(XML2, new SessionConfiguration(PATH2));
    //
    // // Verify.
    // final ISession session2 = Session.beginSession(PATH2);
    // final IReadTransaction rtx2 = session2.beginReadTransaction();
    // rtx2.moveToDocumentRoot();
    //
    // XPathStringChecker.testIAxisConventions(
    // new XPathAxis(rtx2, "fn:count(//PLAY)"),
    // new String[] { "37" });
    //
    // rtx2.moveTo(2L);
    // XPathStringChecker.testIAxisConventions(
    // new XPathAxis(rtx2, "fn:count(TITLE)"),
    // new String[] { "1" });
    //
    // XPathStringChecker.testIAxisConventions(
    // new XPathAxis(rtx2, "fn:string(TITLE)"),
    // new String[] { "The Tragedy of Antony and Cleopatra" });
    //
    // XPathStringChecker.testIAxisConventions(
    // new XPathAxis(rtx2, "fn:count(//*)"),
    // new String[] { "179619" });
    //
    // XPathStringChecker.testIAxisConventions(new XPathAxis(
    // rtx2,
    // "fn:count(/PLAYS/PLAY/PERSONAE)"), new String[] { "37" });
    //
    // final IAxis axis1 = new XPathAxis(rtx2, "//PERSONA");
    // for (int i = 0; i < 969; i++) {
    // assertEquals(true, axis1.hasNext());
    // axis1.next();
    // }
    // assertEquals(false, axis1.hasNext());
    //
    // final IAxis axis2 = new XPathAxis(rtx2, "/PLAYS/PLAY//SPEECH");
    // for (int i = 0; i < 31014; i++) {
    // assertEquals(true, axis2.hasNext());
    // axis2.next();
    // }
    // assertEquals(false, axis2.hasNext());
    //
    // final IAxis axis3 = new XPathAxis(rtx2, "/PLAYS/PLAY//STAGEDIR");
    // for (int i = 0; i < 6257; i++) {
    // assertEquals(true, axis3.hasNext());
    // axis3.next();
    // }
    // assertEquals(false, axis3.hasNext());
    //
    // rtx2.moveToDocumentRoot();
    // final IAxis axis4 = new XPathAxis(rtx2, "PLAYS/PLAY//STAGEDIR/text()");
    // for (int i = 0; i < 6257; i++) {
    // assertEquals(true, axis4.hasNext());
    // axis4.next();
    // }
    // assertEquals(false, axis4.hasNext());
    //
    // final IAxis axis5 = new XPathAxis(rtx2, "/PLAYS/PLAY//SCNDESCR");
    // for (int i = 0; i < 37; i++) {
    // assertEquals(true, axis5.hasNext());
    // axis5.next();
    // }
    // assertEquals(false, axis5.hasNext());
    //
    // final IAxis axis6 = new XPathAxis(rtx2, "//SPEECH/SPEAKER");
    // for (int i = 0; i < 31067; i++) {
    // assertEquals(true, axis6.hasNext());
    // axis6.next();
    // }
    // assertEquals(false, axis6.hasNext());
    //
    // XPathStringChecker.testIAxisConventions(new XPathAxis(
    // rtx2,
    // "PLAYS/PLAY/TITLE/text()"), new String[] {
    // "The Tragedy of Antony and Cleopatra",
    // "All's Well That Ends Well",
    // "As You Like It",
    // "The Comedy of Errors",
    // "The Tragedy of Coriolanus",
    // "Cymbeline",
    // "A Midsummer Night's Dream",
    // "The Tragedy of Hamlet, Prince of Denmark",
    // "The First Part of Henry the Fourth",
    // "The Second Part of Henry the Fourth",
    // "The Life of Henry the Fifth",
    // "The First Part of Henry the Sixth",
    // "The Second Part of Henry the Sixth",
    // "The Third Part of Henry the Sixth",
    // "The Famous History of the Life of Henry the Eighth",
    // "The Tragedy of Julius Caesar",
    // "The Life and Death of King John",
    // "The Tragedy of King Lear",
    // "Love's Labor's Lost",
    // "Measure for Measure",
    // "The Merry Wives of Windsor",
    // "The Tragedy of Macbeth",
    // "The Merchant of Venice",
    // "Much Ado about Nothing",
    // "The Tragedy of Othello, the Moor of Venice",
    // "Pericles, Prince of Tyre",
    // "The Tragedy of Romeo and Juliet",
    // "The Tragedy of King Richard the Second",
    // "The Tragedy of Richard the Third",
    // "Twelfth Night, or What You Will",
    // "The Taming of the Shrew",
    // "The Tempest",
    // "The Life of Timon of Athens",
    // "The Tragedy of Titus Andronicus",
    // "The History of Troilus and Cressida",
    // "The Two Gentlemen of Verona",
    // "The Winter's Tale" });
    //
    // final IAxis axis7 = new XPathAxis(rtx2, "PLAYS/PLAY");
    // for (int i = 0; i < 37; i++) {
    // assertEquals(true, axis7.hasNext());
    // axis7.next();
    // }
    // assertEquals(false, axis7.hasNext());
    //
    // rtx2.close();
    // session2.close();
    // }
}
