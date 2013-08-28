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
     * Generating new data-elements passed on a given number of datas within a revision
     * 
     * @param pDatasPerRevision
     *            denote the number of datas within all versions
     * @return a two-dimensional array containing the datas.
     */
    public static final DumbData[][] createDatas(final int[] pDatasPerRevision) {
        final DumbData[][] returnVal = new DumbData[pDatasPerRevision.length][];
        for (int i = 0; i < pDatasPerRevision.length; i++) {
            returnVal[i] = new DumbData[pDatasPerRevision[i]];
            for (int j = 0; j < pDatasPerRevision[i]; j++) {
                returnVal[i][j] = generateOne();
            }
        }
        return returnVal;
    }
}
