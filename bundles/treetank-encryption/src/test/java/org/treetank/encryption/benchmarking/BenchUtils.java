package org.treetank.encryption.benchmarking;

import java.util.LinkedList;

/**
 * Class holding some methods for benchmarking tests.
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

}
