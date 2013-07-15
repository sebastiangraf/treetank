package org.treetank.io;

import java.util.HashMap;
import java.util.Map;

import org.treetank.exception.TTIOException;

public class MemoryLog implements ILog {

    private final Map<LogKey, LogValue> mMap;
    private boolean mClosed;

    public MemoryLog() {
        mMap = new HashMap<LogKey, LogValue>();
        mClosed = false;
    }

    @Override
    public LogValue get(LogKey pKey) throws TTIOException {
        LogValue val = mMap.get(pKey);
        return val == null ? new LogValue(null, null) : val;
    }

    @Override
    public void put(LogKey pKey, LogValue pValue) throws TTIOException {
        mMap.put(pKey, pValue);
    }

    @Override
    public void close() throws TTIOException {
        mClosed = true;
    }

    @Override
    public boolean isClosed() {
        return mClosed;
    }

}
