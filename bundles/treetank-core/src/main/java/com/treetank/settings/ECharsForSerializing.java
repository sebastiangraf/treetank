/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */

package com.treetank.settings;

/**
 * Holding all byte representations for building up a XML.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum ECharsForSerializing {

    /** " ". */
    SPACE(new byte[] {
        32
    }),

    /** "&lt;". */
    OPEN(new byte[] {
        60
    }),

    /** "&gt;". */
    CLOSE(new byte[] {
        62
    }),

    /** "/". */
    SLASH(new byte[] {
        47
    }),

    /** "=". */
    EQUAL(new byte[] {
        61
    }),

    /** "\"". */
    QUOTE(new byte[] {
        34
    }),

    /** "=\"". */
    EQUAL_QUOTE(EQUAL.getBytes(), QUOTE.getBytes()),

    /** "&lt;/". */
    OPEN_SLASH(OPEN.getBytes(), SLASH.getBytes()),

    /** "/&gt;". */
    SLASH_CLOSE(SLASH.getBytes(), CLOSE.getBytes()),

    /** " rest:"". */
    REST_PREFIX(SPACE.getBytes(), new byte[] {
        114, 101, 115, 116, 58
    }),

    /** "ttid". */
    ID(new byte[] {
        116, 116, 105, 100
    }),

    /** " xmlns=\"". */
    XMLNS(SPACE.getBytes(), new byte[] {
        120, 109, 108, 110, 115
    }, EQUAL.getBytes(), QUOTE.getBytes()),

    /** " xmlns:". */
    XMLNS_COLON(SPACE.getBytes(), new byte[] {
        120, 109, 108, 110, 115, 58
    }),

    /** Newline. */
    NEWLINE(System.getProperty("line.separator").getBytes());

    /** Getting the bytes for the char. */
    private final byte[] mBytes;

    /**
     * Private constructor.
     * 
     * @param paramBytes
     *            the bytes for the chars
     */
    ECharsForSerializing(final byte[]... paramBytes) {
        int index = 0;
        for (final byte[] runner : paramBytes) {
            index = index + runner.length;
        }
        this.mBytes = new byte[index];
        index = 0;
        for (final byte[] runner : paramBytes) {
            System.arraycopy(runner, 0, mBytes, index, runner.length);
            index = index + runner.length;
        }
    }

    /**
     * Getting the bytes.
     * 
     * @return the bytes for the char.
     */
    public byte[] getBytes() {
        return mBytes;
    }

}
