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

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;

import net.sf.saxon.om.NodeInfo;

public final class SaxonXSLTTransformer extends Thread {

  private final Transformer transformer;

  private final NodeInfo nodeInfo;

  private final Result result;

  public SaxonXSLTTransformer(
      final Transformer initTransformer,
      final NodeInfo initNodeInfo,
      final Result initResult) {
    transformer = initTransformer;
    nodeInfo = initNodeInfo;
    result = initResult;
  }

  public final void run() {
    try {
      transformer.transform(nodeInfo, result);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
