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
package com.treetank.gui.view.sunburst;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Provides methods to add and remove {@link PropertyChangeListener}s as well as firing property changes.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
abstract class AbsComponent {

    /** {@link PropertyChangeSupport} to register listeners. */
    private final PropertyChangeSupport mPropertyChangeSupport;

    /**
     * Constructor.
     */
    AbsComponent() {
        mPropertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Add a {@link PropertyChangeListener}.
     * 
     * @param paramListener
     *            The listener to add.
     */
    public final void addPropertyChangeListener(final PropertyChangeListener paramListener) {
        mPropertyChangeSupport.addPropertyChangeListener(paramListener);
    }

    /**
     * Remove a {@link PropertyChangeListener}.
     * 
     * @param paramListener
     *            The listener to remove.
     */
    public final void removePropertyChangeListener(final PropertyChangeListener paramListener) {
        mPropertyChangeSupport.removePropertyChangeListener(paramListener);
    }

    /**
     * Fire a property change.
     * 
     * @param paramPropertyName
     *            Name of the property.
     * @param paramOldValue
     *            Old value.
     * @param paramNewValue
     *            New value.
     */
    protected final void firePropertyChange(final String paramPropertyName, final Object paramOldValue,
        final Object paramNewValue) {
        mPropertyChangeSupport.firePropertyChange(paramPropertyName, paramOldValue, paramNewValue);
    }
}
