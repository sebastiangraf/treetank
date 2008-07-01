
package org.treetank.xpath.comparators;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.treetank.xpath.comparators.CompKind;
import org.treetank.xpath.functions.XPathError;
import org.treetank.xpath.types.Type;

public class CompKindTest {

  @Test
  public void testEQ() {

    assertEquals(CompKind.EQ.compare("2.0", "2", Type.DOUBLE), true);
    assertEquals(CompKind.EQ.compare("2.0", "2.01", Type.DOUBLE), false);
    assertEquals(CompKind.EQ.compare("2.0", "4.0", Type.DOUBLE), false);
    assertEquals(CompKind.EQ.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DOUBLE), true);
    assertEquals(CompKind.EQ.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DOUBLE), false);

    assertEquals(CompKind.EQ.compare("2.0", "2", Type.DECIMAL), true);
    assertEquals(CompKind.EQ.compare("2.0", "2.01", Type.DECIMAL), false);
    assertEquals(CompKind.EQ.compare("2.0", "4.0", Type.DECIMAL), false);
    assertEquals(CompKind.EQ.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DECIMAL), true);
    assertEquals(CompKind.EQ.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DECIMAL), false);

    assertEquals(CompKind.EQ.compare("2.0", "2", Type.FLOAT), true);
    assertEquals(CompKind.EQ.compare("2.0", "2.01", Type.FLOAT), false);
    assertEquals(CompKind.EQ.compare("2.0", "4.0", Type.FLOAT), false);
    assertEquals(CompKind.EQ.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MAX_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.EQ.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MIN_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.EQ.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.EQ.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.FLOAT), false);

    assertEquals(CompKind.EQ.compare("2.0", "2", Type.INTEGER), true);
    assertEquals(CompKind.EQ.compare("2.0", "2.01", Type.INTEGER), true);
    assertEquals(CompKind.EQ.compare("2.0", "4.0", Type.INTEGER), false);
    assertEquals(CompKind.EQ.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MAX_VALUE), Type.INTEGER), true);
    assertEquals(CompKind.EQ.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MIN_VALUE), Type.INTEGER), false);

    assertEquals(CompKind.EQ.compare("2.0", "2", Type.STRING), false);
    assertEquals(CompKind.EQ.compare("2.01", "2.01", Type.STRING), true);
    assertEquals(CompKind.EQ.compare("bla bla blubb", "bla bla blubb",
        Type.STRING), true);

    assertEquals(CompKind.EQ.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de", Type.ANY_URI), true);
    assertEquals(CompKind.EQ.compare("www.uni-konstanz.de/",
        "www.uni-konstanz.de", Type.ANY_URI), false);

    assertEquals(CompKind.EQ.compare("true", "false", Type.BOOLEAN), false);
    assertEquals(CompKind.EQ.compare("true", "true", Type.BOOLEAN), true);

    try {
      assertEquals(CompKind.EQ.compare("2.0", "2", Type.DATE), true);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.EQ.compare("2.0", "4.0", Type.G_MONTH), false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(
          CompKind.EQ.compare("2.0", "2.01", Type.YEAR_MONTH_DURATION), false);
      fail("Expected exception");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate "
          + "the expression or the typedoes not match a required type as"
          + " specified by the matching rules."));
    }

  }

  @Test
  public void testNE() {

    assertEquals(CompKind.NE.compare("2.0", "2", Type.DOUBLE), false);
    assertEquals(CompKind.NE.compare("2.0", "2.01", Type.DOUBLE), true);
    assertEquals(CompKind.NE.compare("2.0", "4.0", Type.DOUBLE), true);
    assertEquals(CompKind.NE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DOUBLE), false);
    assertEquals(CompKind.NE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DOUBLE), true);

    assertEquals(CompKind.NE.compare("2.0", "2", Type.DECIMAL), false);
    assertEquals(CompKind.NE.compare("2.0", "2.01", Type.DECIMAL), true);
    assertEquals(CompKind.NE.compare("2.0", "4.0", Type.DECIMAL), true);
    assertEquals(CompKind.NE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DECIMAL), false);
    assertEquals(CompKind.NE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DECIMAL), true);

    assertEquals(CompKind.NE.compare("2.0", "2", Type.FLOAT), false);
    assertEquals(CompKind.NE.compare("2.0", "2.01", Type.FLOAT), true);
    assertEquals(CompKind.NE.compare("2.0", "4.0", Type.FLOAT), true);
    assertEquals(CompKind.NE.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MAX_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.NE.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MIN_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.NE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.NE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.FLOAT), true);

    assertEquals(CompKind.NE.compare("2.0", "2", Type.INTEGER), false);
    assertEquals(CompKind.NE.compare("2.0", "2.01", Type.INTEGER), false);
    assertEquals(CompKind.NE.compare("2.0", "4.0", Type.INTEGER), true);
    assertEquals(CompKind.NE.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MAX_VALUE), Type.INTEGER), false);
    assertEquals(CompKind.NE.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MIN_VALUE), Type.INTEGER), true);

    assertEquals(CompKind.NE.compare("2.0", "2", Type.STRING), true);
    assertEquals(CompKind.NE.compare("2.01", "2.01", Type.STRING), false);
    assertEquals(CompKind.NE.compare("bla bla blubb", "bla bla blubb",
        Type.STRING), false);

    assertEquals(CompKind.NE.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de", Type.ANY_URI), false);
    assertEquals(CompKind.NE.compare("www.uni-konstanz.de/",
        "www.uni-konstanz.de", Type.ANY_URI), true);

    assertEquals(CompKind.NE.compare("false", "true", Type.BOOLEAN), true);
    assertEquals(CompKind.NE.compare("false", "false", Type.BOOLEAN), false);

    try {
      assertEquals(CompKind.NE.compare("2.0", "2", Type.DATE), false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.NE.compare("2.0", "4.0", Type.G_MONTH), true);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(
          CompKind.NE.compare("2.0", "2.01", Type.YEAR_MONTH_DURATION), true);
      fail("Expected exception");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate "
          + "the expression or the typedoes not match a required type as"
          + " specified by the matching rules."));
    }
  }

  @Test
  public void testLT() {

    assertEquals(CompKind.LT.compare("2.0", "2", Type.DOUBLE), false);
    assertEquals(CompKind.LT.compare("2.0", "2.01", Type.DOUBLE), true);
    assertEquals(CompKind.LT.compare("2.0", "4.0", Type.DOUBLE), true);
    assertEquals(CompKind.LT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DOUBLE), false);
    assertEquals(CompKind.LT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DOUBLE), false);

    assertEquals(CompKind.LT.compare("2.0", "2", Type.DECIMAL), false);
    assertEquals(CompKind.LT.compare("2.0", "2.01", Type.DECIMAL), true);
    assertEquals(CompKind.LT.compare("2.0", "4.0", Type.DECIMAL), true);
    assertEquals(CompKind.LT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DECIMAL), false);
    assertEquals(CompKind.LT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DECIMAL), false);

    assertEquals(CompKind.LT.compare("2.0", "2", Type.FLOAT), false);
    assertEquals(CompKind.LT.compare("2.0", "2.01", Type.FLOAT), true);
    assertEquals(CompKind.LT.compare("2.0", "4.0", Type.FLOAT), true);
    assertEquals(CompKind.LT.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MAX_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.LT.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MIN_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.LT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.LT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.FLOAT), false);

    assertEquals(CompKind.LT.compare("2.0", "2", Type.INTEGER), false);
    assertEquals(CompKind.LT.compare("2.0", "2.01", Type.INTEGER), false);
    assertEquals(CompKind.LT.compare("2.0", "4.0", Type.INTEGER), true);
    assertEquals(CompKind.LT.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MAX_VALUE), Type.INTEGER), false);
    assertEquals(CompKind.LT.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MIN_VALUE), Type.INTEGER), false);

    assertEquals(CompKind.LT.compare("2.0", "2", Type.STRING), false);
    assertEquals(CompKind.LT.compare("2.01", "2.01", Type.STRING), false);
    assertEquals(CompKind.LT.compare("bla bla blubb", "bla bla blubb",
        Type.STRING), false);

    assertEquals(CompKind.LT.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de", Type.ANY_URI), false);
    assertEquals(CompKind.LT.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de/", Type.ANY_URI), true);

    assertEquals(CompKind.LT.compare("false", "true", Type.BOOLEAN), true);
    assertEquals(CompKind.LT.compare("false", "false", Type.BOOLEAN), false);

    try {
      assertEquals(CompKind.LT.compare("2.0", "2", Type.DATE), false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.LT.compare("2.0", "4.0", Type.YEAR_MONTH_DURATION),
          true);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.LT.compare("2.0", "2.01", Type.G_MONTH), true);
      fail("Expected exception");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate "
          + "the expression or the typedoes not match a required type as"
          + " specified by the matching rules."));
    }
  }

  @Test
  public void testLE() {

    assertEquals(CompKind.LE.compare("2.0", "2", Type.DOUBLE), true);
    assertEquals(CompKind.LE.compare("2.0", "2.01", Type.DOUBLE), true);
    assertEquals(CompKind.LE.compare("2.0", "4.0", Type.DOUBLE), true);
    assertEquals(CompKind.LE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DOUBLE), true);
    assertEquals(CompKind.LE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DOUBLE), false);

    assertEquals(CompKind.LE.compare("2.0", "2", Type.DECIMAL), true);
    assertEquals(CompKind.LE.compare("2.0", "2.01", Type.DECIMAL), true);
    assertEquals(CompKind.LE.compare("2.0", "4.0", Type.DECIMAL), true);
    assertEquals(CompKind.LE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DECIMAL), true);
    assertEquals(CompKind.LE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DECIMAL), false);

    assertEquals(CompKind.LE.compare("2.0", "2", Type.FLOAT), true);
    assertEquals(CompKind.LE.compare("2.0", "2.01", Type.FLOAT), true);
    assertEquals(CompKind.LE.compare("2.0", "4.0", Type.FLOAT), true);
    assertEquals(CompKind.LE.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MAX_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.LE.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MIN_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.LE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.LE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.FLOAT), false);

    assertEquals(CompKind.LE.compare("2.0", "2", Type.INTEGER), true);
    assertEquals(CompKind.LE.compare("2.0", "2.01", Type.INTEGER), true);
    assertEquals(CompKind.LE.compare("2.0", "4.0", Type.INTEGER), true);
    assertEquals(CompKind.LE.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MAX_VALUE), Type.INTEGER), true);
    assertEquals(CompKind.LE.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MIN_VALUE), Type.INTEGER), false);

    assertEquals(CompKind.LE.compare("2.0", "2", Type.STRING), false);
    assertEquals(CompKind.LE.compare("2.01", "2.01", Type.STRING), true);
    assertEquals(CompKind.LE.compare("bla bla blubb", "bla bla blubb",
        Type.STRING), true);

    assertEquals(CompKind.LE.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de", Type.ANY_URI), true);
    assertEquals(CompKind.LE.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de/", Type.ANY_URI), true);

    assertEquals(CompKind.LE.compare("true", "false", Type.BOOLEAN), false);
    assertEquals(CompKind.LE.compare("true", "true", Type.BOOLEAN), true);

    try {
      assertEquals(CompKind.LE.compare("2.0", "2", Type.DATE), true);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.LE.compare("2.0", "4.0", Type.YEAR_MONTH_DURATION),
          true);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.LE.compare("2.0", "2.01", Type.G_MONTH), true);
      fail("Expected exception");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate "
          + "the expression or the typedoes not match a required type as"
          + " specified by the matching rules."));
    }

  }

  @Test
  public void testGT() {

    assertEquals(CompKind.GT.compare("2.0", "2", Type.DOUBLE), false);
    assertEquals(CompKind.GT.compare("2.0", "2.01", Type.DOUBLE), false);
    assertEquals(CompKind.GT.compare("2.0", "4.0", Type.DOUBLE), false);
    assertEquals(CompKind.GT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DOUBLE), false);
    assertEquals(CompKind.GT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DOUBLE), true);

    assertEquals(CompKind.GT.compare("2.0", "2", Type.DECIMAL), false);
    assertEquals(CompKind.GT.compare("2.0", "2.01", Type.DECIMAL), false);
    assertEquals(CompKind.GT.compare("2.0", "4.0", Type.DECIMAL), false);
    assertEquals(CompKind.GT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DECIMAL), false);
    assertEquals(CompKind.GT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DECIMAL), true);

    assertEquals(CompKind.GT.compare("2.0", "2", Type.FLOAT), false);
    assertEquals(CompKind.GT.compare("2.0", "2.01", Type.FLOAT), false);
    assertEquals(CompKind.GT.compare("2.0", "4.0", Type.FLOAT), false);
    assertEquals(CompKind.GT.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MAX_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.GT.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MIN_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.GT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.FLOAT), false);
    assertEquals(CompKind.GT.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.FLOAT), true);

    assertEquals(CompKind.GT.compare("2.0", "2", Type.INTEGER), false);
    assertEquals(CompKind.GT.compare("2.0", "2.01", Type.INTEGER), false);
    assertEquals(CompKind.GT.compare("2.0", "4.0", Type.INTEGER), false);
    assertEquals(CompKind.GT.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MAX_VALUE), Type.INTEGER), false);
    assertEquals(CompKind.GT.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MIN_VALUE), Type.INTEGER), true);

    assertEquals(CompKind.GT.compare("2.0", "2", Type.STRING), true);
    assertEquals(CompKind.GT.compare("2.01", "2.01", Type.STRING), false);
    assertEquals(CompKind.GT.compare("bla bla blubb", "bla bla blubb",
        Type.STRING), false);

    assertEquals(CompKind.GT.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de", Type.ANY_URI), false);
    assertEquals(CompKind.GT.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de/", Type.ANY_URI), false);

    assertEquals(CompKind.GT.compare("false", "true", Type.BOOLEAN), false);
    assertEquals(CompKind.GT.compare("false", "false", Type.BOOLEAN), false);

    try {
      assertEquals(CompKind.GT.compare("2.0", "2", Type.DATE), false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.GT.compare("2.0", "4.0", Type.YEAR_MONTH_DURATION),
          false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.GT.compare("2.0", "2.01", Type.G_MONTH), false);
      fail("Expected exception");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate "
          + "the expression or the typedoes not match a required type as"
          + " specified by the matching rules."));
    }
  }

  @Test
  public void testGE() {

    assertEquals(CompKind.GE.compare("2.0", "2", Type.DOUBLE), true);
    assertEquals(CompKind.GE.compare("2.0", "2.01", Type.DOUBLE), false);
    assertEquals(CompKind.GE.compare("2.0", "4.0", Type.DOUBLE), false);
    assertEquals(CompKind.GE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DOUBLE), true);
    assertEquals(CompKind.GE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DOUBLE), true);

    assertEquals(CompKind.GE.compare("2.0", "2", Type.DECIMAL), true);
    assertEquals(CompKind.GE.compare("2.0", "2.01", Type.DECIMAL), false);
    assertEquals(CompKind.GE.compare("2.0", "4.0", Type.DECIMAL), false);
    assertEquals(CompKind.GE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DECIMAL), true);
    assertEquals(CompKind.GE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DECIMAL), true);

    assertEquals(CompKind.GE.compare("2.0", "2", Type.FLOAT), true);
    assertEquals(CompKind.GE.compare("2.0", "2.01", Type.FLOAT), false);
    assertEquals(CompKind.GE.compare("2.0", "4.0", Type.FLOAT), false);
    assertEquals(CompKind.GE.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MAX_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.GE.compare(Float.toString(Float.MAX_VALUE), Float
        .toString(Float.MIN_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.GE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.FLOAT), true);
    assertEquals(CompKind.GE.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.FLOAT), true);

    assertEquals(CompKind.GE.compare("2.0", "2", Type.INTEGER), true);
    assertEquals(CompKind.GE.compare("2.0", "2.01", Type.INTEGER), true);
    assertEquals(CompKind.GE.compare("2.0", "4.0", Type.INTEGER), false);
    assertEquals(CompKind.GE.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MAX_VALUE), Type.INTEGER), true);
    assertEquals(CompKind.GE.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MIN_VALUE), Type.INTEGER), true);

    assertEquals(CompKind.GE.compare("2.0", "2", Type.STRING), true);
    assertEquals(CompKind.GE.compare("2.01", "2.01", Type.STRING), true);
    assertEquals(CompKind.GE.compare("bla bla blubb", "bla bla blubb",
        Type.STRING), true);

    assertEquals(CompKind.GE.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de", Type.ANY_URI), true);
    assertEquals(CompKind.GE.compare("www.uni-konstanz.de",
        "www.uni-konstanz.de/", Type.ANY_URI), false);

    assertEquals(CompKind.GE.compare("false", "true", Type.BOOLEAN), false);
    assertEquals(CompKind.GE.compare("false", "false", Type.BOOLEAN), true);

    try {
      assertEquals(CompKind.GE.compare("2.0", "2", Type.DATE), false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.GE.compare("2.0", "4.0", Type.YEAR_MONTH_DURATION),
          false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(), is("Not implemented for this type yet"));
    }

    try {
      assertEquals(CompKind.GE.compare("2.0", "2.01", Type.G_MONTH), false);
      fail("Expected exception");
    } catch (XPathError e) {
      assertThat(e.getMessage(), is("err:XPTY0004 The type is not appropriate "
          + "the expression or the typedoes not match a required type as"
          + " specified by the matching rules."));
    }
  }

  @Test
  public void testFO() {

    try {
      assertEquals(CompKind.FO.compare("2.0", "2", Type.DATE), false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(),
          is("Evaluation of node comparisons not possible"));
    }
  }

  @Test
  public void testPRE() {

    try {
      assertEquals(CompKind.PRE.compare("2.0", "2", Type.DATE), false);
      fail("Expected exception");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage(),
          is("Evaluation of node comparisons not possible"));
    }
  }

  @Test
  public void testIS() {

    assertEquals(CompKind.IS.compare("2.0", "2", Type.DOUBLE), true);
    assertEquals(CompKind.IS.compare("2.0", "2.01", Type.DOUBLE), true);
    assertEquals(CompKind.IS.compare("2.0", "4.0", Type.DOUBLE), false);
    assertEquals(CompKind.IS.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MAX_VALUE), Type.DOUBLE), true);
    assertEquals(CompKind.IS.compare(Double.toString(Double.MAX_VALUE), Double
        .toString(Double.MIN_VALUE), Type.DOUBLE), false);

    assertEquals(CompKind.IS.compare("2.0", "2", Type.INTEGER), true);
    assertEquals(CompKind.IS.compare("2.0", "2.01", Type.G_DAY), true);
    assertEquals(CompKind.IS.compare("2.0", "4.0", Type.INTEGER), false);
    assertEquals(CompKind.IS.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MAX_VALUE), Type.INTEGER), true);
    assertEquals(CompKind.IS.compare(Integer.toString(Integer.MAX_VALUE),
        Integer.toString(Integer.MIN_VALUE), Type.INTEGER), false);
  }

}
