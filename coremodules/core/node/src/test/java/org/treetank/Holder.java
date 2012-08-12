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
package org.treetank;

import org.treetank.TestHelper.PATHS;
import org.treetank.access.NodeReadTrx;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.INodeReadTrx;
import org.treetank.api.INodeWriteTrx;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;

/**
 * Generating a standard resource within the {@link PATHS#PATH1} path. It also
 * generates a standard resource defined within {@link NodeHelper#RESOURCE}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Holder {

    private IDatabase mDatabase;

    private ISession mSession;

    private IPageReadTrx mPRtx;

    private INodeReadTrx mNRtx;

    public static Holder generateSession(IResourceConfigurationFactory pConf) throws TTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        database.createResource(pConf.create(TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME, 10));
        final ISession session =
            database.getSession(new SessionConfiguration(TestHelper.RESOURCENAME, TestHelper.KEY));
        final Holder holder = new Holder();
        holder.mDatabase = database;
        holder.mSession = session;
        return holder;
    }

    public static Holder generateWtx(IResourceConfigurationFactory pConf) throws TTException {
        final Holder holder = generateSession(pConf);
        final IPageWriteTrx pRtx = holder.mSession.beginPageWriteTransaction();
        holder.mPRtx = pRtx;
        holder.mNRtx = new NodeWriteTrx(holder.mSession, pRtx, HashKind.Rolling);
        return holder;
    }

    public static Holder generateRtx(IResourceConfigurationFactory pConf) throws TTException {
        final Holder holder = generateSession(pConf);
        final IPageReadTrx pRtx =
            holder.mSession.beginPageReadTransaction(holder.mSession.getMostRecentVersion());
        holder.mNRtx = new NodeReadTrx(pRtx);
        return holder;
    }

    public void close() throws TTException {
        if (mNRtx != null && !mNRtx.isClosed()) {
            mNRtx.close();
        }
        mSession.close();
    }

    public IDatabase getDatabase() {
        return mDatabase;
    }

    public ISession getSession() {
        return mSession;
    }

    public IPageReadTrx getPRtx() {
        return mPRtx;
    }

    public IPageWriteTrx getPWtx() {
        if (mPRtx instanceof IPageWriteTrx) {
            return (IPageWriteTrx)mPRtx;
        } else {
            throw new IllegalStateException();
        }

    }

    public INodeReadTrx getNRtx() {
        return mNRtx;
    }

    public INodeWriteTrx getNWtx() {
        if (mNRtx instanceof INodeWriteTrx) {
            return (INodeWriteTrx)mNRtx;
        } else {
            throw new IllegalStateException();
        }

    }

}
