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

import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.EncryptionOperator;
import org.treetank.encryption.utils.BenchUtils;
import org.treetank.exception.TTEncryptionException;

/**
 * Class to test performance for an number of users.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class BenchPerformanceUser {

    private static final String DATAFILE = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "testdataUser.txt";

    private static int joinOps = 0;
    private static int leaveOps = 0;
    private static int counter = 0;

    private final static int userRuns = 50;

    public static void main(String[] args) {
        try {
            new EncryptionController().clear();
            new EncryptionController().setEncryptionOption(true);
            new EncryptionController().init();

            long time = System.currentTimeMillis();
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

    public static void init() throws TTEncryptionException {
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(DATAFILE)));

            String line;

            EncryptionOperator op;

            int userCounter = 0;

            while (((line = in.readLine()) != null && userCounter < userRuns)) {
                op = new EncryptionOperator();
                counter++;

                final String[] dataArray = new BenchUtils().parseData(line);

                final String cleanedGroup = new BenchUtils().cleanGroup(dataArray[2]);
                final LinkedList<String> splittedGroupList = new BenchUtils().splitGroup(cleanedGroup);

                splittedGroupList.add(dataArray[0]); // add user

                final String[] newGroups = splittedGroupList.toArray(new String[0]);

                // at least one user and one group must exist.
                if (newGroups.length > 1) {

                    String user = newGroups[newGroups.length - 1];
                    String group = newGroups[newGroups.length - 2];

                    //if (!op.userExists(user)) { //check not further necessary, after building a unique user data set
                        userCounter++;
                        if (op.nodeExists(group)) {
                            op.join(group, new String[] {
                                user
                            });
                            joinOps++;
                        } else { // if user and group does not exist, check which groups of hierarchy exists,
                                 // create it and add user.
                            LinkedList<String> groupList = new LinkedList<String>();
                            String parent = "ROOT";
                            for (int i = 0; i < newGroups.length - 1; i++) {
                                if (!op.nodeExists(newGroups[i])) {
                                    groupList.add(newGroups[i]);
                                } else {
                                    parent = newGroups[i];
                                }
                                groupList.add(newGroups[newGroups.length - 1]);
                            }

                            if (groupList.size() == newGroups.length) {
                                op.join("ROOT", newGroups);
                                joinOps++;
                            } else {
                                if (groupList.size() > 1) {
                                    op.join(parent, groupList.toArray(new String[0]));
                                    joinOps++;
                                }
                            }
                        //}

                    }
                }

            }

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }
}
