package org.treetank.encryption.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import com.sleepycat.je.UniqueConstraintException;

import org.treetank.encrpytion.exception.TTEncryptionException;
import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.EncryptionOperator;

public class TestDataParser2 {

    private static final String DATAFILE = new StringBuilder(File.separator).append("src").append(
        File.separator).append("test").append(File.separator).append("resources").append(File.separator)
        .append("testdata100.txt").toString();

    public static void main(String[] args) {
        try {
            new EncryptionController().clear();
            new EncryptionController().setEncryptionOption(true);
            new EncryptionController().init();

            long time = System.currentTimeMillis();
            System.out.println("Started...");
            init();
            System.out.println("Ended...");
            System.out.println("Total time needed: " + (System.currentTimeMillis() - time) + "ms");

            new EncryptionController().clear();

        } catch (TTEncryptionException e) {
            e.printStackTrace();
        }
    }

    public static void init() throws TTEncryptionException {
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(DATAFILE)));

            char splitter = '$';
            String line;
            int counter = 0;
            long time = System.currentTimeMillis();

            LinkedList<String> uniqueUser = new LinkedList<String>();

            EncryptionOperator op;

            while ((line = in.readLine()) != null) {
                op = new EncryptionOperator();
                counter++;

                final char[] chars = line.toCharArray();
                final String[] dataString = new String[5];

                int stringCount = 0;
                int charCount = 1;

                final StringBuilder sb = new StringBuilder();

                for (char aChar : chars) {
                    if (aChar == splitter || charCount == chars.length) {
                        dataString[stringCount++] = sb.toString();
                        sb.setLength(0);
                    } else {
                        sb.append(aChar);
                    }
                    charCount++;
                }

                String cleanedGroup = cleanGroup(dataString[2]);
                String[] splittedGroup = splitGroup(cleanedGroup);

                String[] newGroups = new String[splittedGroup.length + 1];

                StringBuilder sb2 = new StringBuilder();

                // rebuild array + user
                for (int i = 0; i < newGroups.length - 1; i++) {
                    newGroups[i] = splittedGroup[i];
                    sb2.append(splittedGroup[i]);
                    sb2.append(" - ");
                }
                newGroups[newGroups.length - 1] = dataString[0];
                sb2.append(dataString[0]);

                // System.out.println(sb2.toString());

                // at least one user and one group must exist.
                if (newGroups.length > 1) {

                    String user = newGroups[newGroups.length - 1];
                    String group = newGroups[newGroups.length - 2];

                    if (!uniqueUser.contains(user)) {
                        uniqueUser.add(user);
                    }

                    if (op.nodeExists(user)) {

                        if (op.nodeExists(group)) {
                            // if user and group exist, and user is not member of group, add user to group
                            if (!op.checkMembership(user, group)) {
                                op.join(group, new String[] {
                                    user
                                });

                            }

                        } else {
                            // if user exist but not group, check which groups of hierarchy already exist,
                            // create it and add user.
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

                                op.join(newGroups[newGroups.length - 1], new String[] {
                                    user
                                });

                            } else {
                                if (groupList.size() > 1) {
                                    op.join(parent, groupList.toArray(new String[0]));

                                    op.join(newGroups[newGroups.length - 1], new String[] {
                                        user
                                    });

                                }
                            }
                        }

                    } else {
                        // if user not exists, but group exists, add user to group
                        if (op.nodeExists(group)) {
                            op.join(group, new String[] {
                                user
                            });

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

                            } else {
                                if (groupList.size() > 1) {
                                    op.join(parent, groupList.toArray(new String[0]));

                                }
                            }
                        }

                    }

                }

            }

            System.out.println("Unique User: " + uniqueUser.size());

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public static String cleanGroup(final String group) {
        char[] groupChars = group.toCharArray();
        int pos = 0;
        // entferne Nullen
        int i = groupChars.length - 1;
        while (i >= 0) {
            if (groupChars[i] != '0') {
                pos = i;
                break;
            }
            i--;
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < pos + 1; j++) {
            sb.append(groupChars[j]);
        }

        return sb.toString();

    }

    public static String[] splitGroup(final String group) {
        char[] groupChars = group.toCharArray();

        String[] groups;

        if (groupChars.length > 1) {
            groups = new String[groupChars.length - 1];
        } else {
            groups = new String[groupChars.length];
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        int j = 0;
        while (i < groupChars.length) {
            if (i == 0) {
                sb.append(groupChars[i++]);
                if (groupChars.length > 1) {
                    sb.append(groupChars[i++]);
                }
                groups[j++] = sb.toString();
            } else {
                sb.append(groupChars[i++]);
                groups[j++] = sb.toString();
            }
        }

        return groups;

    }

}
