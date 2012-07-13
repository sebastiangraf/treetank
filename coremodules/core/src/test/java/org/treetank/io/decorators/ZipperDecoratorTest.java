/**
 * 
 */
package org.treetank.io.decorators;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import java.util.Arrays;

import org.testng.annotations.Test;
import org.treetank.TestHelper;
import org.treetank.exception.TTByteHandleException;

/**
 * Testcase for zipper.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ZipperDecoratorTest {

    /**
     * Test method for {@link org.treetank.io.decorators.ZipperDecorator#serialize(byte[])} and for
     * {@link org.treetank.io.decorators.ZipperDecorator#deserialize(byte[])}.
     * 
     * @throws TTByteHandleException
     */
    @Test
    public void testSerializeAndDeserialize() throws TTByteHandleException {
        final byte[] bytes = TestHelper.generateRandomBytes(10000);
        IByteRepresentation represent = new ZipperDecorator();
        byte[] serialized = represent.serialize(bytes);
        assertFalse(Arrays.equals(bytes, serialized));
        byte[] deserialized = represent.deserialize(serialized);
        assertTrue(Arrays.equals(bytes, deserialized));
    }
}
