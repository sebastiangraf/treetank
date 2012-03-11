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
package org.treetank.service.xml.diff.in;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.treetank.access.Database;
import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.api.IDatabase;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.diff.algorithm.fmes.FMES;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.diff.DiffFactory;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;

/**
 * Import using the FMSE algorithm.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class FMSEImport {

    /**
     * Shredder new revision as temporal resource.
     * 
     * @param paramResNewRev
     *            {@link File} reference for new revision (XML resource)
     * @param paramNewRev
     *            {@link File} reference for shreddered new revision (Treetank
     *            resource)
     * @throws AbsTTException
     *             if Treetank fails to shredder the file
     * @throws IOException
     *             if file couldn't be read
     * @throws XMLStreamException
     *             if XML document isn't well formed
     */
    private void shredder(final File paramResNewRev, final File paramNewRev) throws AbsTTException,
        IOException, XMLStreamException {
        assert paramResNewRev != null;
        assert paramNewRev != null;
        final DatabaseConfiguration config = new DatabaseConfiguration(paramNewRev);
        Database.truncateDatabase(config);
        Database.createDatabase(config);
        final IDatabase db = Database.openDatabase(paramNewRev);
        final ResourceConfiguration.Builder builder2 =
            new ResourceConfiguration.Builder(DiffFactory.RESOURCENAME, config);
        db.createResource(builder2.build());
        final ISession session =
            db.getSession(new SessionConfiguration.Builder(DiffFactory.RESOURCENAME).build());
        final IWriteTransaction wtx = session.beginWriteTransaction();
        final XMLEventReader reader = XMLShredder.createFileReader(paramResNewRev);
        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();

        wtx.close();
        session.close();
    }

    /**
     * Import the data.
     * 
     * @param paramResOldRev
     *            {@link File} for old revision (Treetank resource)
     * @param paramResNewRev
     *            {@link File} for new revision (XML resource)
     */
    private void dataImport(final File paramResOldRev, final File paramResNewRev) {
        try {
            shredder(paramResNewRev, new File("target" + File.separator + paramResNewRev.getName()));
            new FMES(paramResOldRev, paramResNewRev);
        } catch (final AbsTTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final XMLStreamException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Main entry point.
     * 
     * @param args
     *            <p>
     *            arguments:
     *            </p>
     *            <ul>
     *            <li>args[0] - path to resource to update</li>
     *            <li>args[1] - path to new XML document</li>
     *            </ul>
     */
    public static void main(final String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                "Path to Treetank resource as well as the path to the new revision in XML must be specified!");
        }

        final File resOldRev = new File(args[0]);
        final File resNewRev = new File(args[1]);

        final FMSEImport fmse = new FMSEImport();
        fmse.dataImport(resOldRev, resNewRev);
    }
}
