package org.treetank;

import org.treetank.api.INodeFactory;
import org.treetank.page.DumbNodeFactory;

import com.google.inject.AbstractModule;

public class DumpFactoryModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(INodeFactory.class).to(DumbNodeFactory.class);
    }
}
