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

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

/**
 * Interface to provide the abstract layer related to write access of the
 * tt-backend.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public interface IBackendWriter extends IBackendReader {

    /**
     * Writing a page related to the reference.
     * 
     * @param page
     *            page to be written
     * @throws TTException
     *             execption to be thrown if something bad happens
     */
    void write(final IPage page) throws TTException;

    /**
     * Writing the Uberpage including the pointer to the page.
     * 
     * @param page
     *            the UberPage to be written.
     */
    void writeUberPage(final UberPage page) throws TTException;

    /**
     * Closing the write access.
     * 
     * @throws TTIOException
     *             if closing fails
     */
    @Override
    void close() throws TTIOException;

}
