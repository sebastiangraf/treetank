package com.treetank.bench.slidingSnapshot;

import java.io.File;
import java.util.Random;

public class CommonStuff {

    public static final Random ran = new Random(0l);

    public static final File PATH0 = new File(new StringBuilder(File.separator)
            .append("tmp").append(File.separator).append("tnk").toString());

    public static final File PATH1 = new File(new StringBuilder(File.separator)
            .append("tmp").append(File.separator).append("tnk").append(
                    File.separator).append("path10").toString());
    public static final File PATH2 = new File(new StringBuilder(File.separator)
            .append("tmp").append(File.separator).append("tnk").append(
                    File.separator).append("path11").toString());
    public static final File PATH3 = new File(new StringBuilder(File.separator)
            .append("tmp").append(File.separator).append("tnk").append(
                    File.separator).append("path12").toString());
    public static final File PATH4 = new File(new StringBuilder(File.separator)
            .append("tmp").append(File.separator).append("tnk").append(
                    File.separator).append("path13").toString());

    public final static File RESULTFOLDER = new File(new StringBuilder(
            File.separator).append("tmp").append(File.separator).append(
            "results").toString());

    public static final File XMLPath = new File(new StringBuilder(
            File.separator).append("tmp").append(File.separator).append(
            "test10.xml").toString());

    public static final long computeLength(final File file) {
        try {
            long length = 0;
            if (file.isDirectory()) {
                for (final File child : file.listFiles()) {
                    length = length + computeLength(child);
                }
            } else {
                length = length + file.length();
            }
            return length;
        } catch (Exception e) {
            return 0;
        }
    }

    private final static String chars = "abcdefghijklm";
    private final static int NUM_CHARS = 3;

    public final static String getString() {
        char[] buf = new char[NUM_CHARS];

        for (int i = 0; i < buf.length; i++) {
            buf[i] = chars.charAt(CommonStuff.ran.nextInt(chars.length()));
        }

        return new String(buf);
    }

}
