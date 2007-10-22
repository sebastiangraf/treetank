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

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
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

  @Ignore
  public void testSAXGenerator() throws Exception {

    // Setup expected session.
    final ISession session = new Session(PATH);
    final IWriteTransaction trx = session.beginWriteTransaction();
    TestDocument.create(trx);

    final SAXGenerator generator = new SAXGenerator(trx, false);
    generator.start();
    generator.join();

    session.commit();
    session.close();

  }

}
