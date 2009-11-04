package com.treetank.utils;

/**
 * This class computes the Hashes for the offsets regarding the layers for the
 * indirect accesses to the name corresponding to one nameKey.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class NamePageHash {

    /**
     * Private Constructor, class should be instantiated
     */
    private NamePageHash() {
    }

    public static int[] generateOffsets(final int stringKey) {
        final int[] returnval = new int[2];
        returnval[0] = stringKey & 127;

        // final int secondKey = stringKey*(stringKey+3) % 67;
        returnval[1] = (stringKey >> 7) & 127;
        return returnval;

    }

    public static int generateHashForString(final String string) {
        return string.hashCode();
    }

}
