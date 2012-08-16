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
package org.treetank.saxon.wrapper;

import static org.treetank.node.IConstants.NULL_NODE;
import static org.treetank.node.IConstants.ROOT_NODE;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import org.treetank.TestHelper;
import org.treetank.access.Database;
import org.treetank.access.NodeWriteTrx;
import org.treetank.access.NodeWriteTrx.HashKind;
import org.treetank.access.conf.DatabaseConfiguration;
import org.treetank.access.conf.ResourceConfiguration.IResourceConfigurationFactory;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.api.IDatabase;
import org.treetank.api.INodeWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.node.DocumentRootNode;
import org.treetank.node.delegates.NodeDelegate;
import org.treetank.node.delegates.StructNodeDelegate;
import org.treetank.service.xml.shredder.EShredderInsert;
import org.treetank.service.xml.shredder.XMLShredder;

/**
 * Helper class for saxon
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class SaxonHelper {

    /** Path to books file. */
    private static final File BOOKSXML = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).append("data").append(
        File.separator).append("my-books.xml").toString());

    public static void createBookDB(IResourceConfigurationFactory resFac) throws Exception {
        TestHelper.deleteEverything();

        final DatabaseConfiguration dbConfig = new DatabaseConfiguration(TestHelper.PATHS.PATH1.getFile());
        Database.createDatabase(dbConfig);
        final IDatabase database = Database.openDatabase(TestHelper.PATHS.PATH1.getFile());
        database.createResource(resFac.create(TestHelper.PATHS.PATH1.getFile(), TestHelper.RESOURCENAME, 1));
        final ISession session =
            database.getSession(new SessionConfiguration(TestHelper.RESOURCENAME, StandardSettings.KEY));
        final INodeWriteTrx wtx =
            new NodeWriteTrx(session, session.beginPageWriteTransaction(), HashKind.Rolling);
        createDocumentRootNode(wtx);
        final XMLEventReader reader = XMLShredder.createFileReader(BOOKSXML);
        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();
        wtx.close();
        session.close();
        database.close();
    }

    /**
     * Generating a Document Root node.
     * 
     * @param pWtx
     *            where the docroot should be generated.
     * @throws TTException
     */
    public static final void createDocumentRootNode(final INodeWriteTrx pWtx) throws TTException {
        final NodeDelegate nodeDel = new NodeDelegate(ROOT_NODE, NULL_NODE, 0);
        pWtx.getPageWtx()
            .createNode(
                new DocumentRootNode(nodeDel, new StructNodeDelegate(nodeDel, NULL_NODE, NULL_NODE,
                    NULL_NODE, 0)));
        pWtx.moveTo(org.treetank.node.IConstants.ROOT_NODE);
    }

}
