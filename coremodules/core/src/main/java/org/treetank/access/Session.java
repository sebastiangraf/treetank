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

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendWriter;
import org.treetank.io.IOUtils;
import org.treetank.page.UberPage;

/**
 * <h1>Session</h1>
 * 
 * <p>
 * Makes sure that there only is a single session instance bound to a TreeTank file.
 * </p>
 */
public final class Session implements ISession {

    /** Session configuration. */
    private final ResourceConfiguration mResourceConfig;

    /** Session configuration. */
    protected final SessionConfiguration mSessionConfig;

    /** Storage for centralized closure of related Sessions. */
    private final Storage mDatabase;

    /** Strong reference to uber page before the begin of a write transaction. */
    private UberPage mLastCommittedUberPage;

    /** Remember the write separately because of the concurrent writes. */
    private final Set<IPageReadTrx> mPageTrxs;

    /** Remember write references only once. */
    private final AtomicReference<IPageWriteTrx> mPageWtx;

    /** abstract factory for all interaction to the storage. */
    private final IBackend mStorage;

    /** Determines if session was closed. */
    private transient boolean mClosed;

    /**
     * 
     * Hidden constructor, only visible for the Storage-Class for instantiation.
     * 
     * @param pStorage
     *            Storage for centralized operations on related sessions.
     * @param pSessionConf
     *            StorageConfiguration for general setting about the storage
     * @param pResourceConf
     *            ResourceConfiguration for handling this specific session
     * @param pPage
     *            to be set.
     * @throws TTException
     */
    protected Session(final Storage pStorage, final ResourceConfiguration pResourceConf,
        final SessionConfiguration pSessionConf, final UberPage pPage) throws TTException {
        mDatabase = pStorage;
        mResourceConfig = pResourceConf;
        mSessionConfig = pSessionConf;
        mPageTrxs = new CopyOnWriteArraySet<IPageReadTrx>();
        mPageWtx = new AtomicReference<IPageWriteTrx>();
        mStorage = pResourceConf.mStorage;
        mClosed = false;
        mLastCommittedUberPage = pPage;
    }

    public IPageReadTrx beginPageReadTransaction(final long pRevKey) throws TTException {
        assertAccess(pRevKey);
        final PageReadTrx trx = new PageReadTrx(this, mLastCommittedUberPage, pRevKey, mStorage.getReader());
        mPageTrxs.add(trx);
        return trx;
    }

    public IPageWriteTrx beginPageWriteTransaction() throws TTException {
        return beginPageWriteTransaction(mLastCommittedUberPage.getRevisionNumber());

    }

    public IPageWriteTrx beginPageWriteTransaction(final long mRepresentRevision) throws TTException {
        assertAccess(mRepresentRevision);
        final IBackendWriter backendWriter = mStorage.getWriter();
        final IPageWriteTrx trx =
            new PageWriteTrx(this, mLastCommittedUberPage, backendWriter, mRepresentRevision);
        mPageTrxs.add(trx);

        return trx;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized boolean close() throws TTException {
        if (!mClosed) {
            // Forcibly close all open transactions.
            for (final IPageReadTrx rtx : mPageTrxs) {
                rtx.close();
            }

            // Immediately release all resources.
            mLastCommittedUberPage = null;
            mPageTrxs.clear();

            mStorage.close();
            mDatabase.mSessions.remove(mSessionConfig.getResource());
            mClosed = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean truncate() throws TTException {
        checkState(mClosed, "Session must be closed before truncated.");
        mStorage.truncate();
        IOUtils.recursiveDelete(new File(new File(mDatabase.getLocation(), StorageConfiguration.Paths.Data
            .getFile().getName()), mSessionConfig.getResource()));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Session [mResourceConfig=");
        builder.append(mResourceConfig);
        builder.append(", mSessionConfig=");
        builder.append(mSessionConfig);
        builder.append(", mLastCommittedUberPage=");
        builder.append(mLastCommittedUberPage);
        builder.append(", mPageTrxs=");
        builder.append(mPageTrxs);
        builder.append(", mFac=");
        builder.append(mStorage);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Asserting access on this session with the denoted revision number
     * 
     * @param pRevision
     *            the revision to be validated
     */
    private void assertAccess(final long pRevision) {
        checkState(!mClosed, "Session is already closed.");
        checkState(pRevision <= mLastCommittedUberPage.getRevisionNumber(),
            "Revision must not be bigger than %s", mLastCommittedUberPage.getRevisionNumber());
    }

    protected void setLastCommittedUberPage(final UberPage paramPage) {
        this.mLastCommittedUberPage = paramPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getMostRecentVersion() {
        return mLastCommittedUberPage.getRevisionNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceConfiguration getConfig() {
        return mResourceConfig;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deregisterPageTrx(IPageReadTrx pTrx) {
        mPageTrxs.remove(pTrx);
    }
}
