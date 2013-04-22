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

package org.treetank.jscsi;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.jscsi.target.TargetServer;
import org.treetank.access.Storage;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.api.ISession;
import org.treetank.api.IStorage;
import org.treetank.node.ByteNodeFactory;
import org.treetank.node.ISCSIMetaPageFactory;

import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Start a target server that uses a treetank storage as storage device.
 * 
 * @author Andreas Rain
 * 
 */
public class TreetankTargetServer {

    /**
     * Starts the jSCSI target.
     * 
     * Argument one has to be an empty (or already created) storage folder!
     * Argument to can be additionally added if you don't want the default target configuration.
     * It has to be conform with the default target configuration schema file, which you can find
     * in the resources of the target.
     * 
     * @param args
     *            Argument 1 = the storage path
     *            Argument 2 = path to a customized target configuration xml file
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        StorageConfiguration config;
        File configFile;

        System.out.println("This system provides more than one IP Address to advertise.\n");
        
        Enumeration<NetworkInterface> interfaceEnum = NetworkInterface.getNetworkInterfaces();
        NetworkInterface i;
        int addressCounter = 0;
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        while(interfaceEnum.hasMoreElements()){
                i = interfaceEnum.nextElement();
                Enumeration<InetAddress> addressEnum = i.getInetAddresses();
                InetAddress address;
                
                while(addressEnum.hasMoreElements()){
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
        
        while(chosenIndex == null){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = br.readLine();
            try{
                chosenIndex = Integer.parseInt(line);
            }
            catch(NumberFormatException nfe){
                chosenIndex = null;
            }
        }
        
        String targetAddress = addresses.get(chosenIndex).getHostAddress();
        System.out.println("Using ip address " + addresses.get(chosenIndex).getHostAddress());

        switch (args.length) {
        case 0:
            config =
                new StorageConfiguration(new File(new StringBuilder(Files.createTempDir().getAbsolutePath())
                    .append(File.separator).append("tnk").append(File.separator).append("path1").toString()));
            configFile = TreetankConfiguration.CONFIGURATION_CONFIG_FILE;
            break;
        case 1:
            config = new StorageConfiguration(new File(args[0]));
            configFile = TreetankConfiguration.CONFIGURATION_CONFIG_FILE;
            break;
        case 2:
            config = new StorageConfiguration(new File(args[0]));
            configFile = new File(args[1]);
            break;
        default:
            throw new IllegalArgumentException(
                "Only zero or one Parameter (Path to Configuration-File) allowed!");
        }

//        Storage.truncateStorage(config);
        Storage.createStorage(config);

        // Guice Stuff for building the module
        final Injector injector =
            Guice.createInjector(new ModuleSetter().setNodeFacClass(ByteNodeFactory.class).setMetaFacClass(
                ISCSIMetaPageFactory.class).createModule());
        final IResourceConfigurationFactory resFac =
            injector.getInstance(IResourceConfigurationFactory.class);
        final Properties props = StandardSettings.getProps(config.mFile.getAbsolutePath(), "iscsi");
        final ResourceConfiguration resConf = resFac.create(props);
        
        final IStorage db = Storage.openStorage(config.mFile);
        db.createResource(resConf);
        final ISession session = db.getSession(new SessionConfiguration("iscsi", StandardSettings.KEY));

        TargetServer target =
            new TargetServer(TreetankConfiguration.create(TreetankConfiguration.CONFIGURATION_SCHEMA_FILE,
                configFile, session, targetAddress));

        target.call();
    }
}
