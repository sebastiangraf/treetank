/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

package org.treetank.cdl;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.saxon.evaluator.XSLTEvaluator;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;

/**
 * Die class acts as an import for XML into Treetank.
 */
public final class ImportXML {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java -jar CDL \"XMLToImport.xml\" \"TTToStore.tnk\"");
            System.exit(-1);
        }
        System.out.print("Shredding '" + args[0] + "' to '" + args[1] + "' ... ");
        final long time = System.currentTimeMillis();

        // File setup
        final File importFile = new File(args[0]);
        final File storeFile = new File(args[1]);

        // Wtx setup
        final IDatabase db = Database.openDatabase(storeFile);
        final ISession session = db.getSession();
        final IWriteTransaction wtx = session.beginWriteTransaction();

        // Stax Setup
        final XMLEventReader reader = XMLShredder.createReader(importFile);

        final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
        shredder.call();

        // final XSLTEvaluator test = new XSLTEvaluator(db, stylesheet, out, serializer)

        wtx.commit();
        wtx.close();
        session.close();
        db.close();

        System.out.println(" done [" + (System.currentTimeMillis() - time) + "ms].");

    }

}
