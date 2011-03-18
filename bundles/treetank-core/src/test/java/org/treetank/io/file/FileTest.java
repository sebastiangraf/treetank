package org.treetank.io.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.SessionConfiguration;
import org.treetank.exception.AbsTTException;
import org.treetank.io.IOTestHelper;
import org.treetank.io.AbsIOFactory.StorageType;

public class FileTest {
    private DatabaseConfiguration dbConf;
    private SessionConfiguration sessionConf;

    @Before
    public void setUp() throws AbsTTException {
        dbConf = IOTestHelper.createDBConf(StorageType.File);
        sessionConf = IOTestHelper.createSessionConf();
        IOTestHelper.clean();
    }

    @Test
    public void testFactory() throws AbsTTException {
        IOTestHelper.testFactory(dbConf, sessionConf);
    }

    @Test
    public void testFirstRef() throws AbsTTException {
        IOTestHelper.testReadWriteFirstRef(dbConf, sessionConf);
    }

    @After
    public void tearDown() throws AbsTTException {
        IOTestHelper.clean();
    }
}
