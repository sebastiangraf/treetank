package com.treetank.io.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.treetank.exception.TreetankIOException;
import com.treetank.io.IWriter;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;

/**
 * This class represents an reading instance of the Treetank-Application
 * implementing the {@link IWriter}-interface. It inherits and overrides some
 * reader methods because of the transaction layer.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BerkeleyWriter implements IWriter {

    /** Current {@link Database} to write to. */
    private transient final Database mDatabase;

    /** Current {@link Transaction} to write with. */
    private transient Transaction mTxn;

    /** Current {@link BerkeleyReader} to read with. */
    private transient final BerkeleyReader reader;

    private long nodepagekey;

    /**
     * Simple constructor starting with an {@link Environment} and a
     * {@link Database}.
     * 
     * @param env
     *            for the write
     * @param database
     *            where the data should be written to
     * @throws TreetankIOException
     *             if something off happens
     */
    public BerkeleyWriter(final Environment env, final Database database)
            throws TreetankIOException {

        try {
            mTxn = env.beginTransaction(null, null);
            mDatabase = database;
            nodepagekey = getLastNodePage();
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }

        reader = new BerkeleyReader(database, mTxn);
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws TreetankIOException {
        try {
            setLastNodePage(nodepagekey);
            mTxn.commit();
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(final PageReference pageReference)
            throws TreetankIOException {
        final AbstractPage page = pageReference.getPage();

        final DatabaseEntry valueEntry = new DatabaseEntry();
        final DatabaseEntry keyEntry = new DatabaseEntry();

        // TODO make this better
        nodepagekey++;
        final BerkeleyKey key = new BerkeleyKey(nodepagekey);

        BerkeleyFactory.PAGE_VAL_B.objectToEntry(page, valueEntry);
        BerkeleyFactory.KEY.objectToEntry(key, keyEntry);

        try {
            final OperationStatus status = mDatabase.put(mTxn, keyEntry,
                    valueEntry);
            if (status != OperationStatus.SUCCESS) {
                throw new DatabaseException(new StringBuilder("Write of ")
                        .append(pageReference.toString()).append(" failed!")
                        .toString());
            }
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
        pageReference.setKey(key);

    }

    /**
     * Setting the last nodePage to the persistent storage.
     * 
     * @param data
     *            key to be stored
     */
    private final void setLastNodePage(final Long data)
            throws TreetankIOException {
        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry valueEntry = new DatabaseEntry();

        final BerkeleyKey key = BerkeleyKey.getDataInfoKey();
        BerkeleyFactory.KEY.objectToEntry(key, keyEntry);
        BerkeleyFactory.DATAINFO_VAL_B.objectToEntry(data, valueEntry);
        try {
            mDatabase.put(mTxn, keyEntry, valueEntry);

        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }
    }

    /**
     * Getting the last nodePage from the persistent storage.
     * 
     * @return the last nodepage-key
     */
    private final long getLastNodePage() throws TreetankIOException {
        final DatabaseEntry keyEntry = new DatabaseEntry();
        final DatabaseEntry valueEntry = new DatabaseEntry();

        final BerkeleyKey key = BerkeleyKey.getDataInfoKey();
        BerkeleyFactory.KEY.objectToEntry(key, keyEntry);

        try {
            final OperationStatus status = mDatabase.get(mTxn, keyEntry,
                    valueEntry, LockMode.DEFAULT);
            Long val;
            if (status == OperationStatus.SUCCESS) {
                val = BerkeleyFactory.DATAINFO_VAL_B.entryToObject(valueEntry);
            } else {
                val = 0l;
            }
            return val;
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void writeFirstReference(final PageReference pageReference)
            throws TreetankIOException {
        write(pageReference);

        final DatabaseEntry keyEntry = new DatabaseEntry();
        BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(),
                keyEntry);

        final DatabaseEntry valueEntry = new DatabaseEntry();
        BerkeleyFactory.FIRST_REV_VAL_B
                .objectToEntry(pageReference, valueEntry);

        try {
            mDatabase.put(mTxn, keyEntry, valueEntry);
        } catch (final DatabaseException exc) {
            throw new TreetankIOException(exc);
        }

    }

    /**
     * {@inheritDoc}
     */
    public AbstractPage read(final PageReference pageReference)
            throws TreetankIOException {
        return reader.read(pageReference);
    }

    /**
     * {@inheritDoc}
     */
    public PageReference readFirstReference() throws TreetankIOException {
        return reader.readFirstReference();
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
        result = prime * result + ((reader == null) ? 0 : reader.hashCode());
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
        final BerkeleyWriter other = (BerkeleyWriter) obj;
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
        if (reader == null) {
            if (other.reader != null) {
                returnVal = false;
            }
        } else if (!reader.equals(other.reader)) {
            returnVal = false;
        }
        return returnVal;
    }

}
