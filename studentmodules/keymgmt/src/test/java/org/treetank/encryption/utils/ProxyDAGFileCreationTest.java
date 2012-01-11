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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * This class validates a created DAG according to a given graph depth and a fanout.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class ProxyDAGFileCreationTest {


    BufferedReader in_groups;
    BufferedReader in_users;

    /**
     * Creates DAG files with the given parameters and sets up the file readers.
     */
    @Before
    public void setUp() {

        new RandomDAGFileCreation().create();
        new ProxyDAGFileCreation().create();

        try {
            in_groups =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    BenchParams.getBenchFileGroupsProxy())));

            in_users =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    BenchParams.getBenchFileUsersProxy())));

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Tests DAG fanout. It reads in all nodes and calculates the number of its children, writes it into a map
     * and
     * checks if a node has more children than the given fanout value.
     */
    @Test
    public void testDAGFanout() {

        final Map<String, Integer> mNodeFanoutMap =
            new HashMap<String, Integer>();

        String line;
        try {
            while (((line = in_groups.readLine()) != null)) {
                final String[] elements = BenchUtils.splitData(line);
                final String parent = elements[1];

                if (mNodeFanoutMap.containsKey(parent)) {
                    int fanout = mNodeFanoutMap.get(parent);
                    mNodeFanoutMap.put(parent, fanout + 1);
                } else {
                    mNodeFanoutMap.put(parent, 1);
                }
            }

            while (((line = in_users.readLine()) != null)) {
                final String[] elements = BenchUtils.splitData(line);

                for (int i = 1; i < elements.length; i++) {
                    final String curParent = elements[i];
                    if (mNodeFanoutMap.containsKey(curParent)) {
                        int fanout = mNodeFanoutMap.get(curParent);
                        mNodeFanoutMap.put(curParent, fanout + 1);
                    } else {
                        mNodeFanoutMap.put(curParent, 1);
                    }
                }

            }

            final Iterator<String> mapIter = mNodeFanoutMap.keySet().iterator();

            while (mapIter.hasNext()) {
                final String key = mapIter.next();
                final int nodeFanout = mNodeFanoutMap.get(key);
                boolean stat = true;
                if (nodeFanout > BenchParams.getFanout() + 1 && !key.equals("null")) { // +1 because of proxy node
                    stat = false;
                }
                assertTrue(stat);
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests DAG depth. It reads in all nodes and calculates its corresponding depth into the DAG and writes
     * both data into a map. Afterwards it checks if a node has a depth that is higher than given maximal
     * depth value.
     */
    @Test
    public void testDAGDepth() {
        final Map<String, Integer> mNodeDepthMap =
            new HashMap<String, Integer>();
        mNodeDepthMap.put("null", -1);

        LinkedList<String> proxyChilds = new LinkedList<String>();

        String line;
        try {
            while (((line = in_groups.readLine()) != null)) {
                final String[] elements = BenchUtils.splitData(line);
                final String child = elements[0];
                final String parent = elements[1];

                if (!proxyChilds.contains(parent)) {
                    final boolean proxyNode = parent.startsWith("Proxy_");
                    if (!proxyNode) {
                        final int parentDepth = mNodeDepthMap.get(parent);
                        mNodeDepthMap.put(child, parentDepth + 1);
                    } else {
                        proxyChilds.add(child);
                    }
                } else {
                    proxyChilds.add(child);
                }

            }

            final Iterator<String> mapIter = mNodeDepthMap.keySet().iterator();
            while (mapIter.hasNext()) {
                final String key = mapIter.next();
                final int nodeDepth = mNodeDepthMap.get(key);
                assertTrue(nodeDepth <= BenchParams.getMaxDepth() + 1); // +1 because of user level
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
