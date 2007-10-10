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

package org.treetank.api;

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

  /** Length of beacon in bytes. */
  public static final int BEACON_LENGTH = 20;

  /** Default internal encoding. */
  public static final String DEFAULT_ENCODING = "UTF-8";

  /** Default encryption algorithm. */
  public static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES";

  // --- Pages -----------------------------------------------------------------

  /** Uber page (UBP). */
  public static final int UBER_PAGE = 0;

  /** Revision root page (RRP). */
  public static final int REVISION_ROOT_PAGE = 1;

  /** INode page (NDP). */
  public static final int NODE_PAGE = 2;

  /** Indirect page (INP). */
  public static final int INDIRECT_PAGE = 3;

  /** Name page (NMP). */
  public static final int NAME_PAGE = 4;

  //--- Indirect Page ----------------------------------------------------------

  /** Count of indirect references in indirect page. */
  public static final int INP_REFERENCE_COUNT = 256;

  /** 2^INP_REFERENCE_COUNT_EXPONENT = INP_REFERENCE_COUNT. */
  public static final int INP_REFERENCE_COUNT_EXPONENT = 8;

  /** Exponent of pages per level (root level = 0, leaf level = 5). */
  public static final long[] INP_LEVEL_PAGE_COUNT_EXPONENT =
      {
          4 * INP_REFERENCE_COUNT_EXPONENT,
          3 * INP_REFERENCE_COUNT_EXPONENT,
          2 * INP_REFERENCE_COUNT_EXPONENT,
          1 * INP_REFERENCE_COUNT_EXPONENT,
          0 * INP_REFERENCE_COUNT_EXPONENT };

  // --- Uber Page -------------------------------------------------------------

  /** Revision key of unitialized storage. */
  public static final long UBP_INIT_ROOT_REVISION_KEY = -1L;

  /** Root revisionKey guaranteed to exist in empty storage. */
  public static final long UBP_ROOT_REVISION_KEY = 0L;

  /** Root revisionKey guaranteed to exist in empty storage. */
  public static final long UBP_ROOT_REVISION_SIZE = 0L;

  // --- Revision Root Page ----------------------------------------------------

  // --- Node Page -------------------------------------------------------------

  /** Maximum node count per node page. */
  public static final int NDP_NODE_COUNT = 256;

  /** 2^NDP_NODE_COUNT_EXPONENT = NDP_NODE_COUNT. */
  public static final int NDP_NODE_COUNT_EXPONENT = 8;

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
  public static final int COMMIT_TRESHOLD = 1000000;

}
