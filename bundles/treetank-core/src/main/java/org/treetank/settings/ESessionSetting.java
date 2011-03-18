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

package org.treetank.settings;

/**
 * This enum stores all settable properties for treetank plus a standard value.
 * The real setting takes place in the class <code>SessionConfiguration</code>.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum ESessionSetting {

    /** Number of concurrent exclusive write transactions. */
    MAX_WRITE_TRANSACTIONS("1"),
    /** Number of concurrent shared read transactions. */
    MAX_READ_TRANSACTIONS("128"),
    /** Commit threshold. */
    COMMIT_THRESHOLD("262144");

    private final String mValue;

    private ESessionSetting(final String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }

}
