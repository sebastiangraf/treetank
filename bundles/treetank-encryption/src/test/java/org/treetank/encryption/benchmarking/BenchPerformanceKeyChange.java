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
package org.treetank.encryption.benchmarking;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.perfidix.AbstractConfig;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.EncryptionOperator;
import org.treetank.encryption.utils.BenchParams;
import org.treetank.encryption.utils.BenchUtils;
import org.treetank.encryption.utils.RandomDAGFileCreation;
import org.treetank.exception.TTEncryptionException;

/**
 * Class to bench key trails generation according to a number of users in DAG.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class BenchPerformanceKeyChange extends BenchParams {

    /**
     * User who should be benched.
     */
    final private static String benchUser = "User_0";

    /**
     * Group of benched user.
     */
    private static String userParent = "";

    public static void main(String[] args) {
        BufferedReader in_groups;
        BufferedReader in_users;
        try {
            
            in_groups =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    BenchParams.getBenchFileGroups())));

            in_users =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                    BenchParams.getBenchFileUsers())));

            //create DAG
            //new RandomDAGFileCreation().create();
            
            //List with positions on benches should be conducted.
            final LinkedList<Integer> benchPos = BenchParams.getBenchPosList();

            final EncryptionOperator op = new EncryptionOperator();
            
            new EncryptionController().clear();
            new EncryptionController().setEncryptionOption(true);
            new EncryptionController().init();
            new EncryptionController().setUser(benchUser);
            
            System.out.println("Started...");
            
            final LinkedList<String> addedGroups = new LinkedList<String>();
            int groupCounter =0 ;
            
            
            String line;
            // add groups to DAG.
            while (groupCounter < BenchParams.getBenchGroups()) {

                line = in_groups.readLine();
                final String[] elements = BenchUtils.splitData(line);
                final String parent = elements[1];
                final String group = elements[0];
                addedGroups.add(group);

                op.singleJoin(new String[] {
                    parent
                }, group);
                
                groupCounter ++;
            }

            int userCounter = 0;

            // add users to DAG.
            while (userCounter<BenchParams.getUsers()) {

                final String userName = "User_" + userCounter;
                
                final int randParents =
                    (int)(Math.random() * BenchParams.getMaxParents()) +1;

                for (int i = 0; i < randParents; i++) {
                    
                    final int randParent =  (int)(Math.random() * addedGroups.size());
                    
                    op.join(addedGroups.get(randParent), new String[] {
                        userName
                    });
                    
                    if (userCounter == 0 && i == 0) {
                        userParent = addedGroups.get(randParent);
                    }
                }
                
//                final String[] elements = BenchUtils.splitData(line);
//                final String userName = elements[0];

                

                if (benchPos.contains(userCounter)) {
                    KtsMeter.getInstance().reset();
                    op.join(userParent, new String[] {
                        benchUser
                    });
                   
                    System.out.println("User: " + userCounter);
                    System.out.println("Key Trails: "
                        + KtsMeter.getInstance().getValue() + " kts / " + op.getNodes());
                    System.out.println("Time needed: " + op.getKtsTime()
                        + " ms");
                    System.out.println("");
                    // op.leave(benchUser, new String[]{userParent});
                }

               
                userCounter++;
            }

            System.out.println("Done!");

        } catch (final TTEncryptionException e) {
            e.printStackTrace();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
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
    static final KindOfArrangement arrang =
        KindOfArrangement.SequentialMethodArrangement;
    static final double gc = 1;

    static class ThisConfiguration extends AbstractConfig {

        protected ThisConfiguration() {
            super(runs, meters, output, arrang, gc);

        }

    }

}
