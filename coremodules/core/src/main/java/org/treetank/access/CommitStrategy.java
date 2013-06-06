/**
 * 
 */
package org.treetank.access;

import java.util.Iterator;

import org.treetank.exception.TTException;
import org.treetank.io.IBackendWriter;
import org.treetank.log.LRULog;
import org.treetank.log.LogValue;
import org.treetank.page.MetaPage;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface CommitStrategy {

    public void execute() throws TTException;

    public static class BlockingCommit implements CommitStrategy {

        private final LRULog mLog;

        private final IBackendWriter mWriter;

        private final UberPage mUber;

        private final MetaPage mMeta;

        private final RevisionRootPage mRev;

        public BlockingCommit(final LRULog pLog, final IBackendWriter pWriter, final RevisionRootPage pRoot,
            final MetaPage pMeta, final UberPage pUber) {
            mLog = pLog;
            mWriter = pWriter;
            mUber = pUber;
            mRev = pRoot;
            mMeta = pMeta;
        }

        /**
         * {@inheritDoc}
         * 
         * @throws TTException
         */
        @Override
        public void execute() throws TTException {

            Iterator<LogValue> entries = mLog.getIterator();
            while (entries.hasNext()) {
                LogValue next = entries.next();
                mWriter.write(next.getModified());
            }
            mWriter.write(mMeta);
            mWriter.write(mRev);
            mWriter.writeUberPage(mUber);

        }

    }

}
