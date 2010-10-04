package com.treetank.io.berkeley;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TreetankException;
import com.treetank.io.IOTestHelper;
import com.treetank.io.AbsIOFactory.StorageType;

public class BerkeleyTest {

    private DatabaseConfiguration dbConf;
    private SessionConfiguration sessionConf;

    @Before
    public void setUp() throws TreetankException {
        dbConf = IOTestHelper.createDBConf(StorageType.Berkeley);
        sessionConf = IOTestHelper.createSessionConf();
        IOTestHelper.clean();
    }

    @Test
    public void testFactory() throws TreetankException {
        IOTestHelper.testFactory(dbConf, sessionConf);
    }

    @Test
    public void testFirstRef() throws TreetankException {
        IOTestHelper.testReadWriteFirstRef(dbConf, sessionConf);
    }

    @After
    public void tearDown() throws TreetankException {
        IOTestHelper.clean();
    }

}
