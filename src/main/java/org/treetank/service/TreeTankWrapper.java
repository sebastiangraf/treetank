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
 * $Id$
 */

package org.treetank.service;

import java.io.OutputStream;

import org.treetank.api.IAxis;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.ItemList;
import org.treetank.sessionlayer.Session;
import org.treetank.xmllayer.XMLSerializer;
import org.treetank.xpath.XPathAxis;

public class TreeTankWrapper {

  final String UTF8 = "UTF-8";

  final byte[] BEGIN_REST_ITEM =
      { 60, 114, 101, 115, 116, 58, 105, 116, 101, 109, 62 };

  final byte[] END_REST_ITEM =
      { 60, 47, 114, 101, 115, 116, 58, 105, 116, 101, 109, 62 };

  private final ISession session;

  public TreeTankWrapper(String path) {
    session = Session.beginSession(path);
  }

  public final long putText(final long id, final String value) {
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long revision = wtx.getRevisionNumber();
    try {
      if (wtx.moveTo(id)) {
        wtx.setValue(value);
        wtx.commit();
      }
      wtx.close();
      return revision;
    } catch (Exception e) {
      wtx.abort();
      wtx.close();
      throw new RuntimeException(
          "Could not overwrite text node with id=" + id,
          e);
    }
  }

  public final long delete(final long id) {
    final IWriteTransaction wtx = session.beginWriteTransaction();
    final long revision = wtx.getRevisionNumber();
    try {
      if (wtx.moveTo(id)) {
        wtx.remove();
        wtx.commit();
      }
      wtx.close();
      return revision;
    } catch (Exception e) {
      wtx.abort();
      wtx.close();
      throw new RuntimeException("Could not delete node with id=" + id, e);
    }
  }

  public final long getLastRevisionNumber() {
    final IReadTransaction rtx = session.beginReadTransaction();
    final long lastRevisionNumber = rtx.getRevisionNumber();
    rtx.close();
    return lastRevisionNumber;
  }

  public final boolean isValid(final long revision, final long id) {
    boolean isValid = false;
    try {
      final IReadTransaction rtx =
          session.beginReadTransaction(revision, new ItemList());
      isValid = rtx.moveTo(id);
      rtx.close();
    } catch (Exception e) {
      isValid = false;
    }
    return isValid;
  }

  public final void get(
      final OutputStream out,
      final long revision,
      final long id) throws Exception {
    final IReadTransaction rtx =
        session.beginReadTransaction(revision, new ItemList());
    try {
      if (rtx.moveTo(id)) {
        out.write(BEGIN_REST_ITEM);
        new XMLSerializer(rtx, out, false, true).run();
        out.write(END_REST_ITEM);
      }
    } finally {
      rtx.close();
    }
  }

  public final void get(
      final OutputStream out,
      final long revision,
      final long id,
      final String expression) throws Exception {
    final IReadTransaction rtx =
        session.beginReadTransaction(revision, new ItemList());
    try {
      if (rtx.moveTo(id)) {
        final IAxis axis = new XPathAxis(rtx, expression);
        for (final long key : axis) {
          out.write(BEGIN_REST_ITEM);
          if (key >= 0) {
            new XMLSerializer(rtx, out, false, true).run();
          } else {
            out.write(rtx.getValue().getBytes(UTF8));
          }
          out.write(END_REST_ITEM);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      rtx.close();
    }
  }

  public final void close() {
    session.close();
  }

}
