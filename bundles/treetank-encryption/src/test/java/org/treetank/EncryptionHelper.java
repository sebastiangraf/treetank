package org.treetank;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.encrpytion.exception.TTEncryptionException;
import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.KeyManagerHandler;
import org.treetank.encryption.database.model.KeyManager;
import org.treetank.encryption.database.model.KeySelector;

/**
 * This class is providing some helping stuff for tests on encryption classes.
 */
public class EncryptionHelper {

    private static KeyManagerHandler mManager;

    private static EncryptionController mController;

    private final static Logger LOGGER = LoggerFactory
        .getLogger(EncryptionHelper.class);

    public EncryptionHelper() {
        mController = EncryptionController.getInstance();
    }

    public void start() throws TTEncryptionException {

        mController.clear();
        mController.init();
        mManager = mController.getKMHInstance();

    }

    public void close() {
        print();
        mController.close();
    }

    public void setSessionUser(final String user) {
        mController.setUser(user);
    }

    public void setEncryption(final boolean bol) {
        mController.setEncryptionOption(bol);
    }

    public boolean delete(final File mFile) {

        if (mFile.isDirectory()) {
            for (final File child : mFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }

        return true;
    }

    protected static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    public KeyManagerHandler getManager() {
        return mManager;
    }

    public EncryptionController getController() {
        return mController;
    }

    /**
     * Prints all stored information of KeySelector and KeyManager database. This method is just for testing
     * issues.
     */
    public void print() {

        /*
         * print key selector db.
         */
        final SortedMap<Long, KeySelector> mSelMap =
            mController.getKeySelectorInstance().getEntries();
        Iterator<Long> iter = mSelMap.keySet().iterator();

        LOGGER.info("\nSelector DB Size: "
            + mController.getKeySelectorInstance().count());

        while (iter.hasNext()) {

            final KeySelector mSelector = mSelMap.get(iter.next());
            final LinkedList<Long> mParentsList = mSelector.getParents();
            final List<Long> mChildsList = mSelector.getChilds();

            final StringBuilder mParentsString = new StringBuilder();
            for (int k = 0; k < mParentsList.size(); k++) {
                mParentsString.append("#" + mParentsList.get(k));
            }

            final StringBuilder mChildsString = new StringBuilder();
            for (int k = 0; k < mChildsList.size(); k++) {
                mChildsString.append("#" + mChildsList.get(k));
            }

            LOGGER
                .info("Selector: " + mSelector.getPrimaryKey() + " "
                    + mSelector.getName() + " " + mParentsString.toString()
                    + " " + mChildsString.toString() + " "
                    + mSelector.getRevision() + " " + mSelector.getVersion()
                    + " " + mSelector.getSecretKey());

        }
        LOGGER.info(" ");

        /*
         * print key manager db
         */
        final SortedMap<String, KeyManager> sMap =
            mController.getKeyManagerInstance().getEntries();

        // iterate through all users
        final Iterator<String> outerIter = sMap.keySet().iterator();

        LOGGER.info("Key manager DB Size: "
            + mController.getKeyManagerInstance().count());

        StringBuilder sb;
        while (outerIter.hasNext()) {
            final String user = (String)outerIter.next();
            sb = new StringBuilder(user + ": ");

            final Set<Long> mKeySet =
                mController.getKeyManagerInstance().getEntry(user).getKeySet();

            // iterate through user's key set.
            final Iterator<Long> innerIter = mKeySet.iterator();
            while (innerIter.hasNext()) {
                sb.append(innerIter.next() + " ");
            }

            LOGGER.info(sb.toString());

        }
        LOGGER.info(" ");

        /*
         * print key cache.
         */
        final LinkedList<Long> mKeyList =
            mController.getKeyCacheInstance().get(mController.getUser());
        final StringBuilder cacheString =
            new StringBuilder("Key Cache of " + mController.getUser() + ": ");
        for (long aKey : mKeyList) {
            cacheString.append(aKey + " ");
        }
        LOGGER.info(cacheString.toString());

    }

}
