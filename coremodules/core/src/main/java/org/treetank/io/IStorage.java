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
package org.treetank.io;

import java.util.Properties;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.io.file.FileStorage;

import com.google.inject.ImplementedBy;

/**
 * Interface to generate access to the underlaying storage. The underlaying
 * storage is flexible as long as {@link IReader} and {@link IWriter} -implementations are provided. Utility
 * methods for common interaction with
 * the storage are provided via the <code>EStorage</code>-enum.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@ImplementedBy(FileStorage.class)
public interface IStorage {

    /**
     * Getting a writer.
     * 
     * @return an {@link IWriter} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    IWriter getWriter() throws TTException;

    /**
     * Getting a reader.
     * 
     * @return an {@link IReader} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    IReader getReader() throws TTException;

    /**
     * Closing this storage.
     * 
     * @throws TTIOException
     *             exception to be throwns
     */
    void close() throws TTException;

    /**
     * Check if storage exists.
     * 
     * @return true if storage holds data, false otherwise
     * @throws TTIOException
     *             if storage is not accessible
     */
    boolean exists() throws TTException;

    /**
     * Getting the ByteHandlers associated with this Storage.
     * 
     * @return the {@link IByteHandlerPipeline} transforming bytes before storage
     */
    IByteHandlerPipeline getByteHandler();

    /**
     * Truncating a storage.
     * 
     * @throws TTException
     *             if anything weird happens
     */
    void truncate() throws TTException;

    /**
     * 
     * Factory for generating an {@link IStorage}-instance. Needed mainly
     * because of Guice-Assisted utilization.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    public static interface IStorageFactory {

        /**
         * Generating a storage for a fixed file.
         * 
         * @param pProperties
         *            referencing not only to the storage.
         * @return an {@link IStorage}-instance
         */
        IStorage create(Properties pProperties);
    }

}
