package org.treetank.encryption.database;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.SortedMap;

import org.junit.Test;
import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.database.model.KeyManager;
import org.treetank.exception.TTEncryptionException;

public class KeyManagerDatabaseTest {

    private static final File MAN_STORE = new File(new StringBuilder(File.separator).append("tmp").append(
        File.separator).append("tnk").append(File.separator).append("keymanagerdb").toString());

    @Test
    public void testFunctions() throws TTEncryptionException {

        EncryptionController.getInstance().clear();

        final KeyManagerDatabase mKeyManagerDb = new KeyManagerDatabase(MAN_STORE);

        final KeyManager man1 = new KeyManager("User1", new HashSet<Long>());
        final KeyManager man2 = new KeyManager("User2", new HashSet<Long>());
        final KeyManager man3 = new KeyManager("User3", new HashSet<Long>());

        // put and get
        mKeyManagerDb.putEntry(man1);
        mKeyManagerDb.putEntry(man2);
        mKeyManagerDb.putEntry(man3);

        assertEquals(man1.getUser(), mKeyManagerDb.getEntry("User1").getUser());
        assertEquals(man2.getUser(), mKeyManagerDb.getEntry("User2").getUser());
        assertEquals(man3.getUser(), mKeyManagerDb.getEntry("User3").getUser());

        // delete
        assertEquals(mKeyManagerDb.deleteEntry("User1"), true);

        // count
        assertEquals(mKeyManagerDb.count(), 2);

        // get entries as sorted map
        final SortedMap<String, KeyManager> map = mKeyManagerDb.getEntries();
        assertEquals(map.get("User2").getUser(), mKeyManagerDb.getEntry("User2").getUser());
        assertEquals(map.get("User3").getUser(), mKeyManagerDb.getEntry("User3").getUser());

        mKeyManagerDb.clearPersistent();

    }

}
