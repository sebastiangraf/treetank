package com.treetank.xmlprague;

import java.io.File;

import org.perfidix.meter.AbstractMeter;

public class FilesizeMeter extends AbstractMeter {

    private final File mFile;

    public FilesizeMeter(final File paramfile) {
        this.mFile = paramfile;
    }

    @Override
    public double getValue() {
        return mFile.length();
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
    public String getName() {
        return "FileSizeMeter";
    }

    @Override
    public int hashCode() {
        return this.mFile.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

}
