package org.treetank.access.commit;

import org.treetank.api.ISession;
import org.treetank.io.IBackendWriter;
import org.treetank.log.LRULog;
import org.treetank.page.MetaPage;
import org.treetank.page.RevisionRootPage;
import org.treetank.page.UberPage;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * 
 * @author Andreas Rain, University of Konstanz
 *
 */
public class CommitStrategyModule extends AbstractModule {

    private LRULog mLog;

    private IBackendWriter mWriter;

    private UberPage mUber;

    private MetaPage mMeta;

    private RevisionRootPage mRev;

    public CommitStrategyModule setmLog(LRULog mLog) {
        this.mLog = mLog;
        return this;
    }

    public CommitStrategyModule setmWriter(IBackendWriter mWriter) {
        this.mWriter = mWriter;
        return this;
    }

    public CommitStrategyModule setmUber(UberPage mUber) {
        this.mUber = mUber;
        return this;
    }

    public CommitStrategyModule setmMeta(MetaPage mMeta) {
        this.mMeta = mMeta;
        return this;
    }

    public CommitStrategyModule setmRev(RevisionRootPage mRev) {
        this.mRev = mRev;
        return this;
    }

    @Override
    protected void configure() {
        bind(LRULog.class).annotatedWith(Names.named("pLog")).toInstance(mLog);
        bind(IBackendWriter.class).annotatedWith(Names.named("pWriter")).toInstance(mWriter);
        bind(UberPage.class).annotatedWith(Names.named("pUber")).toInstance(mUber);
        bind(MetaPage.class).annotatedWith(Names.named("pMeta")).toInstance(mMeta);
        bind(RevisionRootPage.class).annotatedWith(Names.named("pRoot")).toInstance(mRev);
    }

}
