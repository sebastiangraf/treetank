package com.treetank.access;

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

    @Before
    public void setUp() {
        TestHelper.deleteEverything();
    }

    @Test
    public void adaptHashWithInser() throws TreetankException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();
        
        
        
        
    }
    
    @After
    public void tearDown() {
        TestHelper.closeEverything();
    }


}
