/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
 */package org.treetank.encryption.benchmarking.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.BeforeBenchClass;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;
import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.EncryptionOperator;
import org.treetank.encryption.benchmarking.KtsMeter;
import org.treetank.encryption.utils.BenchUtils;
import org.treetank.exception.TTEncryptionException;

/**
 * Class to test the performance for one join/leave operation on a DAG with a given number users.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class BenchUpdateOnDAG {

    private static final String DATAFILE = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "testdataUser.txt";

    private static int joinOps = 0;
    private static int leaveOps = 0;
    private static int counter = 0;

    private final static int userTotal = 8010;
    private final static String userToDelete = "8A6AEDB9BB95C043DBAB58F091A04032";
    private final static String groupOfUser = "11508";

    private static long time = 0;

    private final static LinkedList<Integer> benchPos = new LinkedList<Integer>();

    @BeforeBenchClass
    public void before() {
        try {
            new EncryptionController().clear();
            new EncryptionController().setEncryptionOption(true);
            new EncryptionController().init();

            benchPos.add(100);
            benchPos.add(500);
            benchPos.add(1000);
            benchPos.add(2000);
            benchPos.add(3000);
            benchPos.add(4000);
            benchPos.add(5000);
            benchPos.add(6000);
            benchPos.add(7000);
            benchPos.add(8000);
            benchPos.add(9000);

            time = System.currentTimeMillis();
            System.out.println("Started...");
            init();
            System.out.println("Ended...");
            System.out.println("Parsed lines: " + counter);
            System.out.println("Total time needed: " + (System.currentTimeMillis() - time) + "ms");
            System.out.println("Total joins: " + joinOps);
            System.out.println("Total leaves: " + leaveOps);
            System.out.println("Total ops: " + (joinOps + leaveOps));

            System.out.println("Nodes DAG: " + new EncryptionController().getDAGDb().count());
            System.out.println("Nodes Revision: " + new EncryptionController().getSelDb().count());
            System.out.println("Nodes KeyManager: " + new EncryptionController().getManDb().count());

            new EncryptionController().clear();

        } catch (TTEncryptionException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        final Benchmark bench = new Benchmark(new ThisConfiguration());
        bench.add(BenchUpdateOnDAG.class);
        final BenchmarkResult res = bench.run();
        new TabularSummaryOutput().visitBenchmark(res);
    }

    @Bench
    public void init() {
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(DATAFILE)));

            String line;

            EncryptionOperator op;

            int userCounter = 0;

            while (((line = in.readLine()) != null && userCounter < userTotal)) {
                op = new EncryptionOperator();
                counter++;

                if (benchPos.contains(userCounter)) {
                    // if a bench position is reached, leave the last added from DAG
                    long benchTime = System.currentTimeMillis();
                    ;

                    // DAGSelector dag =
                    // EncryptionController.getInstance().getDAGDb().getEntry(op.getNodeKey(lastUser));
                    // System.out.println(dag.getName() + " " + dag.getChilds().toString());

                    // EncryptionController.getInstance().print();

                    // String userDelete =
                    // EncryptionController.getInstance().getManDb().getEntries().lastKey();

                    op.leave(userToDelete, new String[] {
                        groupOfUser
                    });

                    long timeNeeded = System.currentTimeMillis() - benchTime;

                    System.out.println("User: " + userCounter);
                    System.out.println("Time needed Leave: " + timeNeeded + " ms");
                    System.out.println("Key Trails: " + KtsMeter.getInstance().getValue() + " kts");
                    System.out.println("DAG Size: " + EncryptionController.getInstance().getDAGDb().count());
                    System.out
                        .println("DAG Revisions Size: " + new EncryptionController().getSelDb().count());
                    System.out.println("Key Manager Size: " + new EncryptionController().getManDb().count());
                    System.out.println("Total Time: " + (System.currentTimeMillis() - time));
                    System.out.println("Memory Usage: "
                        + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
                    System.out.println("");

                    // EncryptionController.getInstance().print();

                    benchTime = System.currentTimeMillis();
                    ;

                    op.join(groupOfUser, new String[] {
                        userToDelete
                    });

                    KtsMeter.getInstance().reset();

                    timeNeeded = System.currentTimeMillis() - benchTime;
                    System.out.println("Time needed Join: " + timeNeeded + " ms");

                    userCounter++;

                } else {

                    final String[] dataArray = new BenchUtils().parseData(line);

                    if (dataArray[2] != null) {

                        final String cleanedGroup = new BenchUtils().cleanGroup(dataArray[2]);
                        final LinkedList<String> splittedGroupList =
                            new BenchUtils().splitGroup(cleanedGroup);

                        splittedGroupList.add(dataArray[0]); // add user

                        final String[] newGroups = splittedGroupList.toArray(new String[0]);

                        // at least one user and one group must exist.
                        if (newGroups.length > 1) {

                            String user = newGroups[newGroups.length - 1];
                            String group = newGroups[newGroups.length - 2];

                            // if (!op.userExists(user)) { //check not further necessary, after building a
                            // unique
                            // user data set
                            userCounter++;
                            if (op.nodeExists(group)) {
                                op.join(group, new String[] {
                                    user
                                });
                                joinOps++;
                            } else { // if user and group does not exist, check which groups of hierarchy
                                     // exists,
                                     // create it and add user.
                                LinkedList<String> groupList = new LinkedList<String>();
                                String parent = "ROOT";
                                int i = 0;
                                while (i < newGroups.length - 1) {
                                    if (!op.nodeExists(newGroups[i])) {
                                        groupList.add(newGroups[i]);
                                    } else {
                                        parent = newGroups[i];
                                        break;
                                    }
                                    i++;
                                }
                                groupList.add(newGroups[newGroups.length - 1]);

                                if (groupList.size() == newGroups.length) {
                                    op.join("ROOT", newGroups);
                                    joinOps++;
                                } else {
                                    if (groupList.size() > 0) {
                                        op.join(parent, groupList.toArray(new String[0]));
                                        joinOps++;
                                    }
                                }
                                // }

                            }

                        }
                    }
                }

            }

        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    static final int runs = 1;
    static final AbstractMeter[] meters = {
        KtsMeter.getInstance()
    };
    static final AbstractOutput[] output = {
        new TabularSummaryOutput()
    };
    static final KindOfArrangement arrang = KindOfArrangement.SequentialMethodArrangement;
    static final double gc = 1;

    static class ThisConfiguration extends AbstractConfig {

        protected ThisConfiguration() {
            super(runs, meters, output, arrang, gc);

        }

    }

}
