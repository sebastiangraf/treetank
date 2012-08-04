package org.treetank.io;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.api.INodeFactory;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler;
import org.treetank.io.bytepipe.Zipper;
import org.treetank.io.file.FileStorage;
import org.treetank.page.DumbNodeFactory;
import org.treetank.revisioning.FullDump;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ModuleFactory implements IModuleFactory {

    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {

        AbstractModule returnVal = new AbstractModule() {

            @Override
            protected void configure() {
                bind(IRevisioning.class).to(FullDump.class);

                bind(INodeFactory.class).to(DumbNodeFactory.class);
                bind(IByteHandler.class).to(Zipper.class);

                install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
                    IStorageFactory.class));

            }
        };
        String suiteName = context.getSuite().getName();
        if ("FileZipper".equals(suiteName)) {
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(IRevisioning.class).to(FullDump.class);

                    bind(INodeFactory.class).to(DumbNodeFactory.class);
                    bind(IByteHandler.class).to(Zipper.class);

                    install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
                        IStorageFactory.class));

                }
            };
        }

        if ("FileEncryptor".equals(suiteName)) {
            returnVal = new AbstractModule() {

                @Override
                protected void configure() {
                    bind(IRevisioning.class).to(FullDump.class);

                    bind(INodeFactory.class).to(DumbNodeFactory.class);

                    bind(IByteHandler.class).to(Encryptor.class);

                    install(new FactoryModuleBuilder().implement(IStorage.class, FileStorage.class).build(
                        IStorageFactory.class));

                }
            };
        }

        return returnVal;
    }
}
