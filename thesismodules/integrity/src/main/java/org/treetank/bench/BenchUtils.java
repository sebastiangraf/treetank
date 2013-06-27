/**
 * 
 */
package org.treetank.bench;

import java.util.Random;

import org.treetank.bucket.DumbNodeFactory.DumbNode;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class BenchUtils {

    /** Common random instance for generating common tag names. */
    private final static Random random = new Random(123l);

    /**
     * Generating one single {@link DumbNode} with random values.
     * 
     * @return one {@link DumbNode} with random values.
     */
    public static final DumbNode generateOne() {
        byte[] data = new byte[1024];
        random.nextBytes(data);
        return new DumbNode(random.nextLong(), random.nextLong(), data);
    }

    /**
     * Generating new nodes passed on a given number of nodes within a revision
     * 
     * @param pNodesPerRevision
     *            denote the number of nodes within all versions
     * @return a two-dimensional array containing the nodes.
     */
    public static final DumbNode[][] createNodes(final int[] pNodesPerRevision) {
        final DumbNode[][] returnVal = new DumbNode[pNodesPerRevision.length][];
        for (int i = 0; i < pNodesPerRevision.length; i++) {
            returnVal[i] = new DumbNode[pNodesPerRevision[i]];
            for (int j = 0; j < pNodesPerRevision[i]; j++) {
                returnVal[i][j] = generateOne();
            }
        }
        return returnVal;
    }
}
