package org.treetank;

import org.treetank.api.INodeFactory;

import com.google.inject.AbstractModule;

public class DumpFactoryModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(INodeFactory.class).to(FactoriesForTest.class);
    }
}
