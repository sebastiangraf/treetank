package com.treetank.io.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.treetank.io.IReader;
import com.treetank.io.StorageProperties;
import com.treetank.io.TreetankIOException;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;

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
    public AbstractPage read(
            final PageReference<? extends AbstractPage> pageReference)
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
    public PageReference<?> readFirstReference() throws TreetankIOException {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(),
                keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry,
                    valueEntry, LockMode.DEFAULT);
            PageReference<?> ref = null;
            if (status == OperationStatus.SUCCESS) {

                ref = BerkeleyFactory.FIRST_REV_VAL_B.entryToObject(valueEntry);
            }
            return ref;
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

}
