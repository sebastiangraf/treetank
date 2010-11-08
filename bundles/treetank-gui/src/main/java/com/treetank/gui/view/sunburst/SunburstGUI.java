/**
 * Copyright (c) 2010, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package com.treetank.gui.view.sunburst;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Calendar;
import java.util.List;

import javax.swing.SwingUtilities;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import controlP5.ControlEvent;
import controlP5.ControlGroup;
import controlP5.ControlP5;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Toggle;

/**
 * <h1>SunburstGUI</h1>
 * 
 * <p>
 * Internal Sunburst view GUI.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
final class SunburstGUI extends AbsView {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4747210906900567484L;

    /** The GUI of the Sunburst view. */
    private static SunburstGUI mGUI;

    /** Hue start value. */
    transient float mHueStart = 273;

    /** Hue end value. */
    transient float mHueEnd = 323;

    /** Saturation start value. */
    transient float mSaturationStart = 73;

    /** Saturation end value. */
    transient float mSaturationEnd = 100;

    /** Brightness start value. */
    transient float mBrightnessStart = 51;

    /** Brightness end value. */
    transient float mBrightnessEnd = 77;

    /** Inner node brightness start value. */
    transient float mInnerNodeBrightnessStart = 20;

    /** Inner node brightness end value. */
    private transient float mInnerNodeBrightnessEnd = 90;

    /** Inner node stroke brightness start value. */
    transient float mInnerNodeStrokeBrightnessStart = 20;

    /** Inner node stroke brightness end value. */
    transient float mInnerNodeStrokeBrightnessEnd = 90;

    private transient float mLeafArcScale = 1.0f;
    private transient float mInnerNodeArcScale = 0.2f;
    transient float mStrokeWeightStart = 0.5f;
    transient float mStrokeWeightEnd = 1.0f;
    transient float mDotSize = 3f;
    transient float mDotBrightness = 1f;
    private transient float mBackgroundBrightness = 100f;

    private transient int mMappingMode = 1;
    private transient boolean mUseArc = true;
    private transient boolean mUseBezierLine = true;
    private transient boolean mShowArcs = true;
    private transient boolean mShowLines = true;
    private transient boolean mSavePDF;

    private final ControlP5 mControlP5;
    private boolean mShowGUI = false;
    private final Slider[] mSliders;
    private final Range[] mRanges;
    private final Toggle[] mToggles;
    private final PApplet mParent;

    /** {@link SunburstController}. */
    private final SunburstController<? extends AbsModel, ? extends AbsView> mController;

    /** {@link PFont}. */
    private transient PFont mFont;

    /**
     * Constructor.
     * 
     * @param paramParentApplet
     *            Parent processing applet.
     * @param paramController
     *            The controller.
     */
    @SuppressWarnings("unchecked")
    private SunburstGUI(final PApplet paramParentApplet,
        final SunburstController<? extends AbsModel, ? extends AbsView> paramController) {
        mParent = paramParentApplet;
        mController = paramController;
        final int activeColor = mParent.color(0, 130, 164);
        mControlP5 = new ControlP5(mParent);
        // controlP5.setAutoDraw(false);
        mControlP5.setColorActive(activeColor);
        mControlP5.setColorBackground(mParent.color(170));
        mControlP5.setColorForeground(mParent.color(50));
        mControlP5.setColorLabel(mParent.color(50));
        mControlP5.setColorValue(mParent.color(255));

        final ControlGroup ctrl = mControlP5.addGroup("menu", 15, 25, 35);
        ctrl.setColorLabel(mParent.color(255));
        ctrl.close();

        mSliders = new Slider[10];
        mRanges = new Range[10];
        mToggles = new Toggle[10];

        int left = 0;
        int top = 5;
        int len = 300;

        int si = 0;
        int ri = 0;
        int ti = 0;
        int posY = 0;

        mRanges[ri++] =
            mControlP5.addRange("leaf node hue range", 0, 360, mHueStart, mHueEnd, left, top + posY + 0, len,
                15);
        mRanges[ri++] =
            mControlP5.addRange("leaf node saturation range", 0, 100, mSaturationStart, mSaturationEnd, left,
                top + posY + 20, len, 15);
        mRanges[ri++] =
            mControlP5.addRange("leaf node brightness range", 0, 100, mBrightnessStart, mBrightnessEnd, left,
                top + posY + 40, len, 15);
        posY += 70;

        mRanges[ri++] =
            mControlP5.addRange("inner node brightness range", 0, 100, mInnerNodeBrightnessStart,
                mInnerNodeBrightnessEnd, left, top + posY + 0, len, 15);
        mRanges[ri++] =
            mControlP5.addRange("inner node stroke brightness range", 0, 100,
                mInnerNodeStrokeBrightnessStart, mInnerNodeStrokeBrightnessEnd, left, top + posY + 20, len,
                15);
        posY += 50;

        mSliders[si++] = mControlP5.addSlider("inndeNodeArcScale", 0, 1, left, top + posY + 0, len, 15);
        mSliders[si++] = mControlP5.addSlider("leafNodeArcScale", 0, 1, left, top + posY + 20, len, 15);
        posY += 50;

        mRanges[ri++] =
            mControlP5.addRange("stroke weight range", 0, 10, mStrokeWeightStart, mStrokeWeightEnd, left, top
                + posY + 0, len, 15);
        posY += 30;

        mSliders[si++] = mControlP5.addSlider("dotSize", 0, 10, left, top + posY + 0, len, 15);
        mSliders[si++] = mControlP5.addSlider("dotBrightness", 0, 100, left, top + posY + 20, len, 15);
        posY += 50;

        mSliders[si++] = mControlP5.addSlider("backgroundBrightness", 0, 100, left, top + posY + 0, len, 15);
        posY += 30;

        mToggles[ti] = mControlP5.addToggle("showArcs", mShowArcs, left + 0, top + posY, 15, 15);
        mToggles[ti++].setLabel("show Arcs");
        mToggles[ti] = mControlP5.addToggle("showLines", mShowLines, left + 0, top + posY + 20, 15, 15);
        mToggles[ti++].setLabel("show Lines");
        mToggles[ti] =
            mControlP5.addToggle("useBezierLine", mUseBezierLine, left + 0, top + posY + 40, 15, 15);
        mToggles[ti++].setLabel("Bezier / Line");
        mToggles[ti] = mControlP5.addToggle("useArc", mUseArc, left + 0, top + posY + 60, 15, 15);
        mToggles[ti++].setLabel("Arc / Rect");

        for (int i = 0; i < si; i++) {
            mSliders[i].setGroup(ctrl);
            mSliders[i].setId(i);
            mSliders[i].captionLabel().toUpperCase(true);
            mSliders[i].captionLabel().style().padding(4, 0, 1, 3);
            mSliders[i].captionLabel().style().marginTop = -4;
            mSliders[i].captionLabel().style().marginLeft = 0;
            mSliders[i].captionLabel().style().marginRight = -14;
            mSliders[i].captionLabel().setColorBackground(0x99ffffff);
        }

        for (int i = 0; i < ri; i++) {
            mRanges[i].setGroup(ctrl);
            mRanges[i].setId(i);
            mRanges[i].captionLabel().toUpperCase(true);
            mRanges[i].captionLabel().style().padding(4, 0, 1, 3);
            mRanges[i].captionLabel().style().marginTop = -4;
            mRanges[i].captionLabel().setColorBackground(0x99ffffff);
        }

        for (int i = 0; i < ti; i++) {
            mToggles[i].setGroup(ctrl);
            mToggles[i].setColorLabel(mParent.color(50));
            mToggles[i].captionLabel().style().padding(4, 3, 1, 3);
            mToggles[i].captionLabel().style().marginTop = -19;
            mToggles[i].captionLabel().style().marginLeft = 18;
            mToggles[i].captionLabel().style().marginRight = 5;
            mToggles[i].captionLabel().setColorBackground(0x99ffffff);
        }
    }

    /**
     * Factory method (Singleton).
     * 
     * @param paramParentApplet
     *            Parent processing applet.
     * @param paramController
     *            The controller.
     * @return a GUI singleton.
     */
    static SunburstGUI createGUI(final PApplet paramParentApplet,
        final SunburstController<? extends AbsModel, ? extends AbsView> paramController) {
        if (mGUI == null) {
            mGUI = new SunburstGUI(paramParentApplet, paramController);
        }
        return mGUI;
    }

    void drawGUI() {
        mControlP5.show();
        mControlP5.draw();
    }

    // called on every change of the gui
    @SuppressWarnings("unchecked")
    void controlEvent(final ControlEvent paramControlEvent) {
        // println("got a control event from controller with id "+theControlEvent.controller().id());
        if (paramControlEvent.controller().name().equals("leaf node hue range")) {
            final float[] f = paramControlEvent.controller().arrayValue();
            mHueStart = f[0];
            mHueEnd = f[1];
        }
        if (paramControlEvent.controller().name().equals("leaf node saturation range")) {
            final float[] f = paramControlEvent.controller().arrayValue();
            mSaturationStart = f[0];
            mSaturationEnd = f[1];
        }
        if (paramControlEvent.controller().name().equals("leaf node brightness range")) {
            final float[] f = paramControlEvent.controller().arrayValue();
            mBrightnessStart = f[0];
            mBrightnessEnd = f[1];
        }
        if (paramControlEvent.controller().name().equals("inner node brightness range")) {
            final float[] f = paramControlEvent.controller().arrayValue();
            mInnerNodeBrightnessStart = f[0];
            mInnerNodeBrightnessEnd = f[1];
        }
        if (paramControlEvent.controller().name().equals("inner node stroke brightness range")) {
            final float[] f = paramControlEvent.controller().arrayValue();
            mInnerNodeStrokeBrightnessStart = f[0];
            mInnerNodeStrokeBrightnessEnd = f[1];
        }
        if (paramControlEvent.controller().name().equals("stroke weight range")) {
            final float[] f = paramControlEvent.controller().arrayValue();
            mStrokeWeightStart = f[0];
            mStrokeWeightEnd = f[1];
        }

        final List<SunburstItem> items = (List<SunburstItem>)mController.get("Items");

        for (final SunburstItem item : items) {
            item.update(mMappingMode);
        }
    }

    /** Initial setup of the GUI. */
    void setupGUI() {
        mParent.colorMode(PConstants.HSB, 360, 100, 100);
        mFont = mParent.createFont("Arial", 14);
        mParent.textFont(mFont, 12);
        mParent.textLeading(14);
        mParent.textAlign(PConstants.LEFT, PConstants.TOP);
        mParent.cursor(PConstants.CROSS);
    }

    @SuppressWarnings("unchecked")
    void draw() {
        if (mSavePDF) {
            PApplet.println("\n" + "saving to pdf – starting");
            mParent.beginRecord(PConstants.PDF, timestamp() + ".pdf");
        }

        mParent.pushMatrix();
        mParent.colorMode(PConstants.HSB, 360, 100, 100, 100);
        mParent.background(0, 0, mBackgroundBrightness);
        mParent.noFill();
        mParent.ellipseMode(PConstants.RADIUS);
        mParent.strokeCap(PConstants.SQUARE);
        mParent.textFont(mFont, 12f);
        mParent.textLeading(14);
        mParent.textAlign(PConstants.LEFT, PConstants.TOP);
        mParent.smooth();

        mParent.translate(mParent.width / 2, mParent.height / 2);

        // Mouse rollover, arc hittest vars.
        int hitTestIndex = -1;
        final float mX = mParent.mouseX - mParent.width / 2;
        final float mY = mParent.mouseY - mParent.height / 2;
        float mAngle = PApplet.atan2(mY - 0, mX - 0);
        final float mRadius = PApplet.dist(0, 0, mX, mY);

        if (mAngle < 0) {
            mAngle = PApplet.map(mAngle, -PConstants.PI, 0, PConstants.PI, PConstants.TWO_PI);
        } else {
            mAngle = PApplet.map(mAngle, 0, PConstants.PI, 0, PConstants.PI);
        }
        // Calc mouse depth with mouse radius ... transformation of calcEqualAreaRadius()
        final int mDepth =
            PApplet.floor(PApplet.pow(mRadius, 2) * (((Integer)mController.get("DepthMax")) + 1)
                / PApplet.pow(mParent.height * 0.5f, 2));

        // Draw the vizualization items.
        int index = 0;
        final List<SunburstItem> items = (List<SunburstItem>)mController.get("Items");
        for (final SunburstItem item : items) {
            // Draw arcs or rects.
            if (mShowArcs) {
                if (mUseArc) {
                    item.drawArc(mInnerNodeArcScale, mLeafArcScale);
                } else {
                    item.drawRect(mInnerNodeArcScale, mLeafArcScale);
                }
            }

            // Hittest, which arc is the closest to the mouse.
            if (item.getDepth() == mDepth && mAngle > item.getAngleStart() && mAngle < item.getAngleEnd()) {
                hitTestIndex = index;
            }

            item.drawDot();

            if (mShowLines) {
                if (mUseBezierLine) {
                    item.drawRelationBezier();
                } else {
                    item.drawRelationLine();
                }
            }

            index++;
        }

        final int depthMax = (Integer)mController.get("DepthMax");

        // Mouse rollover.
        if (!mShowGUI) {
            // Depth level focus.
            if (mDepth <= depthMax) {
                final float firstRad = calcEqualAreaRadius(mDepth, depthMax);
                final float secondRad = calcEqualAreaRadius(mDepth + 1, depthMax);
                mParent.stroke(0, 0, 0, 30);
                mParent.strokeWeight(5.5f);
                mParent.ellipse(0, 0, firstRad, firstRad);
                mParent.ellipse(0, 0, secondRad, secondRad);
            }
            // Rollover text.
            if (hitTestIndex != -1) {
                // TODO
                final String tex = Integer.toString(items.get(hitTestIndex).getDepth());
                final float texW = mParent.textWidth(tex) * 1.2f;
                mParent.fill(0, 0, 0);
                final int offset = 5;
                mParent.rect(mX + offset, mY + offset, texW + 4, mParent.textAscent() * 3.6f);
                mParent.fill(0, 0, 100);
                mParent.text(tex.toUpperCase(), mX + offset + 2, mY + offset + 2);
            }
        }

        mParent.popMatrix();

        if (mSavePDF) {
            mSavePDF = false;
            mParent.endRecord();
            PApplet.println("saving to pdf – done");
        }

        drawGUI();
    }

    /**
     * Is getting called from processings keyRealeased-method and implements it.
     * 
     * @see controlP5.PAppletWindow#keyReleased().
     */
    @SuppressWarnings("unchecked")
    void keyReleased() {
        switch (mParent.key) {
        case 's':
        case 'S':
            mParent.saveFrame(timestamp() + "_##.png");
            break;
        case 'p':
        case 'P':
            mSavePDF = true;
            break;
        case 'o':
        case 'O':
            break;
        case '1':
            mMappingMode = 1;
            break;
        case '2':
            mMappingMode = 2;
            break;
        case '3':
            mMappingMode = 3;
            break;
        default:
            break;
        }

        if (mParent.key == '1' || mParent.key == '2' || mParent.key == '3') {
            final List<SunburstItem> items = (List<SunburstItem>)mController.get("Items");
            for (final SunburstItem item : items) {
                item.update(mMappingMode);
            }
        } else if (mParent.key == 'm' || mParent.key == 'M') {
            mShowGUI = mControlP5.group("menu").isOpen();
            mShowGUI = !mShowGUI;
        }

        if (mShowGUI) {
            mControlP5.group("menu").open();
        } else {
            mControlP5.group("menu").close();
        }
    }

    /**
     * Implements processing mouseEntered.
     * 
     * @see controlP5.PAppletWindow#mouseEntered.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     */
    void mouseEntered(final MouseEvent paramEvent) {
        mParent.loop();
    }

    /**
     * Implements processing mouseExited.
     * 
     * @see controlP5.PAppletWindow#mouseExited.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     */
    void mouseExited(final MouseEvent paramEvent) {
        mParent.noLoop();
    }

    @Override
    protected void modelPropertyChange(final PropertyChangeEvent paramEvt) {
        // Redraw.
        mParent.draw();
    }

    /**
     * Format a timestamp.
     * 
     * @return Formatted timestamp.
     */
    private String timestamp() {
        return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", Calendar.getInstance());
    }

    /**
     * Calculate area so that radiuses have equal areas in each depth.
     * 
     * @param paramDepth
     *            The actual depth.
     * @param paramDepthMax
     *            The maximum depth.
     * @return calculated area.
     */
    float calcEqualAreaRadius(final int paramDepth, final int paramDepthMax) {
        return PApplet.sqrt(paramDepth * PApplet.pow(mParent.height / 2, 2) / (paramDepthMax + 1));
    }

    /**
     * Calculate area radius in a linear way.
     * 
     * @param paramDepth
     *            The actual depth.
     * @param paramDepthMax
     *            The maximum depth.
     * @return calculated area.
     */
    float calcAreaRadius(final int paramDepth, final int paramDepthMax) {
        return PApplet.map(paramDepth, 0, paramDepthMax + 1, 0, mParent.height / 2);
    }

    /**
     * Get mapping mode.
     * 
     * @return mappingMode
     */
    int getMappingMode() {
        return mMappingMode;
    }
}
