/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.access;

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

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.cache.NodePageContainer;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTThreadedException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.EStorage;
import org.treetank.io.IReader;
import org.treetank.io.IStorage;
import org.treetank.io.IWriter;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;
import org.treetank.utils.ItemList;

/**
 * <h1>Session</h1>
 * 
 * <p>
 * Makes sure that there only is a single session instance bound to a TreeTank file.
 * </p>
 */
public final class Session implements ISession {

    /** Session configuration. */
    protected final ResourceConfiguration mResourceConfig;

    /** Session configuration. */
    protected final SessionConfiguration mSessionConfig;

    /** Database for centralized closure of related Sessions. */
    private final Database mDatabase;

    /** Write semaphore to assure only one exclusive write transaction exists. */
    private final Semaphore mWriteSemaphore;

    /** Read semaphore to control running read transactions. */
    private final Semaphore mReadSemaphore;

    /** Strong reference to uber page before the begin of a write transaction. */
    private UberPage mLastCommittedUberPage;

    /** Remember all running transactions (both read and write). */
    private final Map<Long, IReadTransaction> mTransactionMap;

    /** Lock for blocking the commit. */
    protected final Lock mCommitLock;

    /** Remember the write seperatly because of the concurrent writes. */
    private final Map<Long, WriteTransactionState> mWriteTransactionStateMap;

    /** Storing all return futures from the sync process. */
    private final Map<Long, Map<Long, Collection<Future<Void>>>> mSyncTransactionsReturns;

    /** abstract factory for all interaction to the storage. */
    private final IStorage mFac;

    /** Atomic counter for concurrent generation of transaction id. */
    private final AtomicLong mTransactionIDCounter;

    /** Determines if session was closed. */
    private transient boolean mClosed;

    /**
     * Hidden constructor.
     * 
     * @param paramDatabase
     *            Database for centralized operations on related sessions.
     * @param paramDatabaseConf
     *            DatabaseConfiguration for general setting about the storage
     * @param paramSessionConf
     *            SessionConfiguration for handling this specific session
     * @throws AbsTTException
     *             Exception if something weird happens
     */
    protected Session(final Database paramDatabase, final ResourceConfiguration paramResourceConf,
        final SessionConfiguration paramSessionConf) throws AbsTTException {
        mDatabase = paramDatabase;
        mResourceConfig = paramResourceConf;
        mSessionConfig = paramSessionConf;
        mTransactionMap = new ConcurrentHashMap<Long, IReadTransaction>();
        mWriteTransactionStateMap = new ConcurrentHashMap<Long, WriteTransactionState>();
        mSyncTransactionsReturns = new ConcurrentHashMap<Long, Map<Long, Collection<Future<Void>>>>();

        mTransactionIDCounter = new AtomicLong();
        mCommitLock = new ReentrantLock(false);

        // Init session members.
        mWriteSemaphore = new Semaphore(paramSessionConf.mWtxAllowed);
        mReadSemaphore = new Semaphore(paramSessionConf.mRtxAllowed);

        mFac = EStorage.getStorage(mResourceConfig);
        if (!mFac.exists()) {
            // Bootstrap uber page and make sure there already is a root
            // node.
            mLastCommittedUberPage = new UberPage();
        } else {
            final IReader reader = mFac.getReader();
            final PageReference firstRef = reader.readFirstReference();
            mLastCommittedUberPage = (UberPage)firstRef.getPage();
            reader.close();
        }
        mClosed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReadTransaction beginReadTransaction() throws AbsTTException {
        return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(), new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IReadTransaction beginReadTransaction(final long paramRevisionKey) throws AbsTTException {
        return beginReadTransaction(paramRevisionKey, new ItemList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IReadTransaction beginReadTransaction(final long paramRevisionKey,
        final ItemList mItemList) throws AbsTTException {
        assertAccess(paramRevisionKey);
        // Make sure not to exceed available number of read transactions.
        try {
            mReadSemaphore.acquire();
        } catch (final InterruptedException exc) {
            throw new TTThreadedException(exc);
        }

        IReadTransaction rtx = null;
        // Create new read transaction.
        rtx =
            new ReadTransaction(this, mTransactionIDCounter.incrementAndGet(), new ReadTransactionState(this,
                mLastCommittedUberPage, paramRevisionKey, mItemList, mFac.getReader()));

        // Remember transaction for debugging and safe close.
        if (mTransactionMap.put(rtx.getTransactionID(), rtx) != null) {
            throw new TTUsageException("ID generation is bogus because of duplicate ID.");
        }
        return rtx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IWriteTransaction beginWriteTransaction() throws AbsTTException {
        return beginWriteTransaction(0, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized IWriteTransaction beginWriteTransaction(final int paramMaxNodeCount,
        final int paramMaxTime) throws AbsTTException {
        assertAccess(mLastCommittedUberPage.getRevision());

        // Make sure not to exceed available number of write transactions.
        if (mWriteSemaphore.availablePermits() == 0) {
            throw new IllegalStateException("There already is a running exclusive write transaction.");
        }
        try {
            mWriteSemaphore.acquire();
        } catch (final InterruptedException exc) {
            throw new TTThreadedException(exc);

        }

        final long currentID = mTransactionIDCounter.incrementAndGet();
        final WriteTransactionState wtxState =
            createWriteTransactionState(currentID, mLastCommittedUberPage.getRevisionNumber(),
                mLastCommittedUberPage.getRevisionNumber());

        // Create new write transaction.
        final IWriteTransaction wtx =
            new WriteTransaction(currentID, this, wtxState, paramMaxNodeCount, paramMaxTime);

        // Remember transaction for debugging and safe close.
        if (mTransactionMap.put(currentID, wtx) != null
            || mWriteTransactionStateMap.put(currentID, wtxState) != null) {
            throw new TTThreadedException("ID generation is bogus because of duplicate ID.");
        }

        return wtx;

    }

    protected WriteTransactionState createWriteTransactionState(final long mId,
        final long mRepresentRevision, final long mStoreRevision) throws TTIOException {
        final IWriter writer = mFac.getWriter();

        return new WriteTransactionState(this, new UberPage(mLastCommittedUberPage, mStoreRevision + 1),
            writer, mId, mRepresentRevision, mStoreRevision);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws AbsTTException {
        if (!mClosed) {
            // Forcibly close all open transactions.
            for (final IReadTransaction rtx : mTransactionMap.values()) {
                if (rtx instanceof IWriteTransaction) {
                    ((IWriteTransaction)rtx).abort();
                }
                rtx.close();
            }

            // Immediately release all ressources.
            mLastCommittedUberPage = null;
            mTransactionMap.clear();
            mWriteTransactionStateMap.clear();

            mFac.close();
            mDatabase.removeSession(mResourceConfig.mPath);
            mClosed = true;
        }
    }

    /**
     * Checks for valid revision.
     * 
     * @param paramRevision
     *            revision parameter to check
     * @throws IllegalArgumentException
     *             if revision isn't valid
     */
    protected void assertAccess(final long paramRevision) {
        if (mClosed) {
            throw new IllegalStateException("Session is already closed.");
        }
        if (paramRevision < 0) {
            throw new IllegalArgumentException("Revision must be at least 0");
        } else if (paramRevision > mLastCommittedUberPage.getRevision()) {
            throw new IllegalArgumentException(new StringBuilder("Revision must not be bigger than").append(
                Long.toString(mLastCommittedUberPage.getRevision())).toString());
        }
    }

    protected void closeWriteTransaction(final long mTransactionID) {
        // Purge transaction from internal state.
        mTransactionMap.remove(mTransactionID);
        // Removing the write from the own internal mapping
        mWriteTransactionStateMap.remove(mTransactionID);
        // Make new transactions available.
        mWriteSemaphore.release();
    }

    protected void closeReadTransaction(final long mTransactionID) {
        // Purge transaction from internal state.
        mTransactionMap.remove(mTransactionID);
        // Make new transactions available.
        mReadSemaphore.release();
    }

    /** {@inheritDoc} */
    @Override
    public synchronized boolean isClosed() {
        return mClosed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.mSessionConfig);
        return builder.toString();
    }

    @Override
    public String getUser() {
        return mSessionConfig.mUser;
    }

    protected synchronized void syncLogs(final NodePageContainer mContToSync, final long mTransactionId)
        throws TTThreadedException {
        final ExecutorService exec = Executors.newCachedThreadPool();
        final Collection<Future<Void>> returnVals = new ArrayList<Future<Void>>();
        for (final Long key : mWriteTransactionStateMap.keySet()) {
            if (key != mTransactionId) {
                returnVals.add(exec.submit(new LogSyncer(mWriteTransactionStateMap.get(key), mContToSync)));
            }
        }
        exec.shutdown();
        if (!mSyncTransactionsReturns.containsKey(mTransactionId)) {
            mSyncTransactionsReturns.put(mTransactionId,
                new ConcurrentHashMap<Long, Collection<Future<Void>>>());
        }

        if (mSyncTransactionsReturns.get(mTransactionId).put(mContToSync.getComplete().getNodePageKey(),
            returnVals) != null) {
            throw new TTThreadedException(
                "only one commit and therefore sync per id and nodepage is allowed!");
        }

    }

    protected synchronized void waitForFinishedSync(final long mTransactionKey) throws TTThreadedException {
        final Map<Long, Collection<Future<Void>>> completeVals =
            mSyncTransactionsReturns.remove(mTransactionKey);
        if (completeVals != null) {
            for (final Collection<Future<Void>> singleVals : completeVals.values()) {
                for (final Future<Void> returnVal : singleVals) {
                    try {
                        returnVal.get();
                    } catch (final InterruptedException exc) {
                        throw new TTThreadedException(exc);
                    } catch (final ExecutionException exc) {
                        throw new TTThreadedException(exc);
                    }
                }
            }
        }
    }

    class LogSyncer implements Callable<Void> {

        final WriteTransactionState mState;
        final NodePageContainer mCont;

        LogSyncer(final WriteTransactionState paramState, final NodePageContainer paramCont) {
            mState = paramState;
            mCont = paramCont;
        }

        @Override
        public Void call() throws Exception {
            mState.updateDateContainer(mCont);
            return null;
        }

    }

    protected void setLastCommittedUberPage(final UberPage paramPage) {
        this.mLastCommittedUberPage = paramPage;
    }
}
