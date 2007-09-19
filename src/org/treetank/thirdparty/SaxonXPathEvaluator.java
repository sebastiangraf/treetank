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

import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import net.sf.saxon.om.NodeInfo;

public final class SaxonXPathEvaluator extends Thread {

  private final XPathExpression expression;

  private final NodeInfo nodeInfo;

  private List result;

  public SaxonXPathEvaluator(
      final XPathExpression initExpression,
      final NodeInfo initNodeInfo) {
    expression = initExpression;
    nodeInfo = initNodeInfo;
  }

  public void run() {
    try {
      result = (List) expression.evaluate(nodeInfo, XPathConstants.NODESET);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public List getResult() {
    return result;
  }

}
