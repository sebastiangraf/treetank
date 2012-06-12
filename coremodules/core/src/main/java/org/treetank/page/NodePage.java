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
package org.treetank.page;

import java.util.Arrays;

import org.treetank.access.PageWriteTrx;
import org.treetank.api.INode;
import org.treetank.exception.AbsTTException;
import org.treetank.io.ITTSink;
import org.treetank.io.ITTSource;
import org.treetank.node.ENode;
import org.treetank.page.delegates.PageDelegate;
import org.treetank.utils.IConstants;

/**
 * <h1>NodePage</h1>
 * 
 * <p>
 * A node page stores a set of nodes.
 * </p>
 */
public class NodePage implements IPage {

    /** Key of node page. This is the base key of all contained nodes. */
    private final long mNodePageKey;

    /** Array of nodes. This can have null nodes that were removed. */
    private final INode[] mNodes;

    private final PageDelegate mDelegate;

    /**
     * Create node page.
     * 
     * @param nodePageKey
     *            Base key assigned to this node page.
     */
    public NodePage(final long nodePageKey, final long mRevision) {
        mDelegate = new PageDelegate(0, mRevision);
        mNodePageKey = nodePageKey;
        mNodes = new INode[IConstants.NDP_NODE_COUNT];
    }

    /**
     * Read node page.
     * 
     * @param mIn
     *            Input bytes to read page from.
     */
    protected NodePage(final ITTSource mIn) {
        mDelegate = new PageDelegate(0, mIn.readLong());
        mDelegate.initialize(mIn);

        mNodePageKey = mIn.readLong();
        mNodes = new INode[IConstants.NDP_NODE_COUNT];

        // final EncryptionController enController = EncryptionController
        // .getInstance();
        //
        // if (enController.checkEncryption()) {
        // for (int i = 0; i < mNodes.length; i++) {
        // final long mRightKey = getRightKey(mIn);
        //
        // final List<Long> mUserKeys = enController.getKeyCache().get(
        // enController.getUser());
        // byte[] mSecretKey = null;
        //
        // if (mUserKeys.contains(mRightKey) || mRightKey == -1) {
        // final int mElementKind = mIn.readInt();
        //
        // if (mRightKey != -1) {
        //
        // // get secret key
        // mSecretKey = enController.getSelDb()
        // .getEntry(mRightKey).getSecretKey();
        //
        // final int mNodeBytes = mIn.readInt();
        // final int mPointerBytes = mIn.readInt();
        //
        // final byte[] mDecryptedNode;
        //
        // if (mPointerBytes == 0) {
        //
        // final byte[] mEncryptedNode = new byte[mNodeBytes];
        //
        // for (int j = 0; j < mNodeBytes; j++) {
        // mEncryptedNode[j] = mIn.readByte();
        // }
        //
        // mDecryptedNode = NodeEncryption.decrypt(
        // mEncryptedNode, mSecretKey);
        //
        // } else {
        //
        // final byte[] mEncryptedPointer = new byte[mPointerBytes];
        // for (int j = 0; j < mPointerBytes; j++) {
        // mEncryptedPointer[j] = mIn.readByte();
        // }
        //
        // final int mDataBytes = mNodeBytes - mPointerBytes;
        // final byte[] mEncryptedData = new byte[mDataBytes];
        // for (int j = 0; j < mDataBytes; j++) {
        // mEncryptedData[j] = mIn.readByte();
        // }
        //
        // final byte[] mDecryptedPointer = NodeEncryption
        // .decrypt(mEncryptedPointer, mSecretKey);
        //
        // final byte[] mDecryptedData = NodeEncryption
        // .decrypt(mEncryptedData, mSecretKey);
        //
        // mDecryptedNode = new byte[mDecryptedPointer.length
        // + mDecryptedData.length];
        //
        // int mCounter = 0;
        // for (int j = 0; j < mDecryptedPointer.length; j++) {
        // mDecryptedNode[mCounter] = mDecryptedPointer[j];
        // mCounter++;
        // }
        // for (int j = 0; j < mDecryptedData.length; j++) {
        // mDecryptedNode[mCounter] = mDecryptedData[j];
        // mCounter++;
        // }
        //
        // }
        //
        // final NodeInputSource mNodeInput = new NodeInputSource(
        // mDecryptedNode);
        //
        // final ENode mEnumKind = ENode.getKind(mElementKind);
        //
        // if (mEnumKind != ENode.UNKOWN_KIND) {
        // getNodes()[i] = mEnumKind.deserialize(mNodeInput);
        // }
        // }
        //
        // } else {
        // try {
        // throw new TTUsageException(
        // "User has no permission to access the node");
        //
        // } catch (final TTUsageException mExp) {
        // mExp.printStackTrace();
        // }
        // }
        // }
        // } else {
        final int[] kinds = new int[IConstants.NDP_NODE_COUNT];
        for (int i = 0; i < kinds.length; i++) {
            kinds[i] = mIn.readInt();
        }

        for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
            final int kind = kinds[offset];
            final ENode enumKind = ENode.getKind(kind);
            if (enumKind != ENode.UNKOWN_KIND) {
                getNodes()[offset] = enumKind.deserialize(mIn);
            }
        }

        // }
    }

    /**
     * Get key of node page.
     * 
     * @return Node page key.
     */
    public final long getNodePageKey() {
        return mNodePageKey;
    }

    /**
     * Get node at a given offset.
     * 
     * @param mOffset
     *            Offset of node within local node page.
     * @return Node at given offset.
     */
    public INode getNode(final int mOffset) {
        return getNodes()[mOffset];
    }

    /**
     * Overwrite a single node at a given offset.
     * 
     * @param mOffset
     *            Offset of node to overwrite in this node page.
     * @param mNode
     *            Node to store at given nodeOffset.
     */
    public void setNode(final int mOffset, final INode mNode) {
        getNodes()[mOffset] = mNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink mOut) {
        // TODO respect new INode hierarchy, must rely on normal INodes only
        mDelegate.serialize(mOut);
        mOut.writeLong(mNodePageKey);
        for (int i = 0; i < getNodes().length; i++) {
            if (getNodes()[i] != null) {
                final int kind = ((org.treetank.node.interfaces.INode) getNodes()[i])
                        .getKind().getId();
                mOut.writeInt(kind);
            } else {
                mOut.writeInt(ENode.UNKOWN_KIND.getId());
            }
        }

        for (final INode node : getNodes()) {
            if (node != null) {
                org.treetank.node.interfaces.INode nodenode = (org.treetank.node.interfaces.INode) node;
                ENode.getKind(nodenode.getClass()).serialize(mOut, nodenode);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        final StringBuilder returnString = new StringBuilder();
        returnString.append("pagekey=");
        returnString.append(mNodePageKey);
        returnString.append(", nodes: ");
        for (final INode node : getNodes()) {
            if (node != null) {
                returnString.append(node.getNodeKey());
                returnString.append(",");
            }
        }
        return returnString.toString();
    }

    /**
     * @return the mNodes
     */
    public final INode[] getNodes() {
        return mNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (mNodePageKey ^ (mNodePageKey >>> 32));
        result = prime * result + Arrays.hashCode(mNodes);
        return result;
    }

    @Override
    public boolean equals(final Object mObj) {
        if (this == mObj) {
            return true;
        }

        if (mObj == null) {
            return false;
        }

        if (getClass() != mObj.getClass()) {
            return false;
        }

        final NodePage mOther = (NodePage) mObj;
        if (mNodePageKey != mOther.mNodePageKey) {
            return false;
        }

        if (!Arrays.equals(mNodes, mOther.mNodes)) {
            return false;
        }

        return true;
    }

    @Override
    public void commit(PageWriteTrx paramState) throws AbsTTException {
        mDelegate.commit(paramState);

    }

    @Override
    public PageReference[] getReferences() {
        return mDelegate.getReferences();
    }

    @Override
    public long getRevision() {
        return mDelegate.getRevision();
    }

}
