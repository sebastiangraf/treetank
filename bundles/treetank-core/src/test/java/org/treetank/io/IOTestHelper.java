/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Map;
import java.util.Properties;


import org.treetank.TestHelper;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.SessionConfiguration;
import org.treetank.exception.AbsTTException;
import org.treetank.exception.TTUsageException;
import org.treetank.io.AbsIOFactory;
import org.treetank.io.IReader;
import org.treetank.io.IWriter;
import org.treetank.io.AbsIOFactory.StorageType;
import org.treetank.page.PageReference;
import org.treetank.page.UberPage;
import org.treetank.settings.EDatabaseSetting;

/**
 * Helper class for testing the io interfaces
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class IOTestHelper {

    private IOTestHelper() {
    }

    /**
     * Static method to get {@link DatabaseConfiguration}
     * 
     * @param type
     *            for the the {@link DatabaseConfiguration} should be generated
     * @return a suitable {@link DatabaseConfiguration}
     * @throws TTUsageException
     */
    public static DatabaseConfiguration createDBConf(final StorageType type) throws TTUsageException {
        final Properties props = new Properties();
        props.setProperty(EDatabaseSetting.STORAGE_TYPE.name(), type.name());
        return new DatabaseConfiguration(TestHelper.PATHS.PATH1.getFile(), props);
    }

    /**
     * Static method to get {@link SessionConfiguration}
     * 
     * @return a suitable {@link SessionConfiguration}
     * @throws TTUsageException
     */
    public static SessionConfiguration createSessionConf() throws TTUsageException {
        return new SessionConfiguration();
    }

    /**
     * Tear down for all tests related to the io layer.
     */
    public static void clean() throws AbsTTException {

        final Map<SessionConfiguration, AbsIOFactory> mapping = AbsIOFactory.getActiveFactories();
        for (final SessionConfiguration conf : mapping.keySet()) {
            final AbsIOFactory fac = mapping.get(conf);

            // Closing all storages
            fac.closeStorage();
            TestHelper.deleteEverything();
        }

    }

    /**
     * Testing the get and remove in the Factory methods.
     * 
     * @param dbConf
     *            to be tested
     * @param sessionConf
     *            to be tested
     */
    public static void testFactory(final DatabaseConfiguration dbConf, final SessionConfiguration sessionConf)
        throws AbsTTException {
        final AbsIOFactory fac1 = AbsIOFactory.getInstance(dbConf, sessionConf);
        final AbsIOFactory fac2 = AbsIOFactory.getInstance(dbConf, sessionConf);
        assertSame(fac1, fac2);
        fac1.closeStorage();
        final AbsIOFactory fac3 = AbsIOFactory.getInstance(dbConf, sessionConf);
        assertNotSame(fac1, fac3);
        fac3.closeStorage();
    }

    public static void testReadWriteFirstRef(final DatabaseConfiguration dbConf,
        final SessionConfiguration sessionConf) throws AbsTTException {
        final AbsIOFactory fac = AbsIOFactory.getInstance(dbConf, sessionConf);
        final PageReference pageRef1 = new PageReference();
        final UberPage page1 = new UberPage();
        pageRef1.setPage(page1);

        // same instance check
        final IWriter writer = fac.getWriter();
        writer.writeFirstReference(pageRef1);
        final PageReference pageRef2 = writer.readFirstReference();
        assertEquals(pageRef1.getNodePageKey(), pageRef2.getNodePageKey());
        assertEquals(((UberPage)pageRef1.getPage()).getRevisionCount(), ((UberPage)pageRef2.getPage())
            .getRevisionCount());
        writer.close();

        // new instance check
        final IReader reader = fac.getReader();
        final PageReference pageRef3 = reader.readFirstReference();
        assertEquals(pageRef1.getNodePageKey(), pageRef3.getNodePageKey());
        assertEquals(((UberPage)pageRef1.getPage()).getRevisionCount(), ((UberPage)pageRef3.getPage())
            .getRevisionCount());
        reader.close();

    }

    // public static void testReadWriteNodePageStructure(
    // final SessionConfiguration conf) throws TreetankException {
    // final AbstractIOFactory fac = AbstractIOFactory.getInstance(conf);
    // final PageReference pageRef1 = new PageReference();
    // final UberPage page1 = new UberPage();
    // pageRef1.setPage(page1);
    //
    // }
}