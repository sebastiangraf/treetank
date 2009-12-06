package com.treetank.bench.slidingSnapshot;

import java.io.File;

import org.perfidix.meter.AbstractMeter;

public class FileSizeMeter extends AbstractMeter {

    private final File mFile;

    public FileSizeMeter(final File file) {
        mFile = file;
    }

    @Override
    public boolean equals(Object obj) {
        return true;
    }

    @Override
    public String getName() {

        return "FileSize";
    }

    @Override
    public String getUnit() {

        return "bytes";
    }

    @Override
    public String getUnitDescription() {

        return "bytes";
    }

    @Override
    public double getValue() {
        long length = computeLength(mFile);
        return length;
    }

    private final long computeLength(final File file) {
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

    @Override
    public int hashCode() {
        return 1;
    }

}
