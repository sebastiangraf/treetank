package org.treetank.axislayer;

  import java.io.File;

  import org.junit.Before;
  import org.junit.Test;
  import org.treetank.api.ISession;
  import org.treetank.api.IWriteTransaction;
  import org.treetank.sessionlayer.Session;
  import org.treetank.utils.TestDocument;

  public class FollowingSiblingAxisTest {

    public static final String PATH =
        "generated" + File.separator + "FollowingSiblingAxisTest.tnk";

    @Before
    public void setUp() {
      Session.removeSession(PATH);
    }

    @Test
    public void testAxisConventions() {
      final ISession session = Session.beginSession(PATH);
      final IWriteTransaction wtx = session.beginWriteTransaction();
      TestDocument.create(wtx);

      wtx.moveTo(9L);
      IAxisTest
          .testIAxisConventions(new FollowingSiblingAxis(wtx), 
              new long[] {10L});

      wtx.moveTo(4L);
      IAxisTest.testIAxisConventions(new FollowingSiblingAxis(wtx), 
          new long[] {7L, 8L, 11L});

      wtx.moveTo(3L);
      IAxisTest.testIAxisConventions(new FollowingSiblingAxis(wtx), 
          new long[] {4L, 7L, 8L, 11L});


      wtx.moveTo(2L);
      IAxisTest.testIAxisConventions(new FollowingSiblingAxis(wtx), 
          new long[] {});
      
      wtx.moveTo(8L);
      wtx.moveToAttribute(0);
      IAxisTest.testIAxisConventions(new FollowingSiblingAxis(wtx), 
          new long[] {});

      wtx.abort();
      wtx.close();
      session.close();

    }

   
  }

