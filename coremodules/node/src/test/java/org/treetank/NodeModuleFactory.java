package org.treetank;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.StandardNodeSettings;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Module Factory for initializing the modules in correct order depending on the
 * context. Main point for the orthogonal test setup.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class NodeModuleFactory implements IModuleFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {

        AbstractModule returnVal = new StandardNodeSettings();

        return returnVal;
    }

}
