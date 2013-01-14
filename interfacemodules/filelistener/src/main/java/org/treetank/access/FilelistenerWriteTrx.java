package org.treetank.access;

import java.io.File;

import org.treetank.api.IFilelistenerWriteTrx;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;

public class FilelistenerWriteTrx implements IFilelistenerWriteTrx {

    @Override
    public long[] getFileKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getFullFile(long pKey) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close() throws TTIOException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addFile(File file, String relativePath) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFile(long pkey) throws TTException {
        // TODO Auto-generated method stub

    }

    @Override
    public void commit() throws TTException {
        // TODO Auto-generated method stub

    }

    @Override
    public void abort() throws TTException {
        // TODO Auto-generated method stub

    }

    @Override
    public long getMaxNodeKey() {
        // TODO Auto-generated method stub
        return 0;
    }

}
