/**
 * 
 */
package org.treetank.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.treetank.cache.ICachedLog.TransactionLogEntry;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Iterator for the transaction log. Respecting the dual architecture consisting of LRUCache and underlaying
 * BDB-Cache.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class CacheLogIterator implements Iterator<Map.Entry<LogKey, NodePageContainer>>,
    Iterable<Map.Entry<LogKey, NodePageContainer>> {

    private int i = 0;

    private final BerkeleyPersistenceLog mSecondLog;

    private List<Map.Entry<LogKey, NodePageContainer>> mEntries;

    private Cursor mCursor;
    private DatabaseEntry valueEntry;
    private DatabaseEntry keyEntry;

    public CacheLogIterator(final LRUCache pFirstLog, final BerkeleyPersistenceLog pSecondLog) {
        mSecondLog = pSecondLog;
        mEntries = new ArrayList<Map.Entry<LogKey, NodePageContainer>>();
        if (pFirstLog != null) {
            mEntries.addAll(pFirstLog.map.entrySet());
        }
        mCursor = mSecondLog.mDatabase.openCursor(null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        boolean returnVal = true;
        if (i < LRUCache.CACHE_CAPACITY) {
            if (mEntries.size() - i <= 0) {
                returnVal = false;
            }
        } else {
            valueEntry = new DatabaseEntry();
            keyEntry = new DatabaseEntry();
            try {
                final OperationStatus status = mCursor.getNext(keyEntry, valueEntry, LockMode.DEFAULT);
                if (status == OperationStatus.SUCCESS) {
                    returnVal = true;
                } else {
                    returnVal = false;
                }
            } catch (final DatabaseException exc) {
                throw new RuntimeException(exc);
            }

        }
        if (returnVal == false) {
            mCursor.close();
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map.Entry<LogKey, NodePageContainer> next() {
        if (i <LRUCache.CACHE_CAPACITY) {
            Map.Entry<LogKey, NodePageContainer> returnVal = mEntries.get(i);
            i++;
            return returnVal;
        } else {
            return new TransactionLogEntry(mSecondLog.mKeyBinding.entryToObject(keyEntry),
                mSecondLog.mValueBinding.entryToObject(valueEntry));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Map.Entry<LogKey, NodePageContainer>> iterator() {
        return this;
    }

}
