/*
 * Copyright (c) 2008, Marc Kramis (Ph.D. Thesis), University of Konstanz
 * 
 * Patent Pending.
 * 
 * Permission to use, copy, modify, and/or distribute this software for non-
 * commercial use with or without fee is hereby granted, provided that the 
 * above copyright notice, the patent notice, and this permission notice
 * appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 * $Id: AxisStepBench.java 4394 2008-08-25 20:14:56Z kramis $
 */

package com.treetank.bench;

import java.io.File;

import org.perfidix.Benchmark;
import org.perfidix.annotation.BeforeFirstRun;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

import com.treetank.access.Session;
import com.treetank.access.SessionConfiguration;
import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.axis.DescendantAxis;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.XMLShredder;

public class AxisStepBench {

    public final static int TASKS = 3;
    private final static String FILENAME = "shakespeare";
    private final static File XML = new File("src" + File.separator + "test"
            + File.separator + "resources" + File.separator + FILENAME + ".xml");
    private final static File TNK = new File("target" + File.separator + "tnk"
            + File.separator + FILENAME + ".tnk");

    public final static byte[] TNK_KEY = null; // "1234567812345678".getBytes();

    public final static boolean TNK_CHECKSUM = false;

    private SessionConfiguration mSessionConfiguration;

    @BeforeFirstRun
    public void benchShred() {
        try {
            Session.removeSession(TNK);
            mSessionConfiguration = new SessionConfiguration(TNK);
            XMLShredder.shred(XML.getAbsolutePath(), mSessionConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(runs = 100)
    public void benchTreeTankDescendant() throws TreetankException {

        final ISession session = Session.beginSession(mSessionConfiguration);
        // final ExecutorService executor = Executors.newFixedThreadPool(TASKS);
        // executor.execute(new DescendantStepTask(session));
        // executor.shutdown();
        // executor.awaitTermination(1000000, TimeUnit.SECONDS);
        new DescendantStepTask(session).run();

        session.close();
    }

    // @Bench
    // public void benchRandom() throws Exception {
    //
    // final ISession session = Session.beginSession(mSessionConfiguration);
    // final IReadTransaction rtx = session.beginReadTransaction();
    // final Random r = new Random();
    //
    // for (int i = 0; i < 10; i++) {
    // rtx.moveTo(r.nextInt((int) rtx.getNodeCount()));
    // }
    //
    // rtx.close();
    // session.close();
    // }
    //
    // @Bench
    // public void benchConcurrentTreeTankDescendant() throws Exception {
    //
    // final ISession session = Session.beginSession(mSessionConfiguration);
    // final ExecutorService executor = Executors.newFixedThreadPool(TASKS);
    // try {
    //
    // for (int i = 0; i < TASKS; i++) {
    // executor.execute(new DescendantStepTask(session));
    // }
    //
    // executor.shutdown();
    // executor.awaitTermination(1000000, TimeUnit.SECONDS);
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // session.close();
    // }
    //
    // @Bench(runs = 100)
    // public void benchTreeTankChild() {
    //
    // final ISession session = Session.beginSession(mSessionConfiguration);
    // final IReadTransaction rtx = session.beginReadTransaction();
    // for (final long key : new ChildAxis(rtx)) {
    // // Do nothing.
    // }
    // rtx.close();
    // session.close();
    // }

    public static void main(final String[] args) {

        try {
            Benchmark a = new Benchmark();
            a.add(AxisStepBench.class);
            BenchmarkResult r = a.run();
            TabularSummaryOutput v = new TabularSummaryOutput();
            v.visitBenchmark(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DescendantStepTask implements Runnable {

        private final IReadTransaction mRTX;

        public DescendantStepTask(final ISession session)
                throws TreetankException {

            mRTX = session.beginReadTransaction();
        }

        public void run() {

            try {
                final IAxis axis = new DescendantAxis(mRTX);
                while (axis.hasNext()) {
                    axis.next();
                }
                mRTX.close();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
