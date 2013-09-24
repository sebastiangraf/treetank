package org.treetank.access;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.treetank.api.IBucketReadTrx;
import org.treetank.api.IFilelistenerReadTrx;
import org.treetank.api.IMetaEntry;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.filelistener.file.StorageManager;
import org.treetank.filelistener.file.data.FileData;
import org.treetank.filelistener.file.data.FilelistenerMetaDataFactory.MetaKey;
import org.treetank.filelistener.file.data.FilelistenerMetaDataFactory.MetaValue;

import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;

/**
 * @author Andreas Rain
 */
public class FilelistenerReadTrx implements IFilelistenerReadTrx {

    /** State of transaction including all cached stuff. */
    protected IBucketReadTrx mPageReadTrx;

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
    public FilelistenerReadTrx(final IBucketReadTrx pPageTrx) throws TTException {
        mPageReadTrx = pPageTrx;
        mTmpDir = Files.createTempDir();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFilePaths() {
        Set<Map.Entry<IMetaEntry, IMetaEntry>> metaKeys = mPageReadTrx.getMetaBucket().entrySet();

        String[] filePaths = new String[metaKeys.size()];

        int i = 0;
        for (Map.Entry<IMetaEntry, IMetaEntry> entry : metaKeys) {
            filePaths[i] = entry.getKey().toString();
            i++;
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
        MetaValue value = (MetaValue)mPageReadTrx.getMetaBucket().get(new MetaKey(pRelativePath));

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

        FileData node = (FileData)mPageReadTrx.getData(value.getData());

        OutputSupplier<FileOutputStream> supplier = Files.newOutputStreamSupplier(file, true);

        // Iterating as long as we didn't find the end of the file
        // and writing the bytes to a temporary file.
        do {
            supplier.getOutput().write(node.getVal());
            node = (FileData)mPageReadTrx.getData(node.getDataKey() + 1);
        } while (!node.isEof());

        supplier.getOutput().close();

        return file;
    }

    /**
     * {@inheritDoc}
     */
    public int getCount() {
        // This will only apply for the filebench or if this resource name on a local storage is used
//        String resourceName = "bench53473ResourcegraveISCSI9283";
//        String bucketFolder = StorageManager.ROOT_PATH
//        + File.separator + "storage" + File.separator + "resources"
//        + File.separator + resourceName + File.separator + "data"
//        + File.separator + resourceName;
//        String[] buckets = new File(bucketFolder).list();
//        if(buckets == null){
//            return -1;
//        }
//        return buckets.length;
    	return -1;
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
    protected final void setPageTransaction(final IBucketReadTrx paramTransactionState) {
        mPageReadTrx = paramTransactionState;
    }

}
