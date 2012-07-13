/**
 * 
 */
package org.treetank.io.bytepipe;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.treetank.TestHelper;
import org.treetank.exception.TTByteHandleException;
import org.treetank.io.bytepipe.Encryptor;
import org.treetank.io.bytepipe.IByteHandler;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class EncryptorTest {

    /**
     * Test method for {@link org.treetank.io.bytepipe.Encryptor#deserialize(byte[])} and for
     * {@link org.treetank.io.bytepipe.Encryptor#serialize(byte[])}.
     * 
     * @throws TTByteHandleException
     */
    @Test
    public void testSerializeAndDeserialize() throws TTByteHandleException {
        final byte[] bytes = TestHelper.generateRandomBytes(10000);
        IByteHandler represent = new Encryptor();
        byte[] serialized = represent.serialize(bytes);
        assertFalse(Arrays.equals(bytes, serialized));
        byte[] deserialized = represent.deserialize(serialized);
        assertTrue(Arrays.equals(bytes, deserialized));
    }

}
