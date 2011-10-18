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

import java.util.LinkedList;

/**
 * Class holding some methods for benchmarking and JUnit tests.
 * 
 * @author Patrick Lang, University of Konstanz
 * 
 */
public class BenchUtils {

    final char splitter = '$';

    public String[] parseData(final String line) {

        final char[] chars = line.toCharArray();
        final String[] dataString = new String[5];

        int stringCount = 0;
        int charCount = 1;

        final StringBuilder sb = new StringBuilder();

        for (char aChar : chars) {
            if (aChar == splitter || charCount == chars.length) {
                dataString[stringCount++] = sb.toString();
                sb.setLength(0);
            } else {
                sb.append(aChar);
            }
            charCount++;
        }

        return dataString;

    }

    public String cleanGroup(final String group) {
        final char[] groupChars = group.toCharArray();
        int pos = 0;
        // remove nils
        int i = groupChars.length - 1;
        while (i >= 0) {
            if (groupChars[i] != '0') {
                pos = i;
                break;
            }
            i--;
        }

        final StringBuilder sb = new StringBuilder();
        for (int j = 0; j < pos + 1; j++) {
            sb.append(groupChars[j]);
        }

        return sb.toString();

    }

    public LinkedList<String> splitGroup(final String group) {
        final char[] groupChars = group.toCharArray();

        final LinkedList<String> groups = new LinkedList<String>();

        final StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < groupChars.length) {
            if (i == 0) {
                sb.append(groupChars[i++]);
                if (groupChars.length > 1) {
                    sb.append(groupChars[i++]);
                }
                groups.add(sb.toString());
            } else {
                sb.append(groupChars[i++]);
                groups.add(sb.toString());
            }
        }

        return groups;

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
        final char splitter = ';';
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
