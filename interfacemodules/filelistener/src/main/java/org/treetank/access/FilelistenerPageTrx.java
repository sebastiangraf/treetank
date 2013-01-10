package org.treetank.access;

import org.treetank.api.IFilelistenerPageTrx;
import org.treetank.exception.TTIOException;

public class FilelistenerPageTrx implements IFilelistenerPageTrx {

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

}
