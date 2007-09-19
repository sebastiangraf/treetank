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

import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;

public final class SaxonXQueryEvaluator extends Thread {

  public final static String OUTPUT_METHOD = "xml";

  public final static String OUTPUT_INDENT = "yes";

  private final XQueryExpression expression;

  private final NodeInfo nodeInfo;

  private final Result result;

  private final Properties properties;

  public SaxonXQueryEvaluator(
      final XQueryExpression initExpression,
      final NodeInfo initNodeInfo,
      final Result initResult,
      final Properties initProperties) {
    expression = initExpression;
    nodeInfo = initNodeInfo;
    result = initResult;
    properties = initProperties;
  }

  public SaxonXQueryEvaluator(
      final XQueryExpression initExpression,
      final NodeInfo initNodeInfo,
      final Result initResult) {
    expression = initExpression;
    nodeInfo = initNodeInfo;
    result = initResult;
    properties = new Properties();
    properties.setProperty(OutputKeys.METHOD, OUTPUT_METHOD);
    properties.setProperty(OutputKeys.INDENT, OUTPUT_INDENT);
  }

  public void run() {
    try {
      final DynamicQueryContext dynamicContext =
          new DynamicQueryContext(nodeInfo.getConfiguration());
      dynamicContext.setContextItem(nodeInfo);
      expression.run(dynamicContext, result, properties);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
