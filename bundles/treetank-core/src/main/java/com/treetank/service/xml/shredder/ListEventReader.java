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
package com.treetank.service.xml.shredder;

import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * <h1>ListEventReader</h1>
 * 
 * <p>
 * Implements an XMLEventReader based on a list of XMLEvents.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class ListEventReader implements XMLEventReader {

    /**
     * List of {@link XMLEvent}s.
     */
    private transient List<XMLEvent> mEvents;

    /** Index to specify element in list. */
    private transient int mIndex;

    /**
     * Constructor.
     * 
     * @param paramEvents
     *            List of XMLEvents.
     */
    public ListEventReader(final List<XMLEvent> paramEvents) {
        mEvents = paramEvents;
        mIndex = -1;
    }

    @Override
    public void close() throws XMLStreamException {
        // Do nothing.
    }

    @Override
    public String getElementText() throws XMLStreamException {
        final StringBuffer buffer = new StringBuffer();

        if (mEvents.get(mIndex).getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException("Current event is not a start tag!");
        }

        final QName root = mEvents.get(mIndex).asStartElement().getName();
        int level = 0;
        while (level >= 0
            && !(mEvents.get(mIndex).isEndElement() && mEvents.get(mIndex).asEndElement().getName()
                .equals(root))) {
            final XMLEvent event = mEvents.get(mIndex);
            switch (event.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                level++;
                break;
            case XMLStreamConstants.CHARACTERS:
                buffer.append(event.asCharacters().getData());
                break;
            case XMLStreamConstants.END_ELEMENT:
                level--;
                break;
            default:
                // Do nothing.
            }
        }
        return buffer.toString();
    }

    @Override
    public Object getProperty(final String paramName) throws IllegalArgumentException {
        // Do nothing.
        return null;
    }

    @Override
    public boolean hasNext() {
        boolean retVal = true;
        if (mIndex < -1 || mIndex + 1 > mEvents.size() - 1) {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        XMLEvent retVal;
        try {
            retVal = mEvents.get(++mIndex);
        } catch (final IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
        return retVal;
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        final XMLEvent event = mEvents.get(++mIndex);
        while (!event.isStartElement() || !event.isEndElement()) {
            mIndex++;
        }
        return mEvents.get(mIndex);
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        XMLEvent retVal = null;
        try {
            retVal = mEvents.get(mIndex + 1);
        } catch (final IndexOutOfBoundsException e) {
            retVal = null;
        }
        return retVal;
    }

    /**
     * Just calls nextEvent().
     * 
     * @return null if an XMLStreamException occured
     * @throw NoSuchElementException
     *             thrown if no more elements are in the list.
     */
    @Override
    public Object next() {
        Object retVal = null;
        try {
            retVal = nextEvent();
        } catch (final XMLStreamException e) {
            retVal = null;
        }
        return retVal;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported!");
    }
    
    /** 
     * Create a copy, but reset index. 
     * 
     * @return copied {@link ListEventReader}. 
     */
    public XMLEventReader copy() {
        return new ListEventReader(mEvents);
    }

}
