package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.nio.file.WatchEvent;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.treetank.api.IFilelistenerWriteTrx;

/**
 * This class is used to safe notifications
 * from the filesystem for later processing via
 * the WorkingQueue.
 * 
 * @author Andreas Rain
 * 
 */
public class FilesystemNotification implements Callable<Void>{

    /** The file that has been changed */
    private final File mFile;

    /** The relative path as a String */
    private final String mRelativePath;

    /** The root path of the listener folder */
    private final String mRootPath;

    /** The event for this notification */
    private final WatchEvent.Kind<?> mEvtType;
    
    /** Transaction to use */
    private final IFilelistenerWriteTrx mWtx;
    
    /**
     * Create a FilesystemNotification
     * that holds the File
     * 
     * @param pFile
     * @param pRelativePath
     * @param pRootPath
     */
    public FilesystemNotification(File pFile, String pRelativePath, String pRootPath,
        WatchEvent.Kind<?> pEvtType, IFilelistenerWriteTrx pWtx) {
        super();
        mFile = pFile;
        mRelativePath = pRelativePath;
        mRootPath = pRootPath;
        mEvtType = pEvtType;
        mWtx = pWtx;
    }

    public File getFile() {
        return mFile;
    }

    public String getRelativePath() {
        return mRelativePath;
    }

    public String getRootPath() {
        return mRootPath;
    }

    public WatchEvent.Kind<?> getEvtType() {
        return mEvtType;
    }

    @Override
    public int hashCode(){
        return Objects.hash(mEvtType, mRelativePath, mRootPath, mFile);
    }
    
    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    @Override
    public Void call() throws Exception {
        if (this.getEvtType() == ENTRY_CREATE) {
            mWtx.addEmptyFile(this.getRelativePath());
        } else if (this.getEvtType() == ENTRY_MODIFY) {
            mWtx.removeFile(this.getRelativePath());
            if (this.getFile().exists()) {
                mWtx.addFile(this.getFile(), this.getRelativePath());
            }
        } else if (this.getEvtType() == ENTRY_DELETE) {
            mWtx.removeFile(this.getRelativePath());
        }
        mWtx.commit();
        System.out.println("Commited.");
        return null;
    }

}
