package com.treetank.io.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.io.IOTestHelper;
import com.treetank.io.AbstractIOFactory.StorageType;
import com.treetank.session.SessionConfiguration;

public class FileTest {

    private SessionConfiguration conf;

    @Before
    public void setUp() {
        conf = IOTestHelper.createConf(StorageType.File);
        IOTestHelper.clean();
    }

    @Test
    public void testFactory() {
        IOTestHelper.testFactory(conf);
    }

    @Test
    public void testProps() {
        IOTestHelper.testPropsReadWrite(conf);
    }

    @Test
    public void testFirstRef() {
        IOTestHelper.testReadWriteFirstRef(conf);
    }

    @After
    public void tearDown() {
        IOTestHelper.clean();
    }

}
