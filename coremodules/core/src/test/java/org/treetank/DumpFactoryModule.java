package org.treetank;

import org.treetank.api.INodeFactory;
import org.treetank.io.IStorage;
import org.treetank.io.IStorageFactory;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.file.FileStorage;
import org.treetank.page.DumbNodeFactory;
import org.treetank.revisioning.FullDump;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class DumpFactoryModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(IRevisioning.class).to(FullDump.class);

        bind(INodeFactory.class).to(DumbNodeFactory.class);
        bind(IByteHandler.class).to(Zipper.class);

        install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
            IStorageFactory.class));

    }
}
