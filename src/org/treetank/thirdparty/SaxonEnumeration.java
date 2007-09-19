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

package org.treetank.thirdparty;

import org.treetank.nodelayer.IReadTransaction;
import org.treetank.xmllayer.IAxisIterator;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Navigator;
import net.sf.saxon.om.SequenceIterator;

/**
 * <h1>SaxonEnumeration</h1>
 * 
 * <p>
 * Saxon adaptor for axis iterations.
 * </p>
 */
public final class SaxonEnumeration extends Navigator.BaseEnumeration {

  /** Saxon configuraton. */
  private final Configuration config;

  /** TreeTank reading transaction. */
  private final IReadTransaction trx;

  /** TreeTank axis iterator. */
  private final IAxisIterator iterator;

  /**
   * Constructor.
   * 
   * @param initConfig Saxon configuration.
   * @param initTrx TreeTank reading transaction.
   * @param initIterator TreeTank axis iterator.
   */
  public SaxonEnumeration(
      final Configuration initConfig,
      final IReadTransaction initTrx,
      final IAxisIterator initIterator) {

    config = initConfig;
    trx = initTrx;
    iterator = initIterator;

  }

  /**
   * {@inheritDoc}
   */
  public void advance() {
    try {
      if (iterator.next()) {
        current = new SaxonNodeInfo(config, trx);
      } else {
        current = null;
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public SequenceIterator getAnother() {
    throw new UnsupportedOperationException();
  }

}
