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
     * @return blocking queue holding notifications for direct submission
     */
    public LinkedBlockingQueue<FilesystemNotification> getBlockingQueue();

    /**
     * Adds a notification to the blocking queue.
     * @param n - FilesystemNotification
     */
    void addNotification(FilesystemNotification n);

}
