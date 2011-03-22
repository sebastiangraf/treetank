/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

/**
 * <h1>Access to Treetank</h1>
 * <p>
 * The access semantics is as follows:
 * <ul>
 * <li>There can only be a single {@link org.treetank.api.IDatabase} instance per Database-Folder</li>
 * <li>There can only be a single {@link org.treetank.api.ISession} instance per
 * {@link org.treetank.api.IDatabase}</li>
 * <li>There can only be a single {@link org.treetank.api.IWriteTransaction} instance per
 * {@link org.treetank.api.ISession}</li>
 * <li>There can be multiple {@link org.treetank.api.IReadTransaction} instances per
 * {@link org.treetank.api.ISession}.</li>
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
package org.treetank.access;

