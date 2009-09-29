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
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;

public class BerkeleyReader implements IReader {

    private final Database mDatabase;

    private final Transaction mTxn;

    public BerkeleyReader(final Environment env, final Database database,
            final Transaction txn) {
        mTxn = txn;
        mDatabase = database;

    }

    public BerkeleyReader(final Environment env, final Database database)
            throws DatabaseException {
        this(env, database, env.beginTransaction(null, null));
    }

    @Override
    public StorageProperties getProps() {
        try {

            final DatabaseEntry keyEntry = new DatabaseEntry();
            BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getPropsKey(),
                    keyEntry);

            final DatabaseEntry valueEntry = new DatabaseEntry();
            mDatabase.get(mTxn, keyEntry, valueEntry, LockMode.DEFAULT);
            final StorageProperties props = BerkeleyFactory.PROPS_VAL_B
                    .entryToObject(valueEntry);

            return props;

        } catch (final DatabaseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public AbstractPage read(PageReference<? extends AbstractPage> pageReference) {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        BerkeleyFactory.KEY.objectToEntry(pageReference.getKey(), keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry,
                    valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {
                final AbstractPage page = BerkeleyFactory.PAGE_VAL_B
                        .entryToObject(valueEntry);

                return page;
            } else {
                return null;
            }
        } catch (final DatabaseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PageReference<?> readFirstReference() {
        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();
        BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(),
                keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry,
                    valueEntry, LockMode.DEFAULT);
            if (status == OperationStatus.SUCCESS) {

                final PageReference<?> ref = BerkeleyFactory.FIRST_REV_VAL_B
                        .entryToObject(valueEntry);
                return ref;
            } else {
                return null;
            }
        } catch (final DatabaseException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        try {
            mTxn.abort();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);

        }
    }

}
