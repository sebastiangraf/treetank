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

package org.jscsi.target;

import java.io.File;
import java.io.IOException;

import org.treetank.access.conf.StorageConfiguration;

import com.google.common.io.Files;

public class TreetankTargetServer {
  
  /**
   * Starts the jSCSI target.
   * 
   * Argument one has to be an empty (or already created) storage folder!
   * Argument to can be additionally added if you don't want the default target configuration.
   * It has to be conform with the default target configuration schema file, which you can find
   * in the resources of the target.
   * @param args
   *          Argument 1 = the storage path
   *          Argument 2 = path to a customized target configuration xml file
   * @throws IOException
   */
  public static void main(String[] args) throws Exception {

    TargetServer target;
    File file;
    StorageConfiguration configuration;
    switch (args.length) {
      case 0:
        file = new File(new StringBuilder(Files.createTempDir()
            .getAbsolutePath()).append(File.separator).append("tnk")
            .append(File.separator).append("path1").toString());
        configuration = new StorageConfiguration(file);
        target = new TargetServer(TreetankConfiguration.create(
            TreetankConfiguration.CONFIGURATION_SCHEMA_FILE,
            TreetankConfiguration.CONFIGURATION_CONFIG_FILE, file,
            configuration, 512));
        break;
      case 1:
        file = new File(args[0]);
        configuration = new StorageConfiguration(file);
        target = new TargetServer(TreetankConfiguration.create(
            TreetankConfiguration.CONFIGURATION_SCHEMA_FILE,
            TreetankConfiguration.CONFIGURATION_CONFIG_FILE, file,
            configuration, 512));

        break;
      case 2:
        file = new File(args[0]);
        configuration = new StorageConfiguration(file);
        target = new TargetServer(TreetankConfiguration.create(
            TreetankConfiguration.CONFIGURATION_SCHEMA_FILE,
            new File(args[1]), file,
            configuration, 512));
        break;
      default:
        throw new IllegalArgumentException(
            "Only zero or one Parameter (Path to Configuration-File) allowed!");
    }
    
    target.call();
  }
}
