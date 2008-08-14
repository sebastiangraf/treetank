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

import java.io.IOException;
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

  public final long putText(final long id, final String value)
      throws TreeTankException {
    IWriteTransaction wtx = null;
    long revision = 0;
    try {
      wtx = session.beginWriteTransaction();
      revision = wtx.getRevisionNumber();
      if (wtx.moveTo(id)) {
        wtx.setValue(value);
        wtx.commit();
      } else {
        throw new TreeTankException(404, "Node with id=" + id + " not found.");
      }
      return revision;
    } catch (TreeTankException te) {
      wtx.abort();
      throw te;
    } finally {
      if (wtx != null) {
        wtx.close();
      }
    }
  }

  public final long delete(final long id) throws TreeTankException {
    IWriteTransaction wtx = null;
    long revision = 0;
    try {
      wtx = session.beginWriteTransaction();
      revision = wtx.getRevisionNumber();
      if (wtx.moveTo(id)) {
        wtx.remove();
        wtx.commit();
      } else {
        throw new TreeTankException(404, "Node with id=" + id + " not found.");
      }
      return revision;
    } catch (TreeTankException te) {
      wtx.abort();
      throw te;
    } finally {
      if (wtx != null) {
        wtx.close();
      }
    }
  }

  public final long getLastRevision() {
    IReadTransaction rtx = null;
    long lastRevision = 0;
    try {
      rtx = session.beginReadTransaction();
      lastRevision = rtx.getRevisionNumber();
    } finally {
      if (rtx != null) {
        rtx.close();
      }
    }
    return lastRevision;
  }

  public final long checkRevision(final long revision) throws TreeTankException {
    IReadTransaction rtx = null;
    long checkedRevision = revision;
    try {
      rtx = session.beginReadTransaction(revision);
    } catch (Exception e) {
      throw new TreeTankException(404, "Revision=" + revision + " not found.");
    } finally {
      if (rtx != null) {
        rtx.close();
      }
    }
    return checkedRevision;
  }

  public final void get(
      final OutputStream out,
      final long revision,
      final long id) throws TreeTankException {
    IReadTransaction rtx = null;
    try {
      rtx = session.beginReadTransaction(revision);
      if (rtx.moveTo(id)) {
        out.write(BEGIN_REST_ITEM);
        new XMLSerializer(rtx, out, false, true).run();
        out.write(END_REST_ITEM);
      } else {
        throw new TreeTankException(404, "Node with id=" + id + " not found.");
      }
    } catch (TreeTankException te) {
      throw te;
    } catch (IOException ie) {
      throw new TreeTankException(500, ie.getMessage(), ie);
    } finally {
      if (rtx != null) {
        rtx.close();
      }
    }
  }

  public final void get(
      final OutputStream out,
      final long revision,
      final long id,
      final String expression) throws TreeTankException {
    IReadTransaction rtx = null;
    try {
      rtx = session.beginReadTransaction(revision, new ItemList());
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
      } else {
        throw new TreeTankException(404, "Node with id=" + id + " not found.");
      }
    } catch (TreeTankException te) {
      throw te;
    } catch (IOException ie) {
      throw new TreeTankException(500, ie.getMessage(), ie);
    } finally {
      if (rtx != null) {
        rtx.close();
      }
    }
  }

  public final void close() {
    session.close();
  }

}
