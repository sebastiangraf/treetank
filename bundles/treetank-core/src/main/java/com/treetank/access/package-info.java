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

/**
 * <h1>Access to Treetank</h1>
 * <p>
 * The access semantics is as follows:
 * <ul>
 * <li>There can only be a single {@link com.treetank.api.IDatabase} instance per Database-Folder</li>
 * <li>There can only be a single {@link com.treetank.api.ISession} instance per
 * {@link com.treetank.api.IDatabase}</li>
 * <li>There can only be a single {@link com.treetank.api.IWriteTransaction} instance per
 * {@link com.treetank.api.ISession}</li>
 * <li>There can be multiple {@link com.treetank.api.IReadTransaction} instances per
 * {@link com.treetank.api.ISession}.</li>
 * </ul>
 * </p>
 * <p>
 * Code examples:
 * 
 * <pre>
 * final ISession someSession = Session.beginSession(&quot;example.tnk&quot;);
 * final ISession otherSession = Session.beginSession(&quot;other.tnk&quot;);
 * 
 * // ! final ISession concurrentSession = Session.beginSessoin(&quot;other.tnk&quot;);
 * // ! Error: There already is a session bound to &quot;other.tnk&quot; (otherSession).
 * 
 * final IWriteTransaction someWTX = someSession.beginWriteTransaction();
 * final IWriteTransaction otherWTX = otherSession.beginWriteTransaction();
 * 
 * // ! final IWriteTransaction concurrentWTX = otherSession.beginWriteTransaction();
 * // ! Error: There already is a write transaction running (wtx).
 * 
 * final IReadTransaction someRTX = someSession.beginReadTransaction();
 * final IReadTransaction someConcurrentRTX = someSession.beginReadTransaction();
 * 
 * // ! otherSession.close();
 * // ! Error: All transactions must be closed first.
 * 
 * otherWTX.commit();
 * otherWTX.abort();
 * otherWTX.commit();
 * otherWTX.close();
 * otherSession.close();
 * 
 * someWTX.abort();
 * someWTX.close();
 * someRTX.close();
 * someConcurrentRTX.close();
 * someSession.close();
 * </pre>
 * 
 * </p>
 * <p>
 * Best practice to safely manipulate a TreeTank:
 * 
 * <pre>
 *         final ISession session = Session.beginSession("example.tnk");
 *         final IWriteTransaction wtx = session.beginWriteTransaction();
 *         try {
 *           wtx.insertElementAsFirstChild("foo", "", "");
 *           ...
 *           wtx.commit();
 *         } catch (TreetankException e) {
 *           wtx.abort();
 *           throw new RuntimeException(e);
 *         } finally {
 *           wtx.close();
 *         }
 *         session.close(); // Might also stand in the finally...
 * </pre>
 * 
 * </p>
 * 
 * @author Marc Kramis, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 */
package com.treetank.access;

