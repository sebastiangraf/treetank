/*
 * Copyright (c) 2007, Marc Kramis
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

package org.treetank.beanlayer;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Assert;
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
    expectedBean.setStringField("foo");
    expectedBean.setLongField(13L);
    expectedBean.setIntField(14);
    expectedBean.setBooleanField(true);
    expectedBean.setFloatField(13.5f);
    expectedBean.setDoubleField(345.4d);
    expectedBean.setByteArrayField(new byte[] { (byte) 15, (byte) 16 });
    wtx.moveToDocumentRoot();
    final long expectedBeanKey = BeanUtil.write(wtx, expectedBean);

    // Read bean.
    wtx.moveTo(expectedBeanKey);
    final TestBean bean = BeanUtil.read(wtx, TestBean.class);
    TestCase.assertNotNull(bean);
    TestCase.assertEquals(expectedBeanKey, bean.getIdField());
    TestCase.assertEquals("foo", bean.getStringField());
    TestCase.assertEquals(13L, bean.getLongField());
    TestCase.assertEquals(14, bean.getIntField());
    TestCase.assertEquals(2, bean.getByteArrayField().length);
    TestCase.assertEquals((byte) 15, bean.getByteArrayField()[0]);
    TestCase.assertEquals((byte) 16, bean.getByteArrayField()[1]);
    TestCase.assertEquals(true, bean.isBooleanField());
    TestCase.assertEquals(13.5f, bean.getFloatField());
    TestCase.assertEquals(345.4d, bean.getDoubleField());
    TestCase.assertEquals(expectedBeanKey, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testNullBean() throws Exception {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();

    // Write bean.
    final TestBean expectedBean = new TestBean();
    expectedBean.setStringField(null);
    expectedBean.setByteArrayField(null);
    wtx.moveToDocumentRoot();
    final long expectedBeanKey = BeanUtil.write(wtx, expectedBean);

    // Read bean.
    wtx.moveTo(expectedBeanKey);
    final TestBean bean = BeanUtil.read(wtx, TestBean.class);
    TestCase.assertNotNull(bean);
    TestCase.assertEquals(null, bean.getByteArrayField());
    TestCase.assertEquals(null, bean.getStringField());
    TestCase.assertEquals(expectedBeanKey, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testWrongBean() throws Exception {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();

    // Write bean.
    final TestBean expectedBean = new TestBean();
    expectedBean.setStringField(null);
    expectedBean.setByteArrayField(null);
    wtx.moveToDocumentRoot();
    final long expectedBeanKey = BeanUtil.write(wtx, expectedBean);

    // Read bean.
    wtx.moveTo(expectedBeanKey);
    try {
      BeanUtil.read(wtx, Integer.class);
      Assert
          .fail("Should fail because Integer.class is not what was written before.");
    } catch (Exception e) {
      // Must fail.
    }

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testSubelementBean() throws Exception {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();

    // Write bean.
    final TestBean expectedBean = new TestBean();
    wtx.moveToDocumentRoot();
    final long expectedBeanKey = BeanUtil.write(wtx, expectedBean);
    wtx.insertElementAsFirstChild("subelement", "");
    wtx.insertTextAsFirstChild("hello, world");

    // Read bean.
    wtx.moveTo(expectedBeanKey);
    final TestBean bean = BeanUtil.read(wtx, TestBean.class);
    TestCase.assertNotNull(bean);
    TestCase.assertEquals(expectedBeanKey, bean.getIdField());
    TestCase.assertEquals(expectedBeanKey, wtx.getNodeKey());

    wtx.abort();
    wtx.close();
    session.close();

  }

  @Test
  public void testRemoveBean() throws Exception {

    final ISession session = Session.beginSession(PATH);
    final IWriteTransaction wtx = session.beginWriteTransaction();

    // Write bean.
    final TestBean expectedBean1 = new TestBean();
    expectedBean1.setStringField("foo");
    wtx.moveToDocumentRoot();
    final long expectedBeanKey1 = BeanUtil.write(wtx, expectedBean1);

    final TestBean expectedBean2 = new TestBean();
    expectedBean2.setStringField("foobar");
    wtx.moveToDocumentRoot();
    final long expectedBeanKey2 = BeanUtil.write(wtx, expectedBean2);

    // Remove bean.
    wtx.moveTo(expectedBeanKey1);
    BeanUtil.remove(wtx, expectedBean1);
    Assert.assertEquals(false, wtx.moveTo(expectedBeanKey1));

    wtx.moveTo(expectedBeanKey2);
    BeanUtil.remove(wtx, expectedBean2);
    Assert.assertEquals(false, wtx.moveTo(expectedBeanKey2));

    wtx.abort();
    wtx.close();
    session.close();

  }
}
