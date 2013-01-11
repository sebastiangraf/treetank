package org.treetank.filelistener.api.file;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface IWatchCallback {

    public void processFileSystemChanges(Path dir, Path file, WatchEvent.Kind evtType);

}
