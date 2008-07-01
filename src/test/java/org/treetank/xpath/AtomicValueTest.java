
package org.treetank.xpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.Before;
import org.junit.Test;
import org.treetank.utils.TypedValue;
import org.treetank.xpath.AtomicValue;
import org.treetank.xpath.types.Type;

public class AtomicValueTest {

  private AtomicValue a, b, c, d, e, f, zero, n_zero;

  private int aVal = 1;

  private String bVal = "test";

  private float cVal = 2345.1441f;

  private double dVal = 245E2;

  private boolean eVal = true;

  @Before
  public void setUp() {

    a = new AtomicValue(aVal, Type.INTEGER);
    b = new AtomicValue(bVal, Type.STRING);
    c = new AtomicValue(cVal, Type.FLOAT);
    d = new AtomicValue(dVal, Type.DOUBLE);
    e = new AtomicValue(eVal);
    f = new AtomicValue(2.0d, Type.DOUBLE);

    zero = new AtomicValue(+0, Type.INTEGER);
    n_zero = new AtomicValue(-0, Type.INTEGER);

  }

  @Test
  public final void testGetInt() {

    assertThat(a.getInt(), is(aVal));
    assertThat(zero.getInt(), is(+0));
    assertThat(n_zero.getInt(), is(-0));
  }

  @Test
  public final void testGetDBL() {
    assertThat(d.getDBL(), is(dVal));
    assertThat(f.getDBL(), is(2.0d));
  }

   @Test
   public final void testGetFLT() {
     assertThat(c.getFLT(), is((float) cVal));

   }
  
   @Test
   public final void testGetString() {
    
     assertThat(TypedValue.parseString(b.getRawValue()), is(bVal));     
   }
  
   @Test
   public final void testGetBool() {
     assertEquals(true, e.getBool());
        }

  
   @Test
   public final void testGetStringValue() {
     testGetString();
   }
  

}
