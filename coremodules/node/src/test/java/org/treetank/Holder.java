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

import static com.google.common.base.Preconditions.checkState;

import org.treetank.CoreTestHelper.PATHS;
import org.treetank.access.NodeReadTrx;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.api.INodeReadTrx;
import org.treetank.api.INodeWriteTrx;
import org.treetank.api.IPageReadTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;

/**
 * Generating a standard resource within the {@link PATHS#PATH1} path. It also
 * generates a standard resource defined within {@link CoreTestHelper#RESOURCENAME}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Holder {

    private CoreTestHelper.Holder mHolder;

    private INodeReadTrx mNRtx;

    public static Holder generateWtx(CoreTestHelper.Holder pHolder, ResourceConfiguration pConf)
        throws TTException {
        Holder holder = new Holder();
        holder.mHolder = pHolder;
        CoreTestHelper.Holder.generateWtx(pHolder, pConf);
        holder.mNRtx = new NodeWriteTrx(holder.mHolder.mSession, holder.mHolder.mPageWTrx, HashKind.None);
        return holder;
    }

    public static Holder generateRtx(CoreTestHelper.Holder pHolder, ResourceConfiguration pConf)
        throws TTException {
        Holder holder = new Holder();
        holder.mHolder = pHolder;
        CoreTestHelper.Holder.generateRtx(pHolder, pConf);
        holder.mNRtx = new NodeReadTrx(holder.mHolder.mPageRTrx);
        return holder;
    }

    public void close() throws TTException {
        if (mNRtx != null && !mNRtx.isClosed()) {
            mNRtx.close();
        }
        mHolder.close();
    }

    public ISession getSession() {
        return mHolder.mSession;
    }

    public IPageReadTrx getPRtx() {
        return mHolder.mPageRTrx;
    }

    public INodeReadTrx getNRtx() {
        return mNRtx;
    }

    public INodeWriteTrx getNWtx() {
        checkState(mNRtx instanceof INodeWriteTrx);
        return (INodeWriteTrx)mNRtx;
    }
}
