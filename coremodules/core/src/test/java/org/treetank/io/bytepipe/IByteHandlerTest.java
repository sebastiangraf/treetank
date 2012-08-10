/**
 * 
 */
package org.treetank.io.bytepipe;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.TestHelper;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.exception.TTByteHandleException;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IByteHandlerTest {

    /**
     * Test method for {@link org.treetank.io.bytepipe.IByteHandler#deserialize(byte[])} and for
     * {@link org.treetank.io.bytepipe.IByteHandler#serialize(byte[])}.
     * 
     * @throws TTByteHandleException
     */
    @Test(dataProvider = "instantiateByteHandler")
    public void testSerializeAndDeserialize(Class<IByteHandler> clazz, IByteHandler[] pHandlers)
        throws TTByteHandleException {
        for (final IByteHandler handler : pHandlers) {
            final byte[] bytes = TestHelper.generateRandomBytes(10000);
            byte[] serialized = handler.serialize(bytes);
            assertFalse(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
                .toString(), Arrays.equals(bytes, serialized));
            byte[] deserialized = handler.deserialize(serialized);
            assertTrue(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
                .toString(), Arrays.equals(bytes, deserialized));
        }
    }

    /**
     * Providing different implementations of the {@link IByteHandler} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link IByteHandler}
     * @throws TTByteHandleException
     */
    @DataProvider(name = "instantiateByteHandler")
    public Object[][] instantiateByteHandler() throws TTByteHandleException {

        // 128bit key
        byte[] keyValue = new byte[] {
            'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k'
        };
        Key key = new SecretKeySpec(keyValue, "AES");

        new SessionConfiguration("bla", key);

        Object[][] returnVal =
            {
                {
                    IByteHandler.class,
                    new IByteHandler[] {
                        new Encryptor(), new Zipper(), new ByteHandlePipeline(new Encryptor(), new Zipper()),
                        new ByteHandlePipeline(new Zipper(), new Encryptor())
                    }
                }
            };
        return returnVal;
    }
}
