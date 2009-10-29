package com.treetank.io.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.IReader;
import com.treetank.io.StorageProperties;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IReader}-interface.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BerkeleyReader implements IReader {

    /** Link to the {@link Database} */
    private transient final Database mDatabase;

    /** Link to the {@link Transaction} */
    private transient final Transaction mTxn;

    /**
     * Constructor.
     * 
     * @param database
     *            to be connected to
     * @param txn
     *            transaction to be used
     */
    public BerkeleyReader(final Database database, final Transaction txn) {
        mTxn = txn;
        mDatabase = database;

    }

    /**
     * Constructor
     * 
     * @param env
     *            to be used
     * @param database
     *            to be connected to
     * @throws DatabaseException
     *             if something weird happens
     */
    public BerkeleyReader(final Environment env, final Database database)
            throws DatabaseException {
        this(database, env.beginTransaction(null, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StorageProperties getProps() throws TreetankIOException {
        try {

            final DatabaseEntry keyEntry = new DatabaseEntry();
            BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getPropsKey(),
                    keyEntry);

            final DatabaseEntry valueEntry = new DatabaseEntry();
            mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            final StorageProperties props = BerkeleyFactory.PROPS_VAL_B
                    .entryToObject(valueEntry);

            return props;

        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPage read(final PageReference pageReference)
            throws TreetankIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        BerkeleyFactory.KEY.objectToEntry(pageReference.getKey(), keyEntry);

        AbstractPage page = null;
        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry,
                    valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                page = BerkeleyFactory.PAGE_VAL_B.entryToObject(valueEntry);

            }
            return page;
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageReference readFirstReference() throws TreetankIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(),
                keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry,
                    valueEntry, LockMode.DEFAULT);
            PageReference uberPageReference = null;
            if (status == OperationStatus.SUCCESS) {

                uberPageReference = BerkeleyFactory.FIRST_REV_VAL_B
                        .entryToObject(valueEntry);
            }
            final UberPage page = (UberPage) read(uberPageReference);
            uberPageReference.setPage(page);

            return uberPageReference;
        } catch (final DatabaseException e) {
            throw new TreetankIOException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TreetankIOException {
        try {
            mTxn.abort();
        } catch (final DatabaseException e) {
            throw new TreetankIOException(e);

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mDatabase == null) ? 0 : mDatabase.hashCode());
        result = prime * result + ((mTxn == null) ? 0 : mTxn.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        boolean returnVal = true;
        if (obj == null) {
            returnVal = false;
        }
        if (getClass() != obj.getClass()) {
            returnVal = false;
        }
        final BerkeleyReader other = (BerkeleyReader) obj;
        if (mDatabase == null) {
            if (other.mDatabase != null) {
                returnVal = false;
            }
        } else if (!mDatabase.equals(other.mDatabase)) {
            returnVal = false;
        }
        if (mTxn == null) {
            if (other.mTxn != null) {
                returnVal = false;
            }
        } else if (!mTxn.equals(other.mTxn)) {
            returnVal = false;
        }
        return returnVal;
    }

}
