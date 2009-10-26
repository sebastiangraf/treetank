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
 * $Id: XMarkTest.java 4433 2008-08-28 14:26:02Z scherer $
 */

package com.treetank.service.xml.xpath;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.perfidix.annotation.BenchClass;

import com.treetank.ITestConstants;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.exception.TreetankFrameworkException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;

/**
 * Performes the XMark benchmark.
 * 
 * @author Tina Scherer
 */
@BenchClass(runs = 1)
public class XMarkTest {

    // XMark 1 GB
    private static final String XML = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "auction.xml";

    private ISession session;

    private IReadTransaction rtx;

    @Before
    public void setUp() {
        try {
            Session.removeSession(ITestConstants.PATH1);
            // Build simple test tree.
            XMLShredder.shred(XML, new SessionConfiguration(
                    ITestConstants.PATH1));

            // Verify.
            session = Session.beginSession(ITestConstants.PATH1);
            rtx = session.beginReadTransaction();
        } catch (final TreetankFrameworkException exc) {
            fail(exc.toString());
        }
    }

    @After
    public void tearDown() {
        rtx.close();
        session.close();
    }

    @Test
    public void testQ1_10() throws IOException {

        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
                "/site/people/person[@id=\"person0\"]/name/text()"),
                new String[] { "Sinisa Farrel" });

    }

    @Test
    public void testQ1() throws IOException {

        // Q1 The name of the person with ID 'person0' {projecting}
        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
                "for $b in /site/people/person[@id=\"person0\"] "
                        + "return $b/name/text()"),
                new String[] { "Sinisa Farrel" });

    }

    // @Test
    // public void testQ2() throws IOException {
    // // // Q2 Return the initial increases of all open auctions
    // // XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
    // // "for $b in /site/open_auctions/open_auction return $b/bidder[1]"
    // // + "/increase/text()"), new String[] { "9.00", "42.00", "9.00",
    // // "3.00", "6.00", "27.00", "7.50", "6.00", "3.00", "55.50", "15.00", "",
    // // "10.50", "13.50", "28.50", "27.00", "3.00", "7.50", "", "30.00",
    // // "16.50", "25.50", "21.00", "4.50", "9.00", "34.50", "21.00", "9.00",
    // // "16.50", "", "55.50", "1.50", "", "15.00", "3.00", "22.50", "52.50",
    // // "21.00", "4.50", "39.00", "3.00", "9.00", "", "", "", "16.50", "3.00",
    // // "7.50", "", "27.00", "9.00", "51.00", "31.50", "12.00", "6.00",
    // // "42.00", "21.00", "9.00", "52.50", "1.50", "", "4.50", "4.50", "6.00",
    // // "55.50", "13.50", "10.50", "10.50", "25.50", "", "18.00", "15.00", "",
    // // "39.00", "49.50", "15.00", "", "", "7.50", "13.50", "10.50", "3.00",
    // // "6.00", "7.50", "39.00", "6.00", "21.00", "51.00", "16.50", "3.00",
    // // "24.00", "15.00", "7.50", "22.50", "1.50", "15.00", "34.50", "15.00",
    // // "7.50", "33.00", "6.00", "42.00", "10.50", "27.00", "7.50", "19.50",
    // // "19.50", "30.00", "28.50", "6.00", "1.50", "46.50", "7.50", "3.00",
    // "",
    // // "31.50", "10.50", "1.50", "1.50", "9.00" });
    //
    // XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
    // "fn:count(for $b in /site/open_auctions/open_auction return $b/bidder[1]"
    // + "/increase/text())"), new String[] { "106"}); // oder 120
    //
    // XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
    // "/site/open_auctions/open_auction/bidder[1]/increase/text()"),
    // new String[] {"Sinisa Farrel"});

    //  
    // @Test
    // public void testQ3() throws IOException {
    // // //Q3 Return the IDs of all open auctions whose current increase is at
    // // least
    // // //twice as high as the initial increase
    // // XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
    // // "for $b in /site/open_auctions/open_auction return $b/bidder[1]"
    // // + "/increase/text()"), new String[] {});
    // }
    // @Test
    // public void testQ4() throws IOException {
    // }

    @Test
    public void testQ5() throws IOException {

        // Q5 How many sold items cost more than 40?
        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
                "fn:count(for $i in /site/closed_auctions/closed_auction[price/text() >= 40] "
                        + "return $i/price)"), new String[] { "75" });

    }

    @Test
    public void testQ6() throws IOException {

        // Q6 How many items are listed on all continents?
        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
                "for $b in //site/regions return fn:count($b//item)"),
                new String[] { "217" });

    }

    @Test
    public void testQ7() throws IOException {

        // Q7 How many pieces of prose are in our database?
        XPathStringChecker
                .testIAxisConventions(
                        new XPathAxis(
                                rtx,
                                "for $p in /site return fn:count($p//description) + "
                                        + "fn:count($p//annotation) + fn:count($p//emailaddress)"),
                        new String[] { "916.0" }); // TODO: why double?

    }

    // @Test
    // public void testQ8() throws IOException {
    // // Q8 List the names of persons and the number of items they bought
    // // (joins person, closed\_auction)
    // XPathStringChecker.testIAxisConventions(
    // new XPathAxis(rtx, ""),
    // new String[] { "" });
    //
    // }

    // @Test
    // public void testQ9() throws IOException {
    // // // Q9 List the names of persons and the names of the items they bought
    // in
    // // // Europe. (joins person, closed_auction, item)
    // // XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
    // // ""),
    // // new String[] { "" });
    // }

    //  
    // @Test
    // public void testPos() throws IOException {
    // XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
    // "/site/regions/*/item[2]/@id"), new String[] {"item1", "item6",
    // "item26", "item48", "item108", "item208"});
    // }

}