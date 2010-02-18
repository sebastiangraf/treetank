/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: SessionState.java 4470 2008-09-06 15:24:52Z kramis $
 */

package com.treetank.access;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import com.treetank.api.IItemList;
import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
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

    /** Database configuration. */
    private final DatabaseConfiguration mDatabaseConfiguration;

    /** Session configuration. */
    private final SessionConfiguration mSessionConfiguration;

    /** Write semaphore to assure only one exclusive write transaction exists. */
    private final Semaphore mWriteSemaphore;

    /** Read semaphore to control running read transactions. */
    private final Semaphore mReadSemaphore;

    /** Semaphore for blocking the commit */
    protected final Semaphore mCommitSemaphore;

    /** Strong reference to uber page before the begin of a write transaction. */
    private UberPage mLastCommittedUberPage;

    /** Remember all running transactions (both read and write). */
    private final Map<Long, IReadTransaction> mTransactionMap;

    /** Remember the write seperatly because of the concurrent writes */
    private final Map<Long, IWriteTransaction> mWriteTransactionMap;

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
        mWriteTransactionMap = new ConcurrentHashMap<Long, IWriteTransaction>();
        transactionIDCounter = new AtomicLong();
        mCommitSemaphore = new Semaphore(1);

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
                        mSessionConfiguration, mLastCommittedUberPage,
                        revisionNumber, itemList, fac.getReader()));

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

        // Create new write transaction.
        final IWriteTransaction wtx = new WriteTransaction(transactionIDCounter
                .incrementAndGet(), this, createWriteTransactionState(),
                maxNodeCount, maxTime);

        // Remember transaction for debugging and safe close.
        if (mTransactionMap.put(wtx.getTransactionID(), wtx) != null) {
            throw new IllegalStateException(
                    "ID generation is bogus because of duplicate ID.");
        }

        return wtx;
    }

    protected WriteTransactionState createWriteTransactionState()
            throws TreetankIOException {
        final IWriter writer = fac.getWriter();

        return new WriteTransactionState(mDatabaseConfiguration, this,
                new UberPage(mLastCommittedUberPage), writer);
    }

    protected UberPage getLastCommittedUberPage() {
        return mLastCommittedUberPage;
    }

    protected void setLastCommittedUberPage(final UberPage lastCommittedUberPage) {
        mLastCommittedUberPage = lastCommittedUberPage;
    }

    protected void closeWriteTransaction(final long transactionID) {
        // Purge transaction from internal state.
        mTransactionMap.remove(transactionID);
        // Removing the write from the own internal mapping
        mWriteTransactionMap.remove(transactionID);
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
        mWriteTransactionMap.clear();

        fac.closeStorage();
    }

    protected SessionConfiguration getSessionConfiguration() {
        return mSessionConfiguration;
    }

    /**
     * Required to close file handle.
     * 
     * @throws Throwable
     *             if the finalization of the superclass does not work.
     */
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

}
