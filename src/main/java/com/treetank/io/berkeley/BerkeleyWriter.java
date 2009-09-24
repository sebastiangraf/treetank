package com.treetank.io.berkeley;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.treetank.io.IWriter;
import com.treetank.io.StorageProperties;
import com.treetank.page.AbstractPage;
import com.treetank.page.PageReference;
import com.treetank.page.UberPage;

public class BerkeleyWriter implements IWriter {

	private final Database mDatabase;

	private Transaction mTxn;

	private final BerkeleyReader reader;

	public BerkeleyWriter(final Environment env, final Database database) {

		try {
			mTxn = env.beginTransaction(null, null);
			mDatabase = database;
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}

		reader = new BerkeleyReader(env, database, mTxn);
	}

	@Override
	public void close() {
		try {
			mTxn.commit();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void write(PageReference<? extends AbstractPage> pageReference) {
		final AbstractPage page = pageReference.getPage();

		final DatabaseEntry valueEntry = new DatabaseEntry();
		final DatabaseEntry keyEntry = new DatabaseEntry();

		final BerkeleyKey key = new BerkeleyKey(getLastNodePage() + 1);

		BerkeleyFactory.PAGE_VAL_B.objectToEntry(page, valueEntry);
		BerkeleyFactory.KEY.objectToEntry(key, keyEntry);

		try {
			final OperationStatus status = mDatabase.put(mTxn, keyEntry,
					valueEntry);
			if (status != OperationStatus.SUCCESS) {
				System.err.println("Received status " + status);
				throw new RuntimeException();
			}
			// printoutProps();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		setLastNodePage(key.getIdentifier());
		pageReference.setKey(key);

	}

	private final void setLastNodePage(final Long data) {
		final DatabaseEntry keyEntry = new DatabaseEntry();
		final DatabaseEntry valueEntry = new DatabaseEntry();

		final BerkeleyKey key = BerkeleyKey.getDataInfoKey();
		BerkeleyFactory.KEY.objectToEntry(key, keyEntry);
		BerkeleyFactory.DATAINFO_VAL_B.objectToEntry(data, valueEntry);
		try {
			mDatabase.put(mTxn, keyEntry, valueEntry);

		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	private final long getLastNodePage() {
		final DatabaseEntry keyEntry = new DatabaseEntry();
		final DatabaseEntry valueEntry = new DatabaseEntry();

		final BerkeleyKey key = BerkeleyKey.getDataInfoKey();
		BerkeleyFactory.KEY.objectToEntry(key, keyEntry);

		try {
			final OperationStatus status = mDatabase.get(mTxn, keyEntry,
					valueEntry, LockMode.DEFAULT);
			Long val;
			if (status == OperationStatus.SUCCESS) {
				val = BerkeleyFactory.DATAINFO_VAL_B.entryToObject(valueEntry);
			} else {
				val = 0l;
			}
			return val;
		} catch (final DatabaseException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void initializingStorage(final StorageProperties props) {
		final DatabaseEntry valueEntry = new DatabaseEntry();
		final DatabaseEntry keyEntry = new DatabaseEntry();

		BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getPropsKey(), keyEntry);
		BerkeleyFactory.PROPS_VAL_B.objectToEntry(props, valueEntry);

		try {
			mDatabase.put(mTxn, keyEntry, valueEntry);
			// printoutProps();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void writeBeacon(PageReference<UberPage> pageReference) {

		final DatabaseEntry keyEntry = new DatabaseEntry();
		BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getFirstRevKey(),
				keyEntry);

		final DatabaseEntry valueEntry = new DatabaseEntry();
		BerkeleyFactory.FIRST_REV_VAL_B
				.objectToEntry(pageReference, valueEntry);

		try {
			mDatabase.put(mTxn, keyEntry, valueEntry);
			// printoutProps();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public StorageProperties getProps() {
		return reader.getProps();
	}

	@Override
	public AbstractPage read(PageReference<? extends AbstractPage> pageReference) {
		return reader.read(pageReference);
	}

	@Override
	public PageReference<?> readFirstReference() {
		return reader.readFirstReference();
	}

	// protected void printoutProps() throws DatabaseException {
	// // DEBUGGING CODE
	// final DatabaseEntry keyEntry2 = new DatabaseEntry();
	// BerkeleyFactory.KEY.objectToEntry(BerkeleyKey.getPropsKey(), keyEntry2);
	//
	// final DatabaseEntry valueEntry2 = new DatabaseEntry();
	// final OperationStatus status = mDatabase.get(mTxn, keyEntry2,
	// valueEntry2, LockMode.DEFAULT);
	// if (status == OperationStatus.SUCCESS) {
	// final StorageProperties props2 = BerkeleyFactory.PROPS_VAL_B
	// .entryToObject(valueEntry2);
	// System.out.println(props2);
	// }
	// }
}
