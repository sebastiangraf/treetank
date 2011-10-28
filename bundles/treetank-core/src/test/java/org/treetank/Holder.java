package org.treetank;

import org.treetank.TestHelper.PATHS;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;

/**
 * Generating a standard resource within the {@link PATHS#PATH1} path. It also
 * generates a standard resource defined within {@link TestHelper#RESOURCE}.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Holder {

    private IDatabase mDatabase;

    private ISession mSession;

    private IReadTransaction mRtx;

    private IWriteTransaction mWtx;

    public static Holder generateSession() throws AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        database.createResource(new ResourceConfiguration.Builder(TestHelper.RESOURCE, PATHS.PATH1
            .getConfig()).build());
        final ISession session =
            database.getSession(new SessionConfiguration.Builder(TestHelper.RESOURCE).build());
        final Holder holder = new Holder();
        holder.setDatabase(database);
        holder.setSession(session);
        return holder;
    }

    public static Holder generateWtx() throws AbsTTException {
        final Holder holder = generateSession();
        final IWriteTransaction wtx = holder.mSession.beginWriteTransaction();
        holder.setWtx(wtx);
        return holder;
    }

    public static Holder generateRtx() throws AbsTTException {
        final Holder holder = generateSession();
        final IReadTransaction rtx = holder.mSession.beginReadTransaction();
        holder.setRtx(rtx);
        return holder;
    }

    public void close() throws AbsTTException {
        if (mRtx != null && !mRtx.isClosed()) {
            mRtx.close();
        }
        if (mWtx != null && !mWtx.isClosed()) {
            mWtx.abort();
            mWtx.close();
        }
        mSession.close();
    }

    public IDatabase getDatabase() {
        return mDatabase;
    }

    public ISession getSession() {
        return mSession;
    }

    public IReadTransaction getRtx() {
        return mRtx;
    }

    public IWriteTransaction getWtx() {
        return mWtx;
    }

    private void setWtx(final IWriteTransaction paramWtx) {
        this.mWtx = paramWtx;
    }

    private void setRtx(final IReadTransaction paramRtx) {
        this.mRtx = paramRtx;
    }

    private void setSession(final ISession paramSession) {
        this.mSession = paramSession;
    }

    private void setDatabase(final IDatabase paramDatabase) {
        this.mDatabase = paramDatabase;
    }

}
