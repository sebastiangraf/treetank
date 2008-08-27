/*
 * Copyright (c) 2008, Tina Scherer (Master Thesis), University of Konstanz
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

package org.treetank.xpath;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.treetank.api.IReadTransaction;
import org.treetank.api.ISession;
import org.treetank.axislayer.IAxisTest;
import org.treetank.sessionlayer.Session;
import org.treetank.sessionlayer.SessionConfiguration;
import org.treetank.xmllayer.XMLShredder;

/**
 * Performes the XPathFunctionalityTest provided on 
 * <a href="http://sole.dimi.uniud.it/~massimo.franceschet/xpathmark/FT.html">
 * XPathMark</a>
 * 
 * @author Tina Scherer
 */
public class XPathFunctionTest {

  public static final String XML =
      "src"
          + File.separator
          + "test"
          + File.separator
          + "resources"
          + File.separator
          + "alphabet.xml";

  public static final String PATH =
      "target"
          + File.separator
          + "tnk"
          + File.separator
          + "XPathFunctionTest.tnk";

  @Before
  public void setUp() {

    Session.removeSession(PATH);
  }

  @Test
  public void testA_Axes() throws IOException {

    // Build simple test tree.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/*"), new long[] {
        21L,
        23L,
        28L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/parent::*"),
        new long[] { 8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/descendant::*"),
        new long[] { 21L, 23L, 24L, 26L, 28L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/descendant-or-self::*"),
        new long[] { 19L, 21L, 23L, 24L, 26L, 28L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/ancestor::*"),
        new long[] { 8L, 2L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/ancestor-or-self::*"),
        new long[] { 19L, 8L, 2L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//L/following-sibling::*"), new long[] { 30L, 35L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//L/preceding-sibling::*"), new long[] { 14L, 9L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/following::*"),
        new long[] { 30L, 31L, 33L, 35L, 36L, 38L, 40L, 41L, 43L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/preceding::*"),
        new long[] { 17L, 15L, 14L, 12L, 10L, 9L, 6L, 4L, 3L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/self::*"),
        new long[] { 19L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/@id/parent::*"),
        new long[] { 8L });

    rtx.close();
    session.close();

  }

  @Test
  public void testP_Filters() throws IOException {

    // Build simple test tree.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[L]"),
        new long[] { 8L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[parent::L]"),
        new long[] { 21L, 23L, 28L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[descendant::L]"),
        new long[] { 2L, 8L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//*[descendant-or-self::L]"), new long[] { 2L, 8L, 19L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[ancestor::L]"),
        new long[] { 21L, 23L, 28L, 24L, 26L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[ancestor-or-self::L]"),
        new long[] { 19L, 21L, 23L, 28L, 24L, 26L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//*[following-sibling::L]"), new long[] { 9L, 14L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//*[preceding-sibling::L]"), new long[] { 30L, 35L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[following::L]"),
        new long[] { 3L, 4L, 6L, 9L, 14L, 10L, 12L, 15L, 17L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[preceding::L]"),
        new long[] { 40L, 30L, 35L, 31L, 33L, 36L, 38L, 41L, 43L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[self::L]"),
        new long[] { 19L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[@id]"), new long[] {
        2L,
        3L,
        8L,
        40L,
        4L,
        6L,
        9L,
        14L,
        19L,
        30L,
        35L,
        10L,
        12L,
        15L,
        17L,
        21L,
        23L,
        28L,
        24L,
        26L,
        31L,
        33L,
        36L,
        38L,
        41L,
        43 });

    rtx.moveTo(40L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "preceding::node()"),
        new long[] {
            39L,
            38L,
            37L,
            36L,
            35L,
            34L,
            33L,
            32L,
            31L,
            30L,
            29L,
            28L,
            27L,
            26L,
            25L,
            24L,
            23L,
            22L,
            21L,
            20L,
            19L,
            18L,
            17L,
            16L,
            15L,
            14L,
            13L,
            12L,
            11L,
            10L,
            9L,
            8L,
            7L,
            6L,
            5L,
            4L,
            3L });

    rtx.moveTo(6L);
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "following::node()"),
        new long[] {
            8L,
            9L,
            10L,
            11L,
            12L,
            13L,
            14L,
            15L,
            16L,
            17L,
            18L,
            19L,
            20L,
            21L,
            22L,
            23L,
            24L,
            25L,
            26L,
            27L,
            28L,
            29L,
            30L,
            31L,
            32L,
            33L,
            34L,
            35L,
            36L,
            37L,
            38L,
            39L,
            40L,
            41L,
            42L,
            43L,
            44L });

    rtx.close();
    session.close();

  }

  @Test
  public void testT_NodeTests() throws IOException {

    // Build simple test tree.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/text()"),
        new long[] { 20L, 22L });

    //comments are not supported yet
    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/comment()"),
        new long[] {});

    //porcessing instructions are not supported yet
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//L/processing-instruction()"), new long[] {});

    //porcessing instructions are not supported yet
    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//L/processing-instruction(\"myPI\")"), new long[] {});

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//L/node()"),
        new long[] { 20L, 21L, 22L, 23L, 28L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, " //L/N"),
        new long[] { 23L });

    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//L/*"), new long[] {
        21L,
        23L,
        28L });

    rtx.close();
    session.close();

  }

  @Test
  public void testQ_Operators() throws IOException {

    // Build simple test tree.
    XMLShredder.shred(XML, new SessionConfiguration(PATH));

    // Verify.
    final ISession session = Session.beginSession(PATH);
    final IReadTransaction rtx = session.beginReadTransaction();
    rtx.moveToDocumentRoot();

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[preceding::Q]"),
        new long[] { 40L, 30L, 35L, 31L, 33L, 36L, 38L, 41L, 43L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//*[child::* and preceding::Q]"), new long[] { 40L, 30L, 35L });

    //    IAxisTest.testIAxisConventions(new XPathAxis(rtx, "//*[fn:not(child::*) and preceding::Q]"),
    //        new long[] {31L, 33L, 36L, 38L, 41L, 43L});

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//*[preceding::L or following::L]"), new long[] {
        3L,
        40L,
        4L,
        6L,
        9L,
        14L,
        30L,
        35L,
        10L,
        12L,
        15L,
        17L,
        31L,
        33L,
        36L,
        38L,
        41L,
        43L });

    IAxisTest.testIAxisConventions(new XPathAxis(
        rtx,
        "//L/ancestor::* | //L/descendant::*"), new long[] {
        8L,
        2L,
        21L,
        23L,
        24L,
        26L,
        28L });

    //    IAxisTest.testIAxisConventions(new XPathAxis(rtx, 
    //        "//*[.=\"happy-go-lucky man\"]"), new long[] { 13L});

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[@pre > 12 and @post < 15]"),
        new long[] { 21L, 23L, 28L, 24L, 26L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[@pre != @post]"),
        new long[] {
            2L,
            3L,
            8L,
            40L,
            4L,
            6L,
            19L,
            10L,
            12L,
            15L,
            17L,
            21L,
            23L,
            28L,
            24L,
            26L,
            31L,
            33L,
            36L,
            38L,
            41L,
            43L });

    IAxisTest.testIAxisConventions(
        new XPathAxis(rtx, "//*[@pre mod 2 = 0]"),
        new long[] {
            3L,
            40L,
            6L,
            9L,
            19L,
            30L,
            12L,
            15L,
            23L,
            26L,
            33L,
            36L,
            43L });

    //  IAxisTest.testIAxisConventions(new XPathAxis(rtx,
    //  "//*[((@post * @post + @pre * @pre) div (@post + @pre)) > ((@post - @pre) * (@post - @pre))] "), new long[] { });

    rtx.close();
    session.close();

  }

  //TODO: functions!

}