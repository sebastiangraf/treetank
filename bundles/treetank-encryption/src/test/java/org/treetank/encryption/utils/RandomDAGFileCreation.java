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
 * This class creates two random DAG files. One for random users and one for random groups. During creation
 * a maximal depth for DAG is considered.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class RandomDAGFileCreation {

    /**
     * Char set for random group names.
     */
    final private static char[] mChars = {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1',
        '2', '3', '4', '5', '6', '7', '8', '9',
    };

    /**
     * List of generated users.
     */
    final private static LinkedList<String> mUserList =
        new LinkedList<String>();

    /**
     * List of generated groups.
     */
    final private static LinkedList<String> mGroupList =
        new LinkedList<String>();


    /**
     * DAG files generation.
     */
    public void create() {
        // Map holding added nodes with its depth.
        final Map<String, Integer> mDeepCtrlList =
            new HashMap<String, Integer>();

        // Map holding all added nodes with a counter of its childs.
        final Map<String, Integer> mNodeChildNum =
            new HashMap<String, Integer>();

        try {
            initUserList();
            initGroupList();

            // List of groups already added to DAG. Just to prevent double group adding.
            final LinkedList<String> groupsInDag = new LinkedList<String>();
  
            final FileWriter fstream_groups = new FileWriter(BenchParams.getBenchFileGroups());
            final BufferedWriter out_groups =
                new BufferedWriter(fstream_groups);

            final FileWriter fstream_users = new FileWriter(BenchParams.getBenchFileUsers());
            final BufferedWriter out_users = new BufferedWriter(fstream_users);
            
            final LinkedList<String> rootNodesList = new LinkedList<String>();
            
            // create and add inital root nodes.
            for(int i=0; i<BenchParams.getRootNodes(); i++){
                final String rootNode = createGroupName();
                groupsInDag.add(rootNode);
                mDeepCtrlList.put(rootNode, 0);
                rootNodesList.add(rootNode);
                out_groups.write(rootNode + ";null\n");
            }

            // add groups to DAG.
            int i = BenchParams.getRootNodes()-1;
            while (i < mGroupList.size()) {
                final String groupName = mGroupList.get(i);

                // choose randomly an parent of already added group for the new group. If null node is chosen,
                // the group will be added as an
                // another root node.
                final int randParentGroup =
                    new Random().nextInt(groupsInDag.size());
                final String parent = groupsInDag.get(randParentGroup);

                // check whether depth of parent is less than max depth. If so, add node with parent,
                // otherwise, choose another random parent.
                final int deepOfParent = mDeepCtrlList.get(parent);
                if (deepOfParent < BenchParams.getMaxDepth()) {
                    int nodeChilds = 0;
                    if (!rootNodesList.contains(parent)) {
                        nodeChilds = mNodeChildNum.get(parent);
                    }

                    out_groups.write(groupName + ";" + parent + "\n");
                    groupsInDag.add(groupName);
                    mNodeChildNum.put(parent, nodeChilds + 1); // increase childs of parent by one
                    mNodeChildNum.put(groupName, 0); // add new node to map with child value 0.
                    mDeepCtrlList.put(groupName, deepOfParent + 1);
                    i++;
                }
            }

            for (int k = 0; k < mUserList.size(); k++) {
                final int randParentsNumber =
                    (int)(Math.random() * BenchParams.getMaxParents()) + 1;

                LinkedList<String> mParent = new LinkedList<String>();
                StringBuilder parents = new StringBuilder();
                int j = 0;

                while (j < randParentsNumber) {

                    final int randParentPos =
                        new Random().nextInt(groupsInDag.size());
                    final String group = groupsInDag.get(randParentPos);

                    if (!mParent.contains(group) && group != null) {
                        mParent.add(group);
                        parents.append(group);

                        j++;
                        if (j != randParentsNumber) {
                            parents.append(";");
                        }
                    }
                }
                out_users.write(mUserList.get(k) + ";" + parents.toString()
                    + "\n");
            }
            out_groups.close();
            out_users.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inits users list with given number of user in for of User_<number>
     */
    public static void initUserList() {
        for (int i = 0; i < BenchParams.getUsers(); i++) {
            mUserList.add("User_" + i);
        }
    }

    /**
     * Generates random unique group names and inits groups list with generated groups.
     */
    public static void initGroupList() {
        int i = 0;
        while (i < BenchParams.getGroups()) {
            final String groupName = createGroupName();
            if (!mGroupList.contains(groupName)) {
                mGroupList.add(groupName);
                i++;
            }
        }
    }

    /**
     * Creates a random group name.
     * 
     * @return
     *         a random group name.
     */
    private static String createGroupName() {
        final StringBuilder groupName = new StringBuilder();
        for (int j = 0; j < BenchParams.getGroupNameLength(); j++) {
            final int randCharPos = new Random().nextInt(mChars.length);
            groupName.append(mChars[randCharPos]);
        }
        return groupName.toString();
    }
}
