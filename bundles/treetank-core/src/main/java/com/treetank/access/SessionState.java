/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.access;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.treetank.api.IItemList;
import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.cache.NodePageContainer;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankThreadedException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.io.AbstractIOFactory;
import com.treetank.io.IReader;
import com.treetank.io.IWriter;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;
import com.treetank.settings.ESessionSetting;

/**
 * <h1>SessionState</h1>
 * 
 * <p>
 * State of each session.
 * </p>
 */
public final class SessionState {

    /** Lock for blocking the commit */
    protected final Lock mCommitLock;

    /** Session configuration. */
    protected final SessionConfiguration mSessionConfiguration;

    /** Database configuration. */
    private final DatabaseConfiguration mDatabaseConfiguration;

    /** Write semaphore to assure only one exclusive write transaction exists. */
    private final Semaphore mWriteSemaphore;

    /** Read semaphore to control running read transactions. */
    private final Semaphore mReadSemaphore;

    /** Strong reference to uber page before the begin of a write transaction. */
    private UberPage mLastCommittedUberPage;

    /** Remember all running transactions (both read and write). */
    private final Map<Long, IReadTransaction> mTransactionMap;

    /** Remember the write seperatly because of the concurrent writes */
    private final Map<Long, WriteTransactionState> mWriteTransactionStateMap;

    /** Storing all return futures from the sync process */
    private final Map<Long, Map<Long, Collection<Future<Void>>>> mSyncTransactionsReturns;

    /** abstract factory for all interaction to the storage */
    private final AbstractIOFactory fac;

    /** Atomic counter for concurrent generation of transaction id */
    private final AtomicLong transactionIDCounter;

    /**
     * Constructor to bind to a TreeTank file.
     * 
     * @param sessionConfiguration
     *            Session configuration for the TreeTank.
     */
    protected SessionState(final DatabaseConfiguration databaseConfiguration,
            final SessionConfiguration sessionConfiguration)
            throws TreetankException {
        mDatabaseConfiguration = databaseConfiguration;
        mSessionConfiguration = sessionConfiguration;
        mTransactionMap = new ConcurrentHashMap<Long, IReadTransaction>();
        mWriteTransactionStateMap = new ConcurrentHashMap<Long, WriteTransactionState>();
        mSyncTransactionsReturns = new ConcurrentHashMap<Long, Map<Long, Collection<Future<Void>>>>();

        transactionIDCounter = new AtomicLong();
        mCommitLock = new ReentrantLock(true);

        // Init session members.
        mWriteSemaphore = new Semaphore(Integer.parseInt(sessionConfiguration
                .getProps().getProperty(
                        ESessionSetting.MAX_WRITE_TRANSACTIONS.name())));
        mReadSemaphore = new Semaphore(Integer.parseInt(sessionConfiguration
                .getProps().getProperty(
                        ESessionSetting.MAX_READ_TRANSACTIONS.name())));
        final PageReference uberPageReference = new PageReference();

        fac = AbstractIOFactory.getInstance(mDatabaseConfiguration,
                mSessionConfiguration);
        if (!fac.exists()) {
            // Bootstrap uber page and make sure there already is a root
            // node.
            mLastCommittedUberPage = new UberPage();
            uberPageReference.setPage(mLastCommittedUberPage);

        } else {
            final IReader reader = fac.getReader();
            final PageReference firstRef = reader.readFirstReference();
            mLastCommittedUberPage = (UberPage) firstRef.getPage();
            reader.close();
        }

    }

    protected int getReadTransactionCount() {
        return Integer.parseInt(mSessionConfiguration.getProps().getProperty(
                ESessionSetting.MAX_READ_TRANSACTIONS.name()))
                - mReadSemaphore.availablePermits();
    }

    protected int getWriteTransactionCount() {
        return Integer.parseInt(mSessionConfiguration.getProps().getProperty(
                ESessionSetting.MAX_WRITE_TRANSACTIONS.name()))
                - mWriteSemaphore.availablePermits();
    }

    protected IReadTransaction beginReadTransaction() throws TreetankException {
        return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(),
                null);
    }

    protected IReadTransaction beginReadTransaction(final IItemList itemList)
            throws TreetankException {
        return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(),
                itemList);
    }

    protected IReadTransaction beginReadTransaction(final long revisionNumber,
            final IItemList itemList) throws TreetankException {

        // Make sure not to exceed available number of read transactions.
        try {
            mReadSemaphore.acquire();
        } catch (final InterruptedException exc) {
            throw new TreetankException(exc) {
                private static final long serialVersionUID = 1L;
            };
        }

        IReadTransaction rtx = null;
        // Create new read transaction.
        rtx = new ReadTransaction(transactionIDCounter.incrementAndGet(), this,
                new ReadTransactionState(mDatabaseConfiguration,
                        mLastCommittedUberPage, revisionNumber, itemList,
                        fac.getReader()));

        // Remember transaction for debugging and safe close.
        if (mTransactionMap.put(rtx.getTransactionID(), rtx) != null) {
            throw new TreetankUsageException(
                    "ID generation is bogus because of duplicate ID.");
        }
        return rtx;
    }

    protected IWriteTransaction beginWriteTransaction(final int maxNodeCount,
            final int maxTime) throws TreetankException {

        // Make sure not to exceed available number of write transactions.
        if (mWriteSemaphore.availablePermits() == 0) {
            throw new IllegalStateException(
                    "There already is a running exclusive write transaction.");
        }
        try {
            mWriteSemaphore.acquire();
        } catch (final InterruptedException exc) {
            throw new TreetankThreadedException(exc);

        }

        final long currentID = transactionIDCounter.incrementAndGet();
        final WriteTransactionState wtxState = createWriteTransactionState(
                currentID, mLastCommittedUberPage.getRevisionNumber(),
                mLastCommittedUberPage.getRevisionNumber());

        // Create new write transaction.
        final IWriteTransaction wtx = new WriteTransaction(currentID, this,
                wtxState, maxNodeCount, maxTime);

        // Remember transaction for debugging and safe close.
        if (mTransactionMap.put(currentID, wtx) != null
                || mWriteTransactionStateMap.put(currentID, wtxState) != null) {
            throw new TreetankThreadedException(
                    "ID generation is bogus because of duplicate ID.");
        }

        return wtx;
    }

    protected WriteTransactionState createWriteTransactionState(final long id,
            final long representRevision, final long storeRevision)
            throws TreetankIOException {
        final IWriter writer = fac.getWriter();

        return new WriteTransactionState(mDatabaseConfiguration, this,
                new UberPage(mLastCommittedUberPage, storeRevision + 1),
                writer, id, representRevision, storeRevision);
    }

    protected final synchronized void syncLogs(
            final NodePageContainer contToSync, final long transactionId)
            throws TreetankThreadedException {
        final ExecutorService exec = Executors.newCachedThreadPool();
        final Collection<Future<Void>> returnVals = new ArrayList<Future<Void>>();
        for (final Long key : mWriteTransactionStateMap.keySet()) {
            if (key != transactionId) {
                returnVals.add(exec.submit(new LogSyncer(
                        mWriteTransactionStateMap.get(key), contToSync)));
            }
        }

        if (!mSyncTransactionsReturns.containsKey(transactionId)) {
            mSyncTransactionsReturns.put(transactionId,
                    new ConcurrentHashMap<Long, Collection<Future<Void>>>());
        }

        if (mSyncTransactionsReturns.get(transactionId).put(
                contToSync.getComplete().getNodePageKey(), returnVals) != null) {
            throw new TreetankThreadedException(
                    "only one commit and therefore sync per id and nodepage is allowed!");
        }

    }

    protected final synchronized void waitForFinishedSync(
            final long transactionKey) throws TreetankThreadedException {
        final Map<Long, Collection<Future<Void>>> completeVals = mSyncTransactionsReturns
                .remove(transactionKey);
        if (completeVals != null) {
            for (final Collection<Future<Void>> singleVals : completeVals
                    .values()) {
                for (final Future<Void> returnVal : singleVals) {
                    try {
                        returnVal.get();
                    } catch (final InterruptedException exc) {
                        throw new TreetankThreadedException(exc);
                    } catch (final ExecutionException exc) {
                        throw new TreetankThreadedException(exc);
                    }
                }
            }
        }
    }

    protected void setLastCommittedUberPage(final UberPage lastCommittedUberPage) {
        mLastCommittedUberPage = lastCommittedUberPage;
    }

    protected void closeWriteTransaction(final long transactionID) {
        // Purge transaction from internal state.
        mTransactionMap.remove(transactionID);
        // Removing the write from the own internal mapping
        mWriteTransactionStateMap.remove(transactionID);
        // Make new transactions available.
        mWriteSemaphore.release();
    }

    protected void closeReadTransaction(final long transactionID) {
        // Purge transaction from internal state.
        mTransactionMap.remove(transactionID);
        // Make new transactions available.
        mReadSemaphore.release();
    }

    protected void close() throws TreetankException {
        // Forcibly close all open transactions.
        for (final IReadTransaction rtx : mTransactionMap.values()) {
            if (rtx instanceof IWriteTransaction) {
                ((IWriteTransaction) rtx).abort();
            }
            rtx.close();
        }

        // Immediately release all ressources.
        mLastCommittedUberPage = null;
        mTransactionMap.clear();
        mWriteTransactionStateMap.clear();

        fac.closeStorage();
    }

    class LogSyncer implements Callable<Void> {

        final WriteTransactionState state;
        final NodePageContainer cont;

        LogSyncer(final WriteTransactionState paramState,
                final NodePageContainer paramCont) {
            state = paramState;
            cont = paramCont;
        }

        @Override
        public Void call() throws Exception {
            state.updateDateContainer(cont);
            return null;
        }

    }

    protected void assertValidRevision(final long rev)
            throws TreetankUsageException {
        if (rev < 0) {
            throw new TreetankUsageException("Revision must be at least 0");
        } else if (rev > mLastCommittedUberPage.getRevision()) {
            throw new TreetankUsageException(
                    "Revision must not be bigger than",
                    Long.toString(mLastCommittedUberPage.getRevision()));
        }
    }

}
