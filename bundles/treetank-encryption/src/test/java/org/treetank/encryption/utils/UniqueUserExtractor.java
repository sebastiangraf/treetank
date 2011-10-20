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
