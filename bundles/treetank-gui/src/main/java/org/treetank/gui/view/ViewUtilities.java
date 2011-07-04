/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.treetank.gui.view;

import java.io.File;
import java.lang.Thread.State;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import controlP5.ControlP5;

import org.treetank.access.FileDatabase;
import org.treetank.access.SessionConfiguration;
import org.treetank.api.IReadTransaction;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.sunburst.AbsSunburstGUI;

import processing.core.PApplet;

/**
 * Provides some helper methods for views, which couldn't otherwise be encapsulated together.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class ViewUtilities {

    /** Path to save visualization as a PDF or PNG file. */
    public static final String SAVEPATH = "target" + File.separator;
    
    /** Private constructor. */
    private ViewUtilities() {
        // Just in case of a helper method tries to invoke the constructor.
        throw new AssertionError();
    }

    /**
     * Serialization compatible String representation of a {@link QName} reference.
     * 
     * @param paramQName
     *            The {@link QName} reference.
     * @return the string representation
     */
    public static String qNameToString(final QName paramQName) {
        assert paramQName != null;
        String retVal;

        if (paramQName.getPrefix().isEmpty()) {
            retVal = paramQName.getLocalPart();
        } else {
            retVal = paramQName.getPrefix() + ":" + paramQName.getLocalPart();
        }

        return retVal;
    }

    /**
     * Refresh resource with latest revision.
     * 
     * @param paramDb
     *            {@link ReadDB} instance
     * @return {@link ReadDB} instance
     * @throws AbsTTException
     *             if something went wrong while reading the newest revision
     */
    public static ReadDB refreshResource(final ReadDB paramDb) throws AbsTTException {
        assert paramDb != null;
        final File file = ((FileDatabase)paramDb.getDatabase()).mFile;
        if (paramDb != null) {
            paramDb.close();
        }
        return new ReadDB(file);
    }

    /**
     * Format a timestamp.
     * 
     * @return Formatted timestamp.
     */
    public static String timestamp() {
        return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", Calendar.getInstance());
    }

    /**
     * Draw controlP5 GUI.
     * 
     * @param paramControlP5
     *            {@link ControlP5} instance
     */
    public static void drawGUI(final ControlP5 paramControlP5) {
        paramControlP5.show();
        paramControlP5.draw();
    }

    /** Debugging threads. */
    public static void stackTraces() {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        Iterator<Thread> itr = map.keySet().iterator();
        while (itr.hasNext()) {
            Thread t = itr.next();
            StackTraceElement[] elem = map.get(t);
            System.out.print("\"" + t.getName() + "\"");
            System.out.print(" prio=" + t.getPriority());
            System.out.print(" tid=" + t.getId());
            State s = t.getState();
            String state = null;
            switch (s) {
            case NEW:
                state = "NEW";
                break;
            case BLOCKED:
                state = "BLOCKED";
                break;
            case RUNNABLE:
                state = "RUNNABLE";
                break;
            case TERMINATED:
                state = "TERMINATED";
                break;
            case TIMED_WAITING:
                state = "TIME WAITING";
                break;
            case WAITING:
                state = "WAITING";
                break;
            }
            System.out.println(" " + state + "\n");
            for (int i = 0; i < elem.length; i++) {
                System.out.println("  at ");
                System.out.print(elem[i].toString());
                System.out.println("\n");
            }
            System.out.println("----------------------------\n");
        }

    }
    
    public static void legend(final AbsSunburstGUI paramGUI, final PApplet paramApplet) {
        paramApplet.translate(0, 0);
        paramApplet.strokeWeight(0);

        if (paramGUI.isShowArcs()) {
            paramApplet.fill(paramGUI.getHueStart(), paramGUI.getSaturationStart(), paramGUI.getBrightnessStart());
            paramApplet.rect(20f, paramApplet.height - 70f, 50, 17);
            color(paramGUI, paramApplet);
            paramApplet.text("-", 78, paramApplet.height - 70f);
            paramApplet.fill(paramGUI.getHueEnd(), paramGUI.getSaturationEnd(), paramGUI.getBrightnessEnd());
            paramApplet.rect(90f, paramApplet.height - 70f, 50, 17);
            color(paramGUI, paramApplet);
            paramApplet.text("text length", 150f, paramApplet.height - 70f);
            paramApplet.fill(0, 0, paramGUI.getInnerNodeBrightnessStart());
            paramApplet.rect(20f, paramApplet.height - 50f, 50, 17);
            color(paramGUI, paramApplet);
            paramApplet.text("-", 78, paramApplet.height - 50f);
            paramApplet.fill(0, 0, paramGUI.getInnerNodeBrightnessEnd());
            paramApplet.rect(90f, paramApplet.height - 50f, 50, 17);
            color(paramGUI, paramApplet);
            paramApplet.text("descendants per node", 150f, paramApplet.height - 50f);
        }

        if (paramGUI.isSavePDF()) {
            paramApplet.translate(paramApplet.width / 2, paramApplet.height / 2);
            paramGUI.setSavePDF(false);
            paramApplet.endRecord();
            PApplet.println("saving to pdf – done");
        }
    }
    

    /**
     * Fill color which changes to white or black depending on the background brightness.
     */
    public static void color(final AbsSunburstGUI paramGUI, final PApplet paramApplet) {
        assert paramGUI != null;
        assert paramApplet != null;
        if (paramGUI.getBackgroundBrightness() > 40f) {
            paramApplet.fill(0, 0, 0);
        } else {
            paramApplet.fill(360, 0, 100);
        }
    }

    /**
     * @param smallMultiplesGUI
     * @param paramApplet
     */
    public static void compareLegend(final AbsSunburstGUI paramGUI, final PApplet paramApplet) {
        assert paramGUI != null;
        assert paramApplet != null;
        if (paramGUI.getDotSize() > 0) {
            paramApplet.fill(200, 100, paramGUI.getDotBrightness());
            paramApplet.ellipse(paramApplet.width - 160f, paramApplet.height - 90f, 8, 8);
            color(paramGUI, paramApplet);
            paramApplet.text("node inserted", paramApplet.width - 140f, paramApplet.height - 100f);
            paramApplet.fill(360, 100, paramGUI.getDotBrightness());
            paramApplet.ellipse(paramApplet.width - 160f, paramApplet.height - 67f, 8, 8);
            color(paramGUI, paramApplet);
            paramApplet.text("node deleted", paramApplet.width - 140f, paramApplet.height - 77f);
            paramApplet.fill(120, 100, paramGUI.getDotBrightness());
            paramApplet.ellipse(paramApplet.width - 160f, paramApplet.height - 44f, 8, 8);
            color(paramGUI, paramApplet);
            paramApplet.text("node updated", paramApplet.width - 140f, paramApplet.height - 54f);
        }
        
    }
}
