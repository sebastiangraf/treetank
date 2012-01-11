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
package org.treetank.encryption.database;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.SortedMap;

import org.junit.Test;
import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.database.model.KeyManager;
import org.treetank.exception.TTEncryptionException;

public class KeyManagerDatabaseTest {

//    private static final File MAN_STORE = new File(new StringBuilder(
//        File.separator).append("tmp").append(File.separator).append("tnk")
//        .append(File.separator).append("keymanagerdb").toString());

    
    public void testFunctions() throws TTEncryptionException {
        
        new EncryptionController().clear();
        new EncryptionController().setEncryptionOption(true);
        new EncryptionController().init();


        final KeyManagerDatabase mKeyManagerDb = new EncryptionController().getManDb();
            

        final KeyManager man1 = new KeyManager("User1", new HashSet<Long>());
        final KeyManager man2 = new KeyManager("User2", new HashSet<Long>());
        final KeyManager man3 = new KeyManager("User3", new HashSet<Long>());

        // put and get
        mKeyManagerDb.putEntry(man1);
        mKeyManagerDb.putEntry(man2);
        mKeyManagerDb.putEntry(man3);

        assertEquals(man1.getUser(), mKeyManagerDb.getEntry("User1").getUser());
        assertEquals(man2.getUser(), mKeyManagerDb.getEntry("User2").getUser());
        assertEquals(man3.getUser(), mKeyManagerDb.getEntry("User3").getUser());

        // delete
        assertEquals(mKeyManagerDb.deleteEntry("User1"), true);

        // count
        assertEquals(2, mKeyManagerDb.count());

        // get entries as sorted map
        final SortedMap<String, KeyManager> map = mKeyManagerDb.getEntries();
        assertEquals(map.get("User2").getUser(), mKeyManagerDb
            .getEntry("User2").getUser());
        assertEquals(map.get("User3").getUser(), mKeyManagerDb
            .getEntry("User3").getUser());

        mKeyManagerDb.clearPersistent();

    }

 

}
