/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id$
 * 
 */

package org.treetank.nodelayer;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.treetank.nodelayer.IReadTransaction;
import org.treetank.nodelayer.ISession;
import org.treetank.nodelayer.IWriteTransaction;
import org.treetank.nodelayer.Session;
import org.treetank.utils.IConstants;
import org.treetank.utils.UTF;


public class UpdateTest {

  public static final String TEST_PATH = "generated/UpdateTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(TEST_PATH).delete();
  }

  @Test
  public void testInsertChild() throws Exception {

    ISession session = new Session(TEST_PATH);

    // Document root.
    IWriteTransaction trx = session.beginWriteTransaction();
    trx.insertRoot("test");
    session.commit();

    IReadTransaction rTrx = session.beginReadTransaction();
    assertEquals(1L, rTrx.revisionSize());
    assertEquals(1L, rTrx.revisionKey());

    // Insert 100 children.
    for (int i = 1; i <= 100; i++) {
      session = new Session(TEST_PATH);
      trx = session.beginWriteTransaction();
      trx.moveToRoot();
      trx.insertFirstChild(IConstants.TEXT, "", "", "", UTF.convert(Integer
          .toString(i)));
      session.commit();
      session.close();

      rTrx = session.beginReadTransaction();
      rTrx.moveToRoot();
      rTrx.moveToFirstChild();
      assertEquals(Integer.toString(i), new String(rTrx.getValue()));
      assertEquals(i + 1L, rTrx.revisionSize());
      assertEquals(i + 1L, rTrx.revisionKey());
    }
    
    session = new Session(TEST_PATH);
    rTrx = session.beginReadTransaction();
    rTrx.moveToRoot();
    rTrx.moveToFirstChild();
    assertEquals(Integer.toString(100), new String(rTrx.getValue()));
    assertEquals(101L, rTrx.revisionSize());
    assertEquals(101L, rTrx.revisionKey());
    session.close();

  }

}
