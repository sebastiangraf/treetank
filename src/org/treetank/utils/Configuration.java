/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.utils;

import java.util.Properties;

public final class Configuration {

  private final int blockSize;

  private final int blockExponent;

  private final int addressSize;

  private final int addressExponent;

  private final String checksumAlgorithm;

  private final String encryptionKey;

  private final String encryptionAlgorithm;

  private final int cacheSize;

  private final String stringEncoding;

  private final String pathfinderScript;

  private final static Configuration config;

  static {
    config = new Configuration();
  }

  private Configuration() {
    Properties properties = new Properties();
    try {
      properties.load(ClassLoader
          .getSystemResourceAsStream("treetank.properties"));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
    blockSize = Integer.parseInt(properties.getProperty("block_size"));
    blockExponent = Integer.parseInt(properties.getProperty("block_exponent"));
    addressSize = Integer.parseInt(properties.getProperty("address_size"));
    addressExponent =
        Integer.parseInt(properties.getProperty("address_exponent"));
    checksumAlgorithm = properties.getProperty("checksum_algorithm");
    encryptionKey = properties.getProperty("encryption_key");
    encryptionAlgorithm = properties.getProperty("encryption_algorithm");
    cacheSize = Integer.parseInt(properties.getProperty("cache_size"));
    stringEncoding = properties.getProperty("string_encoding");
    pathfinderScript = properties.getProperty("pathfinder_script");
  }

  public static final int getBlockSize() {
    return config.blockSize;
  }

  public static final int getBlockExponent() {
    return config.blockExponent;

  }

  public static final int getAddressSize() {
    return config.addressSize;
  }

  public static final int getAddressExponent() {
    return config.addressExponent;
  }

  public static final String getChecksumAlgorithm() {
    return config.checksumAlgorithm;
  }

  public static final byte[] getEncryptionKey() {
    return config.encryptionKey.getBytes();
  }

  public static final String getEncryptionAlgorithm() {
    return config.encryptionAlgorithm;
  }

  public static final int getCacheSize() {
    return config.cacheSize;
  }

  public static final String getStringEncoding() {
    return config.stringEncoding;
  }

  public static final String getPathfinderScript() {
    return config.pathfinderScript;
  }

}
