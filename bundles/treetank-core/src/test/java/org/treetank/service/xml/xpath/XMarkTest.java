/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.service.xml.xpath;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.perfidix.annotation.BenchClass;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.xpath.XPathAxis;


/**
 * Performes the XMark benchmark.
 * 
 * @author Tina Scherer
 */
@BenchClass(runs = 1)
public class XMarkTest {

    // XMark 1 GB
    private static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "auction.xml";

    private IDatabase database;

    private ISession session;

    private IReadTransaction rtx;

    @Before
    public void setUp() throws Exception {
        TestHelper.deleteEverything();
        // Build simple test tree.
        XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());

        // Verify.
        database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        session = database.getSession();
        rtx = session.beginReadTransaction();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testQ1_10() throws AbsTTException {

        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
            "/site/people/person[@id=\"person0\"]/name/text()"), new String[] {
            "Sinisa Farrel"
        });

    }

    @Test
    public void testQ1() throws AbsTTException {

        // Q1 The name of the person with ID 'person0' {projecting}
        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
            "for $b in /site/people/person[@id=\"person0\"] " + "return $b/name/text()"), new String[] {
            "Sinisa Farrel"
        });

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
    public void testQ5() throws AbsTTException {

        // Q5 How many sold items cost more than 40?
        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
            "fn:count(for $i in /site/closed_auctions/closed_auction[price/text() >= 40] "
                + "return $i/price)"), new String[] {
            "75"
        });

    }

    @Test
    public void testQ6() throws AbsTTException {

        // Q6 How many items are listed on all continents?
        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
            "for $b in //site/regions return fn:count($b//item)"), new String[] {
            "217"
        });

    }

    @Test
    public void testQ7() throws AbsTTException {

        // Q7 How many pieces of prose are in our database?
        XPathStringChecker.testIAxisConventions(new XPathAxis(rtx,
            "for $p in /site return fn:count($p//description) + "
                + "fn:count($p//annotation) + fn:count($p//emailaddress)"), new String[] {
            "916.0"
        }); // TODO: why double?

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
