/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package org.treetank.page;

import java.util.Arrays;
import java.util.List;

import org.treetank.access.WriteTransactionState;
import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.database.model.KeySelector;
import org.treetank.encryption.utils.NodeEncryption;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.ITTSink;
import org.treetank.io.ITTSource;
import org.treetank.node.AbsNode;
import org.treetank.node.ENodes;
import org.treetank.node.io.NodeInputSource;
import org.treetank.node.io.NodeOutputSink;
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
    private final AbsNode[] mNodes;

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
        mNodes = new AbsNode[IConstants.NDP_NODE_COUNT];
    }

    /**
     * Read node page.
     * 
     * @param mIn
     *            Input bytes to read page from.
     * @throws
     */
    protected NodePage(final ITTSource mIn) {
        mDelegate = new PageDelegate(0, mIn.readLong());
        mDelegate.initialize(mIn);

        mNodePageKey = mIn.readLong();
        mNodes = new AbsNode[IConstants.NDP_NODE_COUNT];

        final EncryptionController enController = EncryptionController
                .getInstance();

        if (enController.checkEncryption()) {
            for (int i = 0; i < mNodes.length; i++) {
                final long mRightKey = getRightKey(mIn);

                final List<Long> mUserKeys = enController.getKeyCache().get(
                        enController.getUser());
                byte[] mSecretKey = null;

                if (mUserKeys.contains(mRightKey) || mRightKey == -1) {
                    final int mElementKind = mIn.readInt();

                    if (mRightKey != -1) {

                        // get secret key
                        mSecretKey = enController.getSelDb()
                                .getEntry(mRightKey).getSecretKey();

                        final int mNodeBytes = mIn.readInt();
                        final int mPointerBytes = mIn.readInt();

                        final byte[] mDecryptedNode;

                        if (mPointerBytes == 0) {

                            final byte[] mEncryptedNode = new byte[mNodeBytes];

                            for (int j = 0; j < mNodeBytes; j++) {
                                mEncryptedNode[j] = mIn.readByte();
                            }

                            mDecryptedNode = NodeEncryption.decrypt(
                                    mEncryptedNode, mSecretKey);

                        } else {

                            final byte[] mEncryptedPointer = new byte[mPointerBytes];
                            for (int j = 0; j < mPointerBytes; j++) {
                                mEncryptedPointer[j] = mIn.readByte();
                            }

                            final int mDataBytes = mNodeBytes - mPointerBytes;
                            final byte[] mEncryptedData = new byte[mDataBytes];
                            for (int j = 0; j < mDataBytes; j++) {
                                mEncryptedData[j] = mIn.readByte();
                            }

                            final byte[] mDecryptedPointer = NodeEncryption
                                    .decrypt(mEncryptedPointer, mSecretKey);

                            final byte[] mDecryptedData = NodeEncryption
                                    .decrypt(mEncryptedData, mSecretKey);

                            mDecryptedNode = new byte[mDecryptedPointer.length
                                    + mDecryptedData.length];

                            int mCounter = 0;
                            for (int j = 0; j < mDecryptedPointer.length; j++) {
                                mDecryptedNode[mCounter] = mDecryptedPointer[j];
                                mCounter++;
                            }
                            for (int j = 0; j < mDecryptedData.length; j++) {
                                mDecryptedNode[mCounter] = mDecryptedData[j];
                                mCounter++;
                            }

                        }

                        final NodeInputSource mNodeInput = new NodeInputSource(
                                mDecryptedNode);

                        final ENodes mEnumKind = ENodes
                                .getEnumKind(mElementKind);

                        if (mEnumKind != ENodes.UNKOWN_KIND) {
                            getNodes()[i] = mEnumKind
                                    .createNodeFromPersistence(mNodeInput);
                        }
                    }

                } else {
                    try {
                        throw new TTUsageException(
                                "User has no permission to access the node");

                    } catch (final TTUsageException mExp) {
                        mExp.printStackTrace();
                    }
                }
            }
        } else {
            final int[] kinds = new int[IConstants.NDP_NODE_COUNT];
            for (int i = 0; i < kinds.length; i++) {
                kinds[i] = mIn.readInt();
            }

            for (int offset = 0; offset < IConstants.NDP_NODE_COUNT; offset++) {
                final int kind = kinds[offset];
                final ENodes enumKind = ENodes.getEnumKind(kind);
                if (enumKind != ENodes.UNKOWN_KIND) {
                    getNodes()[offset] = enumKind
                            .createNodeFromPersistence(mIn);
                }
            }

        }
    }

    /**
     * Get key of node page.
     * 
     * @return Node page key.
     */
    public final long getNodePageKey() {
        return mNodePageKey;
    }

    private long getRightKey(final ITTSource mIn) {
        final long rightKey = mIn.readLong();
        mIn.readInt();
        mIn.readInt();
        return rightKey;
    }

    /**
     * Get node at a given offset.
     * 
     * @param mOffset
     *            Offset of node within local node page.
     * @return Node at given offset.
     */
    public AbsNode getNode(final int mOffset) {
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
    public void setNode(final int mOffset, final AbsNode mNode) {
        getNodes()[mOffset] = mNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(final ITTSink mOut) {
        mDelegate.serialize(mOut);
        mOut.writeLong(mNodePageKey);

        final EncryptionController enController = EncryptionController
                .getInstance();

        if (enController.checkEncryption()) {
            NodeOutputSink mNodeOut = null;
            for (final AbsNode node : getNodes()) {
                if (node != null) {
                    mNodeOut = new NodeOutputSink();

                    final long mDek = enController.getDataEncryptionKey();

                    final KeySelector mKeySel = enController.getSelDb()
                            .getEntry(mDek);
                    final byte[] mSecretKey = mKeySel.getSecretKey();

                    mOut.writeLong(mKeySel.getPrimaryKey());
                    mOut.writeInt(mKeySel.getRevision());
                    mOut.writeInt(mKeySel.getVersion());
                    final int kind = node.getKind().getNodeIdentifier();
                    mOut.writeInt(kind);
                    node.serialize(mNodeOut);

                    final byte[] mStream = mNodeOut.getOutputStream()
                            .toByteArray();

                    byte[] mEncrypted = null;
                    final int pointerEnSize;

                    if (mStream.length > 0) {

                        final byte[] mPointer = new byte[ENodes.MPOINTERSIZE];

                        for (int i = 0; i < mPointer.length; i++) {
                            mPointer[i] = mStream[i];
                        }

                        final byte[] mData = new byte[mStream.length
                                - mPointer.length];
                        for (int i = 0; i < mData.length; i++) {
                            mData[i] = mStream[mPointer.length + i];
                        }

                        final byte[] mEnPointer = NodeEncryption.encrypt(
                                mPointer, mSecretKey);
                        pointerEnSize = mEnPointer.length;
                        final byte[] mEnData = NodeEncryption.encrypt(mData,
                                mSecretKey);

                        mEncrypted = new byte[mEnPointer.length
                                + mEnData.length];

                        int mCounter = 0;
                        for (int i = 0; i < mEnPointer.length; i++) {
                            mEncrypted[mCounter] = mEnPointer[i];
                            mCounter++;
                        }
                        for (int i = 0; i < mEnData.length; i++) {
                            mEncrypted[mCounter] = mEnData[i];
                            mCounter++;
                        }

                    } else {
                        pointerEnSize = 0;
                        mEncrypted = NodeEncryption
                                .encrypt(mStream, mSecretKey);
                    }

                    mOut.writeInt(mEncrypted.length);
                    mOut.writeInt(pointerEnSize);

                    for (byte aByte : mEncrypted) {
                        mOut.writeByte(aByte);
                    }

                } else {
                    mOut.writeLong(-1);
                    mOut.writeInt(-1);
                    mOut.writeInt(-1);
                    mOut.writeInt(ENodes.UNKOWN_KIND.getNodeIdentifier());
                }
            }
        } else {
            for (int i = 0; i < getNodes().length; i++) {
                if (getNodes()[i] != null) {
                    final int kind = getNodes()[i].getKind()
                            .getNodeIdentifier();
                    mOut.writeInt(kind);
                } else {
                    mOut.writeInt(ENodes.UNKOWN_KIND.getNodeIdentifier());
                }
            }

            for (final AbsNode node : getNodes()) {
                if (node != null) {
                    node.serialize(mOut);
                }
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
        for (final AbsNode node : getNodes()) {
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
    public final AbsNode[] getNodes() {
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
    public PageReference getChildren(int paramOffset) {
        return mDelegate.getChildren(paramOffset);
    }

    @Override
    public void commit(WriteTransactionState paramState) throws AbsTTException {
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
