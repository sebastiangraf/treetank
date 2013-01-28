package org.treetank.access;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import org.treetank.api.IFilelistenerWriteTrx;
import org.treetank.api.IPageWriteTrx;
import org.treetank.api.ISession;
import org.treetank.exception.TTException;
import org.treetank.exception.TTIOException;
import org.treetank.filelistener.exceptions.WrongFilenodeDataLengthException;
import org.treetank.filelistener.file.node.FileNode;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory.MetaKey;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory.MetaValue;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * @author Andreas Rain
 */
public class FilelistenerWriteTrx implements IFilelistenerWriteTrx {

    /** Session for abort/commit. */
    private final ISession mSession;

    /** Delegator for the read access */
    private final FilelistenerPageTrx mDelegate;

    /**
     * {@inheritDoc}
     */
    public FilelistenerWriteTrx(IPageWriteTrx pPageTrx, ISession pSession) throws TTException {
        mSession = pSession;
        mDelegate = new FilelistenerPageTrx(pPageTrx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getFilePaths() {
        return mDelegate.getFilePaths();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fileExists(String pRelativePath) {
        return mDelegate.fileExists(pRelativePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFullFile(String pRelativePath) throws TTIOException, IOException {
        return mDelegate.getFullFile(pRelativePath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws TTIOException {
        mDelegate.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return mDelegate.isClosed();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws TTException
     * @throws IOException
     */
    @Override
    public synchronized void addFile(File pFile, String pRelativePath) throws TTException, IOException {
        try {
            BufferedInputStream stream = Files.asByteSource(pFile).openBufferedStream();
            
            byte[] fileBytes = new byte[FileNode.FILENODESIZE];
            stream.read(fileBytes);
            
            long newKey = getPageTransaction().incrementNodeKey();

            if (fileExists(pRelativePath)) {
                removeFile(pRelativePath);
            }
            

            // Setting a new header file node
            MetaKey key = new MetaKey(pRelativePath);
            MetaValue value = new MetaValue(newKey);

            // And adding it to the meta map
            getPageTransaction().createEntry(key, value);

            //Creating and setting the headernode.
            FileNode headerNode = new FileNode(newKey, new byte[FileNode.FILENODESIZE]);
            headerNode.setHeader(true);
            headerNode.setEof(false);
                
            headerNode.setVal(fileBytes);
            
            getPageTransaction().setNode(headerNode);
            
            // Creating and setting following nodes based on the file size.
            FileNode node;
            FileNode lastNode;
            while(stream.read(fileBytes) >= 0){
                node = new FileNode(getPageTransaction().incrementNodeKey(), new byte[FileNode.FILENODESIZE]);
                node.setHeader(false);
                node.setEof(false);

                lastNode =
                    (FileNode)getPageTransaction().prepareNodeForModification(node.getNodeKey() - 1);
                lastNode.setNextNodeKey(node.getNodeKey());
                getPageTransaction().finishNodeModification(lastNode);
                
                node.setVal(fileBytes);
                getPageTransaction().setNode(node);
            }
            
            node = new FileNode(getPageTransaction().incrementNodeKey(), new byte[FileNode.FILENODESIZE]);
            node.setHeader(false);
            node.setEof(true);

            lastNode =
                (FileNode)getPageTransaction().prepareNodeForModification(node.getNodeKey() - 1);
            lastNode.setNextNodeKey(node.getNodeKey());
            getPageTransaction().finishNodeModification(lastNode);
            getPageTransaction().setNode(node);
            
            Preconditions.checkArgument(getPageTransaction().getNode(newKey) != null);
            
        } catch (WrongFilenodeDataLengthException e) {
        }
        
        System.out.println("Done writing.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeFile(String pRelativePath) throws TTException {
        // If the file already exists we just override it
        // and remove the last meta entry since the key won't be correct anymore.
        getPageTransaction().getMetaPage().getMetaMap().remove(new MetaKey(pRelativePath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TTException {
        checkAccessAndCommit();

        // Commit uber page.
        getPageTransaction().commit();
    }

    /**
     * Checking write access and intermediate commit.
     * 
     * @throws TTException
     *             if anything weird happens
     */
    private void checkAccessAndCommit() throws TTException {

        mDelegate.assertNotClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() throws TTException {
        mDelegate.assertNotClosed();

        long revisionToSet = 0;
        revisionToSet = mDelegate.mPageReadTrx.getActualRevisionRootPage().getRevision() - 1;

        getPageTransaction().close();

        // Reset internal transaction state to last committed uber page.
        mDelegate.setPageTransaction(mSession.beginPageWriteTransaction(revisionToSet));
    }

    /**
     * Getter for superclasses.
     * 
     * @return The state of this transaction.
     */
    private PageWriteTrx getPageTransaction() {

        return (PageWriteTrx)mDelegate.mPageReadTrx;
    }
}