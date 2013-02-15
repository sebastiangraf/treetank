package org.treetank;

import org.testng.IModuleFactory;
import org.testng.ITestContext;
import org.treetank.access.StandardByteNodeSettings;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

/**
 * Module Factory for initializing the modules in correct order depending on the
 * context. Main point for the orthogonal test setup.
 * 
 * @author Andreas Rain
 * 
 */
public class ByteModuleFactory implements IModuleFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public Module createModule(ITestContext context, Class<?> testClass) {

        AbstractModule returnVal = new StandardByteNodeSettings();
        return returnVal;
    }

}
