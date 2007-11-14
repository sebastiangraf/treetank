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

package org.treetank.api;

/**
 * <h1>IAxis</h1>
 * 
 * <p>
 * Interface to iterate over the TreeTank according to an iteration logic.
 * All implementations must comply with the following:
 * <li>next() must be called exactly once after hasNext() yields true.</li>
 * <li>after hasNext() is false, the transaction points to the node where
 *     it started</li>
 * <li>before each hasNext(), the cursor is guaranteed to point to the last
 *     node found with hasNext().</li>
 * </p>
 * <p>
 * This behavior can be achieved by:
 * <li>Always call super.hasNext() as the first thing in hasNext().</li>
 * <li>Always call reset() before return false in hasNext().</li> 
 * </p>
 */
public interface IAxisFilter {

  /**
   * Access transaction to which this axis is bound.
   * 
   * @return Transaction to which this axis is bound.
   */
  public boolean test(final IReadTransaction rtx);

}
