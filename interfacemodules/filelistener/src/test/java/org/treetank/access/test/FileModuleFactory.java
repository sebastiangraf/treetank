package org.treetank.access.test;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.conf.GuiSettings;
import org.treetank.io.jclouds.JCloudsStorage;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Module Factory for initializing the modules in correct order depending on the
 * context. Main point for the orthogonal test setup.
 * 
 * @author Andreas Rain
 * 
 */
public class FileModuleFactory implements IModuleFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {

        AbstractModule returnVal;

        returnVal = new GuiSettings(JCloudsStorage.class);

        return returnVal;
    }
}
