package org.treetank.access.commit;

import org.treetank.bucket.MetaBucket;
import org.treetank.bucket.RevisionRootBucket;
import org.treetank.bucket.UberBucket;
import org.treetank.io.IBackendWriter;
import org.treetank.log.LRULog;

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

    private UberBucket mUber;

    private MetaBucket mMeta;

    private RevisionRootBucket mRev;

    public CommitStrategyModule setmLog(LRULog mLog) {
        this.mLog = mLog;
        return this;
    }

    public CommitStrategyModule setmWriter(IBackendWriter mWriter) {
        this.mWriter = mWriter;
        return this;
    }

    public CommitStrategyModule setmUber(UberBucket mUber) {
        this.mUber = mUber;
        return this;
    }

    public CommitStrategyModule setmMeta(MetaBucket mMeta) {
        this.mMeta = mMeta;
        return this;
    }

    public CommitStrategyModule setmRev(RevisionRootBucket mRev) {
        this.mRev = mRev;
        return this;
    }

    @Override
    protected void configure() {
        bind(LRULog.class).annotatedWith(Names.named("pLog")).toInstance(mLog);
        bind(IBackendWriter.class).annotatedWith(Names.named("pWriter")).toInstance(mWriter);
        bind(UberBucket.class).annotatedWith(Names.named("pUber")).toInstance(mUber);
        bind(MetaBucket.class).annotatedWith(Names.named("pMeta")).toInstance(mMeta);
        bind(RevisionRootBucket.class).annotatedWith(Names.named("pRoot")).toInstance(mRev);
    }

}
