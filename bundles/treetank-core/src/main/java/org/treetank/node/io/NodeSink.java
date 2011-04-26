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
package org.treetank.node.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Interface for providing byte access to the write-process for a single node.
 * All node features are written to a byte output stream before it is serialized,
 * encrypted and written to the storage.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public interface NodeSink {

    /**
     * Writing a long to the stream.
     * 
     * @param mLongVal
     *            to be written
     * @throws IOException
     */
    void writeLong(final long mLongVal) throws IOException;

    /**
     * Writing a int to the stream.
     * 
     * @param mIntVal
     *            to be written
     * @throws IOException
     */
    void writeInt(final int mIntVal) throws IOException;

    /**
     * Writing a sinlge byte to the stream.
     * 
     * @param mByteVal
     * @throws IOException
     */
    void writeByte(final byte mByteVal) throws IOException;

    /**
     * Get the ouput stream instance.
     * 
     * @return
     *         byte array stream instance.
     */
    ByteArrayOutputStream getOutputStream();

}
