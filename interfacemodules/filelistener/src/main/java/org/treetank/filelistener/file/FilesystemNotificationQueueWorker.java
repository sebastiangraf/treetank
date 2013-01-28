package org.treetank.filelistener.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.treetank.api.IFilelistenerWriteTrx;
import org.treetank.exception.TTException;

public class FilesystemNotificationQueueWorker implements Callable<Void> {

    /** Queue this worker is working on */
    private ConcurrentLinkedQueue<FilesystemNotification> mQueue;
    
    /** Filelistener that created this worked */
    private Filelistener mListener;
    
    /** Determine whether or not this callable has been disposed. */
    private boolean mDisposed = false;
    
    private boolean mWorks = false;

    public FilesystemNotificationQueueWorker(Filelistener pListener) {
        mQueue = new ConcurrentLinkedQueue<>();
        mListener = pListener;
    }

    /**
     * Start the worker using this method.
     * it will handle all filesystemchanges.
     */
    @Override
    public Void call() throws FileNotFoundException, ClassNotFoundException, IOException, TTException, InterruptedException {
        while (!mDisposed) {
            if(!mQueue.isEmpty()){
                FilesystemNotification workTask = mQueue.poll();
                mWorks = true;
                handleWorktask(workTask);
                mWorks = false;
                
                synchronized(mListener){
                    mListener.notify();
                }
            }
            else{
                synchronized(this){
                    this.wait();
                }
            }
        }
        return null;
    }
    
    private void handleWorktask(FilesystemNotification workTask) throws FileNotFoundException, ClassNotFoundException, IOException, TTException, InterruptedException{
        IFilelistenerWriteTrx trx = null;
        
        String relativePath = workTask.getRelativePath();
        
        for(Entry<String, String> e : Filelistener.getFilelisteners().entrySet()){
            if(e.getValue().equals(workTask.getRootPath())){
                trx = mListener.getTrx(e.getKey());
            }
        }
        System.out.println(workTask.getEvtType());
        System.out.println(relativePath);
        if(workTask.getEvtType() == ENTRY_CREATE){
            while(!workTask.getFile().exists()){
                Thread.sleep(50);
            }
            
            trx.addFile(workTask.getFile(), relativePath);
        }
        else if(workTask.getEvtType() == ENTRY_MODIFY){
            trx.removeFile(relativePath);
            if(workTask.getFile().exists()){
                trx.addFile(workTask.getFile(), relativePath);
            }
        }
        else if(workTask.getEvtType() == ENTRY_DELETE){
            trx.removeFile(relativePath);
        }
        trx.commit();
        System.out.println("Commited.");
    }

    /**
     * Get the queue and add elements to it, so this
     * worker can handle them.
     * 
     * @return
     */
    public ConcurrentLinkedQueue<FilesystemNotification> getQueue() {
        return mQueue;
    }

    /**
     * Determine whether or not this callable has been disposed.
     */
    public boolean isDisposed() {
        return mDisposed;
    }

    /**
     * Dispose this worker.
     */
    public void dispose() {
        mDisposed = true;
    }

    public boolean isWorking() {
        return mWorks;
    }

}
