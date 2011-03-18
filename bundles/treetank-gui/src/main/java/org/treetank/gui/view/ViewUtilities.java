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
package org.treetank.gui.view;

import javax.xml.namespace.QName;

/**
 * Provides some helper methods for views, which couldn't otherwise be encapsulated together.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public final class ViewUtilities {
    
    /** Private constructor. */
    private ViewUtilities() {
        // Just in case of a helper method tries to invoke the constructor.
        throw new AssertionError();
    }
    
    /**
     * Serialization compatible String representation of a {@link QName} reference.
     * 
     * @param paramQName
     *            The {@QName} reference.
     * @return the string representation
     */
    public static String qNameToString(final QName paramQName) {
        assert paramQName != null;
        String retVal;

        if (paramQName.getPrefix().isEmpty()) {
            retVal = paramQName.getLocalPart();
        } else {
            retVal = paramQName.getPrefix() + ":" + paramQName.getLocalPart();
        }

        return retVal;
    }
}
