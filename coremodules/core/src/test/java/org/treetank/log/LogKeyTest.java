/**
 * 
 */
package org.treetank.log;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.treetank.CoreTestHelper;
import org.treetank.log.LogKey.LogKeyBinding;

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
                new LogKey(CoreTestHelper.random.nextBoolean(), CoreTestHelper.random.nextLong(),
                    CoreTestHelper.random.nextLong());
            TupleOutput output = new TupleOutput();
            binding.objectToEntry(key, output);

            TupleInput input = new TupleInput(output);
            LogKey toCompare = binding.entryToObject(input);
            assertEquals(key, toCompare);
        }

    }
}
