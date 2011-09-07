package org.treetank.encryption;

import org.treetank.EncryptionHelper;
import org.treetank.encrpytion.exception.TTEncryptionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KeyManagerHandlerTest {

    private static EncryptionHelper enHelper;

    @Before
    public void setUp() throws Exception {
        enHelper = new EncryptionHelper();
        enHelper.setEncryption(true);
        enHelper.start();
    }

    @After
    public void tearDown() {
        enHelper.setEncryption(false);
        enHelper.close();
    }

    @Test
    public void testJoinAndLeave() throws TTEncryptionException {
        enHelper.getManager().join(new String[] {
            "User1"
        }, new String[] {
            "Inf", "Disy"
        }, "ALL");
        enHelper.getManager().join(new String[] {
            "User2"
        }, new String[] {
            "Inf", "Disy"
        }, "ALL");
        enHelper.getManager().leave(new String[] {
            "User2"
        }, new String[] {
            "Disy"
        });
        enHelper.getManager().join(new String[] {
            "User3"
        }, new String[] {
            "TT"
        }, "Disy");
        enHelper.getManager().join(new String[] {
            "User4"
        }, new String[] {
            "Inf", "Dbis"
        }, "ALL");

        enHelper.setSessionUser("User1");
        enHelper.getManager().join(new String[] {
            "User5"
        }, new String[] {
            "Disy"
        }, "ALL");

        enHelper.getManager().leave(new String[] {}, new String[] {
            "Disy"
        });

        // after all joins and leaves and join/leave updates database size must be 31.
        assertEquals(enHelper.getController().getKeySelectorInstance().count(),
            31);

    }
}
