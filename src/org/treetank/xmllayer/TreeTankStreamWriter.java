/*
 * Copyright (c) 2007, Marc Kramis
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id$
 */

package org.treetank.xmllayer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.treetank.api.IWriteTransaction;

/**
 * <h1>TreeTankStreamWriter</h1>
 * 
 * <p>
 * XMLStreamWriter implementation writing streamed XML into a TreeTank.
 * The incoming document node will be written as the first child of the
 * currently selected node in the TreeTank transaction. The following nodes
 * will be written as the descendants of just created node.
 * </p>
 */
public final class TreeTankStreamWriter implements XMLStreamWriter {

  /** Write transaction to write to. */
  private final IWriteTransaction mWTX;

  /**
   * Initialize XMLStreamWriter implementation with transaction.
   * 
   * @param wtx Transaction with cursor pointing to the parent node of the
   * tree to be inserted.
   */
  public TreeTankStreamWriter(final IWriteTransaction wtx) {
    mWTX = wtx;
  }

  /**
   * {@inheritDoc}
   */
  public final void close() throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void flush() throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final NamespaceContext getNamespaceContext() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public final String getPrefix(final String arg0) throws XMLStreamException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public final Object getProperty(final String arg0)
      throws IllegalArgumentException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public final void setDefaultNamespace(final String arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void setNamespaceContext(final NamespaceContext arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void setPrefix(final String arg0, final String arg1)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeAttribute(final String arg0, final String arg1)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeAttribute(
      final String arg0,
      final String arg1,
      final String arg2) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeAttribute(
      final String arg0,
      final String arg1,
      final String arg2,
      final String arg3) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeCData(final String arg0) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeCharacters(final String arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeCharacters(
      final char[] arg0,
      final int arg1,
      final int arg2) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeComment(final String arg0) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeDTD(final String arg0) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeDefaultNamespace(final String arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeEmptyElement(final String arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeEmptyElement(final String arg0, final String arg1)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeEmptyElement(
      final String arg0,
      final String arg1,
      final String arg2) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeEndDocument() throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeEndElement() throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeEntityRef(final String arg0) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeNamespace(final String arg0, final String arg1)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeProcessingInstruction(final String arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeProcessingInstruction(
      final String arg0,
      final String arg1) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeStartDocument() throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeStartDocument(final String arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeStartDocument(final String arg0, final String arg1)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeStartElement(final String arg0)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeStartElement(final String arg0, final String arg1)
      throws XMLStreamException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public final void writeStartElement(
      final String arg0,
      final String arg1,
      final String arg2) throws XMLStreamException {
    // TODO Auto-generated method stub

  }

}
