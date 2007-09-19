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

public final class Namespace {

  private int mURIKey;

  private int mPrefixKey;

  public Namespace(final int uriKey, final int prefixKey) {
    mURIKey = uriKey;
    mPrefixKey = prefixKey;
  }

  public Namespace(final Namespace namespace) {
    mURIKey = namespace.getURIKey();
    mPrefixKey = namespace.getPrefixKey();
  }

  public final int getPrefixKey() {
    return mPrefixKey;
  }

  public final void setPrefixKey(final int prefixKey) {
    mPrefixKey = prefixKey;
  }

  public final int getURIKey() {
    return mURIKey;
  }

  public final void setURIKey(final int uriKey) {
    mURIKey = uriKey;
  }

}
