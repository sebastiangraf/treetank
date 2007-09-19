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

package org.treetank.xmllayer;

/**
 * <h1>IAxisIterator</h1>
 * 
 * <p>
 * The axis iterator interactively iterates over the nodeStore using
 * an IReadTransaction instance. It assumes, that the cursor is not modified
 * or moved by another thread during an iteration.
 * </p>
 */
public interface IAxisIterator {

  /**
   * Iterate to next node if there is one.
   * 
   * @return Key of node if there is one or NULL_KEY if the
   * iteration is finished.
   * @throws Exception of any kind.
   */
  public boolean next() throws Exception;

}
