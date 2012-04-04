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

package org.treetank.service.xml.diff;

import org.treetank.api.INodeReadTrx;
import org.treetank.exception.AbsTTException;
import org.treetank.node.ENode;
import org.treetank.node.ElementNode;
import org.treetank.service.xml.diff.DiffFactory.Builder;

/**
 * Full diff including attributes and namespaces. Note that this class is thread
 * safe.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class FullDiff extends AbsDiff {

    /**
     * Constructor.
     * 
     * @param paramBuilder
     *            {@link Builder} reference
     * @throws AbsTTException
     *             if anything goes wrong while setting up Treetank transactions
     */
    FullDiff(final Builder paramBuilder) throws AbsTTException {
        super(paramBuilder);
    }

    /** {@inheritDoc} */
    @Override
    boolean checkNodes(final INodeReadTrx paramFirstRtx, final INodeReadTrx paramSecondRtx) {
        assert paramFirstRtx != null;
        assert paramSecondRtx != null;

        boolean found = false;

        if (paramFirstRtx.getNode().getNodeKey() == paramSecondRtx.getNode().getNodeKey()
            && paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
            final long nodeKey = paramFirstRtx.getNode().getNodeKey();

            if (paramFirstRtx.getNode().getKind() == ENode.ELEMENT_KIND) {
                if (((ElementNode)paramFirstRtx.getNode()).getNamespaceCount() == 0
                    && ((ElementNode)paramFirstRtx.getNode()).getAttributeCount() == 0
                    && ((ElementNode)paramSecondRtx.getNode()).getAttributeCount() == 0
                    && ((ElementNode)paramSecondRtx.getNode()).getNamespaceCount() == 0) {
                    found = true;
                } else {
                    if (((ElementNode)paramFirstRtx.getNode()).getNamespaceCount() == 0) {
                        found = true;
                    } else {
                        for (int i = 0; i < ((ElementNode)paramFirstRtx.getNode()).getNamespaceCount(); i++) {
                            paramFirstRtx.moveToNamespace(i);
                            for (int j = 0; j < ((ElementNode)paramSecondRtx.getNode()).getNamespaceCount(); j++) {
                                paramSecondRtx.moveToNamespace(i);

                                if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                                    found = true;
                                    break;
                                }
                            }
                            paramFirstRtx.moveTo(nodeKey);
                            paramSecondRtx.moveTo(nodeKey);
                        }
                    }

                    if (found) {
                        for (int i = 0; i < ((ElementNode)paramFirstRtx.getNode()).getAttributeCount(); i++) {
                            paramFirstRtx.moveToAttribute(i);
                            for (int j = 0; j < ((ElementNode)paramSecondRtx.getNode()).getAttributeCount(); j++) {
                                paramSecondRtx.moveToAttribute(i);

                                if (paramFirstRtx.getNode().equals(paramSecondRtx.getNode())) {
                                    found = true;
                                    break;
                                }
                            }
                            paramFirstRtx.moveTo(nodeKey);
                            paramSecondRtx.moveTo(nodeKey);
                        }
                    }
                }
            } else {
                found = true;
            }
        }

        return found;
    }
}
