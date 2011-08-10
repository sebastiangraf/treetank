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

import java.io.File;

import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.exception.TTIOException;
import org.treetank.io.berkeley.BerkeleyFactory;
import org.treetank.io.file.FileFactory;

/**
 * Abstract Factory to build up a concrete storage for the data. The Abstract
 * Instance must provide Reader and Writers as well as some additional methods.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public abstract class AbsIOFactory {

    /** Type for different storages. */
    public enum StorageType {
        /** File Storage. */
        File,
        /** Berkeley Storage. */
        Berkeley
    }

    /** Folder to store the data. */
    public final File mFile;

    /**
     * Protected constructor, just setting the sessionconfiguration.
     * 
     * @param paramFile
     *            to be set
     */
    protected AbsIOFactory(final File paramFile) {
        mFile = paramFile;
    }

    /**
     * Getting a writer.
     * 
     * @return an {@link IWriter} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    public abstract IWriter getWriter() throws TTIOException;

    /**
     * Getting a reader.
     * 
     * @return an {@link IReader} instance
     * @throws TTIOException
     *             if the initalisation fails
     */
    public abstract IReader getReader() throws TTIOException;

    /**
     * Getting a Closing this storage. Is equivalent to Session.close
     * 
     * @throws TTIOException
     *             exception to be throwns
     */
    public abstract void close() throws TTIOException;

    /**
     * Check if storage exists.
     * 
     * @return true if storage holds data, false otherwise
     * @throws TTIOException
     *             if storage is not accessible
     */
    public abstract boolean exists() throws TTIOException;

    /**
     * Truncate database completely
     * 
     * @throws TTIOException
     *             if storage is not accessible
     */
    public abstract void truncate() throws TTIOException;

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param paramFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    protected static boolean recursiveDelete(final File paramFile) {
        if (paramFile.isDirectory()) {
            for (final File child : paramFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return paramFile.delete();
    }

    public synchronized static final AbsIOFactory getInstance(final ResourceConfiguration paramResourceConf)
        throws TTIOException {
        AbsIOFactory fac = null;
        final AbsIOFactory.StorageType storageType = paramResourceConf.mType;
        switch (storageType) {
        case File:
            fac = new FileFactory(paramResourceConf.mPath);
            break;
        case Berkeley:
            fac = new BerkeleyFactory(paramResourceConf.mPath);
            break;
        default:
            throw new TTIOException("Type", storageType.toString(), "not valid!");
        }
        return fac;

    }

}
