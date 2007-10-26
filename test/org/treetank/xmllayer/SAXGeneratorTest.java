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
 * $Id$
 */

package org.treetank.xmllayer;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;
import org.treetank.utils.TestDocument;

public class SAXGeneratorTest {

  public static final String PATH =
      "generated" + File.separator + "SAXGeneratorTest.tnk";

  @Before
  public void setUp() throws Exception {
    new File(PATH).delete();
  }

  @Test
  public void testSAXGenerator() {
    try {
      // Setup session.
      final ISession session = Session.beginSession(PATH);
      final IWriteTransaction wtx = session.beginWriteTransaction();
      TestDocument.create(wtx);
      wtx.commit();

      // Generate from this session.
      final Writer writer = new StringWriter();
      final IReadTransaction rtx = session.beginReadTransaction();
      final SAXGenerator generator =
          new SAXGenerator(new DescendantAxis(rtx), writer, false);
      generator.run();
      TestCase.assertEquals(TestDocument.XML, writer.toString());
      rtx.close();
      session.close();
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }

}
