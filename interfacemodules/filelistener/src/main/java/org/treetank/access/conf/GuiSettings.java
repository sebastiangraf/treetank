package org.treetank.access.conf;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration.ISessionConfigurationFactory;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.api.INodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;
import org.treetank.io.IBackend;
import org.treetank.io.IBackend.IBackendFactory;
import org.treetank.io.bytepipe.ByteHandlerPipeline;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.page.DumbNodeFactory;
import org.treetank.revisioning.Differential;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class GuiSettings extends AbstractModule {

    private static byte[] keyValue = new byte[] {
        'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k'
    };
    public static final Key KEY;
    static {
        KEY = new SecretKeySpec(keyValue, "AES");
    }

    private final Class backend;

    public GuiSettings(Class backend) {
        this.backend = backend;
    }

    @Override
    protected void configure() {
        bind(INodeFactory.class).to(DumbNodeFactory.class);
        bind(IRevisioning.class).to(Differential.class);
        bind(IMetaEntryFactory.class).to(FilelistenerMetaPageFactory.class);
        bind(IByteHandlerPipeline.class).toInstance(new ByteHandlerPipeline(new Zipper()));
        install(new FactoryModuleBuilder().implement(IBackend.class, backend).build(IBackendFactory.class));
        install(new FactoryModuleBuilder().build(IResourceConfigurationFactory.class));
        bind(Key.class).toInstance(KEY);
        install(new FactoryModuleBuilder().build(ISessionConfigurationFactory.class));
    }

}
