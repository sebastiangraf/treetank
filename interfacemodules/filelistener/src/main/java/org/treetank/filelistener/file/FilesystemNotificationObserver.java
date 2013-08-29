package org.treetank.filelistener.file;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Since FilesystemNotifications mainly run in different threads than the observer,
 * the notifications are observed using this interface instead.
 * 
 * @author Andreas Rain
 *
 */
public interface FilesystemNotificationObserver {
    
    /**
     *  Adds a new notification to the observer.
     * @param notification
     */
    public LinkedBlockingQueue<FilesystemNotification> getBlockingQueue();

}
