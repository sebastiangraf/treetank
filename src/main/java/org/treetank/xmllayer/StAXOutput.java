/*
 * Copyright (c) 2008, Johannes Lichtenberger (HiWi), University of Konstanz
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.events.XMLEvent;

import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.sessionlayer.Session;

/**
 * Takes a TreeTank file and outputs an XML document.
 * 
 * @author johannes
 *
 */
public class StAXOutput {

  /** Variable used for debugging. */
  private static final boolean VERBOSE = false;

  private static boolean emptyElement = false;

  /**
   * Output XML file into a StringBuilder.
   * 
   * @param parser 
   *               TreeTankStreamReader to read from.
   * @return StringBuilder with the XML-document.
   */
  public final static String output(final TreeTankStreamReader parser) {

    try {
      final StringBuilder sb = new StringBuilder();

      // XML decl.
      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\""
          + " standalone=\"yes\"?>");

      // Iterate over all nodes.
      while (parser.hasNext()) {

        switch (parser.next()) {
        case XMLEvent.PROCESSING_INSTRUCTION:
          if (VERBOSE) {
            System.out.println("PROCESS INSTRUCTION: "
                + parser.getPITarget()
                + " "
                + parser.getPIData());
          }

          emptyElement(sb);

          emptyElement = false;

          sb.append("<?");
          sb.append(parser.getPITarget());
          sb.append(" ");
          sb.append(parser.getPIData());
          sb.append("?>");

          break;

        case XMLEvent.START_ELEMENT:
          if (VERBOSE) {
            System.out.println("ELEMENT: " + parser.getName());
          }

          emptyElement(sb);

          sb.append("<");
          sb.append(parser.getName());

          // Parse namespaces.
          for (int i = 0, l = parser.getNamespaceCount(); i < l; i++) {
            if (i == 0) {
              sb.append(" ");
            }

            final String prefix = parser.getNamespacePrefix(i);
            if (prefix == null || prefix.length() == 0) {
              sb.append("xmlns=\"");
              sb.append(parser.getNamespaceURI(i));
              sb.append("\"");
            } else {
              sb.append("xmlns:");
              sb.append(prefix);
              sb.append("=\"");
              sb.append(parser.getNamespaceURI(i));
              sb.append("\"");
            }
          }

          // Parse attributes.
          for (int i = 0, l = parser.getAttributeCount(); i < l; i++) {
            sb.append(" ");

            //            if (parser.getAttributePrefix(i).equalsIgnoreCase("null")) {
            if (parser.getAttributePrefix(i) == null) {
              sb.append(parser.getAttributeLocalName(i));
              sb.append("=\"");
              sb.append(parser.getAttributeValue(i));
              sb.append("\"");
            } else {
              sb.append(parser.getAttributeName(i));
              sb.append("=\"");
              sb.append(parser.getAttributeValue(i));
              sb.append("\"");
            }
          }

          if (VERBOSE) {
            System.out.println(sb.toString());
          }

          emptyElement = true;

          break;

        case XMLEvent.END_ELEMENT:
          if (VERBOSE) {
            System.out.println("END ELEMENT: " + parser.getName());
          }

          if (emptyElement) {
            sb.append("/>");
            emptyElement = false;
          } else {
            sb.append("</");
            sb.append(parser.getName());
            sb.append(">");
          }

          if (VERBOSE) {
            System.out.println(sb.toString());
          }

          break;

        case XMLEvent.CHARACTERS:
          if (VERBOSE) {
            System.out.println("TEXT: " + parser.getText());
          }

          emptyElement(sb);

          emptyElement = false;

          sb.append(parser.getText());

          break;

        case XMLEvent.COMMENT:
          if (VERBOSE) {
            System.out.println("COMMENT: " + parser.getText());
          }

          emptyElement(sb);

          emptyElement = false;

          sb.append("<!-- ");
          sb.append(parser.getText());
          sb.append(" -->");

          break;

        case XMLEvent.START_DOCUMENT:
          break;

        default:
          // Do nothing.
        }
      }

      parser.close();

      if (VERBOSE) {
        System.out.println("String Builder: " + sb.toString());
      }

      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static final void emptyElement(final StringBuilder sb) {
    if (emptyElement) {
      sb.append(">");
    }
  }

  /**
   * Main method.
   * 
   * @param args First Argument is the TreeTank file, second argument is 
   *             the (XML-) file to write to.
   */
  public static final void main(final String[] args) {
    if (args.length < 2 || args.length > 3) {
      System.out.println("Usage: XMLOutputter input.tnk output.xml [key]");
      System.exit(1);
    }

    try {
      System.out
          .print("Outputting '" + args[0] + "' to '" + args[1] + "' ... ");
      long time = System.currentTimeMillis();
      new File(args[1]).delete();

      final ISession session = Session.beginSession(args[0]);
      final IReadTransaction rtx = session.beginReadTransaction();
      final TreeTankStreamReader parser = new TreeTankStreamReader(rtx);

      final String str = output(parser);

      Writer fw = null;

      try {
        fw = new FileWriter(args[1]);
        fw.write(str);
      } catch (IOException e) {
        System.err.println("Couldn't create file: " + args[1]);
      } finally {
        if (fw != null) {
          try {
            fw.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      System.out.println(" done ["
          + (System.currentTimeMillis() - time)
          + "ms].");

      session.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
