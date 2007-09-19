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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.treetank.utils.UTF;

public class UTFTest {

  @Test
  public void test() {
    assertEquals("a", UTF.convert(UTF.convert("a")));
    assertEquals("foo", UTF.convert(UTF.convert("foo")));
    assertEquals("fö§", UTF.convert(UTF.convert("fö§")));
    assertEquals("", UTF.convert(UTF.convert("")));
  }

}
