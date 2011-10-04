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
package org.treetank.encryption.database;

import java.io.File;
import java.util.SortedMap;

import org.treetank.encryption.database.model.DAGSelector;
import org.treetank.exception.TTEncryptionException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.PrimaryIndex;

/**
 * Berkeley implementation of a persistent DAG database. That means that all
 * data of current dag is stored within this storage.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class CurrentDAGDatabase {

	/**
	 * Name for the database.
	 */
	private static final String NAME = "berkeleyDAGSelector";

	/**
	 * DB-Util for summarizing common access.
	 */
	private final DatabaseUtil mUtil;

	/**
	 * Constructor. Building up the berkeley db and setting necessary settings.
	 * 
	 * @param paramFile
	 *            the place where the berkeley db is stored.
	 */
	public CurrentDAGDatabase(final File paramFile) {
		mUtil = new DatabaseUtil(paramFile, NAME);
	}

	/**
	 * Clearing the database. That is removing all elements
	 * 
	 * @throws TTEncryptionException
	 */
	public final void clearPersistent() throws TTEncryptionException {
		mUtil.clearPersistent();
	}

	/**
	 * Putting a {@link DAGSelector} into the database.
	 * 
	 * @param paramEntity
	 *            key selector instance to put into database.
	 */
	public final void putEntry(final DAGSelector paramEntity) {
		PrimaryIndex<Long, DAGSelector> primaryIndex;
		try {
			primaryIndex = (PrimaryIndex<Long, DAGSelector>) mUtil.mStore
					.getPrimaryIndex(Long.class, DAGSelector.class);

			primaryIndex.put(paramEntity);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Getting a {@link DAGSelector} related to a given key.
	 * 
	 * @param paramKey
	 *            key for related dag selector instance.
	 * @return dag selector instance.
	 */
	public final DAGSelector getEntry(final long paramKey) {
		PrimaryIndex<Long, DAGSelector> primaryIndex;
		DAGSelector entity = null;
		try {
			primaryIndex = (PrimaryIndex<Long, DAGSelector>) mUtil.mStore
					.getPrimaryIndex(Long.class, DAGSelector.class);

			entity = (DAGSelector) primaryIndex.get(paramKey);

		} catch (final DatabaseException mDbExp) {
			mDbExp.printStackTrace();
		}
		return entity;
	}

	/**
	 * Deletes an entry from database.
	 * 
	 * @param paramKey
	 *            key to delete.
	 * @return status of deletion.
	 */
	public final boolean deleteEntry(final long paramKey) {
		PrimaryIndex<Long, DAGSelector> primaryIndex;
		boolean status = false;
		try {
			primaryIndex = (PrimaryIndex<Long, DAGSelector>) mUtil.mStore
					.getPrimaryIndex(Long.class, DAGSelector.class);

			status = primaryIndex.delete(paramKey);

		} catch (final DatabaseException mDbExp) {
			mDbExp.printStackTrace();
		}
		return status;
	}

	/**
	 * Returns number of database entries.
	 * 
	 * @return number of entries in database.
	 */
	public final int count() {
		PrimaryIndex<Long, DAGSelector> primaryIndex;
		long counter = 0;
		try {
			primaryIndex = (PrimaryIndex<Long, DAGSelector>) mUtil.mStore
					.getPrimaryIndex(Long.class, DAGSelector.class);
			counter = primaryIndex.count();

		} catch (final DatabaseException mDbExp) {
			mDbExp.printStackTrace();
		}
		return (int) counter;
	}

	/**
	 * Returns all database entries as {@link SortedMap}.
	 * 
	 * @return all database entries.
	 */
	public final SortedMap<Long, DAGSelector> getEntries() {
		PrimaryIndex<Long, DAGSelector> primaryIndex;
		SortedMap<Long, DAGSelector> sMap = null;
		try {
			primaryIndex = (PrimaryIndex<Long, DAGSelector>) mUtil.mStore
					.getPrimaryIndex(Long.class, DAGSelector.class);
			sMap = primaryIndex.sortedMap();

		} catch (final DatabaseException mDbExp) {
			mDbExp.printStackTrace();
		}
		return sMap;
	}

	public final boolean containsKey(final long paramKey) {
		PrimaryIndex<Long, DAGSelector> primaryIndex;
		boolean mContains = false;
		try {
			primaryIndex = (PrimaryIndex<Long, DAGSelector>) mUtil.mStore
					.getPrimaryIndex(Long.class, DAGSelector.class);
			mContains = primaryIndex.contains(paramKey);

		} catch (final DatabaseException mDbExp) {
			mDbExp.printStackTrace();
		}
		return mContains;

	}

}
