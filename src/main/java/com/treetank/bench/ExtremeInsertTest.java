package com.treetank.bench;

import java.io.File;
import java.util.Random;

import org.perfidix.annotation.Bench;

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.session.Session;

public class ExtremeInsertTest {

    private static int NUM_CHARS = 4;
    private static int NUM_CHARS2 = 30000;
    private static int ELEMENTS = 1000000;
    private static final Random ran = new Random(0l);
    public static String chars = "abcdefghijklmonpqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Bench(runs = 1)
    public void bench() {
        try {
            final File file = new File("bla");
            // Session.removeSession(file);

            final ISession session = Session.beginSession(file);
            IWriteTransaction wtx = session.beginWriteTransaction();
            wtx.insertElementAsFirstChild(getString(), "");
            for (int i = 0; i < ELEMENTS; i++) {

                if (ran.nextBoolean()) {
                    if (ran.nextBoolean()) {
                        wtx.insertElementAsFirstChild(getString(), "");
                    } else {
                        wtx.insertElementAsRightSibling(getString(), "");
                    }
                    while (ran.nextBoolean()) {
                        wtx.insertAttribute(getString(), getString(),
                                getString());
                        wtx.moveToParent();
                    }
                    while (ran.nextBoolean()) {
                        wtx.insertNamespace(getString(), getString());
                        wtx.moveToParent();
                    }
                } else {
                    if (ran.nextBoolean()) {
                        wtx.insertTextAsFirstChild(getStringForVal());
                    } else {
                        wtx.insertTextAsRightSibling(getStringForVal());
                    }
                }

                if (ran.nextBoolean()) {
                    wtx.commit();
                }
            }
            wtx.commit();
            wtx.close();
            session.close();
            super.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // final Benchmark bench = new Benchmark();
        // bench.add(ExtremeInsertTest.class);
        // final BenchmarkResult res = bench.run();
        // final TabularSummaryOutput output = new TabularSummaryOutput();
        // output.visitBenchmark(res);
        if (args.length > 0) {
            ELEMENTS = Integer.parseInt(args[0]);
        }
        final long time = System.currentTimeMillis();
        new ExtremeInsertTest().bench();
        System.out.println(System.currentTimeMillis() - time + "[ms]");

    }

    public static String getString() {
        char[] buf = new char[NUM_CHARS];

        for (int i = 0; i < buf.length; i++) {
            buf[i] = chars.charAt(ran.nextInt(chars.length()));
        }

        return new String(buf);
    }

    public static String getStringForVal() {
        char[] buf = new char[NUM_CHARS2];

        for (int i = 0; i < buf.length; i++) {
            buf[i] = chars.charAt(ran.nextInt(chars.length()));
        }

        return new String(buf);
    }
}
