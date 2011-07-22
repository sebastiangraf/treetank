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

package org.treetank.gui;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.treetank.access.FileDatabase;
import org.treetank.access.DatabaseConfiguration;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.EShredderCommit;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.shredder.XMLUpdateShredder;

/**
 * Determines how to shred.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
enum EShredder {

    /** Determines normal shredding. */
    NORMAL {
        @Override
        boolean shred(final File paramSource, final File paramTarget) {
            boolean retVal = true;
            try {
                FileDatabase.truncateDatabase(paramTarget);
                FileDatabase.createDatabase(paramTarget, new DatabaseConfiguration.Builder().build());
                final IDatabase database = FileDatabase.openDatabase(paramTarget);
                final ISession session = database.getSession(new SessionConfiguration.Builder().build());
                final IWriteTransaction wtx = session.beginWriteTransaction();

                final XMLEventReader reader = XMLShredder.createReader(paramSource);
                final ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD));
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);

                wtx.close();
                session.close();
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            } catch (final IOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            } catch (final XMLStreamException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            }

            return retVal;
        }
    },

    /** Determines update only shredding. */
    UPDATEONLY {
        @Override
        boolean shred(final File paramSource, final File paramTarget) {
            boolean retVal = true;
            try {
                final IDatabase database = FileDatabase.openDatabase(paramTarget);
                final ISession session = database.getSession(new SessionConfiguration.Builder().build());
                final IWriteTransaction wtx = session.beginWriteTransaction();

                final XMLEventReader reader = XMLShredder.createReader(paramSource);
                final ExecutorService executor = Executors.newSingleThreadExecutor();
                final XMLUpdateShredder shredder = new XMLUpdateShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD,
                    paramSource, EShredderCommit.COMMIT);
                executor.submit(shredder);
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
                
                shredder.getLatch().await();
                wtx.close();
                session.close();
            } catch (final InterruptedException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            } catch (final AbsTTException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            } catch (final IOException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            } catch (final XMLStreamException e) {
                LOGWRAPPER.error(e.getMessage(), e);
                retVal = false;
            }

            return retVal;
        }
    };

    /** Logger. */
    private static final Logger LOGWRAPPER = LoggerFactory.getLogger(EShredder.class);

    /**
     * Shred XML file.
     * 
     * @param paramSource
     *            source XML file
     * @param paramTarget
     *            target folder
     * @return true if successfully shreddered, false otherwise
     */
    abstract boolean shred(final File paramSource, final File paramTarget);
}
