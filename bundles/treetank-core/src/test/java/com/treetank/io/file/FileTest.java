package com.treetank.io.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.AbsTTException;
import com.treetank.io.IOTestHelper;
import com.treetank.io.AbsIOFactory.StorageType;

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
