package com.treetank.io.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TreetankException;
import com.treetank.io.IOTestHelper;
import com.treetank.io.AbstractIOFactory.StorageType;

public class FileTest {
    private DatabaseConfiguration dbConf;
    private SessionConfiguration sessionConf;

    @Before
    public void setUp() throws TreetankException {
        dbConf = IOTestHelper.createDBConf(StorageType.File);
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
