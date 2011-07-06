/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.gui.view.model;

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IVisualItem;
import org.treetank.gui.view.sunburst.SunburstContainer;
import org.treetank.gui.view.sunburst.SunburstItem;
import org.treetank.gui.view.sunburst.SunburstView;
import org.treetank.service.xml.shredder.EShredderInsert;

import processing.core.PApplet;

/**
 * Interface which models of the {@link SunburstView} have to implement.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * @param <T>
 * 
 */
public interface IModel<T extends IVisualItem> extends Iterable<T>, Iterator<T>, PropertyChangeListener {
    /**
     * Get the {@link IVisualItem} implementation at the specified index.
     * 
     * @param paramIndex
     *            the index
     * @return the {@link SunburstItem} at the specified index
     * @throws IndexOutOfBoundsException
     *             if index > mItems.size() - 1 or < 0
     */
    T getItem(final int paramIndex) throws IndexOutOfBoundsException;

    /**
     * Traverse the tree and create a {@link List} of {@link SunburstItem}s.
     * 
     * @param paramContainer
     *            {@link IContainer} implementation with options
     */
    void traverseTree(final IContainer paramContainer);

    /** Undo operation. */
    void undo();

    /**
     * Update root of the tree with the node currently clicked.
     * 
     * @param paramContainer
     *            {@link IContainer} reference with options
     */
    void update(final IContainer paramContainer);

    /**
     * XPath evaluation.
     * 
     * @param paramXPathExpression
     *            XPath expression to evaluate.
     */
    void evaluateXPath(final String paramXPathExpression);

    /**
     * Spefify how to insert an XML fragment.
     * 
     * @param paramInsert
     *            determines how to insert an XMl fragment
     */
    void setInsert(final EShredderInsert paramInsert);

    /**
     * Update {@link ReadDB} instance.
     * 
     * @param paramDB
     *            new {@link ReadDB} instance
     * @param paramContainer
     *            {@link IContainer} instance
     */
    void updateDb(final ReadDB paramDB, final IContainer paramContainer);

    /**
     * Add a {@link PropertyChangeListener}.
     * 
     * @param paramListener
     *            the listener to add
     */
    void addPropertyChangeListener(final PropertyChangeListener paramListener);

    /**
     * Remove a {@link PropertyChangeListener}.
     * 
     * @param paramListener
     *            the listener to remove
     */
    void removePropertyChangeListener(final PropertyChangeListener paramListener);

    /**
     * Fire a property change.
     * 
     * @param paramPropertyName
     *            name of the property
     * @param paramOldValue
     *            old value
     * @param paramNewValue
     *            new value
     */
    void firePropertyChange(final String paramPropertyName, final Object paramOldValue,
        final Object paramNewValue);

    /**
     * Get the database handle.
     * 
     * @return {@link ReadDB} reference
     */
    ReadDB getDb();
}
