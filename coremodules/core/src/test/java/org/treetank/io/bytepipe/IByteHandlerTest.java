/**
 * 
 */
package org.treetank.io.bytepipe;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.exception.TTByteHandleException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

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
     * @throws IOException
     */
    @Test(dataProvider = "instantiateByteHandler")
    public void testSerializeAndDeserialize(Class<IByteHandler> clazz, IByteHandler[] pHandlers)
        throws TTByteHandleException, IOException {
        for (final IByteHandler handler : pHandlers) {
//            final byte[] bytes = CoreTestHelper.generateRandomBytes(10000);
//
//            ByteArrayOutputStream output = new ByteArrayOutputStream();
//            OutputStream handledOutout = handler.serialize(output);
//
//            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
//            ByteStreams.copy(input, output);
//
//            assertFalse(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
//                .toString(), Arrays.equals(bytes, output.toByteArray()));

            // OutputStream deserialized = handler.deserialize(handledOutout);
            //
            //
            //
            //
            // assertTrue(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
            // .toString(), Arrays.equals(bytes, output.toByteArray()));
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
                    new ByteHandlerPipeline[] {
                        new ByteHandlerPipeline(new Encryptor()), new ByteHandlerPipeline(new Zipper()),
                        new ByteHandlerPipeline(new Encryptor(), new Zipper()),
                        new ByteHandlerPipeline(new Zipper(), new Encryptor())
                    }
                }
            };
        return returnVal;
    }
}
