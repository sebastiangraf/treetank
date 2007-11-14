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

import org.treetank.api.IAxis;
import org.treetank.utils.UTF;

/**
 * <h1>ValueTestAxis</h1>
 * 
 * <p>
 * Iterate over all children of kind ATTRIBUTE starting at a given
 * node.
 * </p>
 */
public class ValueTestAxis extends AbstractAxis {

  /** Remember next key to visit. */
  private final IAxis mAxis;

  /** Name test to do. */
  private final byte[] mValue;

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final IAxis axis, final byte[] value) {
    super(axis.getTransaction());
    mAxis = axis;
    mValue = value;
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final AbstractAxis axis, final String value) {
    this(axis, UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final AbstractAxis axis, final int value) {
    this(axis, UTF.getBytes(value));
  }

  /**
   * Constructor initializing internal state.
   * 
   * @param axis Axis iterator over which we should find values.
   * @param value Value to find.
   */
  public ValueTestAxis(final AbstractAxis axis, final long value) {
    this(axis, UTF.getBytes(value));
  }

  /**
   * {@inheritDoc}
   */
  public final boolean hasNext() {
    resetToLastKey();
    while (mAxis.hasNext()) {
      mAxis.next();
      if (getTransaction().isText()
          && (UTF.equals(getTransaction().getValue(), mValue))) {
        return true;
      }
    }
    resetToStartKey();
    return false;
  }

}
