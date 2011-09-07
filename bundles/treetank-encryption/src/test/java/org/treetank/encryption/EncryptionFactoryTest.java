package org.treetank.encryption;

import org.treetank.encrpytion.exception.TTEncryptionException;

import org.junit.Before;
import org.junit.Test;

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
