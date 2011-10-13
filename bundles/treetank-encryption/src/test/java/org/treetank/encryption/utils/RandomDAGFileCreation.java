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
 */
package org.treetank.encryption.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * This class creates a two random DAG files. One for random users and one for random groups. During creation
 * a fanout (if activated) and a maximal depth of DAG is considered.
 */
public class RandomDAGFileCreation {

    /**
     * Output File for random groups.
     */
    private static final String OUTFILE_GROUPS = "src" + File.separator + "test" + File.separator
        + "resources" + File.separator + "randomDAGGroups.txt";

    /**
     * Output File for random users.
     */
    private static final String OUTFILE_USERS = "src" + File.separator + "test" + File.separator
        + "resources" + File.separator + "randomDAGUsers.txt";

    /**
     * Char set for random group names.
     */
    final private static char[] mChars = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    /**
     * List of generated users.
     */
    final private static LinkedList<String> mUserList = new LinkedList<String>();

    /**
     * List of generated groups.
     */
    final private static LinkedList<String> mGroupList = new LinkedList<String>();

    /**
     * Number of groups in DAG.
     */
    final private static int mGroups = 10000;

    /**
     * Number of users in DAG.
     */
    final private static int mUsers = 50000;

    /**
     * Length of random group names.
     */
    final private static int mGroupNameLength = 10;

    /**
     * Max number of parents a user can has.
     */
    final private static int mMaxParents = 3;

    /**
     * Max depth of the DAG.
     */
    final private static int mMaxDepth = 5;

    /**
     * Sets whether fanout should be considered.
     */
    final private static boolean mFanoutStatus = true;

    /**
     * Fanout of DAG.
     */
    final private static int mFanout = 4;

    public static void main(String[] args) {

        // Map holding added nodes with its depth.
        final Map<String, Integer> mDeepCtrlList = new HashMap<String, Integer>();

        // Map holding all added nodes with a counter of its childs.
        final Map<String, Integer> mNodeChildNum = new HashMap<String, Integer>();

        // Map holding all nodes having more childs than given fanout and its corresponding proxy node.
        final Map<String, String> mProxyNodes = new HashMap<String, String>();

        // counter of proxy nodes that where generated.
        int proxyCounter = 0;

        try {
            initUserList();
            initGroupList();

            // List of groups already added to DAG. Just to prevent double group adding.
            final LinkedList<String> groupsInDag = new LinkedList<String>();
            // add an initial null value. all nodes getting null value as root are root nodes itself.
            groupsInDag.add(null);
            mDeepCtrlList.put(null, -1);

            final FileWriter fstream_groups = new FileWriter(OUTFILE_GROUPS);
            final BufferedWriter out_groups = new BufferedWriter(fstream_groups);

            final FileWriter fstream_users = new FileWriter(OUTFILE_USERS);
            final BufferedWriter out_users = new BufferedWriter(fstream_users);

            // add groups to DAG.
            int i = 0;
            while (i < mGroupList.size()) {
                final String groupName = mGroupList.get(i);

                // choose randomly an parent of already added group for the new group. If null node is chosen,
                // the group will be added as an
                // another root node.
                final int randParentGroup = new Random().nextInt(groupsInDag.size());
                final String parent = groupsInDag.get(randParentGroup);

                // check whether depth of parent is less than max depth. If so, add node with parent,
                // otherwise, choose another random parent.
                final int deepOfParent = mDeepCtrlList.get(parent);
                if (deepOfParent < mMaxDepth) {

                    int nodeChilds = 0;
                    if (parent != null) {
                        nodeChilds = mNodeChildNum.get(parent);
                    }

                    // max fanout is considered when activated.
                    if (mFanoutStatus) {
                        // check childs of parent. If child number is less than fanout, add node. Otherwise,
                        // check
                        // if proxy node already exist. If not create it, otherwise check childs of proxy node
                        // and
                        // so on. Loop through all proxy nodes until a empty space for adding node is found.
                        if (nodeChilds < mFanout) {
                            out_groups.write(groupName + ";" + parent + "\n");
                            groupsInDag.add(groupName);
                            mNodeChildNum.put(parent, nodeChilds + 1); // increase childs of parent by one
                            mNodeChildNum.put(groupName, 0); // add new node to map with child value 0.
                            mDeepCtrlList.put(groupName, deepOfParent + 1);
                            i++;
                        } else {
                            if (mProxyNodes.containsKey(parent)) {
                                boolean emptySpaceFound = false;
                                String curProxy = mProxyNodes.get(parent);
                                while (emptySpaceFound == false) {
                                    int proxyFanout = mNodeChildNum.get(curProxy);
                                    if (proxyFanout < mFanout) {
                                        out_groups.write(groupName + ";" + curProxy + "\n");
                                        groupsInDag.add(groupName);
                                        mNodeChildNum.put(curProxy, proxyFanout + 1);
                                        mNodeChildNum.put(groupName, 0);
                                        mDeepCtrlList.put(groupName, deepOfParent + 1);
                                        i++;
                                        emptySpaceFound = true;
                                    } else {
                                        if (!mProxyNodes.containsKey(curProxy)) {
                                            final String proxyNode = "Proxy_" + proxyCounter;
                                            out_groups.write(proxyNode + ";" + curProxy + "\n");
                                            out_groups.write(groupName + ";" + proxyNode + "\n");
                                            mNodeChildNum.put(proxyNode, 1);
                                            mProxyNodes.put(curProxy, proxyNode);
                                            proxyCounter++;
                                            i++;
                                        } else {
                                            curProxy = mProxyNodes.get(curProxy);
                                        }
                                    }
                                }

                            } else {
                                final String proxyNode = "Proxy_" + proxyCounter;
                                out_groups.write(proxyNode + ";" + parent + "\n");
                                out_groups.write(groupName + ";" + proxyNode + "\n");
                                mNodeChildNum.put(proxyNode, 1);
                                mProxyNodes.put(parent, proxyNode);
                                proxyCounter++;
                                i++;

                            }
                        }
                    } else {

                        out_groups.write(groupName + ";" + parent + "\n");
                        groupsInDag.add(groupName);
                        mNodeChildNum.put(parent, nodeChilds + 1); // increase childs of parent by one
                        mNodeChildNum.put(groupName, 0); // add new node to map with child value 0.
                        mDeepCtrlList.put(groupName, deepOfParent + 1);
                        i++;
                    }
                }
            }

            for (int k = 0; k < mUserList.size(); k++) {
                final int randParentsNumber = (int)(Math.random() * mMaxParents) + 1;

                final LinkedList<String> mParent = new LinkedList<String>();
                final StringBuilder parents = new StringBuilder();
                int j = 0;

                while (j < randParentsNumber) {

                    final int randParentPos = new Random().nextInt(groupsInDag.size());
                    final String group = groupsInDag.get(randParentPos);
                    if (!mParent.contains(group) && group != null) {

                        if (mFanoutStatus) {
                            final int nodeChilds = mNodeChildNum.get(group);
                            if (nodeChilds < mFanout) {
                                mNodeChildNum.put(group, nodeChilds + 1);
                                mParent.add(group);
                                parents.append(group);
                            } else {
                                if (mProxyNodes.containsKey(group)) {
                                    boolean emptySpaceFound = false;
                                    String curProxy = mProxyNodes.get(group);
                                    while (emptySpaceFound == false) {
                                        int proxyFanout = mNodeChildNum.get(curProxy);
                                        if (proxyFanout < mFanout) {
                                            mNodeChildNum.put(group, nodeChilds + 1);
                                            mParent.add(group);
                                            parents.append(group);
                                            emptySpaceFound = true;
                                        } else {
                                            if (!mProxyNodes.containsKey(curProxy)) {
                                                final String proxyNode = "Proxy_" + proxyCounter;
                                                out_groups.write(proxyNode + ";" + curProxy + "\n");
                                                mParent.add(group);
                                                parents.append(group);
                                                mProxyNodes.put(curProxy, proxyNode);
                                                mNodeChildNum.put(proxyNode, 1);
                                                proxyCounter++;
                                            } else {
                                                curProxy = mProxyNodes.get(curProxy);
                                            }
                                        }
                                    }

                                } else {
                                    final String proxyNode = "Proxy_" + proxyCounter;
                                    out_groups.write(proxyNode + ";" + group + "\n");
                                    mParent.add(group);
                                    parents.append(group);
                                    mProxyNodes.put(group, proxyNode);
                                    mNodeChildNum.put(proxyNode, 1);
                                    proxyCounter++;
                                }
                            }

                            j++;
                            if (j != randParentsNumber) {
                                parents.append(";");
                            }

                        } else {
                            mParent.add(group);
                            parents.append(group);

                            j++;
                            if (j != randParentsNumber) {
                                parents.append(";");
                            }
                        }
                    }

                }
                out_users.write(mUserList.get(k) + ";" + parents.toString() + "\n");
            }

            out_groups.close();
            out_users.close();

            System.out.println("DAG files have been created!");
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Inits users list with given number of user in for of User_<number>
     */
    public static void initUserList() {
        for (int i = 0; i < mUsers; i++) {
            mUserList.add("User_" + i);
        }
    }

    /**
     * Generates random unique group names and inits groups list with generated groups.
     */
    public static void initGroupList() {
        int i = 0;
        while (i < mGroups) {
            final StringBuilder groupName = new StringBuilder();
            for (int j = 0; j < mGroupNameLength; j++) {
                final int randCharPos = new Random().nextInt(mChars.length);
                groupName.append(mChars[randCharPos]);
            }
            if (!mGroupList.contains(groupName.toString())) {
                mGroupList.add(groupName.toString());
                i++;
            }
        }
    }
}
