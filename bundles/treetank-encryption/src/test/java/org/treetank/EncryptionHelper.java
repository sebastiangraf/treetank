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


    public EncryptionHelper() {
        mController = EncryptionController.getInstance();
    }

    public void start() throws TTEncryptionException {
        mController.clear();
        mController.init();
        mManager = mController.getKMHInstance();

    }

    public void close() {
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

}
