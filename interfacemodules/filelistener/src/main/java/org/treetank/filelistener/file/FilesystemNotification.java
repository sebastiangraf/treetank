package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.nio.file.WatchEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.treetank.api.IFilelistenerWriteTrx;

/**
 * This class is used to safe notifications from the filesystem for later
 * processing via the WorkingQueue.
 * 
 * @author Andreas Rain
 * 
 */
public class FilesystemNotification implements Callable<Void> {

    /** The file that has been changed */
    private final File mFile;

    /** Determine whether or not this fsn has finished. */
    private boolean mFinished;

    /** The relative path as a String */
    private final String mRelativePath;

    /** The root path of the listener folder */
    private final String mRootPath;

    /** The event for this notification */
    private WatchEvent.Kind<?> mEvtType;

    /**
     * Time this notification needed to be processed.
     */
    private long mTimeTaken;

    /**
     * Bucket amount
     */
    private int mBuckets;

    /** Transaction to use */
    private final IFilelistenerWriteTrx mWtx;

    /**
     * FilesystemNotificationObservers for this notification
     */
    private Collection<FilesystemNotificationObserver> mObservers;

    /**
     * Create a FilesystemNotification that holds the File
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
        mFinished = false;
        mObservers = new HashSet<>();
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

    public void setEvtType(WatchEvent.Kind<?> pEvtType) {
        mEvtType = pEvtType;
    }

    public WatchEvent.Kind<?> getEvtType() {
        return mEvtType;
    }

    public boolean isFinished() {
        return mFinished;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mEvtType, mRelativePath, mRootPath, mFile);
    }

    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    @Override
    public Void call() throws Exception {
        mTimeTaken = System.currentTimeMillis();

        if (this.getEvtType() == ENTRY_CREATE) {
            mWtx.addEmptyFile(this.getRelativePath());
        } else if (this.getEvtType() == ENTRY_MODIFY) {
            mWtx.removeFile(this.getRelativePath());
            if (this.getFile().exists()) {
                mWtx.addFile(this.getFile(), this.getRelativePath());

                if (this.mObservers.size() > 0) {
                    // For bench purposes
                    // System.out.println("Commiting blocked");
                    mWtx.commitBlocked();
                    // System.out.println("Commit finsihed.");
                } else {
                    // Non blocking
                    mWtx.commit();
                }
            }
        } else if (this.getEvtType() == ENTRY_DELETE) {
            mWtx.removeFile(this.getRelativePath());
        }

        mFinished = true;

        mTimeTaken = System.currentTimeMillis() - mTimeTaken;

        // Notifying observers that a task has been finished.
        this.notifyObservers();
        return null;
    }

    /**
     * Determine how long this notifcation was processed.
     * 
     * @return time in form of a long in ms
     */
    public long getTime() {
        return mTimeTaken;
    }

    /**
     * Determine how many buckets are in the storage after this notifcation was processed.
     * 
     * @return amount of buckets
     */
    public int getBucketAmount() {
        return mBuckets;
    }

    /**
     * @throws InterruptedException
     */
    public void notifyObservers() throws InterruptedException {
        synchronized (this) {
            for (FilesystemNotificationObserver o : mObservers) {
                mBuckets = mWtx.getCount();
                o.addNotification(this);
            }
        }
    }

    /**
     * @param observer
     */
    public void addObserver(FilesystemNotificationObserver observer) {
        this.mObservers.add(observer);
    }

}
