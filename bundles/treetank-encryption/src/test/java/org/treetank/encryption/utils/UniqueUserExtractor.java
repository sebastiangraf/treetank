/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;


/**
 * This class extracts only one data set of each user and builds a new data set only with unique users.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class UniqueUserExtractor {

    private static final String INPUTFILE = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "testdata.txt";

    private static final String OUTFILE = "src" + File.separator + "test" + File.separator + "resources"
    + File.separator + "testdataUser.txt";

    public static void main(String[] args) {
        System.out.println("Started...");
        init();
        System.out.println("Done!");
    }

    public static void init() {
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(INPUTFILE)));

            String line;
            final LinkedList<String> parsedUsers = new LinkedList<String>();

            final FileWriter fstream = new FileWriter(OUTFILE);
            final BufferedWriter out = new BufferedWriter(fstream);

            while (((line = in.readLine()) != null)) {

                final String[] dataArray = new BenchUtils().parseData(line);

                if (!parsedUsers.contains(dataArray[0])) { // dataArray[0] is username.
                    parsedUsers.add(dataArray[0]);
                    out.write(line + "\n");

                }

            }
            out.close();

        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

}
