/*
 * TreeTank - Embedded Native XML Database
 * 
 * Copyright 2007 Marc Kramis
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * $Id:UpdateTest.java 3019 2007-10-10 13:28:24Z kramis $
 */

package org.treetank.sessionlayer;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.utils.TestDocument;

public class AttributeAndNamespaceTest {

  public static final String PATH =
      "generated" + File.separator + "AttributeAndNamespaceTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testAttribute() throws IOException {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(2L);
    TestCase.assertEquals(1, wtx.getAttributeCount());
    TestCase.assertEquals("i", wtx.getAttributeLocalPart(0));

    wtx.moveTo(8L);
    TestCase.assertEquals(1, wtx.getAttributeCount());
    TestCase.assertEquals("x", wtx.getAttributeLocalPart(0));
    TestCase.assertEquals("p", wtx.getAttributePrefix(0));
    TestCase.assertEquals("ns", wtx.getAttributeURI(0));

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testNamespace() throws IOException {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();
    TestDocument.create(wtx);

    wtx.moveTo(2L);
    TestCase.assertEquals(1, wtx.getNamespaceCount());
    TestCase.assertEquals("p", wtx.getNamespacePrefix(0));
    TestCase.assertEquals("ns", wtx.getNamespaceURI(0));

    wtx.abort();
    wtx.close();
    session.close();

  }

}
