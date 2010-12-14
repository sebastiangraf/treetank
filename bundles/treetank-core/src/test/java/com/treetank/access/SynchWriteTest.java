
package com.treetank.access;

import java.util.concurrent.Exchanger;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.utils.DocumentCreater;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore public class SynchWriteTest extends TestCase {
    Exchanger<Boolean> threadsFinished = new Exchanger();
    Exchanger<Boolean> verify = new Exchanger();

    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        DocumentCreater.createWithoutNamespace(wtx);
        wtx.commit();
        wtx.close();
        session.close();
    }

    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }
    
    @Test
    public void testEmptyCommit() throws TreetankException{
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.commit();
        wtx.close();
        session.close();
        TestCase.assertTrue(true);
    }

    @Test
    public void testAbort() throws TreetankException{
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.abort();
        wtx.close();
        session.close();
        TestCase.assertTrue(true);
    }

    @Test
    public void testAbortAfterCommit() throws TreetankException{
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.commit();
        wtx.abort();
        wtx.close();
        session.close();
        TestCase.assertTrue(true);
    }

    @Test
    public void testNewWtxAfterDirtyClose() throws TreetankException{
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        wtx.insertElementAsFirstChild(new QName("test"));
        wtx.close();
        try {
            IWriteTransaction wtx2 = session.beginWriteTransaction();
            wtx2.insertElementAsFirstChild(new QName("should work"));
            TestCase.assertTrue(true);
        } catch (Exception e) {
            TestCase.fail();
        }

        session.close();
        TestCase.assertTrue(true);
    }

    @Test
    public void testConcurrentWrite() throws TreetankException{
        IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final IWriteTransaction wtx2 = session.beginWriteTransaction();

        Thread t1 = new Thread(new Wtx1(wtx, threadsFinished));
        Thread t2 = new Thread(new Wtx2(wtx2, threadsFinished, verify));
        t1.start();
        t2.start();

        boolean ready = false;
        while(!ready){
            try {
                ready = verify.exchange(false);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        IReadTransaction rtx = session.beginReadTransaction();
        try {
            rtx = session.beginReadTransaction();
            TestCase.assertTrue(rtx.moveToFirstChild());
            TestCase.assertTrue(rtx.moveToFirstChild());
            TestCase.assertTrue(rtx.moveToRightSibling());
            TestCase.assertTrue(rtx.moveToFirstChild());        //should be at 6:foo
            TestCase.assertEquals("foobar", rtx.getValueOfCurrentNode());

            rtx.moveToDocumentRoot();
            TestCase.assertTrue(rtx.moveToFirstChild());
            TestCase.assertTrue(rtx.moveToFirstChild());
            TestCase.assertTrue(rtx.moveToRightSibling());
            TestCase.assertTrue(rtx.moveToRightSibling());
            TestCase.assertTrue(rtx.moveToRightSibling());
            TestCase.assertTrue(rtx.moveToFirstChild()); 
            TestCase.assertTrue(rtx.moveToRightSibling()); //should be at 12:bar
            TestCase.assertEquals("barfoo", rtx.getValueOfCurrentNode());
        } catch (TreetankException e) {
            TestCase.fail();
        }
    }
}

class Wtx1 implements Runnable{
    IWriteTransaction wtx;
    Exchanger<Boolean> threadsFinished;
    Wtx1(IWriteTransaction swtx, Exchanger<Boolean> threadsFinished){
        this.wtx = swtx;
        this.threadsFinished = threadsFinished;
    }
    @Override
    public void run(){
        wtx.moveToFirstChild();
        wtx.moveToFirstChild();
        wtx.moveToRightSibling();
        wtx.moveToFirstChild();        //should be at 6:foo
        try {
            wtx.setValue("foobar");
            wtx.commit();
            wtx.close();
        } catch (TreetankIOException e1) {
            e1.printStackTrace();
        } catch (TreetankException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            threadsFinished.exchange(true);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class Wtx2 implements Runnable{

    IWriteTransaction wtx;
    boolean t1ready = false;
    Exchanger threadsFinished;
    Exchanger verify;
    Wtx2(IWriteTransaction swtx, Exchanger<Boolean> threadsFinished, Exchanger<Boolean> verify){
        this.wtx = swtx;
        this.verify = verify;
        this.threadsFinished = threadsFinished;
    }
    @Override
    public void run(){
        wtx.moveToFirstChild();
        wtx.moveToFirstChild();
        wtx.moveToRightSibling();
        wtx.moveToRightSibling();
        wtx.moveToRightSibling();
        wtx.moveToFirstChild(); 
        wtx.moveToRightSibling(); //should be at 12:bar
        try {
            wtx.setValue("barfoo");
            wtx.commit();
            wtx.close();
        } catch (TreetankIOException e) {
            e.printStackTrace();
        } catch (TreetankException e) {
            e.printStackTrace();
        }

        while(!t1ready){        //waiting for t1 to finish
            try {
                t1ready = (Boolean)threadsFinished.exchange(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            verify.exchange(true);  //signaling that t2 has finished after t1 has finished
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}





