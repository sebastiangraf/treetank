package org.treetank.bench;

import org.perfidix.BeforeFirstBenchRun;
import org.perfidix.Bench;
import org.perfidix.BenchClass;
import org.perfidix.Benchmark;
import org.perfidix.visitor.AsciiTable;
import org.treetank.utils.FastByteArrayReader;
import org.treetank.utils.FastByteArrayWriter;

@BenchClass(runs = 1)
public class FastByteArrayBenchmark {
  
  private final int START = -1000000;
  private final int STOP = START * -1;
  
  private byte[] mBytes;

  @BeforeFirstBenchRun
  public void benchBeforeBenchClass() throws Exception {

  }

  @Bench
  public void benchWritePseudoLong() throws Exception {
    final FastByteArrayWriter out = new FastByteArrayWriter();
    for (int i = START; i <= STOP; i++) {
      out.writePseudoLong(i);
    }
    mBytes = out.getBytes();
  }

  @Bench
  public void benchReadPseudoLong() throws Exception {
    final FastByteArrayReader in = new FastByteArrayReader(mBytes);
    for (int i = START; i <= STOP; i++) {
      in.readPseudoLong();
    }
  }
  
  @Bench
  public void benchWritePseudoLongNew() throws Exception {
    final FastByteArrayWriter out = new FastByteArrayWriter();
    for (int i = START; i <= STOP; i++) {
      out.writePseudoLongNew(i);
    }
    mBytes = out.getBytes();
  }

  @Bench
  public void benchReadPseudoLongNew() throws Exception {
    final FastByteArrayReader in = new FastByteArrayReader(mBytes);
    for (int i = START; i <= STOP; i++) {
      in.readPseudoLongNew();
    }
  }
  
  
  public static void main(final String[] args) {
    System.out
        .println("$Id:FastByteArrayBenchmark.java 3004 09.10.07 15:59 $");
    try {
      Benchmark a = new Benchmark();
      FastByteArrayBenchmark bench = new FastByteArrayBenchmark();
      a.add(bench);
      org.perfidix.Result r = a.run();
      AsciiTable v = new AsciiTable();
      v.visit(r);
      System.out.println(v.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
