package org.treetank.encryption.database;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.LinkedList;
import java.util.SortedMap;

import org.junit.Test;
import org.treetank.EncryptionHelper;
import org.treetank.encryption.database.model.KeySelector;

public class KeySelectorDatabaseTest {

    private static final File SEL_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("selectordb").toString());

    @Test
    public void testFunctions() {
        
        final EncryptionHelper helper = new EncryptionHelper();
        helper.delete(SEL_STORE);

        final KeySelectorDatabase mKeySelectorDb = new KeySelectorDatabase(SEL_STORE);

        final KeySelector sel1 =
            new KeySelector("A", new LinkedList<Long>(),
                new LinkedList<Long>(), 0, 0);
        
        final KeySelector sel2 =
            new KeySelector("B", new LinkedList<Long>(),
                new LinkedList<Long>(), 0, 0);
        
        final KeySelector sel3 =
            new KeySelector("C", new LinkedList<Long>(),
                new LinkedList<Long>(), 0, 0);
        
        
        //put and get
        mKeySelectorDb.putEntry(sel1);
        mKeySelectorDb.putEntry(sel2);
        mKeySelectorDb.putEntry(sel3);

        assertEquals(sel1.getName(), mKeySelectorDb.getEntry(0).getName());
        assertEquals(sel2.getName(), mKeySelectorDb.getEntry(1).getName());
        assertEquals(sel3.getName(), mKeySelectorDb.getEntry(2).getName());
        
        //delete
        assertEquals(mKeySelectorDb.deleteEntry(0), true);    
        
        //count
        assertEquals(mKeySelectorDb.count(), 2);
        
        //get entries as sorted map
        final SortedMap<Long, KeySelector> map = mKeySelectorDb.getEntries();
        assertEquals(map.get(1L).getName(), mKeySelectorDb.getEntry(1).getName());
        assertEquals(map.get(2L).getName(), mKeySelectorDb.getEntry(2).getName());
        
        mKeySelectorDb.clearPersistent();

    }


}
