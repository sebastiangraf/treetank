package org.treetank.access;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.treetank.api.IFilelistenerReadTrx;
import org.treetank.api.IPageReadTrx;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.filelistener.file.node.FileNode;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory.MetaKey;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory.MetaValue;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;

/**
 * @author Andreas Rain
 */
public class FilelistenerReadTrx implements IFilelistenerReadTrx {

    /** State of transaction including all cached stuff. */
    protected IPageReadTrx mPageReadTrx;

    /** A dir used to generate the files in. */
    private final File mTmpDir;

    /** A special Key for empty files */
    public static final long emptyFileKey = Long.MIN_VALUE;

    /**
     * Constructor.
     * 
     * 
     * @param pPageTrx
     *            Transaction state to work with.
     * @throws TTIOException
     *             if something odd happens within the creation process.
     */
    public FilelistenerReadTrx(final IPageReadTrx pPageTrx) throws TTException {
        mPageReadTrx = pPageTrx;
        mTmpDir = Files.createTempDir();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFilePaths() {
        Object[] metaKeys = mPageReadTrx.getMetaPage().getMetaMap().keySet().toArray();

        String[] filePaths = new String[metaKeys.length];

        for (int i = 0; i < metaKeys.length; i++) {
            filePaths[i] = ((MetaKey)metaKeys[i]).getKey();
        }

        return filePaths;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fileExists(String pRelativePath) {
        String paths[] = this.getFilePaths();
        for (String s : paths) {
            if (s.equals(pRelativePath)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTIOException
     */
    @Override
    public File getFullFile(String pRelativePath) throws TTIOException, IOException {
        MetaValue value = (MetaValue)mPageReadTrx.getMetaPage().getMetaMap().get(new MetaKey(pRelativePath));

        File file =
            new File(new StringBuilder().append(mTmpDir.getAbsolutePath()).append(File.separator).append(
                pRelativePath).toString());

        if (file.exists()) {
            file.delete();
        }

        file.createNewFile();

        if (value.getData() == emptyFileKey) {
            return file;
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput(FileNode.FILENODESIZE);

        FileNode node = (FileNode)mPageReadTrx.getNode(value.getData());

        OutputSupplier<FileOutputStream> supplier = Files.newOutputStreamSupplier(file, true);

        // Iterating as long as we didn't find the end of the file
        // and writing the bytes to a temporary file.
        do {
            supplier.getOutput().write(node.getVal());
            node = (FileNode)mPageReadTrx.getNode(node.getNextNodeKey());
        } while (!node.isEof());

        supplier.getOutput().close();

        return file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        mTmpDir.delete();
        mPageReadTrx.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mPageReadTrx.isClosed();
    }

    /**
     * Make sure that the session is not yet closed when calling this method.
     */
    protected final void assertNotClosed() {
        if (mPageReadTrx.isClosed()) {
            throw new IllegalStateException("Transaction is already closed.");
        }
    }

    /**
     * Replace the state of the transaction.
     * 
     * @param paramTransactionState
     *            State of transaction.
     */
    protected final void setPageTransaction(final IPageReadTrx paramTransactionState) {
        mPageReadTrx = paramTransactionState;
    }

}
