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

package org.treetank.pagelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treetank.pagelayer.Node;
import org.treetank.utils.IConstants;


public class NodeTest {

  @Test
  public void testKeys() throws Exception {

    assertEquals(0L, Node.nodePageKey(0L));
    assertEquals(0, Node.nodePageOffset(0L));
    assertEquals(0L, Node.keyBase(0L));

    assertEquals(0L, Node.nodePageKey(1L));
    assertEquals(0, Node.nodePageOffset(1L));
    assertEquals(0L, Node.keyBase(0L));

    assertEquals(0L, Node
        .nodePageKey(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT));
    assertEquals(1, Node
        .nodePageOffset(1L << IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT));
    assertEquals(0L, Node.keyBase(0L));

    assertEquals(
        1L,
        Node
            .nodePageKey(1L << (IConstants.NDP_NODE_COUNT_EXPONENT + IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT)));
    assertEquals(
        0,
        Node
            .nodePageOffset(1L << (IConstants.NDP_NODE_COUNT_EXPONENT + IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT)));
    assertEquals(1L << (IConstants.NDP_NODE_COUNT_EXPONENT + IConstants.NDP_ATTRIBUTE_COUNT_EXPONENT), Node.keyBase(1L));

  }

}
