/*
 * Copyright 2007, Marc Kramis
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * $Id$
 */

package org.treetank.axislayer;

import org.treetank.api.IAxisFilter;
import org.treetank.api.IReadTransaction;
import org.treetank.utils.UTF;

/**
 * <h1>ValueAxisTest</h1>
 * 
 * <p>
 * Only match nodes of kind TEXT whoms value matches.
 * </p>
 */
public class ValueAxisFilter implements IAxisFilter {

  /** Value test to do. */
  private final byte[] mValue;

  /**
   * Constructor initializing internal state.
   * 
   * @param value Value to find.
   */
  public ValueAxisFilter(final byte[] value) {
    mValue = value;
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param value Value to find.
   */
  public ValueAxisFilter(final String value) {
    this(UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param value Value to find.
   */
  public ValueAxisFilter(final int value) {
    this(UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param value Value to find.
   */
  public ValueAxisFilter(final long value) {
    this(UTF.getBytes(value));
  }

  /**
   * {@inheritDoc}
   */
  public final boolean test(final IReadTransaction rtx) {
    return (rtx.isText() && (UTF.equals(rtx.getValue(), mValue)));
  }

}
