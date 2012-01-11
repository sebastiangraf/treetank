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
package org.treetank.encryption.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * JUnit-test class to test the functionality of the NodeEncryption methods.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class NodeEncryptionTest {

    @Test
    public void testEnDecryption() {
        final String mOriginalString = "This is a String to encrypt";
        final byte[] mStringAsByteArray = mOriginalString.getBytes();

        final byte[] mSecretKey = NodeEncryption.generateSecretKey();

        final byte[] mEncryptedBytes = NodeEncryption.encrypt(mStringAsByteArray, mSecretKey);

        final byte[] mDecryptedBytes = NodeEncryption.decrypt(mEncryptedBytes, mSecretKey);

        final String mDecryptedString = new String(mDecryptedBytes);

        assertEquals(mOriginalString, mDecryptedString);

    }

    @Test
    public void testLongByteConversion() {
        final long mOrginialLong = 1000000;

        final byte[] mLongAsByteArray = NodeEncryption.longToByteArray(mOrginialLong);

        final long mByteArrayAsLong = NodeEncryption.byteArrayToLong(mLongAsByteArray);

        assertEquals(mOrginialLong, mByteArrayAsLong);

    }

    @Test
    public void testIntByteConversion() {
        final int mOrginialInt = 1000;

        final byte[] mIntAsByteArray = NodeEncryption.intToByteArray(mOrginialInt);

        final int mByteArrayAsInt = NodeEncryption.byteArrayToInt(mIntAsByteArray);

        assertEquals(mOrginialInt, mByteArrayAsInt);

    }

}
