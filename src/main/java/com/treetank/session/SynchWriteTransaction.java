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
 * $Id: WriteTransaction.java 4417 2008-08-27 21:19:26Z scherer $
 */

package com.treetank.session;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.treetank.api.IItem;
import com.treetank.api.IWriteTransaction;
import com.treetank.node.AbstractNode;
import com.treetank.node.AttributeNode;
import com.treetank.node.ElementNode;
import com.treetank.node.NamespaceNode;
import com.treetank.page.UberPage;
import com.treetank.utils.TypedValue;

/**
 * <h1>WriteTransaction</h1>
 * 
 * <p>
 * Allowes for concurrent WriteTransactions in one session.
 * </p>
 */
public class SynchWriteTransaction extends ReadTransaction implements
		IWriteTransaction {

	/** Maximum number of node modifications before auto commit. */
	private final int mMaxNodeCount;

	/** Scheduler to commit after mMaxTime seconds. */
	private ScheduledExecutorService mCommitScheduler;

	/** Modification counter. */
	private long mModificationCount;
	
	private final Set<Long> registeredNodePageKeys;
	
	/** Highest node in node tree ever used in this transaction. */
	private long transactionRootnodeKey;
	
	/** Lockmanager of the session this WriteTransaction is bound to */
	private LockManager lock;

	/**
	 * 	 * Constructor.
	 * 
	 * @param transactionID
	 *            ID of transaction.
	 * @param sessionState
	 *            State of the session.
	 * @param transactionState
	 *            State of this transaction.
	 * @param maxNodeCount
	 *            Maximum number of node modifications before auto commit.
	 * @param maxTime
	 *            Maximum number of seconds before auto commit.
	 * @param transactionRootNodekey
	 * 			 Key value of highest node used in this WriteTransaction
	 */
	protected SynchWriteTransaction(final long transactionID,
			final SessionState sessionState,
			final WriteTransactionState transactionState,
			final int maxNodeCount, final int maxTime, long transactionRootNodekey) {
		super(transactionID, sessionState, transactionState);

		this.transactionRootnodeKey = transactionRootNodekey;
		this.lock = LockManager.getInstance(sessionState.getSessionConfiguration());
		// After locking ancestors of trn the current position in the tree will be on trn
		lockAncestors();
		
		registeredNodePageKeys = new TreeSet<Long>();
		
		// Do not accept negative values.
		if ((maxNodeCount < 0) || (maxTime < 0)) {
			throw new IllegalArgumentException(
					"Negative arguments are not accepted.");
		}

		// Only auto commit by node modifications if it is more then 0.
		mMaxNodeCount = maxNodeCount;
		mModificationCount = 0L;

		// Only auto commit by time if the time is more than 0 seconds.
		if (maxTime > 0) {
			mCommitScheduler = Executors.newScheduledThreadPool(1);
			mCommitScheduler.scheduleAtFixedRate(new Runnable() {
				public final void run() {
					if (mModificationCount > 0) {
						commit();
					}
				}
			}, 0, maxTime, TimeUnit.SECONDS);
		} else {
			mCommitScheduler = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertElementAsFirstChild(final String name,
			final String uri) {
		return insertFirstChild(((WriteTransactionState) getTransactionState())
				.createElementNode(getCurrentNode().getNodeKey(),
						NULL_NODE_KEY, NULL_NODE_KEY, getCurrentNode()
								.getFirstChildKey(),
						((WriteTransactionState) getTransactionState())
								.createNameKey(name),
						((WriteTransactionState) getTransactionState())
								.createNameKey(uri),
						((WriteTransactionState) getTransactionState())
								.createNameKey("xs:untyped")));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertTextAsFirstChild(final int valueType,
			final byte[] value) {
		return insertFirstChild(((WriteTransactionState) getTransactionState())
				.createTextNode(getCurrentNode().getNodeKey(), NULL_NODE_KEY,
						getCurrentNode().getFirstChildKey(), valueType, value));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertTextAsFirstChild(final String value) {
		return insertTextAsFirstChild(
				((WriteTransactionState) getTransactionState())
						.createNameKey("xs:untyped"), TypedValue
						.getBytes(value));
	}

		/**
		 * {@inheritDoc}
		 */
	public final synchronized long insertElementAsRightSibling(
			final String name, final String uri) {
		
		if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
			throw new IllegalStateException("Creating siblings in transation root node prohibited");
		}
		return insertRightSibling(((WriteTransactionState) getTransactionState())
				.createElementNode(getCurrentNode().getParentKey(),
						NULL_NODE_KEY, getCurrentNode().getNodeKey(),
						getCurrentNode().getRightSiblingKey(),
						((WriteTransactionState) getTransactionState())
								.createNameKey(name),
						((WriteTransactionState) getTransactionState())
								.createNameKey(uri),
						((WriteTransactionState) getTransactionState())
								.createNameKey("xs:untyped")));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertTextAsRightSibling(
			final int valueType, final byte[] value) {
		if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
			throw new IllegalStateException("Creating siblings in transation root node prohibited");
		}
		return insertRightSibling(((WriteTransactionState) getTransactionState())
				.createTextNode(getCurrentNode().getParentKey(),
						getCurrentNode().getNodeKey(), getCurrentNode()
								.getRightSiblingKey(), valueType, value));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertTextAsRightSibling(final String value) {
		if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
			throw new IllegalStateException("Creating siblings in transation root node prohibited");
		}
		return insertTextAsRightSibling(
				((WriteTransactionState) getTransactionState())
						.createNameKey("xs:untyped"), TypedValue
						.getBytes(value));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertAttribute(final String name,
			final String uri, final int valueType, final byte[] value) {
		return insertAttribute(((WriteTransactionState) getTransactionState())
				.createAttributeNode(getCurrentNode().getNodeKey(),
						((WriteTransactionState) getTransactionState())
								.createNameKey(name),
						((WriteTransactionState) getTransactionState())
								.createNameKey(uri),
						((WriteTransactionState) getTransactionState())
								.createNameKey("xs:untypedAtomic"), value));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertAttribute(final String name,
			final String uri, final String value) {
		return insertAttribute(name, uri,
				((WriteTransactionState) getTransactionState())
						.createNameKey("xs:untypedAtomic"), TypedValue
						.getBytes(value));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized long insertNamespace(final String uri,
			final String prefix) {
		return insertNamespace(((WriteTransactionState) getTransactionState())
				.createNamespaceNode(getCurrentNode().getNodeKey(),
						((WriteTransactionState) getTransactionState())
								.createNameKey(uri),
						((WriteTransactionState) getTransactionState())
								.createNameKey(prefix)));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized void remove() {

		assertNotClosed();
		mModificationCount++;
		// Remember all related nodes.
		AbstractNode node = null;
		AbstractNode leftSibling = null;
		AbstractNode rightSibling = null;
		AbstractNode parent = null;

		node = (AbstractNode) getCurrentNode();

		if (getCurrentNode().isDocumentRoot()) {
			throw new IllegalStateException("Root node can not be removed.");
		} else if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
			throw new IllegalStateException("Transaction root node can not be removed.");
		}
		
		else if (getCurrentNode().isElement()) {

			node = (AbstractNode) getCurrentNode();
			if (node.hasLeftSibling()) {
				moveToLeftSibling();
				leftSibling = (AbstractNode) getCurrentNode();
				moveToRightSibling();
			}
			if (node.hasRightSibling()) {
				moveToRightSibling();
				rightSibling = (AbstractNode) getCurrentNode();
			}
			moveToParent();
			parent = (AbstractNode) getCurrentNode();

			// Remove old node.
			((WriteTransactionState) getTransactionState()).removeNode(node
					.getNodeKey());

			// Adapt left sibling node if there is one.
			if (leftSibling != null) {
				leftSibling = ((WriteTransactionState) getTransactionState())
						.prepareNodeForModification(leftSibling.getNodeKey());
				if (rightSibling != null) {
					leftSibling.setRightSiblingKey(rightSibling.getNodeKey());
				} else {
					leftSibling.setRightSiblingKey(NULL_NODE_KEY);
				}
			}

			// Adapt right sibling node if there is one.
			if (rightSibling != null) {
				rightSibling = ((WriteTransactionState) getTransactionState())
						.prepareNodeForModification(rightSibling.getNodeKey());
				if (leftSibling != null) {
					rightSibling.setLeftSiblingKey(leftSibling.getNodeKey());
				} else {
					rightSibling.setLeftSiblingKey(NULL_NODE_KEY);
				}
			}

			// Adapt parent.
			parent = ((WriteTransactionState) getTransactionState())
					.prepareNodeForModification(parent.getNodeKey());
			parent.decrementChildCount();
			if (parent.getFirstChildKey() == node.getNodeKey()) {
				if (rightSibling != null) {
					parent.setFirstChildKey(rightSibling.getNodeKey());
				} else {
					parent.setFirstChildKey(NULL_NODE_KEY);
				}
			}

			// Set current node.
			if (rightSibling != null) {
				setCurrentNode(rightSibling);
				return;
			}

			if (leftSibling != null) {
				setCurrentNode(leftSibling);
				return;
			}

			setCurrentNode(parent);
		} else if (getCurrentNode().isAttribute()) {
			moveToParent();

			parent = ((WriteTransactionState) getTransactionState())
					.prepareNodeForModification(getCurrentNode().getNodeKey());

			((ElementNode) parent).removeAttribute(node.getNodeKey());

		}

	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized void setName(final String name) {

		assertNotClosed();
		mModificationCount++;

		prepareCurrentNode().setNameKey(
				((WriteTransactionState) getTransactionState())
						.createNameKey(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized void setURI(final String uri) {

		assertNotClosed();
		mModificationCount++;

		prepareCurrentNode().setURIKey(
				((WriteTransactionState) getTransactionState())
						.createNameKey(uri));
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized void setValue(final int valueType,
			final byte[] value) {

		assertNotClosed();
		mModificationCount++;

		final AbstractNode node = prepareCurrentNode();
		node.setValue(valueType, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized void setValue(final String value) {
		setValue(((WriteTransactionState) getTransactionState())
				.createNameKey("xs:untyped"), TypedValue.getBytes(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final synchronized void close() {
		if (!isClosed()) {
			// Make sure to commit all dirty data.
			if (mModificationCount > 0) {
				commit();
			}
			unlockAncestors();
			// Make sure to cancel the periodic commit task if it was started.
			if (mCommitScheduler != null) {
				mCommitScheduler.shutdownNow();
				mCommitScheduler = null;
			}
			// Release all state immediately.
			getTransactionState().close();
			getSessionState().closeWriteTransaction(getTransactionID());
			setSessionState(null);
			setTransactionState(null);
			setCurrentNode(null);
			// Remember that we are closed.
			setClosed();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized void commit() {
		
		assertNotClosed();
		registeredNodePageKeys.clear();

		// Commit uber page.
		final UberPage uberPage = ((WriteTransactionState) getTransactionState())
				.commit(getSessionState().getSessionConfiguration());

		// Remember succesfully committed uber page in session state.
		getSessionState().setLastCommittedUberPage(uberPage);

		// Reset modification counter.
		mModificationCount = 0L;

		getTransactionState().close();

		// Reset internal transaction state to new uber page.
		setTransactionState(getSessionState().createWriteTransactionState());

	}

	/**
	 * {@inheritDoc}
	 */
	public final synchronized void abort() {

		assertNotClosed();

		// Reset modification counter.
		mModificationCount = 0L;

		// Reset internal transaction state to last committed uber page.
		setTransactionState(getSessionState().createWriteTransactionState());
	}

	private final void intermediateCommitIfRequired() {
		assertNotClosed();
		if ((mMaxNodeCount > 0) && (mModificationCount > mMaxNodeCount)) {
			commit();
		}
	}

	private final AbstractNode prepareCurrentNode() {
		final AbstractNode modNode = ((WriteTransactionState) getTransactionState())
				.prepareNodeForModification(getCurrentNode().getNodeKey());
		setCurrentNode(modNode);

		return modNode;
	}

	private final long insertFirstChild(final AbstractNode node) {

		assertNotClosed();
		mModificationCount++;
		intermediateCommitIfRequired();

		setCurrentNode(node);

		updateParentAfterInsert(true);
		updateRightSibling();

		return node.getNodeKey();
	}

	private final long insertRightSibling(final AbstractNode node) {

		assertNotClosed();
		mModificationCount++;
		intermediateCommitIfRequired();

		if (getCurrentNode().getNodeKey() == DOCUMENT_ROOT_KEY) {
			throw new IllegalStateException("Root node can not have siblings.");
		}
		if (getCurrentNode().getNodeKey() == transactionRootnodeKey){
			throw new IllegalStateException("Transaction root node can not have siblings");
		}

		setCurrentNode(node);

		updateParentAfterInsert(false);
		updateLeftSibling();
		updateRightSibling();

		return node.getNodeKey();
	}

	private final long insertAttribute(final AttributeNode node) {

		assertNotClosed();
		mModificationCount++;
		intermediateCommitIfRequired();

		if (!getCurrentNode().isElement()) {
			throw new IllegalStateException(
					"Only element nodes can have attributes.");
		}

		setCurrentNode(node);

		final WriteTransactionState state = (WriteTransactionState) getTransactionState();
		final AbstractNode parentNode = state.prepareNodeForModification(node
				.getParentKey());
		parentNode.insertAttribute(node.getNodeKey());
		return node.getNodeKey();
	}

	private final long insertNamespace(final NamespaceNode node) {

		assertNotClosed();
		mModificationCount++;
		intermediateCommitIfRequired();

		if (!getCurrentNode().isElement()) {
			throw new IllegalStateException(
					"Only element nodes can have attributes.");
		}

		setCurrentNode(node);

		final WriteTransactionState state = (WriteTransactionState) getTransactionState();
		final AbstractNode parentNode = state.prepareNodeForModification(node
				.getParentKey());
		parentNode.insertNamespace(node.getNodeKey());
		return node.getNodeKey();
	}

	private final void updateParentAfterInsert(final boolean updateFirstChild) {
		final WriteTransactionState state = (WriteTransactionState) getTransactionState();
		final AbstractNode parentNode = state
				.prepareNodeForModification(getCurrentNode().getParentKey());
		parentNode.incrementChildCount();
		if (updateFirstChild) {
			parentNode.setFirstChildKey(getCurrentNode().getNodeKey());
		}
		
		checkNodePageOverflow(WriteTransactionState.nodePageKey(parentNode.getNodeKey()));

	}

	private final void updateRightSibling() {
		if (getCurrentNode().hasRightSibling()) {
			final WriteTransactionState state = (WriteTransactionState) getTransactionState();
			final AbstractNode rightSiblingNode = state
					.prepareNodeForModification(getCurrentNode()
							.getRightSiblingKey());
			rightSiblingNode.setLeftSiblingKey(getCurrentNode().getNodeKey());
			
			checkNodePageOverflow(WriteTransactionState.nodePageKey(rightSiblingNode.getNodeKey()));
		}
	}

	private final void updateLeftSibling() {
		final WriteTransactionState state = (WriteTransactionState) getTransactionState();
		final AbstractNode leftSiblingNode = state
				.prepareNodeForModification(getCurrentNode()
						.getLeftSiblingKey());
		leftSiblingNode.setRightSiblingKey(getCurrentNode().getNodeKey());
		
		checkNodePageOverflow(WriteTransactionState.nodePageKey(leftSiblingNode.getNodeKey()));
	}

	private final void checkNodePageOverflow(final long nodePageKey){
		if (!(registeredNodePageKeys.contains(nodePageKey))) {
			registeredNodePageKeys.add(nodePageKey);
			mModificationCount++;
		}
	}

	
	private void lockAncestors(){
		super.moveTo(transactionRootnodeKey);
		
		//If trk is locked abort
		if(lock.islocked(transactionRootnodeKey)){
			throw new IllegalArgumentException(
			"Locking of subtree failed!");
		}
		//Find first locked parent node (if any). If this locked node is a trn abort.
		boolean foundLockedNode = false;
		while(super.moveTo(getCurrentNode().getParentKey())&&!foundLockedNode){
			if(this.lock.islocked(getCurrentNode().getNodeKey())){
				foundLockedNode = true;
				if(this.lock.isTransactionRootNode(getCurrentNode().getNodeKey())){
				throw new IllegalStateException (
						"Subtree already locked by another transaction.");
				}
			}
		}
		super.moveTo(transactionRootnodeKey);
		//register transactionRootnodeKey
		lock.registerTransactionRootNodeKey(transactionRootnodeKey);
		
		//lock trk and all its parents
		lock.lockKey(transactionRootnodeKey);
		if(super.moveToParent()){
			lock.lockKey(getCurrentNode().getNodeKey());
		}
		//reposition to starting node
		super.moveTo(transactionRootnodeKey);
	}
	
	private void unlockAncestors(){
		super.moveTo(transactionRootnodeKey);
		lock.deregisterTransactionRootNodeKey(transactionRootnodeKey);
		lock.unlockKey(getCurrentNode().getNodeKey());
		while(super.moveToParent()){
			lock.unlockKey(getCurrentNode().getNodeKey());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean moveTo(final long nodeKey) {
		assertNotClosed();
		if(nodeKey == NULL_NODE_KEY){
			return false;
		}
		if(isInTransactionSubtree(nodeKey)){
			return super.moveTo(nodeKey);
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean moveToRightSibling(){
		if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
			//Try to conquer node outside of current subtree
//			if(conquer(getCurrentNode().getRightSiblingKey())){
//				return super.moveTo(getCurrentNode().getRightSiblingKey());
//			} else {
				return false;
//			}
			
		} else {
			return super.moveTo(getCurrentNode().getRightSiblingKey());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean moveToLeftSibling(){
		if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
			return false;
		} else {
			return super.moveTo(getCurrentNode().getLeftSiblingKey());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean moveToParent(){
		if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
			return false;
		} else {
			return super.moveTo(getCurrentNode().getParentKey());
		}
	}
	
	private boolean isInTransactionSubtree(long nodeKey){
		long current = getCurrentNode().getNodeKey();
		if(current == transactionRootnodeKey){
			return true;
		}
		while(super.moveTo(getCurrentNode().getParentKey())){
			if(getCurrentNode().getNodeKey() == transactionRootnodeKey){
				super.moveTo(current);
				return true;
			}
		}
		super.moveTo(current);
		return false;		
	}
}
