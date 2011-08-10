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
import org.treetank.api.IItemList;
import org.treetank.api.IReadTransaction;
import org.treetank.api.IWriteTransaction;
import org.treetank.cache.NodePageContainer;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTIOException;
import org.treetank.exception.TTThreadedException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.AbsIOFactory;
import org.treetank.io.IReader;
import org.treetank.io.IWriter;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;

/**
 * <h1>SessionState</h1>
 * 
 * <p>
 * State of each session.
 * </p>
 */
public final class SessionState {

    /** Lock for blocking the commit. */
    protected final Lock mCommitLock;

    /** Session configuration. */
    protected final ResourceConfiguration mResourceConfig;

    /** Session configuration. */
    protected final SessionConfiguration mSessionConfig;

    /** Write semaphore to assure only one exclusive write transaction exists. */
    private final Semaphore mWriteSemaphore;

    /** Read semaphore to control running read transactions. */
    private final Semaphore mReadSemaphore;

    /** Strong reference to uber page before the begin of a write transaction. */
    private UberPage mLastCommittedUberPage;

    /** Remember all running transactions (both read and write). */
    private final Map<Long, IReadTransaction> mTransactionMap;

    /** Remember the write seperatly because of the concurrent writes. */
    private final Map<Long, WriteTransactionState> mWriteTransactionStateMap;

    /** Storing all return futures from the sync process. */
    private final Map<Long, Map<Long, Collection<Future<Void>>>> mSyncTransactionsReturns;

    /** abstract factory for all interaction to the storage. */
    private final AbsIOFactory mFac;

    /** Atomic counter for concurrent generation of transaction id. */
    private final AtomicLong mTransactionIDCounter;

    /**
     * Constructor to bind to a TreeTank file.
     * 
     * @param paramSessionConfig
     *            Session configuration for the TreeTank.
     * @throws AbsTTException
     *             if Session state error
     */
    protected SessionState(final ResourceConfiguration paramResourceConf,
        final SessionConfiguration paramSessionConfig) throws AbsTTException {
        mResourceConfig = paramResourceConf;
        mSessionConfig = paramSessionConfig;
        mTransactionMap = new ConcurrentHashMap<Long, IReadTransaction>();
        mWriteTransactionStateMap = new ConcurrentHashMap<Long, WriteTransactionState>();
        mSyncTransactionsReturns = new ConcurrentHashMap<Long, Map<Long, Collection<Future<Void>>>>();

        mTransactionIDCounter = new AtomicLong();
        mCommitLock = new ReentrantLock(false);

        // Init session members.
        mWriteSemaphore = new Semaphore(paramSessionConfig.mWtxAllowed);
        mReadSemaphore = new Semaphore(paramSessionConfig.mRtxAllowed);
        final PageReference uberPageReference = new PageReference();

        mFac = AbsIOFactory.getInstance(mResourceConfig);
        if (!mFac.exists()) {
            // Bootstrap uber page and make sure there already is a root
            // node.
            mLastCommittedUberPage = new UberPage();
            uberPageReference.setPage(mLastCommittedUberPage);

        } else {
            final IReader reader = mFac.getReader();
            final PageReference firstRef = reader.readFirstReference();
            mLastCommittedUberPage = (UberPage)firstRef.getPage();
            reader.close();
        }

    }

    protected IReadTransaction beginReadTransaction() throws AbsTTException {
        return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(), null);
    }

    protected IReadTransaction beginReadTransaction(final IItemList mItemList) throws AbsTTException {
        return beginReadTransaction(mLastCommittedUberPage.getRevisionNumber(), mItemList);
    }

    protected IReadTransaction beginReadTransaction(final long mRevisionNumber, final IItemList mItemList)
        throws AbsTTException {

        // Make sure not to exceed available number of read transactions.
        try {
            mReadSemaphore.acquire();
        } catch (final InterruptedException exc) {
            throw new AbsTTException(exc) {
                private static final long serialVersionUID = 1L;
            };
        }

        IReadTransaction rtx = null;
        // Create new read transaction.
        rtx =
            new ReadTransaction(this, mTransactionIDCounter.incrementAndGet(), new ReadTransactionState(this,
                mLastCommittedUberPage, mRevisionNumber, mItemList, mFac.getReader()));

        // Remember transaction for debugging and safe close.
        if (mTransactionMap.put(rtx.getTransactionID(), rtx) != null) {
            throw new TTUsageException("ID generation is bogus because of duplicate ID.");
        }
        return rtx;
    }

    protected IWriteTransaction beginWriteTransaction(final int maxNodeCount, final int maxTime)
        throws AbsTTException {

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
        final IWriteTransaction wtx = new WriteTransaction(currentID, this, wtxState, maxNodeCount, maxTime);

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

    protected void setLastCommittedUberPage(final UberPage lastCommittedUberPage) {
        mLastCommittedUberPage = lastCommittedUberPage;
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

    protected void close() throws AbsTTException {
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

    /**
     * Checks for valid revision.
     * 
     * @param paramRevision
     *            revision parameter to check
     * @throws IllegalArgumentException
     *             if revision isn't valid
     */
    protected void assertValidRevision(final long paramRevision) {
        if (paramRevision < 0) {
            throw new IllegalArgumentException("Revision must be at least 0");
        } else if (paramRevision > mLastCommittedUberPage.getRevision()) {
            throw new IllegalArgumentException(new StringBuilder("Revision must not be bigger than").append(
                Long.toString(mLastCommittedUberPage.getRevision())).toString());
        }
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

}
