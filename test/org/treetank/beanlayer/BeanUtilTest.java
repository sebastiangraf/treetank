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
 * $Id: ParentAxisTest.java 3396 2007-11-05 12:43:35Z kramis $
 */

package org.treetank.beanlayer;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.ISession;
import org.treetank.api.IWriteTransaction;
import org.treetank.sessionlayer.Session;

public class BeanUtilTest {

  public static final String PATH =
      "generated" + File.separator + "BeanUtilTest.tnk";

  @Before
  public void setUp() {
    Session.removeSession(PATH);
  }

  @Test
  public void testBean() throws Exception {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();

    // Write bean.
    final TestBean expectedBean = new TestBean();
    expectedBean.setToken("foo");
    expectedBean.setDate(13L);
    wtx.moveToDocumentRoot();
    final long expectedBeanKey = BeanUtil.write(wtx, expectedBean);

    // Read bean.
    wtx.moveTo(expectedBeanKey);
    final TestBean bean = BeanUtil.read(wtx, TestBean.class);
    TestCase.assertNotNull(bean);
    TestCase.assertEquals("foo", bean.getToken());
    TestCase.assertEquals(13L, bean.getDate());
    TestCase.assertEquals(expectedBeanKey, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

}
