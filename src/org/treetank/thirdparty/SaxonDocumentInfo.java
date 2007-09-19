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

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;

/**
 * <h1>SaxonDocumentInfo</h1>
 * 
 * <p>
 * Saxon adaptor for document node.
 * </p>
 */
public class SaxonDocumentInfo extends SaxonNodeInfo implements DocumentInfo {

  /**
   * Constructor.
   * 
   * @param initConfig Saxon configuration.
   * @param initTrx TreeTank reading transaction.
   */
  public SaxonDocumentInfo(
      final Configuration initConfig,
      final IReadTransaction initTrx) {
    super(initConfig, initTrx);
  }

  /**
   * {@inheritDoc}
   */
  public final String[] getUnparsedEntity(final String arg0) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public final NodeInfo selectID(final String arg0) {
    throw new UnsupportedOperationException();
  }

}
