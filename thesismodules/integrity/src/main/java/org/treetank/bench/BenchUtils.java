/**
 * 
 */
package org.treetank.bench;

import java.util.Random;

import org.treetank.bucket.DumbDataFactory.DumbData;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BenchUtils {

    /** Common random instance for generating common tag names. */
    public final static Random random = new Random(123l);

    /**
     * Generating one single {@link DumbData} with random values.
     * 
     * @return one {@link DumbData} with random values.
     */
    public static final DumbData generateOne() {
        byte[] data = new byte[1024];
        random.nextBytes(data);
        return new DumbData(random.nextLong(), data);
    }

    /**
     * Generating new nodes passed on a given number of nodes within a revision
     * 
     * @param pNodesPerRevision
     *            denote the number of nodes within all versions
     * @return a two-dimensional array containing the nodes.
     */
    public static final DumbData[][] createNodes(final int[] pNodesPerRevision) {
        final DumbData[][] returnVal = new DumbData[pNodesPerRevision.length][];
        for (int i = 0; i < pNodesPerRevision.length; i++) {
            returnVal[i] = new DumbData[pNodesPerRevision[i]];
            for (int j = 0; j < pNodesPerRevision[i]; j++) {
                returnVal[i][j] = generateOne();
            }
        }
        return returnVal;
    }
}
