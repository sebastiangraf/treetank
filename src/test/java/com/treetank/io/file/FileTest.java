package com.treetank.io.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.access.SessionConfiguration;
import com.treetank.exception.TreetankException;
import com.treetank.io.IOTestHelper;
import com.treetank.io.AbstractIOFactory.StorageType;

public class FileTest {

    private SessionConfiguration conf;

    @Before
    public void setUp() throws TreetankException {
        conf = IOTestHelper.createConf(StorageType.File);
        IOTestHelper.clean();
    }

    @Test
    public void testFactory() throws TreetankException {
        IOTestHelper.testFactory(conf);
    }

    @Test
    public void testProps() throws TreetankException {
        IOTestHelper.testPropsReadWrite(conf);
    }

    @Test
    public void testFirstRef() throws TreetankException {
        IOTestHelper.testReadWriteFirstRef(conf);
    }

    @After
    public void tearDown() throws TreetankException {
        IOTestHelper.clean();
    }

}
