/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All
 * rights reserved. Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met: * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. * Neither the name of
 * the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.iscsi.bundle;

import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jscsi.target.TargetServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.treetank.access.Storage;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.io.IBackend;
import org.treetank.io.IOUtils;
import org.treetank.iscsi.data.BlockDataElementFactory;
import org.treetank.iscsi.data.ISCSIMetaPageFactory;
import org.treetank.iscsi.jscsi.TreetankConfiguration;
import org.treetank.revisioning.IRevisioning;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * <h1>Treetank Iscsi Target</h1>
 * 
 * This bundle allows you to run iscsi targets within an osgi container using treetank.
 * 
 * @author Andreas Rain
 */
public class IScsiActivator implements BundleActivator {

    private ExecutorService runner;
    private TargetServer target;
    private ISession session;

    // Setting up default values for the target:
    private String publishingAddress = "127.0.0.1";
    private String backend = "org.treetank.io.jclouds.JCloudsStorage";
    private String revisioning = "org.treetank.revisioning.SlidingSnapshot";
    // This resolves to the user home folder in a special folder called .treetank
    private String storagePath = new StringBuilder().append(System.getProperty("user.home")).append(
        File.separator).append(".treetank").append(File.separator).toString();

    // Defining no more than one target
    private String targetName = "local-test:disk-1";
    private String size = "5.0";
    private String port = "3260";
    private String createDevice = "true";

    // Configupdate registration:
    private ServiceRegistration serviceReg;

    @Override
    public void start(BundleContext context) throws Exception {
        String s;

        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, "org.treetank.iscsi");

        // Initializing properties for the target server
        if ((s = context.getProperty("publishingAddress")) != null) {
            publishingAddress = s;
        } else {
            properties.put("publishingAddress", publishingAddress);
        }
        if ((s = context.getProperty("backend")) != null) {
            backend = s;
        } else {
            properties.put("backend", backend);
        }
        if ((s = context.getProperty("revisioning")) != null) {
            revisioning = s;
        } else {
            properties.put("revisioning", revisioning);
        }
        if ((s = context.getProperty("storagePath")) != null) {
            storagePath = s;
        } else {
            properties.put("storagePath", storagePath);
        }
        if ((s = context.getProperty("targetName")) != null) {
            targetName = s;
        } else {
            properties.put("targetName", targetName);
        }
        if ((s = context.getProperty("size")) != null) {
            size = s;
        } else {
            properties.put("size", size);
        }
        if ((s = context.getProperty("port")) != null) {
            port = s;
        } else {
            properties.put("port", port);
        }
        if ((s = context.getProperty("createDevice")) != null) {
            createDevice = s;
        } else {
            properties.put("createDevice", createDevice);
        }

        serviceReg = context.registerService(ManagedService.class.getName(), new ConfigUpdater(), properties);

        File file = new File(storagePath);
        StorageConfiguration config = new StorageConfiguration(file);

        // Determining whether to create the device
        if (createDevice.toLowerCase().equals("true") && !config.mFile.exists()) {
            IOUtils.recursiveDelete(config.mFile);
            Storage.createStorage(config);
        }

        final String resourceName = "bench53473ResourcegraveISCSI9284";

        // Guice Stuff for building the module
        @SuppressWarnings("unchecked")
        final Injector injector =
            Guice.createInjector(new ModuleSetter().setDataFacClass(BlockDataElementFactory.class)
                .setMetaFacClass(ISCSIMetaPageFactory.class).setBackendClass(
                    (Class<? extends IBackend>)Class.forName(backend)).setRevisioningClass(
                    (Class<? extends IRevisioning>)Class.forName(revisioning)).createModule());
        final IResourceConfigurationFactory resFac =
            injector.getInstance(IResourceConfigurationFactory.class);
        final Properties props = StandardSettings.getProps(storagePath, resourceName);
        final ResourceConfiguration resConf = resFac.create(props);

        final IStorage db = Storage.openStorage(config.mFile);
        db.createResource(resConf);
        session = db.getSession(new SessionConfiguration(resourceName, StandardSettings.KEY));

        target =
            new TargetServer(TreetankConfiguration.create(session, publishingAddress, port, targetName, size));

        runner = Executors.newSingleThreadExecutor();
        runner.submit(target);

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceReg.unregister();
        target.stop();
        runner.shutdown();

        while (!runner.isShutdown()) {
            // waiting for termination
        }
        if (session.close()) {
            System.out.println("Target successfully stopped.");
        }

    }

    /**
     * 
     * This simple configuration service is used to react to updates within the configuration.
     * 
     * @author Andreas Rain, University of Konstanz
     * 
     */
    private final class ConfigUpdater implements ManagedService {

        @Override
        public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
            if (properties == null) {
                return;
            }

            // TODO: recognize new targets and so on.

        }

    }

}
