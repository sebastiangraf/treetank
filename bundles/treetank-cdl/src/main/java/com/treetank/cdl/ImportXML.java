/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.treetank.cdl;

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
