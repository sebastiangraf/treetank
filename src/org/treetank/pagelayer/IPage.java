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

import org.treetank.utils.FastByteArrayWriter;

/**
 * <h1>IPage</h1>
 * 
 * <p>
 * Each page must implement this interface to allow recursive commit on all
 * dirty pages and to recursively serialize all dirty pages.
 * </p>
 */
public interface IPage {

  /**
   * Recursively call commit on all referenced pages.
   * 
   * @param pageWriter Page writer.
   * @throws Exception occurring during commit operation.
   */
  public void commit(final PageWriter pageWriter) throws Exception;

  /**
   * Serialize self into object output stream.
   * 
   * @param out Object output stream.
   * @throws Exception if the stream encounters an error.
   */
  public void serialize(final FastByteArrayWriter out) throws Exception;

}
