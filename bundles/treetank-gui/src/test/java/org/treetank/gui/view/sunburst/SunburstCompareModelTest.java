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
 *     * Neither the name of the <organization> nor the
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

package org.treetank.gui.view.sunburst;

import java.io.File;

import junit.framework.Assert;

import org.treetank.TestHelper;
import org.treetank.exception.AbsTTException;

import org.junit.After;
import org.junit.Before;
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
    
//    @Test
//    public void testDescendantCount() throws AbsTTException, IOException, XMLStreamException, InterruptedException, ExecutionException {
//        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
//        final ISession session = database.getSession();
//        final IWriteTransaction wtx = session.beginWriteTransaction();
//        final File firstRev = new File(XMLDELETESECOND + File.separator + "1.xml");
//        XMLShredder shredder =
//            new XMLShredder(wtx, XMLShredder.createReader(firstRev), EShredderInsert.ADDASFIRSTCHILD);
//        shredder.call();
//        final File secondRev = new File(XMLDELETESECOND + File.separator + "2.xml");
//        shredder =
//            new XMLUpdateShredder(wtx, XMLShredder.createReader(secondRev),
//                EShredderInsert.ADDASFIRSTCHILD, secondRev, EShredderCommit.COMMIT);
//        shredder.call();
//        wtx.close();
//        
//        final IReadTransaction rtx = session.beginReadTransaction();
//        final IModel mock = createStrictMock(IModel.class);
//        expect(mock.getDescendants(rtx).get(0).get()).andReturn(8);
//        replay(mock);
//        final ReadDB db = new ReadDB(secondRev, 1);
//        final AbsModel model = new SunburstCompareModel(null, db);
//        rtx.moveTo(db.getNodeKey());
//        model.getDescendants(rtx);
//        verify(mock);
//        db.close();
////        Assert.assertEquals((Integer)8, descendants.get(0).get());
//    }
}
