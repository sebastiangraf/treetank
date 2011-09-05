/**
 * Copyright (c) 2011, Distributed Systems Group, University of Konstanz
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
package org.treetank.encryption.database;

import java.io.File;

/**
 * Abstract class for holding common data for all key databases involved in
 * encryption process. Each instance of this class stores the data in a place
 * related to the {@link ResourceConfiguration} at a different subfolder.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public abstract class AbsKeyDatabase {

    /**
     * Place to store the data.
     */
    protected final File place;

    /**
     * Counter to give every instance a different place.
     */
    private static int counter;

    /**
     * Constructor with the place to store the data.
     * 
     * @param paramFile
     *            {@link File} which holds the place to store the data.
     */
    protected AbsKeyDatabase(final File paramFile) {
        place =
            new File(paramFile, new StringBuilder(new File("keyselector")
                .getName()).append(File.separator).append(counter).toString());
        place.mkdirs();
        counter++;
    }

}
