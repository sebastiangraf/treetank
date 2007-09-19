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

/**
 * <h1>IConstants</h1>
 * 
 * <p>
 * Interface to hold all constants of the node layer. The node kinds
 * are equivalent to DOM node kinds for interoperability with saxon.
 * </p>
 */
public interface IConstants {

  //--- Varia ------------------------------------------------------------------

  /** Length of beacon. */
  public static final int BEACON_LENGTH = 20;

  /** Number of strong references in SoftHashMap. */
  public static final int STRONG_REFERENCE_COUNT = 512;

  /** Default internal encoding. */
  public static final String ENCODING = "UTF-8";

  /** Is encryption active? */
  public static final boolean ENCRYPT = true;

  /** Is compression active? */
  public static final boolean COMPRESS = true;

  /** Is checksumming active? */
  public static final boolean CHECKSUM = true;

  /** Encryption algorithm. */
  public static final String ENCRYPTION_ALGORITHM = "AES";

  /** Encryption key. */
  public static final byte[] ENCRYPTION_KEY = "1234567812345678".getBytes();

  /** Checksum algorithm. */
  public static final String CHECKSUM_ALGORITHM = "CRC";

  // --- Pages -----------------------------------------------------------------

  /** Uber page (UP). */
  public static final int UBER_PAGE = 0;

  /** Revision root page (RRP). */
  public static final int REVISION_ROOT_PAGE = 1;

  /** INode page (NDP). */
  public static final int NODE_PAGE = 2;

  /** Indirect page (IP). */
  public static final int INDIRECT_PAGE = 3;

  /** Name page (NMP). */
  public static final int NAME_PAGE = 4;

  //--- Indirect Page ----------------------------------------------------------

  /** Count of indirect references in indirect page. */
  public static final int IP_REFERENCE_COUNT = 256;

  /** 2^IP_REFERENCE_COUNT_EXPONENT = IP_REFERENCE_COUNT. */
  public static final int IP_REFERENCE_COUNT_EXPONENT = 8;

  /** Pages per level. */
  public static final long[] IP_LEVEL_PAGE_COUNT =
      {
          IP_REFERENCE_COUNT,
          IP_REFERENCE_COUNT * IP_REFERENCE_COUNT,
          IP_REFERENCE_COUNT * IP_REFERENCE_COUNT * IP_REFERENCE_COUNT,
          IP_REFERENCE_COUNT
              * IP_REFERENCE_COUNT
              * IP_REFERENCE_COUNT
              * IP_REFERENCE_COUNT,
          IP_REFERENCE_COUNT
              * IP_REFERENCE_COUNT
              * IP_REFERENCE_COUNT
              * IP_REFERENCE_COUNT
              * IP_REFERENCE_COUNT };

  /** Exponent of pages per level. */
  public static final long[] IP_LEVEL_PAGE_COUNT_EXPONENT =
      {
          1 * IP_REFERENCE_COUNT_EXPONENT,
          2 * IP_REFERENCE_COUNT_EXPONENT,
          3 * IP_REFERENCE_COUNT_EXPONENT,
          4 * IP_REFERENCE_COUNT_EXPONENT,
          5 * IP_REFERENCE_COUNT_EXPONENT };

  // --- Uber Page -------------------------------------------------------------

  /** Revision key of unitialized storage. */
  public static final long UP_INIT_ROOT_REVISION_KEY = -1L;

  /** Root revisionKey guaranteed to exist in empty storage. */
  public static final long UP_ROOT_REVISION_KEY = 0L;

  /** Root revisionKey guaranteed to exist in empty storage. */
  public static final long UP_ROOT_REVISION_SIZE = 0L;

  /** Immediate revision root page count per uber page. */
  public static final int UP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT = 8;

  /** Cumulated node pages per level. */
  public static final long[] UP_CUMULATED_REVISION_ROOT_PAGE_COUNT =
      {
          UP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT,
          UP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT + IP_LEVEL_PAGE_COUNT[0],
          UP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1],
          UP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1]
              + IP_LEVEL_PAGE_COUNT[2],
          UP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1]
              + IP_LEVEL_PAGE_COUNT[2]
              + IP_LEVEL_PAGE_COUNT[3],
          UP_IMMEDIATE_REVISION_ROOT_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1]
              + IP_LEVEL_PAGE_COUNT[2]
              + IP_LEVEL_PAGE_COUNT[3]
              + IP_LEVEL_PAGE_COUNT[4] };

  /** Maximum number of indirection levels for uber pages. */
  public static final int UP_MAX_REVISION_ROOT_PAGE_INDIRECTION_LEVEL = 5;

  // --- Revision Root Page ----------------------------------------------------

  /** Immediate node page count per revision root page. */
  public static final int RRP_IMMEDIATE_NODE_PAGE_COUNT = 16;

  /** Cumulated node pages per level. */
  public static final long[] RRP_CUMULATED_NODE_PAGE_COUNT =
      {
          RRP_IMMEDIATE_NODE_PAGE_COUNT,
          RRP_IMMEDIATE_NODE_PAGE_COUNT + IP_LEVEL_PAGE_COUNT[0],
          RRP_IMMEDIATE_NODE_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1],
          RRP_IMMEDIATE_NODE_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1]
              + IP_LEVEL_PAGE_COUNT[2],
          RRP_IMMEDIATE_NODE_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1]
              + IP_LEVEL_PAGE_COUNT[2]
              + IP_LEVEL_PAGE_COUNT[3],
          RRP_IMMEDIATE_NODE_PAGE_COUNT
              + IP_LEVEL_PAGE_COUNT[0]
              + IP_LEVEL_PAGE_COUNT[1]
              + IP_LEVEL_PAGE_COUNT[2]
              + IP_LEVEL_PAGE_COUNT[3]
              + IP_LEVEL_PAGE_COUNT[4] };

  /** Maximum number of indirection levels for node pages. */
  public static final int RRP_MAX_NODE_PAGE_INDIRECTION_LEVEL = 5;

  // --- Node Page -------------------------------------------------------------

  /** Maximum node count per node page. */
  public static final int NDP_NODE_COUNT = 256;

  /** 2^NDP_NODE_COUNT_EXPONENT = NDP_NODE_COUNT. */
  public static final int NDP_NODE_COUNT_EXPONENT = 8;

  /** Maximum attribute count per node. */
  public static final int NDP_ATTRIBUTE_COUNT = 16;

  /** 2^NDP_ATTRIBUTE_COUNT_EXPONENT = NDP_ATTRIBUTE_COUNT. */
  public static final int NDP_ATTRIBUTE_COUNT_EXPONENT = 4;

  //--- Node Layer -------------------------------------------------------------

  /** Number of concurrent IWriteTransactions. */
  public static final int MAX_NUMBER_OF_WRITE_TRANSACTIONS = 1;

  /** Null nodeKey constant. */
  public static final long NULL_KEY = -1L;

  /** Root nodeKey constant. */
  public static final long ROOT_KEY = 0L;

  /** INode kind is element. */
  public static final int ELEMENT = 1;

  /** INode kind is attribute. */
  public static final int ATTRIBUTE = 2;

  /** INode kind is text. */
  public static final int TEXT = 3;

  /** INode kind is processing instruction. */
  public static final int PROCESSING_INSTRUCTION = 7;

  /** INode kind is comment. */
  public static final int COMMENT = 8;

  /** INode kind is document. */
  public static final int DOCUMENT = 9;
  
  //--- XML Layer --------------------------------------------------------------
  
  /** Commit threshold. */
  public static final int COMMIT_TRESHOLD = 1000;
  
}
