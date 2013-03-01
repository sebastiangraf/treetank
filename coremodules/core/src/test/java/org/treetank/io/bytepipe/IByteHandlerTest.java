/**
 * 
 */
package org.treetank.io.bytepipe;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.access.conf.SessionConfiguration;
import org.treetank.access.conf.StandardSettings;
import org.treetank.exception.TTByteHandleException;

import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class IByteHandlerTest {

    /**
     * Test method for {@link org.treetank.io.bytepipe.IByteHandler#deserialize(InputStream)} and for
     * {@link org.treetank.io.bytepipe.IByteHandler#serialize(OutputStream)}.
     * 
     * @throws TTByteHandleException
     * @throws IOException
     */
    @Test(dataProvider = "instantiateByteHandler")
    public void testSerializeAndDeserialize(Class<IByteHandler> clazz, IByteHandler[] pHandlers)
        throws TTByteHandleException, IOException {
        for (final IByteHandler handler : pHandlers) {
            final int datasize = 10000;
            final byte[] bytes = CoreTestHelper.generateRandomBytes(datasize);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            OutputStream handledOutout = handler.serialize(output);

            ByteArrayInputStream input = new ByteArrayInputStream(bytes);
            ByteStreams.copy(input, handledOutout);
            output.close();
            handledOutout.close();
            input.close();

            final byte[] encoded = output.toByteArray();
            assertFalse(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
                .toString(), Arrays.equals(bytes, encoded));

            input = new ByteArrayInputStream(encoded);
            InputStream handledInput = handler.deserialize(input);
            output = new ByteArrayOutputStream();
            ByteStreams.copy(handledInput, output);
            output.close();
            handledInput.close();
            input.close();

            final byte[] decoded = output.toByteArray();

            assertTrue(new StringBuilder("Check for ").append(handler.getClass()).append(" failed.")
                .toString(), Arrays.equals(bytes, decoded));
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
                        new ByteHandlerPipeline(new Encryptor(StandardSettings.KEY)),
                        new ByteHandlerPipeline(new Zipper()),
                        new ByteHandlerPipeline(new Encryptor(StandardSettings.KEY), new Zipper()),
                        new ByteHandlerPipeline(new Zipper(), new Encryptor(StandardSettings.KEY))
                    }
                }
            };
        return returnVal;
    }

}
