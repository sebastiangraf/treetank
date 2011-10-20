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

import java.io.File;
import java.util.LinkedList;

/**
 * Class holding benching parameters.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class BenchParams {

    /**
     * Input file groups.
     */
    private static final String FILE_GROUPS = "src" + File.separator + "test"
        + File.separator + "resources" + File.separator + "randomDAGGroups.txt";

    /**
     * Input file users.
     */
    private static final String FILE_USERS = "src" + File.separator + "test"
        + File.separator + "resources" + File.separator + "randomDAGUsers.txt";

    /**
     * Input file groups.
     */
    private static final String FILE_GROUPS_PROXY = "src" + File.separator
        + "test" + File.separator + "resources" + File.separator
        + "randomDAGGroupsProxy.txt";

    /**
     * Input file users.
     */
    private static final String FILE_USERS_PROXY = "src" + File.separator
        + "test" + File.separator + "resources" + File.separator
        + "randomDAGUsersProxy.txt";

    /**
     * Number of groups in DAG.
     */
    private static int mGroups = 250;            ;

    /**
     * Number of users in DAG.
     */
    private static int mUsers = 6400;

    /**
     * Length of random group names.
     */
    private static int mGroupNameLength = 10;

    /**
     * Max number of parents a user can has.
     */
    private static int mMaxParents = 3;

    /**
     * Max depth of the DAG.
     */
    private static int mMaxDepth = 10;

    /**
     * Number of root nodes in DAG.
     */
    private static int mRootNodes = 8;

    /**
     * Fanout of DAG.
     */
    private static int mFanout = 4;
    
    /**
     * Groups for benching.
     */
    private static int benchingGroups = 250;

    /**
     * Array of positions where a benchmark should be conducted.
     */
    private final static int[] benchPos = new int[] {
        50, 100, 200, 400, 800, 1600, 3200, 6400
    };

    /**
     * Getter methods.
     */

    public static int getGroups() {
        return mGroups;
    }

    public static int getUsers() {
        return mUsers;
    }

    public static int getGroupNameLength() {
        return mGroupNameLength;
    }

    public static int getMaxParents() {
        return mMaxParents;
    }

    public static int getMaxDepth() {
        return mMaxDepth;
    }

    public static int getRootNodes() {
        return mRootNodes;
    }

    public static int getFanout() {
        return mFanout;
    }

    public static LinkedList<Integer> getBenchPosList() {
        final LinkedList<Integer> posList = new LinkedList<Integer>();
        for (int i = 0; i < benchPos.length; i++) {
            posList.add(benchPos[i]);
        }
        return posList;
    }

    public static String getBenchFileGroups() {
        return FILE_GROUPS;
    }

    public static String getBenchFileUsers() {
        return FILE_USERS;
    }

    public static String getBenchFileGroupsProxy() {
        return FILE_GROUPS_PROXY;
    }

    public static String getBenchFileUsersProxy() {
        return FILE_USERS_PROXY;
    }
    
    public static int getBenchGroups(){
        return benchingGroups;
    }

}
