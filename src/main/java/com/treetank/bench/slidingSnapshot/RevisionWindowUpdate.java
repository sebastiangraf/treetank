package com.treetank.bench.slidingSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.constants.ESettable;
import com.treetank.constants.EStorage;
import com.treetank.exception.TreetankUsageException;
import com.treetank.service.xml.XMLShredder;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;

public class RevisionWindowUpdate {

    private final static int NUMBEROFREVISIONS = 10000;

    private final static int mProb = 20;

    private static ISession windowSession1;
    private static IWriteTransaction windowWtx1;

    private static ISession windowSession2;
    private static IWriteTransaction windowWtx2;

    private static boolean FIRSTRESULT = true;

    private static PrintStream window1;
    private static PrintStream window2;
    private static PrintStream revisions;

    public static void begin() {
        try {
            final Properties props1 = new Properties();
            props1.put(ESettable.SNAPSHOT_WINDOW.getName(), 1);

            final Properties props2 = new Properties();
            props2.put(ESettable.SNAPSHOT_WINDOW.getName(), 4);

            Session.removeSession(CommonStuff.PATH1);
            Session.removeSession(CommonStuff.PATH2);
            XMLShredder.shred(CommonStuff.XMLPath.getAbsolutePath(),
                    new SessionConfiguration(CommonStuff.PATH1));
            XMLShredder.shred(CommonStuff.XMLPath.getAbsolutePath(),
                    new SessionConfiguration(CommonStuff.PATH2));

            windowSession1 = Session.beginSession(new SessionConfiguration(
                    CommonStuff.PATH1, props1));
            windowSession2 = Session.beginSession(new SessionConfiguration(
                    CommonStuff.PATH2, props2));

            windowWtx1 = windowSession1.beginWriteTransaction();
            windowWtx2 = windowSession2.beginWriteTransaction();

            window1 = new PrintStream(new FileOutputStream(new File(
                    CommonStuff.RESULTFOLDER, "window1")));
            window2 = new PrintStream(new FileOutputStream(new File(
                    CommonStuff.RESULTFOLDER, "window2")));
            revisions = new PrintStream(new FileOutputStream(new File(
                    CommonStuff.RESULTFOLDER, "revisions")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeData() {
        try {
            if (!FIRSTRESULT) {
                window1.append(",");
                window2.append(",");
                revisions.append(",");
            }
            FIRSTRESULT = false;
            window1.append(Long.toString(CommonStuff.computeLength(new File(
                    CommonStuff.PATH1, "tt"))));
            window2.append(Long.toString(CommonStuff.computeLength(new File(
                    CommonStuff.PATH2, "tt"))));
            revisions.append(Long.toString(windowWtx2.getRevisionNumber()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insert() {
        try {
            while (true) {
                long nextKey = 0;
                do {
                    nextKey = CommonStuff.ran.nextLong();
                    if (nextKey < 0) {
                        nextKey = nextKey * -1;
                    }
                    nextKey = nextKey % windowWtx1.getNodeCount();
                } while (nextKey == 0);

                windowWtx1.moveTo(nextKey);
                final String data = CommonStuff.getString();
                if (windowWtx2.getNode().isElement()) {

                    windowWtx1.setName(data);
                    windowWtx2.setName(data);
                } else {
                    windowWtx1.setValue(data);
                    windowWtx2.setValue(data);
                }
                if (CommonStuff.ran.nextInt(100) < mProb) {
                    windowWtx1.commit();
                    windowWtx2.commit();
                    break;
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public static void end() {
        try {
            windowWtx1.close();
            windowWtx2.close();
            windowSession1.close();
            windowSession2.close();
            Session.removeSession(CommonStuff.PATH1);
            Session.removeSession(CommonStuff.PATH2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) throws TreetankUsageException {
        EStorage.recursiveDelete(CommonStuff.RESULTFOLDER);
        CommonStuff.RESULTFOLDER.mkdirs();

        begin();
        for (int i = 0; i < NUMBEROFREVISIONS; i++) {
            insert();
            // if (i % 100 == 0) {
            storeData();
            // }
        }
        end();

    }

}
