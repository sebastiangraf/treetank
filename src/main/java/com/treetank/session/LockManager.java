package com.treetank.session;

import java.util.HashMap;
import java.util.HashSet;

/**
 * <h1>LockManager</h1>
 * 
 * <h2>Description</h2>
 * 
 * <p>Each <code>Session</code> owns a <code>LockManager</code> which keeps track of all 
 * <code>WriteTransaction</code> and their respective transaction root nodes. The <code>LockManager</code>
 * checks for a new <code>WriteTransaction</code> if the requested subtree is currently free for use. 
 * 
 */

public class LockManager {
	private HashMap<Long, Integer> lockedNodes;
	private HashSet<Long> transactionRootNodes;
	private static HashMap<SessionConfiguration, LockManager> lockManagers;
	
	/**
	 * 
	 * @param session
	 * 			The <code>Session</code> of the current <code>WriteTransaction</code>
	 * @return
	 * 			The <code>LockManager</code> corresponing to the <code>Session</code>
	 */
	public static synchronized LockManager getInstance(SessionConfiguration session){
		if(lockManagers == null){
			lockManagers = new HashMap(256);
		}
		if(lockManagers.containsKey(session)){
			return lockManagers.get(session);
		} else {
			lockManagers.put(session, new LockManager());
			return lockManagers.get(session);
		}
	}
	/**
	 * The actual LockManager with a HashMap of locked nodes and used transaction root nodes
	 */
	private LockManager(){
		this.lockedNodes = new HashMap(1000);
		this.transactionRootNodes = new HashSet(128);
	}
	/**
	 * Increments the lock count of the nodekey <code>key</code> by 1.
	 * Creates new entry in HashMap if the key is not present yet.
	 * 
	 * @param key
	 * 			The key of the node to lock
	 */
	public void lockKey(long key){
		if(lockedNodes.containsKey(key)){
			lockedNodes.put(key, lockedNodes.get(key) + 1);
		}
		else
		{
			lockedNodes.put(key, 1);
		}
	}
	/**
	 * Decrements the lock count of the nodekey <code>key</code> by 1.
	 * Removes the entry in HashMap if the lock count would become 0.
	 * 
	 * @param key
	 * 			The key of the node to unlock
	 */			
	public void unlockKey(long key){
		if(lockedNodes.containsKey(key)){
			if(lockedNodes.get(key) > 1){
				lockedNodes.put(key, lockedNodes.get(key) - 1);
				return;
			}
			else if(lockedNodes.get(key) == 1){
				lockedNodes.remove(key);
				return;
			}
		}
		throw new IllegalStateException("Key not in hashmap: " + key);
	}
	
	/**
	 * Checks if a given nodekey is locked
	 * 
	 * @param nodekey
	 * 			Key to check if locked
	 * @return
	 * 			True if key is locked, false otherwise
	 */
	public boolean islocked(long nodekey){
		if(this.lockedNodes.containsKey(nodekey)){
			return true;
		}
		return false;
	}
	/**
	 * Adds a nodekey to the list of transation root nodes
	 * 
	 * @param key
	 * 			Nodekey to add to the list
	 */
	public void registerTransactionRootNodeKey(long key){
		this.transactionRootNodes.add(key);
	}
	/**
	 * Removes a nodekey from the list of transaction root nodes
	 * 
	 * @param key
	 * 			Nodekey to remove from the list
	 */
	public void deregisterTransactionRootNodeKey(long key){
		this.transactionRootNodes.remove(key);
	}
	
	/**
	 * Checks if a nodekey belongs to a transaction root node
	 * 
	 * @param key
	 * 			Nodekey to check
	 * @return
	 * 			True if nodekey belongts to a transaction root node, false otherwise
	 */
	public boolean isTransactionRootNode(long key){
		if (transactionRootNodes.contains(key)){
			return true;
		} else {
			return false;
		}
	}
}
