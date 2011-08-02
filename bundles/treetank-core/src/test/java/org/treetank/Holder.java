package org.treetank;

import org.treetank.TestHelper.PATHS;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTUsageException;

public class Holder {

    public final IDatabase database;

    public final ISession session;

    public final IReadTransaction rtx;

    private Holder(final IDatabase database, final ISession session, final IReadTransaction rtx) {
        this.database = database;
        this.session = session;
        this.rtx = rtx;
    }

    public static Holder generate() throws TTUsageException, AbsTTException {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession(new SessionConfiguration.Builder());
        final IReadTransaction rtx = session.beginReadTransaction();
        return new Holder(database, session, rtx);
    }

    public void close() throws AbsTTException {
        rtx.close();
        session.close();
    }

}
