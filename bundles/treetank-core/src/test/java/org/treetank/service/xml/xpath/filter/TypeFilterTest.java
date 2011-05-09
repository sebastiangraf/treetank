/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package org.treetank.service.xml.xpath.filter;

import java.io.File;

import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axis.AbsAxis;
import org.treetank.axis.filter.IFilterTest;
import org.treetank.axis.filter.TypeFilter;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.xpath.XPathAxis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TypeFilterTest {

    public static final String XML = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "test.xml";

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void testIFilterConvetions() throws Exception {

        // Build simple test tree.
        // final ISession session = Session.beginSession(PATH);
        // final IWriteTransaction wtx = session.beginWriteTransaction();
        // TestDocument.create(wtx);
        XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());

        // Verify.
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration());
        final IReadTransaction rtx = session.beginReadTransaction();
        final AbsAxis axis = new XPathAxis(rtx, "a");
        final IReadTransaction xtx = axis.getTransaction();

        xtx.moveTo(9L);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"), true);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:long"), false);

        xtx.moveTo(4L);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untyped"), true);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:double"), false);

        xtx.moveTo(1L);
        xtx.moveToAttribute(0);
        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:untypedAtomic"), true);

        IFilterTest.testIFilterConventions(new TypeFilter(xtx, "xs:anyType"), false);

        xtx.close();
        rtx.close();
        session.close();

    }
}
