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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.treetank.encryption.EncryptionController;
import org.treetank.encryption.EncryptionOperator;
import org.treetank.exception.TTEncryptionException;

/**
 * This class builds up and benches a DAG using a DAG file created with
 * org.treetank.encryption.utils.RandomDAGFileCreation class.
 */
public class BenchRandomDAG {

    /**
     * Input file groups.
     */
    private static final String INPUTFILE_GROUPS = "src" + File.separator + "test" + File.separator
        + "resources" + File.separator + "randomDAGGroups.txt";

    /**
     * Input file users.
     */
    private static final String INPUTFILE_USERS = "src" + File.separator + "test" + File.separator
        + "resources" + File.separator + "randomDAGUsers.txt";

    /**
     * Data splitter.
     */
    final private static char splitter = ';';

    public static void main(String[] args) {
        BufferedReader in_groups;
        BufferedReader in_users;
        try {

            System.out.println("Starting...");

            new EncryptionController().clear();
            new EncryptionController().setEncryptionOption(true);
            new EncryptionController().init();

            EncryptionOperator op = new EncryptionOperator();

            in_groups = new BufferedReader(new InputStreamReader(new FileInputStream(INPUTFILE_GROUPS)));

            in_users = new BufferedReader(new InputStreamReader(new FileInputStream(INPUTFILE_USERS)));

            String line;
         // add a groups to DAG.
            while (((line = in_groups.readLine()) != null)) {
                final String[] elements = splitData(line);
                final String parent = elements[1];
                final String group = elements[0];

                // op.singleJoin(new String[] {
                // parent
                // }, group);

            }

         // add a users to DAG.
            while (((line = in_users.readLine()) != null)) {
                final String[] elements = splitData(line);
                final String userName = elements[0];

                for (int i = 1; i < elements.length; i++) {
                    System.out.println(userName + " " + elements[i]);
                    op.join(elements[i], new String[] {
                        userName
                    });
                }

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

    /**
     * Splits a line by given splitter.
     * 
     * @param line
     *            line to be splitted.
     * @return
     *         array with splitted elements.
     */
    public static String[] splitData(final String line) {

        final LinkedList<String> elements = new LinkedList<String>();
        final char[] chars = line.toCharArray();
        int charCount = 1;

        final StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            if (aChar == splitter) {
                elements.add(sb.toString());
                sb.setLength(0);
            } else if (charCount == chars.length) {
                sb.append(aChar);
                elements.add(sb.toString());
            } else {
                sb.append(aChar);
            }
            charCount++;
        }
        return elements.toArray(new String[0]);

    }
}
