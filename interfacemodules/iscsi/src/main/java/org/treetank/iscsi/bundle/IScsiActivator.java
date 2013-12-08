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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.ReaderFactory;
import org.jscsi.target.TargetServer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.treetank.access.Storage;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
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

    private String[] revisionings = {
        "org.treetank.revisioning.Differential", "org.treetank.revisioning.FullDump",
        "org.treetank.revisioning.Incremental", "org.treetank.revisioning.SlidingSnapshot"
    };
    private String[] revisioningDescriptions = {
        "Differential versioning of the data", "Holding full data dumps with each version",
        "Incremental versioning of the data", "Versioning based on sliding snapshots"
    };

    private String[] backends = {
        "org.treetank.io.berkeley.BerkeleyStorage", "org.treetank.io.jclouds.JCloudsStorage",
        "org.treetank.io.combined.CombinedStorage"
    };
    private String[] backendDescriptions = {
        "Fast local backend using a berkeley database", "JClouds based backend",
        "Backend using both - berkeley and jclouds - for faster access"
    };

    private TargetServer targetServer;

    @Override
    public void start(BundleContext context) throws Exception {
        // Getting the correct network interface
        
        System.out.println("\nThis system provides more than one IP Address to advertise.\n");

        Enumeration<NetworkInterface> interfaceEnum = NetworkInterface.getNetworkInterfaces();
        NetworkInterface i;
        int addressCounter = 0;
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        while (interfaceEnum.hasMoreElements()) {
            i = interfaceEnum.nextElement();
            Enumeration<InetAddress> addressEnum = i.getInetAddresses();
            InetAddress address;

            while (addressEnum.hasMoreElements()) {
                address = addressEnum.nextElement();
                System.out.println("[" + addressCounter + "] " + address.getHostAddress());
                addresses.add(address);
                addressCounter++;
            }
        }

        /*
         * Getting the desired address from the command line.
         * You can't automatically make sure to always use the correct
         * host address.
         */
        System.out.print("\nWhich one should be used?\nType in the number: ");
        Integer chosenIndex = null;

        while (chosenIndex == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = br.readLine();
            try {
                chosenIndex = Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                chosenIndex = null;
            }
        }

        String targetAddress = addresses.get(chosenIndex).getHostAddress();
        System.out.println("Using ip address " + addresses.get(chosenIndex).getHostAddress());

        int backend = choose("Which backend type should be used?", backends, backendDescriptions);
        int revisioning =
            choose("Which revision type should be used?", revisionings, revisioningDescriptions);

        Class<? extends IBackend> backendClass;
        Class<? extends IRevisioning> revisioningClass;

        backendClass = (Class<? extends IBackend>)Class.forName(backends[backend]);
        revisioningClass =
            (Class<? extends IRevisioning>)Class.forName(revisionings[revisioning]);
        
        // Specifiying storage and config paths
        
        String storagePath = getString("\nSpecifiy a storage path on your system: ");
        String configPath = getString("\nSpecifiy the configuration path (iscsi configuration xml based on jscsi, default leave empty): ");
        String resourceName = getString("\nSpecifiy a resource name that is to be used for the storage: ");

        StorageConfiguration config;
        File storageFile = new File(storagePath);
        config = new StorageConfiguration(storageFile);

        // Choose config file
        File configFile = (configPath.equals("")) ? TreetankConfiguration.CONFIGURATION_CONFIG_FILE : new File(configPath);

        IOUtils.recursiveDelete(config.mFile);
        Storage.createStorage(config);

        // Guice Stuff for building the module
        final Injector injector =
            Guice.createInjector(new ModuleSetter().setDataFacClass(BlockDataElementFactory.class)
                .setMetaFacClass(ISCSIMetaPageFactory.class).setBackendClass(backendClass)
                .setRevisioningClass(revisioningClass).createModule());
        final IResourceConfigurationFactory resFac =
            injector.getInstance(IResourceConfigurationFactory.class);
        final Properties props = StandardSettings.getProps(config.mFile.getAbsolutePath(), resourceName);
        final ResourceConfiguration resConf = resFac.create(props);

        final IStorage db = Storage.openStorage(config.mFile);
        db.createResource(resConf);
        final ISession session = db.getSession(new SessionConfiguration(resourceName, StandardSettings.KEY));

        targetServer =
            new TargetServer(TreetankConfiguration.create(TreetankConfiguration.CONFIGURATION_SCHEMA_FILE,
                configFile, session, targetAddress));

        targetServer.call();
        
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Adapt to jscsi target..
        System.out.println("Staaph");
    }

    /**
     * 
     * @param pQuestion
     *            - Specific question asked for the elements to choose
     * @param pOptions
     *            - Options available
     * @param pDescriptions
     *            - Descriptions for each option
     * @return chosen index in form of an integer
     * @throws IOException
     */
    private int choose(String pQuestion, String[] pOptions, String[] pDescriptions) throws IOException {
        System.out.println(pQuestion);
        for (int i = 0; i < pOptions.length; i++) {
            System.out.println("[" + i + "] " + pOptions[i] + " \t\t -" + pDescriptions[i]);
        }

        System.out.print("\nYour answer: ");

        Integer chosenIndex = null;

        while (chosenIndex == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = br.readLine();
            try {
                chosenIndex = Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                chosenIndex = null;
            }
        }
        System.out.println();

        return chosenIndex;
    }

    /**
     * 
     * @param pQuestion
     *            - Specific question asked for the elements to choose
     * @param pOptions
     *            - Options available
     * @param pDescriptions
     *            - Descriptions for each option
     * @return chosen index in form of an integer
     * @throws IOException
     */
    private String getString(String pQuestion) throws IOException {
        System.out.print(pQuestion);
        String answer = null;
        while (answer == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            answer = br.readLine();
        }
        System.out.println();

        return answer;
    }

}
