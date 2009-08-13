package com.treetank.service.versaKey;

/**
 * Implementing a key storage. The aim is to build a long-term key storage where
 * flexible handling of permissions should be the main focus.
 * 
 * That means that in the lifetime of the storage, permissions are bound to
 * different revisions. In the end, this results in the handling of two
 * behaviours regarding the changing of the user group:
 * <ul>
 * <li>Adding a new user</li>
 * <li>Removing an existing user</li>
 * </ul>
 * These two behaviours have to be adapted by the storage. The changes are bound
 * to the revisions of Treetank. That means that data with a given revision can
 * be read only regarding the revisions where the keys are actual valid.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IKeyStorage {

}
