package org.treetank.encryption.utils;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import org.treetank.EncryptionHelper;
import org.treetank.encryption.cache.KeyCache;
import org.treetank.encryption.database.KeyManagerDatabase;
import org.treetank.encryption.database.KeySelectorDatabase;
import org.treetank.encryption.database.model.KeySelector;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EncryptionDAGParserTest {

    private static final String FILENAME = "src" + File.separator + "test"
        + File.separator + "resources" + File.separator + "treeTest.xml";

    private static final File SEL_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("selectordb").toString());

    private static final File MAN_STORE = new File(new StringBuilder(
        File.separator).append("tmp").append(File.separator).append("tnk")
        .append(File.separator).append("keymanagerdb").toString());

    private final String mLoggedUser = "User1";

    @Ignore
    @Test
    public void testDAG() {
        
        final EncryptionHelper helper = new EncryptionHelper();
        helper.delete(SEL_STORE);
        helper.delete(MAN_STORE);
        
        //preparation of tree structure for comparison
        
        final LinkedList<KeySelector> selectors = new LinkedList<KeySelector>();
        
        final LinkedList<Long> parents1 = new LinkedList<Long>();
        final LinkedList<Long> childs1 = new LinkedList<Long>();
        childs1.add(5L);
        childs1.add(7L);
        final KeySelector sel1 = new KeySelector("A", parents1, childs1, 0, 0);
        selectors.add(sel1);
        
        final LinkedList<Long> parents2 = new LinkedList<Long>();
        parents2.add(4L);
        final LinkedList<Long> childs2 = new LinkedList<Long>();
        childs2.add(6L);
        final KeySelector sel2 = new KeySelector("B", parents2, childs2, 0, 0);
        selectors.add(sel2);
        
        final LinkedList<Long> parents3 = new LinkedList<Long>();
        parents3.add(5L);
        parents3.add(7L);
        final LinkedList<Long> childs3 = new LinkedList<Long>();
        final KeySelector sel3 = new KeySelector("C", parents3, childs3, 0, 0);
        selectors.add(sel3);
        
        final LinkedList<Long> parents4 = new LinkedList<Long>();
        parents4.add(4L);
        final LinkedList<Long> childs4 = new LinkedList<Long>();
        childs4.add(6L);
        final KeySelector sel4 = new KeySelector("D", parents4, childs4, 0, 0);
        selectors.add(sel4);

        
        
        //initializes databases and execute dag parser
        
        final KeySelectorDatabase mKeySelectorDb =
            new KeySelectorDatabase(SEL_STORE);
        final KeyManagerDatabase mKeyManagerDb =
            new KeyManagerDatabase(MAN_STORE);
        final KeyCache mKeyCache = new KeyCache();
        new EncryptionDAGParser().init(FILENAME, mKeySelectorDb, mKeyManagerDb,
            mKeyCache, mLoggedUser);

        //test

        final SortedMap<Long, KeySelector> mSelMap =
            mKeySelectorDb.getEntries();
        final Iterator iter = mSelMap.keySet().iterator();
        int i = 0;

        while (iter.hasNext()) {

            final KeySelector mSelector = mSelMap.get(iter.next());
            final LinkedList<Long> mParentsList = mSelector.getParents();
            final List<Long> mChildsList = mSelector.getChilds();
            
            final KeySelector testSelector = selectors.get(i);
            final LinkedList<Long> testParentsList = testSelector.getParents();
            final List<Long> testChildsList = testSelector.getChilds();

            assertEquals(mSelector.getName(), testSelector.getName());
            
            assertEquals(mParentsList.size(), testParentsList.size());
            for(int j = 0; j < mParentsList.size(); j++){
                assertEquals(mParentsList.get(j), testParentsList.get(j));
            }
            
            assertEquals(mChildsList.size(), testChildsList.size());
            for(int k = 0; k < mChildsList.size(); k++){
                assertEquals(mChildsList.get(k), testChildsList.get(k));
            }

            i++;
        }

        mKeySelectorDb.clearPersistent();
        mKeyManagerDb.clearPersistent();
    
    }
    
   

}
