package org.treetank.encryption.benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.EncryptionOperator;
import org.treetank.exception.TTEncryptionException;

public class BenchPerformanceDataset {

    private static final String DATAFILE = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "testdata.txt";

    private static int joinOps = 0;
    private static int leaveOps = 0;
    private static int dataCounter = 0;

    private static int dataRuns = 10000;

    private static long time = 0;

    public static void main(String[] args) {
        try {
            new EncryptionController().clear();
            new EncryptionController().setEncryptionOption(true);
            new EncryptionController().init();

            time = System.currentTimeMillis();
            System.out.println("Started...");
            init();
            System.out.println("Ended...");
            System.out.println("Parsed lines: " + dataCounter);
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

            while (((line = in.readLine()) != null && dataCounter < dataRuns)) {
                op = new EncryptionOperator();
                dataCounter++;

                if (dataCounter % 1000 == 0) {
                    System.out.println(dataCounter);
                    System.out.println("Time needed: " + (System.currentTimeMillis() - time) + "ms");
                    System.out.println("Joins: " + joinOps + " Leaves: " + leaveOps);
                    System.out.println("");
                }

                final String[] dataArray = new BenchUtils().parseData(line);

                final String cleanedGroup = new BenchUtils().cleanGroup(dataArray[2]);
                final LinkedList<String> splittedGroupList = new BenchUtils().splitGroup(cleanedGroup);

                splittedGroupList.add(dataArray[0]); // add user

                final String[] newGroups = splittedGroupList.toArray(new String[0]);

                // at least one user and one group must exist.
                if (newGroups.length > 1) {

                    String user = newGroups[newGroups.length - 1];
                    String group = newGroups[newGroups.length - 2];
                    if (op.nodeExists(user)) {

                        if (op.nodeExists(group)) {
                            // if user and group exist, and user is not member of group, add user to group
                            if (!op.checkMembership(user, group)) {
                                op.join(group, new String[] {
                                    user
                                });
                                joinOps++;
                            }

                        } else {
                            // if user exist but not group, check which groups of hierarchy already exist,
                            // create it
                            // and add user.
                            LinkedList<String> groupList = new LinkedList<String>();
                            String parent = "ROOT";
                            for (int i = 0; i < newGroups.length - 1; i++) {
                                if (!op.nodeExists(newGroups[i])) {
                                    groupList.add(newGroups[i]);
                                } else {
                                    parent = newGroups[i];
                                }
                                // groupList.add(newGroups[newGroups.length-1]);
                            }

                            if (groupList.size() == newGroups.length - 1) {
                                op.join("ROOT", newGroups);
                                joinOps++;
                                op.join(newGroups[newGroups.length - 1], new String[] {
                                    user
                                });
                                joinOps++;
                            } else {
                                if (groupList.size() > 1) {
                                    op.join(parent, groupList.toArray(new String[0]));
                                    joinOps++;
                                    op.join(newGroups[newGroups.length - 1], new String[] {
                                        user
                                    });
                                    joinOps++;
                                }
                            }
                        }

                    } else {
                        // if user not exists, but group exists, add user to group
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
                        }

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
