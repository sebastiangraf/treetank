/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.bucket;

/**
 * <h1>ConstructorProps</h1>
 * 
 * <p>
 * Interface to hold all constants of the bucket-layer.
 * </p>
 * 
 * @author Sebastian Graf, University of Konstanz
 * @author Marc Kramis, University of Konstanz
 */
public interface IConstants {

    // --- Bucket Kinds
    // ----------------------------------------------------------
    public final static int NODEBUCKET = 1;
    public final static int METABUCKET = 2;
    public final static int UBERBUCKET = 3;
    public final static int INDIRCTBUCKET = 4;
    public final static int REVISIONROOTBUCKET = 5;

    /** ID for not existing nodes. */
    public final static int NULL_NODE = -22;
    public final static int DELETEDNODE = -44;
    public final static int INTERFACENODE = -66;

    /** Count of indirect references in indirect bucket. */
    public static final int CONTENT_COUNT = 128;

    /** Exponent of buckets per level (root level = 0, leaf level = 5). */
    public static final int[] INP_LEVEL_BUCKET_COUNT_EXPONENT = {
        4 * 7, 3 * 7, 2 * 7, 1 * 7, 0 * 7
    };

    public static final byte[] NON_HASHED = new byte[] {
        -55
    };

}
