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
import java.util.LinkedList;
import java.util.Random;

import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.EncryptionOperator;
import org.treetank.exception.TTEncryptionException;

/**
 * This class creates a random DAG file with a given number of groups and given number of users.
 */
public class RandomDAGFileCreation {

    /**
     * Output File.
     */
    private static final String OUTFILE = "src" + File.separator + "test"
        + File.separator + "resources" + File.separator + "randomDAG.txt";

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
     * Number of groups in DAG.
     */
    final private static int mGroups = 30;

    /**
     * Number of users in DAG.
     */
    final private static int mUsers = 50;

    /**
     * Length of random group names.
     */
    final private static int mGroupNameLength = 10;

    /**
     * Max number of parents a user can has.
     */
    final private static int mMaxParents = 3;

    public static void main(String[] args) {

        try {
            initUserList();
            initGroupList();

            // List of groups already added to DAG. Just to prevent double group adding.
            final LinkedList<String> groupsInDag = new LinkedList<String>();
            // add an initial root node and a null node.
            groupsInDag.add(null);

            final FileWriter fstream = new FileWriter(OUTFILE);
            final BufferedWriter out = new BufferedWriter(fstream);

            // add groups to DAG.
            for (int i = 0; i < mGroupList.size(); i++) {
                System.out.println("Group: " + i);
                final String groupName = mGroupList.get(i);

                // choose randomly an parent of already added group for the new group. If null node is chosen,
                // the group will be added as an
                // another root node.
                final int randParentGroup =
                    new Random().nextInt(groupsInDag.size());
                final String parent = groupsInDag.get(randParentGroup);
                groupsInDag.add(groupName);
                
                out.write(groupName + ";" + parent + "\n");
            }

            out.write("//\n");

            for (int i = 0; i < mUserList.size(); i++) {
                System.out.println("User: " + i);
                final int randParentsNumber =
                    (int)(Math.random() * mMaxParents) + 1;

                final LinkedList<String> mParent = new LinkedList<String>();
                final StringBuilder parents = new StringBuilder();
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
                out.write(mUserList.get(i) + ";" + parents.toString()+"\n");
            }

            out.close();
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
