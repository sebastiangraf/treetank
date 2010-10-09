package com.treetank.access;

import javax.xml.namespace.QName;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HashTest {

    private final static String NAME="a";
    
    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @Test
    public void adaptHashWithInser() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        
        wtx.insertElementAsFirstChild(new QName(NAME));
        final long hash11Element = wtx.getNode().getHash();
        wtx.insertElementAsFirstChild(new QName(NAME));
        final long hash12Element = wtx.getNode().getHash();
        wtx.moveToParent();
        final long hash21Element = wtx.getNode().getHash();
        wtx.moveToFirstChild();
        wtx.remove();
        final long hash13Element = wtx.getNode().getHash();
        System.out.println();
        
        
    }
    
    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }


}
