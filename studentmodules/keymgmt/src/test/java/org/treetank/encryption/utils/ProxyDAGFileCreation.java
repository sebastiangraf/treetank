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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * This class creates a DAG file where a maximal depth and a given fanout is considered. The class uses a not
 * regulated (fanout)
 * DAG created with RandomDAGFileCreation.class and creates a new regulated one where fanout is considered.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class ProxyDAGFileCreation {

    /**
     * DAG files creation.
     */
    public void create() {

        // Map holding all added nodes with a counter of its childs.
        final Map<String, Integer> mNodeChildNum =
            new HashMap<String, Integer>();
        mNodeChildNum.put("null", -1);

        // Map holding all nodes having more childs than given fanout and its corresponding proxy node.
        final Map<String, String> mProxyNodes = new HashMap<String, String>();

        // counter of proxy nodes that where generated.
        int proxyCounter = 0;

        try {

            final FileWriter fstream_groups =
                new FileWriter(BenchParams.getBenchFileGroupsProxy());
            final BufferedWriter out_groups =
                new BufferedWriter(fstream_groups);

            final FileWriter fstream_users =
                new FileWriter(BenchParams.getBenchFileUsersProxy());
            final BufferedWriter out_users = new BufferedWriter(fstream_users);

            BufferedReader in_groups =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    BenchParams.getBenchFileGroups())));

            BufferedReader in_users =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    BenchParams.getBenchFileUsers())));

            int groupCounter = 0;
            // add groups to DAG.
            String line;
            //while (groupCounter < BenchParams.getBenchGroups()) {
            while ((line = in_groups.readLine()) != null) {
                
                final String[] elements = BenchUtils.splitData(line);
                final String groupName = elements[0];
                final String parent = elements[1];

                int nodeChilds = 0;
                if (!parent.equals("null")) {
                    nodeChilds = mNodeChildNum.get(parent);
                }

                // check childs of parent. If child number is less than fanout, add node. Otherwise,
                // check
                // if proxy node already exist. If not create it, otherwise check childs of proxy node
                // and
                // so on. Loop through all proxy nodes until a empty space for adding node is found.
                if (nodeChilds < BenchParams.getFanout()) {
                    out_groups.write(groupName + ";" + parent + "\n");
                    mNodeChildNum.put(parent, nodeChilds + 1); // increase childs of parent by one
                    mNodeChildNum.put(groupName, 0); // add new node to map with child value 0.

                } else {
                    if (mProxyNodes.containsKey(parent)) {
                        boolean emptySpaceFound = false;
                        String curProxy = mProxyNodes.get(parent);
                        while (emptySpaceFound == false) {
                            int proxyFanout = mNodeChildNum.get(curProxy);
                            if (proxyFanout < BenchParams.getFanout()) {
                                out_groups.write(groupName + ";" + curProxy
                                    + "\n");

                                mNodeChildNum.put(curProxy, proxyFanout + 1);
                                mNodeChildNum.put(groupName, 0);
                                emptySpaceFound = true;
                            } else {
                                if (!mProxyNodes.containsKey(curProxy)) {
                                    final String proxyNode =
                                        "Proxy_" + proxyCounter;
                                    out_groups.write(proxyNode + ";" + curProxy
                                        + "\n");
                                    out_groups.write(groupName + ";"
                                        + proxyNode + "\n");
                                    mNodeChildNum.put(proxyNode, 1);
                                    mNodeChildNum.put(groupName, 0);
                                    mProxyNodes.put(curProxy, proxyNode);
                                    proxyCounter++;
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
                        mNodeChildNum.put(groupName, 0);
                        mProxyNodes.put(parent, proxyNode);
                        proxyCounter++;

                    }
                }
                groupCounter++;

            }

            while ((line = in_users.readLine()) != null) {

                final String[] elements = BenchUtils.splitData(line);
                final String user = elements[0];

                LinkedList<String> mParent = null;
                StringBuilder parents = null;
                int j = 1;

                while (j < elements.length) {
                    mParent = new LinkedList<String>();
                    parents = new StringBuilder();
                    String group = elements[j];

                    if (!mParent.contains(group) && group != null && mNodeChildNum.containsKey(group)) {

                        final int nodeChilds = mNodeChildNum.get(group);
                        if (nodeChilds < BenchParams.getFanout()) {
                            mNodeChildNum.put(group, nodeChilds + 1);
                            mParent.add(group);
                            parents.append(group);
                        } else {
                            if (mProxyNodes.containsKey(group)) {
                                boolean emptySpaceFound = false;
                                String curProxy = mProxyNodes.get(group);
                                while (emptySpaceFound == false) {
                                    int proxyFanout =
                                        mNodeChildNum.get(curProxy);
                                    if (proxyFanout < BenchParams.getFanout()) {
                                        mNodeChildNum.put(curProxy,
                                            nodeChilds + 1);
                                        mParent.add(curProxy);
                                        parents.append(curProxy);
                                        emptySpaceFound = true;
                                    } else {
                                        if (!mProxyNodes.containsKey(curProxy)) {
                                            final String proxyNode =
                                                "Proxy_" + proxyCounter;
                                            out_groups.write(proxyNode + ";"
                                                + curProxy + "\n");
                                            mParent.add(proxyNode);
                                            parents.append(proxyNode);
                                            mProxyNodes
                                                .put(curProxy, proxyNode);
                                            mNodeChildNum.put(proxyNode, 1);
                                            proxyCounter++;
                                            emptySpaceFound = true;
                                        } else {
                                            curProxy =
                                                mProxyNodes.get(curProxy);
                                        }
                                    }
                                }

                            } else {
                                final String proxyNode =
                                    "Proxy_" + proxyCounter;
                                out_groups
                                    .write(proxyNode + ";" + group + "\n");
                                mParent.add(proxyNode);
                                parents.append(proxyNode);
                                mProxyNodes.put(group, proxyNode);
                                mNodeChildNum.put(proxyNode, 1);
                                proxyCounter++;
                            }
                        }

                        j++;
                        if (j < elements.length) {
                            parents.append(";");
                        }

                    }
                    j++;

                }
                out_users.write(user + ";" + parents.toString() + "\n");
            }

            out_groups.close();
            out_users.close();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
