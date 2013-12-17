/**
 * 
 */
package org.treetank.io;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.treetank.io.LogKey.LogKeyBinding;
import org.treetank.testutil.CoreTestHelper;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * Test for LogKey
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class LogKeyTest {

    private final static int NUMBEROFELEMENTS = 100;

    @Test
    public void test() {

        LogKeyBinding binding = new LogKeyBinding();
        for (int i = 0; i < NUMBEROFELEMENTS; i++) {
            LogKey key =
                new LogKey(CoreTestHelper.random.nextBoolean(), CoreTestHelper.random.nextInt(),
                    CoreTestHelper.random.nextLong());
            TupleOutput output = new TupleOutput();
            binding.objectToEntry(key, output);

            TupleInput input = new TupleInput(output);
            LogKey toCompare = binding.entryToObject(input);
            assertEquals(key, toCompare);
        }

    }
}
