package org.treetank.encryption;

import org.junit.Before;
import org.junit.Test;
import org.treetank.exception.TTEncryptionException;

public class EncryptionFactoryTest {

    IEncryption factory;

    @Before
    public void setUp() throws Exception {
        factory = new EncryptionFactory().getController();
    }

    @Test
    public void testFactory() throws TTEncryptionException {
    }

}
