/**
<h1>Access to Treetank</h1>
    <p>
      The access semantics is as follows:
      <li>There can only be a single Database instance per JVM and TreeTank directory.
      <li>There can only be a single Session instance per JVM and Database
      <li>There can only be a single WriteTransaction instance per Session.
      <li>There can be multiple ReadTransaction instances per session.
    </p>
    <p>
      Code examples:
      <pre>
        final ISession someSession = Session.beginSession("example.tnk");
        final ISession otherSession = Session.beginSession("other.tnk");
        
        //! final ISession concurrentSession = Session.beginSessoin("other.tnk");
        //! Error: There already is a session bound to "other.tnk" (otherSession).
        
        final IWriteTransaction someWTX = someSession.beginWriteTransaction();
        final IWriteTransaction otherWTX = otherSession.beginWriteTransaction();
        
        //! final IWriteTransaction concurrentWTX = otherSession.beginWriteTransaction();
        //! Error: There already is a write transaction running (wtx).
        
        final IReadTransaction someRTX = someSession.beginReadTransaction();
        final IReadTransaction someConcurrentRTX = someSession.beginReadTransaction();
        
        //! otherSession.close();
        //! Error: All transactions must be closed first.
        
        otherWTX.commit();
        otherWTX.abort();
        otherWTX.commit();
        otherWTX.close();
        otherSession.close();
        
        someWTX.abort();
        someWTX.close();
        someRTX.close();
        someConcurrentRTX.close();
        someSession.close();
      </pre>
    </p>
    <p>
      Best practice to safely manipulate a TreeTank:
      <pre>
        final ISession session = Session.beginSession("example.tnk");
        final IWriteTransaction wtx = session.beginWriteTransaction();
        try {
          wtx.insertElementAsFirstChild("foo", "", "");
          ...
          wtx.commit();
        } catch (TreetankException e) {
          wtx.abort();
          throw new RuntimeException(e);
        } finally {
          wtx.close();
        }
        session.close(); // Might also stand in the finally...        
      </pre>
    </p>
 *
 * @author Marc Kramis, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 */
package com.treetank.saxon.evaluator;

