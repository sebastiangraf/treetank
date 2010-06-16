/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
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
 * $Id: XMLShredder.java 4455 2008-09-01 14:46:46Z kramis $
 */

package com.treetank.service.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.exception.TreetankUsageException;
import com.treetank.settings.EFixed;
import com.treetank.settings.ENodes;
import com.treetank.settings.EXMLSerializing;
import com.treetank.utils.FastStack;
import com.treetank.utils.TypedValue;

/**
 * This class appends a given {@link XMLStreamReader} to a
 * {@link IWriteTransaction}. The content of the stream is added as a subtree.
 * Based on a boolean which identifies the point of insertion, the subtree is
 * either added as subtree or as rightsibling
 * 
 * @author Marc Kramis, Seabix
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public final class XMLShredder implements Callable<Long> {

  private final IWriteTransaction mWtx;

  private final XMLEventReader mReader;

  private final boolean mFirstChildAppend;

  private final boolean mInsertOnlyModified;

  /**
   * Normal constructor to invoke a shredding process on a existing
   * {@link WriteTransaction}
   * 
   * @param wtx
   *            where the new XML Fragment should be placed
   * @param reader
   *            of the XML Fragment
   * @param addAsFirstChild
   *            if the insert is occuring on a node in an existing tree.
   *            <code>false</code> is not possible when wtx is on root node.
   * @throws TreetankUsageException
   *             if insertasfirstChild && updateOnly is both true OR if wtx is
   *             not pointing to doc-root and updateOnly= true
   * @deprecated use constructor with XMLEventReader instead
   */
  @Deprecated
  public XMLShredder(
      final IWriteTransaction wtx,
      final XMLStreamReader reader,
      final boolean addAsFirstChild) throws TreetankUsageException {
    this(wtx, getReader(reader), addAsFirstChild, false);
  }

  /**
   * Static convencience method to provide still a stream interface for
   * treetank
   * 
   * @param streamReader
   *            for streams as an input
   * @return {@link XMLEventReader} as return since Treetank works with this
   *         internally
   * @throws TreetankUsageException
   */
  private final static XMLEventReader getReader(
      final XMLStreamReader streamReader) throws TreetankUsageException {
    XMLInputFactory fac = XMLInputFactory.newInstance();
    try {
      return fac.createXMLEventReader(streamReader);
    } catch (final XMLStreamException exc) {
      throw new TreetankUsageException(exc.toString());
    }
  }

  /**
   * Normal constructor to invoke a shredding process on a existing
   * {@link WriteTransaction}
   * 
   * @param wtx
   *            where the new XML Fragment should be placed
   * @param reader
   *            of the XML Fragment
   * @param addAsFirstChild
   *            if the insert is occuring on a node in an existing tree.
   *            <code>false</code> is not possible when wtx is on root node.
   * @throws TreetankUsageException
   *             if insertasfirstChild && updateOnly is both true OR if wtx is
   *             not pointing to doc-root and updateOnly= true
   */
  public XMLShredder(
      final IWriteTransaction wtx,
      final XMLEventReader reader,
      final boolean addAsFirstChild) throws TreetankUsageException {
    this(wtx, reader, addAsFirstChild, false);
  }

  /**
   * Normal constructor to invoke a shredding process on a existing
   * {@link WriteTransaction}
   * 
   * @param wtx
   *            where the new XML Fragment should be placed
   * @param reader
   *            of the XML Fragment
   * @param addAsFirstChild
   *            if the insert is occuring on a node in an existing tree.
   *            <code>false</code> is not possible when wtx is on root node.
   * @param updateOnly
   *            if true, only modified nodes are updated in the structure.
   *            Note that this method is time consuming and makes only use of
   *            the {@link EXMLSerializing#ID} tag when used.
   * @throws TreetankUsageException
   *             if insertasfirstChild && updateOnly is both true OR if wtx is
   *             not pointing to doc-root and updateOnly= true
   */
  public XMLShredder(
      final IWriteTransaction wtx,
      final XMLEventReader reader,
      final boolean addAsFirstChild,
      final boolean updateOnly) throws TreetankUsageException {
    mWtx = wtx;
    mReader = reader;
    if (updateOnly && wtx.getNode().getKind() != ENodes.ROOT_KIND) {
      throw new TreetankUsageException(
          "WriteTransaction must point to doc-root at the beginning!");
    }
    mFirstChildAppend = addAsFirstChild;
    mInsertOnlyModified = updateOnly;
  }

  /**
   * Invoking the shredder.
   */
  public Long call() throws Exception {
    final long revision = mWtx.getRevisionNumber();

    if (mInsertOnlyModified) {
      updateOnly();
    } else {
      insertNewContent();
    }

    mWtx.commit();
    return revision;
  }

  /**
   * Update a shreddered file.
   * 
   * @throws TreetankException
   */
  private void updateOnly() throws TreetankException {
    try {
      final Set<Long> visitedKeys = new HashSet<Long>();

      // setting the maxNodeKey for the compare-wtx
      final long maxNodeKey = mWtx.getMaxNodeKey();

      // setting up boolean-Stack. This stack is for holding the current
      // position to determine if an insertasright-sib should occure.F
      FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();
      leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
          .getStandardProperty());

      // setup up of first element of the data
      XMLEvent event = mReader.nextEvent();
      mWtx.moveToDocumentRoot();
      
      // if structure already exists, make a sync against the current
      // structure
      if (mWtx.getMaxNodeKey() != 0) {
        
        // find the start key for the update operation
        long startkey = (Long) EFixed.ROOT_NODE_KEY.getStandardProperty() + 1;
        while (!mWtx.moveTo(startkey)) {
          startkey++;
        }

        // Iterate over all nodes.(
        do {
          // storing the visited key
          visitedKeys.add(mWtx.getNode().getNodeKey());

          switch (event.getEventType()) {
          case XMLStreamConstants.START_ELEMENT:
            //            // Searching for the attribute with the id at the
            //            // current node
            //            long id = (Long) EFixed.NULL_NODE_KEY.getStandardProperty();
            //            for (Iterator<?> it = ((StartElement) event).getAttributes(); it
            //                .hasNext();) {
            //              final Attribute attribute = (Attribute) it.next();
            //              final String attributeName =
            //                  new String(EXMLSerializing.ID.getBytes());
            //              // checking for id-attribute
            //              if (attribute.getName().getLocalPart().equals(attributeName)) {
            //                id = Long.parseLong(attribute.getValue());
            //                break;
            //              }
            //            }
            //
            //            // found id tag
            //            if (id != (Long) EFixed.NULL_NODE_KEY.getStandardProperty()) {
            //              // moving transaction to key;
            //              if (!mWtx.moveTo(id)) {
            //                mWtx.abort();
            //                throw new TreetankUsageException(
            //                    "Move to",
            //                    Long.toString(id),
            //                    "not successful.",
            //                    "That means that the revisioned file is not suitable to the databasae");
            //              }
            //
            //              // TODO node check: Since setName is now resulting
            //              // in new node, this one should be obsolete but a
            //              // little bit integrity should not be the problem
            //              checkElement((StartElement) event);
            //            }
            //            // not finding any id tag -> just appending the node as
            //            // a new one based on the original XML and the current
            //            // mWtx-position
            //            else {

            /*
             * Check if an element in the shreddered file on the same level
             * equals the current element node.
             */
            final long nodeKey = mWtx.getNode().getNodeKey();
            boolean found = false;
            boolean isRightsibling = false;
            long keyMatches;
            do {
              found = checkElement((StartElement) event);

              if (mWtx.getNode().getNodeKey() != nodeKey) {
                isRightsibling = true;
              }

              keyMatches = mWtx.getNode().getNodeKey();
            } while (!found && mWtx.moveToRightSibling());
            mWtx.moveTo(nodeKey);

            /*
             * If current node in the file which has to be shreddered is found 
             * in one of the right siblings of the current node nodes have been
             * removed, otherwise it has been inserted. If they match at the 
             * current position do nothing.
             */
            if (found && isRightsibling) {
              do {
                mWtx.remove();
              } while (mWtx.moveToRightSibling()
                  && mWtx.getNode().getNodeKey() != keyMatches);
            } else if (!found && isRightsibling) {
              leftSiblingKeyStack =
                  addNewElement(
                      false,
                      leftSiblingKeyStack,
                      (StartElement) event);
            } else {
              leftSiblingKeyStack.pop();
              leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
              leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY.getStandardProperty());
              
              if (!mWtx.getNode().isLeaf()) {
                mWtx.moveToFirstChild(); 
              } else if (mWtx.moveToRightSibling()) {
              } else if (mWtx.getNode().hasParent()) {
                mWtx.moveToParent();
              }
            }

            break;
          case XMLStreamConstants.CHARACTERS:
            final String valFromXML = ((Characters) event).getData().trim();
            // if wtx is text and equal to the val, do no insert ->
            // insert if unequal, identification takes place on the
            // node
            if (mWtx.moveToFirstChild()) {
              boolean foundText = false;
              do {
                if (!(mWtx.getNode().getKind() == ENodes.TEXT_KIND && mWtx
                    .getValueOfCurrentNode()
                    .equals(valFromXML))) {
                  foundText = true;
                }
                if (!foundText) {
                  leftSiblingKeyStack.push(mWtx.getNode().getNodeKey());
                  leftSiblingKeyStack =
                      addNewText(leftSiblingKeyStack, (Characters) event);
                }
              } while (mWtx.moveToRightSibling());
              mWtx.moveToParent();
            } else {
              leftSiblingKeyStack.push((Long) EFixed.ROOT_NODE_KEY
                  .getStandardProperty());
              leftSiblingKeyStack =
                  addNewText(leftSiblingKeyStack, (Characters) event);
            }

            break;
          case XMLStreamConstants.END_ELEMENT:
            leftSiblingKeyStack.pop();
            mWtx.moveTo(leftSiblingKeyStack.peek());
            break;
          }// end switch

          // parsing the next event
          event = mReader.nextEvent();
        } while (mReader.hasNext() && mWtx.getNode().getNodeKey() < maxNodeKey);
      } // if no content is in the XML, a normal insertNewContent is
      // executed
      else {
        insertNewContent();
      }

    } catch (final XMLStreamException exc1) {
      throw new TreetankIOException(exc1);
    }

  }

  /**
   * Insert new content.
   * 
   * @throws TreetankException
   */
  private final void insertNewContent() throws TreetankException {
    try {

      FastStack<Long> leftSiblingKeyStack = new FastStack<Long>();

      leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY
          .getStandardProperty());
      boolean firstElement = true;

      // Iterate over all nodes.
      while (mReader.hasNext()) {

        final XMLEvent event = mReader.nextEvent();
        switch (event.getEventType()) {

        case XMLStreamConstants.START_ELEMENT:
          leftSiblingKeyStack =
              addNewElement(
                  firstElement,
                  leftSiblingKeyStack,
                  (StartElement) event);
          firstElement = false;
          break;

        case XMLStreamConstants.END_ELEMENT:
          leftSiblingKeyStack.pop();
          mWtx.moveTo(leftSiblingKeyStack.peek());
          break;

        case XMLStreamConstants.CHARACTERS:
          leftSiblingKeyStack =
              addNewText(leftSiblingKeyStack, (Characters) event);
          break;
        }
      }
    } catch (final XMLStreamException exc1) {
      throw new TreetankIOException(exc1);
    }
  }

  private final FastStack<Long> addNewElement(
      final boolean firstElement,
      final FastStack<Long> leftSiblingKeyStack,
      final StartElement event) throws TreetankException {
    long key;

    final QName name = event.getName();

    if (firstElement && !mFirstChildAppend) {
      if (mWtx.getNode().getKind() == ENodes.ROOT_KIND) {
        throw new TreetankUsageException(
            "Subtree can not be inserted as sibling of Root");
      }
      key = mWtx.insertElementAsRightSibling(name);
    } else {

      if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
          .getStandardProperty()) {
        key = mWtx.insertElementAsFirstChild(name);
      } else {
        key = mWtx.insertElementAsRightSibling(name);
      }
    }

    leftSiblingKeyStack.pop();
    leftSiblingKeyStack.push(key);
    leftSiblingKeyStack.push((Long) EFixed.NULL_NODE_KEY.getStandardProperty());

    // Parse namespaces.
    for (Iterator<?> it = event.getNamespaces(); it.hasNext();) {
      final Namespace namespace = (Namespace) it.next();
      mWtx.insertNamespace(namespace.getNamespaceURI(), namespace.getPrefix());
      mWtx.moveTo(key);
    }

    // Parse attributes.
    for (Iterator<?> it = event.getAttributes(); it.hasNext();) {
      final Attribute attribute = (Attribute) it.next();
      mWtx.insertAttribute(attribute.getName(), attribute.getValue());
      mWtx.moveTo(key);
    }
    return leftSiblingKeyStack;
  }

  private final FastStack<Long> addNewText(
      final FastStack<Long> leftSiblingKeyStack,
      final Characters event) throws TreetankException {
    final String text = event.getData().trim();
    long key;
    final ByteBuffer textByteBuffer =
        ByteBuffer.wrap(TypedValue.getBytes(text));
    if (textByteBuffer.array().length > 0) {

      if (leftSiblingKeyStack.peek() == (Long) EFixed.NULL_NODE_KEY
          .getStandardProperty()) {
        key =
            mWtx.insertTextAsFirstChild(
                mWtx.keyForName("xs:untyped"),
                textByteBuffer.array());
      } else {
        key =
            mWtx.insertTextAsRightSibling(
                mWtx.keyForName("xs:untyped"),
                textByteBuffer.array());
      }

      leftSiblingKeyStack.pop();
      leftSiblingKeyStack.push(key);

    }
    return leftSiblingKeyStack;
  }

  /**
   * Check if current element matches the element in the shreddered file.
   * 
   * @param event 
   *              StartElement event, from the XML file to shredder.
   * @return true if they are equal, false otherwise.
   */
  private final boolean checkElement(final StartElement event) {
    final long nodeKey = mWtx.getNode().getNodeKey();

    // Check attributes.
    boolean foundAtts = true;
    Iterator<?> it = event.getAttributes();
    for (int i = 0, attCount = mWtx.getNode().getAttributeCount(); i < attCount
        && it.hasNext(); i++) {
      final Attribute attribute = (Attribute) it.next();
      mWtx.moveToAttribute(i);
      if (!attribute.getName().equals(mWtx.getQNameOfCurrentNode())) {
        foundAtts = false;
        break;
      }
      mWtx.moveTo(nodeKey);
    }

    // Check namespaces.
    boolean foundNamesp = true;
    Iterator<?> namespIt = event.getNamespaces();
    for (int i = 0, namespCount = mWtx.getNode().getNamespaceCount(); i < namespCount
        && namespIt.hasNext(); i++) {
      final Namespace namespace = (Namespace) it.next();
      mWtx.moveToNamespace(i);
      if (!namespace.getName().equals(mWtx.getQNameOfCurrentNode())) {
        foundNamesp = false;
        break;
      }
      mWtx.moveTo(nodeKey);
    }

    boolean retVal;
    if (foundAtts && foundNamesp) {
      retVal = true;
    } else {
      retVal = false;
    }
    return retVal;
  }

  /**
   * Main method.
   * 
   * @param args 
   *              Input and output files.
   * @throws Exception 
   *              In case of any exception.
   */
  public static void main(String... args) throws Exception {
    if (args.length < 2 || args.length > 3) {
      System.out.println("Usage: XMLShredder input.xml output.tnk [true/false]");
      System.exit(1);
    }

    System.out.print("Shredding '" + args[0] + "' to '" + args[1] + "' ... ");
    long time = System.currentTimeMillis();
    final File target = new File(args[1]);
    Database.truncateDatabase(target);
    Database.createDatabase(new DatabaseConfiguration(target));
    final IDatabase db = Database.openDatabase(target);
    final ISession session = db.getSession();
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final XMLEventReader reader = createReader(new File(args[0]));
    boolean updateOnly = false;
    if (args.length == 3) {
      updateOnly = Boolean.parseBoolean(args[2]);
    }
    final XMLShredder shredder = new XMLShredder(wtx, reader, true, updateOnly);
    shredder.call();

    wtx.close();
    session.close();
    db.close();

    System.out
        .println(" done [" + (System.currentTimeMillis() - time) + "ms].");
  }

  /**
   * Create a StAX reader.
   * 
   * @param file 
   *              File to shredder.
   * @return an XMLEventReader.
   * @throws IOException
   *              In case of any I/O error.
   * @throws XMLStreamException
   *              In case of any XML parser error.
   */
  public static XMLEventReader createReader(final File file)
      throws IOException,
      XMLStreamException {
    final InputStream in = new FileInputStream(file);
    final XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    final XMLEventReader parser = factory.createXMLEventReader(in);
    return parser;
  }
}
