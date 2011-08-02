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

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LockManagerTest {

    private static Long[] nodes;

    private Holder holder;

    @After
    public void tearDown() throws AbsTTException {
        holder.close();
        TestHelper.closeEverything();
    }

    @Before
    public void setUp() throws AbsTTException {
        TestHelper.deleteEverything();

        holder = Holder.generate();
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();

        nodes = new Long[13];
        nodes[0] = Long.valueOf("0");

        wtx.insertElementAsFirstChild(new QName("1"));
        nodes[1] = wtx.getNode().getNodeKey();

        wtx.insertElementAsRightSibling(new QName("2"));
        nodes[2] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("3"));
        nodes[3] = wtx.getNode().getNodeKey();
        wtx.moveToLeftSibling();
        wtx.moveToLeftSibling();

        wtx.insertElementAsFirstChild(new QName("4"));
        nodes[4] = wtx.getNode().getNodeKey();

        wtx.insertElementAsRightSibling(new QName("5"));
        nodes[5] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("6"));
        nodes[6] = wtx.getNode().getNodeKey();
        wtx.moveToParent();
        wtx.moveToRightSibling();

        wtx.insertElementAsFirstChild(new QName("7"));
        nodes[7] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("8"));
        nodes[8] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("9"));
        nodes[9] = wtx.getNode().getNodeKey();
        wtx.moveToParent();
        wtx.moveToRightSibling();

        wtx.insertElementAsFirstChild(new QName("10"));
        nodes[10] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("11"));
        nodes[11] = wtx.getNode().getNodeKey();
        wtx.insertElementAsRightSibling(new QName("12"));
        nodes[12] = wtx.getNode().getNodeKey();
        wtx.commit();
        wtx.close();
    }

    @Ignore
    @Test
    /**
     * Simply locking an available subtree without interference
     */
    public void basicLockingTest() throws AbsTTException {
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        try {
            lock.getWritePermission(wtx.getNode().getNodeKey(), (SynchWriteTransaction)wtx);
        } catch (Exception e) {
            TestCase.fail();
        }
        wtx.close();
    }

    @Ignore
    @Test
    /**
     * Locking an available subtree with other wtx holding locks on different subtrees
     */
    public void permitLockingInFreeSubtreeTest() throws AbsTTException {
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        final IWriteTransaction wtx2 = holder.session.beginWriteTransaction();

        LockManager lock = LockManager.getLockManager();
        try {
            lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx);
            lock.getWritePermission(nodes[7], (SynchWriteTransaction)wtx2);
        } catch (Exception e) {
            TestCase.fail();
        }
    }

    @Ignore
    @Test
    /**
     * Trying to lock a subtree blocked by a foreign transaction root node has to fail
     */
    public void denyLockingOnForeignTrnTest() throws AbsTTException {
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        final IWriteTransaction wtx2 = holder.session.beginWriteTransaction();

        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(wtx.getNode().getNodeKey(), (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(wtx2.getNode().getNodeKey(), (SynchWriteTransaction)wtx2);
            TestCase.fail();
        } catch (Exception e) {
            TestCase.assertTrue(true); // has to fail
        }
    }

    @Ignore
    @Test
    /**
     * Trying to lock a subtree which is part of a blocked subtree (has parent which is trn 
     * of a foreign transaction) has to fail
     */
    public void denyLockingUnderForeignTrnTest() throws AbsTTException {
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        final IWriteTransaction wtx2 = holder.session.beginWriteTransaction();
        ;
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx2);
            TestCase.fail();
        } catch (Exception e) {
            TestCase.assertTrue(true); // has to fail
        }
    }

    @Ignore
    @Test
    /**
     * Trying to lock a subtree which would contain a blocked subtree (has ancestor which is trn
     * of a foreign transaction) has to fail
     */
    public void denyLockingAboveForeignTrnTest() throws AbsTTException {
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        final IWriteTransaction wtx2 = holder.session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx2);
            TestCase.fail();
        } catch (Exception e) {
            TestCase.assertTrue(true); // has to fail
        }
    }

    @Ignore
    @Test
    /**
     * Locking a subtree which would contain one or more subtrees previously locked by the same
     * transaction is permitted
     */
    public void permitLockingAboveMultipleOwnTrnTest() throws AbsTTException {
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[4], (SynchWriteTransaction)wtx);
        lock.getWritePermission(nodes[7], (SynchWriteTransaction)wtx);
        lock.getWritePermission(nodes[10], (SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[0], (SynchWriteTransaction)wtx);
        } catch (Exception e) {
            TestCase.fail();
        }
    }

    @Ignore
    @Test
    /**
     * Locking a subtree which has been blocked and afterwards released by a foreign
     * transaction is possible
     */
    public void conquerReleasedSubtreeTest() throws AbsTTException {
        final IWriteTransaction wtx = holder.session.beginWriteTransaction();
        final IWriteTransaction wtx2 = holder.session.beginWriteTransaction();
        LockManager lock = LockManager.getLockManager();
        lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx);
        lock.releaseWritePermission((SynchWriteTransaction)wtx);
        try {
            lock.getWritePermission(nodes[1], (SynchWriteTransaction)wtx2);
        } catch (Exception e) {
            TestCase.fail();
        }
    }

}
