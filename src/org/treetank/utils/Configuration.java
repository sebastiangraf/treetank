/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright (C) 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id$
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
