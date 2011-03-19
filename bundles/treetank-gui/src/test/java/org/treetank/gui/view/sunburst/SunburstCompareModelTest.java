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
