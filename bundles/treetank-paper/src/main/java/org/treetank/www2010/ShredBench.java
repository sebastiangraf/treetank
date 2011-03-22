/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.www2010;

import java.io.File;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.access.WriteTransaction;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;

import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.result.BenchmarkResult;

public class ShredBench {

    private XMLShredder shredderNone;

    private static final int RUNS = 100;

    public static File XMLFile = new File("src" + File.separator + "main" + File.separator + "resources"
        + File.separator + "small.xml");

    public void setUpNone() {
        TestHelper.deleteEverything();
        try {
            TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.None.name());

            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            shredderNone =
                new XMLShredder(wtx, XMLShredder.createReader(XMLFile), EShredderInsert.ADDASFIRSTCHILD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpRolling() {
        TestHelper.deleteEverything();
        try {
            TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Rolling.name());

            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            shredderNone =
                new XMLShredder(wtx, XMLShredder.createReader(XMLFile), EShredderInsert.ADDASFIRSTCHILD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUpPostorder() {
        TestHelper.deleteEverything();
        try {
            TestHelper.setDB(TestHelper.PATHS.PATH1.getFile(), WriteTransaction.HashKind.Postorder.name());

            final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            shredderNone =
                new XMLShredder(wtx, XMLShredder.createReader(XMLFile), EShredderInsert.ADDASFIRSTCHILD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpPostorder", runs = RUNS)
    public void benchPostorder() {
        try {
            shredderNone.call();
        } catch (AbsTTException e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpRolling", runs = RUNS)
    public void benchRolling() {
        try {
            shredderNone.call();
        } catch (AbsTTException e) {
            e.printStackTrace();
        }
    }

    @Bench(beforeEachRun = "setUpNone", runs = RUNS)
    public void benchNone() {
        try {
            shredderNone.call();
        } catch (AbsTTException e) {
            e.printStackTrace();
        }
    }

    @AfterEachRun
    public void tearDown() {
        TestHelper.closeEverything();
    }

    public static void main(final String[] args) {

        if (args.length != 2) {
            System.out
                .println("Please use java -jar JAR \"folder with xmls to parse\" \"folder to write csv\"");
            System.exit(-1);
        }

        // Argument is a folder with only XML in there. For each XML one benchmark should be executed.
        final File filetoshred = new File(args[0]);
        final File[] files = filetoshred.listFiles();
        final File filetoexport = new File(args[1]);
        for (final File currentFile : files) {
            System.out.println("Starting benchmark for " + currentFile.getName());
            final int index = currentFile.getName().lastIndexOf(".");
            final File folder = new File(filetoexport, currentFile.getName().substring(0, index));
            folder.mkdirs();
            XMLFile = currentFile;
            final Benchmark bench = new Benchmark();
            bench.add(ShredBench.class);
            final BenchmarkResult res = bench.run();
            new CSVOutput(folder).visitBenchmark(res);
            System.out.println("Finished benchmark for " + currentFile.getName());
        }

    }
}
