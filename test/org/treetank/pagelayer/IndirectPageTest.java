package org.treetank.pagelayer;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;
import org.treetank.api.IConstants;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

public class IndirectPageTest {

  @Test
  public void testSerializeDeserialize() {
    Random rnd = new Random();
    FastByteArrayWriter out = new FastByteArrayWriter();
    PageReference[] ref = new PageReference[IConstants.INP_REFERENCE_COUNT];
    for (int i = 0; i < IConstants.INP_REFERENCE_COUNT; i++) {
      ref[i] =
          new PageReference(null, rnd.nextLong(), rnd.nextInt(), rnd.nextLong());
      out.writeBoolean(true);
      ref[i].serialize(out);
    }
    byte[] bytes = out.getBytes();
    
    IndirectPage page = new IndirectPage(new FastByteArrayReader(bytes));
    for (int i = 0; i < IConstants.INP_REFERENCE_COUNT; i++) {
      assertEquals(page.getReference(i),ref[i]);
    }
  }
  
  @Test
  public void testLazySerializeDeserialize() {
    Random rnd = new Random();
    FastByteArrayWriter out = new FastByteArrayWriter();
    PageReference[] ref = new PageReference[IConstants.INP_REFERENCE_COUNT];
    for (int i = 0; i < IConstants.INP_REFERENCE_COUNT; i++) {
      ref[i] =
          new PageReference(null, rnd.nextLong(), rnd.nextInt(), rnd.nextLong());
      out.writeBoolean(true);
      ref[i].serialize(out);
    }
    byte[] bytes = out.getBytes();
    
    IndirectPageNew page = new IndirectPageNew(true, new FastByteArrayReader(bytes));
    for (int i = 0; i < IConstants.INP_REFERENCE_COUNT; i++) {
      assertEquals(page.getReference(i),ref[i]);
    }
  }

}
