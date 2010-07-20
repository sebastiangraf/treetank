package com.treetank.settings;

/**
 * Holding all byte representations for building up a XML.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum ECharsForSerializing {

    /** " ". */
    SPACE(new byte[] { 32 }),

    /** "&lt;". */
    OPEN(new byte[] { 60 }),

    /** "&gt;". */
    CLOSE(new byte[] { 62 }),

    /** "/". */
    SLASH(new byte[] { 47 }),

    /** "=". */
    EQUAL(new byte[] { 61 }),

    /** "\"". */
    QUOTE(new byte[] { 34 }),

    /** "=\"". */
    EQUAL_QUOTE(EQUAL.getBytes(), QUOTE.getBytes()),

    /** "&lt;/". */
    OPEN_SLASH(OPEN.getBytes(), SLASH.getBytes()),

    /** "/&gt;". */
    SLASH_CLOSE(SLASH.getBytes(), CLOSE.getBytes()),

    /** " rest:"". */
    REST_PREFIX(SPACE.getBytes(), new byte[] { 114, 101, 115, 116, 58 }),

    /** "ttid". */
    ID(new byte[] { 116, 116, 105, 100 }),

    /** " xmlns=\"". */
    XMLNS(SPACE.getBytes(), new byte[] { 120, 109, 108, 110, 115 }, EQUAL
            .getBytes(), QUOTE.getBytes()),

    /** " xmlns:". */
    XMLNS_COLON(SPACE.getBytes(), new byte[] { 120, 109, 108, 110, 115, 58 });

    private final byte[] bytes;

    ECharsForSerializing(final byte[]... paramBytes) {
        int index = 0;
        for (final byte[] runner : paramBytes) {
            index = index + runner.length;
        }
        this.bytes = new byte[index];
        index = 0;
        for (final byte[] runner : paramBytes) {
            System.arraycopy(runner, 0, bytes, index, runner.length);
            index = index + runner.length;
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

}
