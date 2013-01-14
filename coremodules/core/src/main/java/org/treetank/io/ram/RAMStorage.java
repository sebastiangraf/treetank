package org.treetank.io.ram;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.io.IBackend;
import org.treetank.io.IBackendReader;
import org.treetank.io.IBackendWriter;
import org.treetank.io.bytepipe.IByteHandler.IByteHandlerPipeline;
import org.treetank.page.UberPage;
import org.treetank.page.interfaces.IPage;

import com.google.inject.Inject;

public class RAMStorage implements IBackend {

    private final Map<Long, IPage> mStorage;

    private final IByteHandlerPipeline mHandler;

    private final RAMAccess access;

    @Inject
    public RAMStorage(IByteHandlerPipeline pByteHandler) {
        mStorage = new ConcurrentHashMap<Long, IPage>();
        mHandler = pByteHandler;
        access = new RAMAccess();
    }

    @Override
    public IBackendWriter getWriter() throws TTException {
        return access;
    }

    @Override
    public IBackendReader getReader() throws TTException {
        return access;
    }

    @Override
    public void close() throws TTException {
    }

    @Override
    public IByteHandlerPipeline getByteHandler() {
        return mHandler;
    }

    @Override
    public boolean truncate() throws TTException {
        mStorage.clear();
        return true;
    }

    public class RAMAccess implements IBackendWriter {

        @Override
        public IPage read(long pKey) throws TTIOException {
            return mStorage.get(new Long(pKey));
        }

        @Override
        public UberPage readUber() throws TTIOException {
            return (UberPage)mStorage.get(new Long(-1));
        }

        @Override
        public void write(IPage page) throws TTException {
            mStorage.put(page.getPageKey(), page);
        }

        @Override
        public void writeUberPage(UberPage page) throws TTException {
            mStorage.put(new Long(-1), page);
        }

        @Override
        public void close() throws TTIOException {
        }

    }

}
