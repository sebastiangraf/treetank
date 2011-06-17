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

package org.treetank.gui.view.sunburst;

import static org.easymock.EasyMock.*;

import java.io.File;

import junit.framework.Assert;

import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.sunburst.Item.Builder;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.model.ITraverseModel;
import org.treetank.gui.view.sunburst.model.SunburstCompareModel;
import org.treetank.service.xml.shredder.EShredderCommit;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.shredder.XMLUpdateShredder;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test SunburstCompareModel.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public class SunburstCompareModelTest {
    private static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources";

    private static final String XMLDELETESECOND = RESOURCES + File.separator + "revXMLsDelete1";

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();
    }

    @After
    public void tearDown() throws AbsTTException {
        TestHelper.closeEverything();
    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

    @Ignore
    @Test
    public void testDescendantCount() throws Exception {
        Assert.assertTrue(true);
        final ITraverseModel mock = createStrictMock(ITraverseModel.class);
        final Builder builder = Item.BUILDER;
        builder.set(0, 0, -1).setDescendantCount(70);
        mock.createSunburstItem(builder.build(), 0, -1);
        replay(mock);
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration());
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final File firstRev = new File(XMLDELETESECOND + File.separator + "1.xml");
        XMLShredder shredder =
            new XMLShredder(wtx, XMLShredder.createReader(firstRev), EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        final File secondRev = new File(XMLDELETESECOND + File.separator + "2.xml");
        shredder =
            new XMLUpdateShredder(wtx, XMLShredder.createReader(secondRev), EShredderInsert.ADDASFIRSTCHILD,
                secondRev, EShredderCommit.COMMIT);
        shredder.call();
        wtx.close();

        final IReadTransaction rtx = session.beginReadTransaction();
        final ReadDB db = new ReadDB(secondRev, 1);
        final AbsModel model = new SunburstCompareModel(null, db);
        rtx.moveTo(db.getNodeKey());
//        model.traverseTree(new SunburstContainer().setRevision(rtx.getRevisionNumber()).setModWeight(0.7f));
        verify(mock);
        db.close();
    }
}
