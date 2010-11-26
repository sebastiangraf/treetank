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

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.Calendar;
import java.util.List;

import controlP5.ControlEvent;
import controlP5.ControlGroup;
import controlP5.ControlP5;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Toggle;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

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
    transient float mInnerNodeBrightnessEnd = 90;

    /** Inner node stroke brightness start value. */
    transient float mInnerNodeStrokeBrightnessStart = 20;

    /** Inner node stroke brightness end value. */
    transient float mInnerNodeStrokeBrightnessEnd = 90;

    /** Inner node arc scale. */
    transient float mInnerNodeArcScale = 0.7f;

    /** Stroke weight start. */
    transient float mStrokeWeightStart = 0.5f;

    /** Stroke weight end. */
    transient float mStrokeWeightEnd = 1.0f;

    /** Dot size. */
    transient float mDotSize = 3f;

    /** Dot brightness. */
    transient float mDotBrightness = 1f;

    /** Show arcs. */
    transient boolean mShowArcs = true;
    
    /** Determines how much text lenght should be weighted. */
    transient float mTextWeight = 0.2f;

    /** Leaf node arc scale. */
    private transient float mLeafArcScale = 1.0f;

    /** Background brightness. */
    private transient float mBackgroundBrightness = 100f;

    /** Color mapping mode. */
    private transient int mMappingMode = 1;

    /** Determines if arcs should be used (Default: true). */
    private transient boolean mUseArc = true;

    /** Determines if bezier lines for connections between parent/child should be used (Default: true). */
    private transient boolean mUseBezierLine = true;

    /** Determines if line connextions between parent/child should be drawn. */
    private transient boolean mShowLines = true;

    /** Determines if current state should be saved as a PDF-file. */
    private transient boolean mSavePDF;

    /** {@link ControlP5} reference. */
    private transient ControlP5 mControlP5;

    /** Determines if SunburstGUI interface should be shown. */
    private boolean mShowGUI;

    /** {@link ControlP5} sliders. */
    private transient Slider[] mSliders;

    /** {@link ControlP5} ranges. */
    private transient Range[] mRanges;

    /** {@link ControlP5} toggles. */
    private transient Toggle[] mToggles;

    /** Parent {@link PApplet}. */
    private final PApplet mParent;

    /** {@link SunburstController}. */
    private final SunburstController<? extends AbsModel, ? extends AbsView> mController;

    /** {@link PFont}. */
    private transient PFont mFont;

    /** {@link List} of {@link SunburstItem}s. */
    private transient List<SunburstItem> mItems;

    /**
     * Constructor.
     * 
     * @param paramParentApplet
     *            Parent processing applet.
     * @param paramController
     *            The controller.
     */
    private SunburstGUI(final PApplet paramParentApplet,
        final SunburstController<? extends AbsModel, ? extends AbsView> paramController) {
        mParent = paramParentApplet;
        mController = paramController;
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

    /** Initial setup of the GUI. */
    void setupGUI() {
        final int activeColor = mParent.color(0, 130, 164);
        mControlP5 = new ControlP5(mParent);
        mControlP5.setColorActive(activeColor);
        mControlP5.setColorBackground(mParent.color(170));
        mControlP5.setColorForeground(mParent.color(50));
        mControlP5.setColorLabel(mParent.color(50));
        mControlP5.setColorValue(mParent.color(255));

        mSliders = new Slider[10];
        mRanges = new Range[10];
        mToggles = new Toggle[10];

        final int left = 0;
        final int top = 5;
        final int len = 300;

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

        // name, minimum, maximum, default value (float), x, y, width, height
        mSliders[si] =
            mControlP5.addSlider("mInnerNodeArcScale", 0, 1, mInnerNodeArcScale, left, top + posY + 0, len,
                15);
        mSliders[si++].setLabel("innerNodeArcScale");
        mSliders[si] =
            mControlP5.addSlider("mLeafArcScale", 0, 1, mLeafArcScale, left, top + posY + 20, len, 15);
        mSliders[si++].setLabel("leafNodeArcScale");
        posY += 50;

        mRanges[ri++] =
            mControlP5.addRange("stroke weight range", 0, 10, mStrokeWeightStart, mStrokeWeightEnd, left, top
                + posY + 0, len, 15);
        posY += 30;

        mSliders[si] = mControlP5.addSlider("mDotSize", 0, 10, mDotSize, left, top + posY + 0, len, 15);
        mSliders[si++].setLabel("dotSize");
        mSliders[si] =
            mControlP5.addSlider("mDotBrightness", 0, 100, mDotBrightness, left, top + posY + 20, len, 15);
        mSliders[si++].setLabel("dotBrightness");
        posY += 50;

        mSliders[si] =
            mControlP5.addSlider("mBackgroundBrightness", 0, 100, mBackgroundBrightness, left,
                top + posY + 0, len, 15);
        mSliders[si++].setLabel("backgroundBrightness");
        posY += 30;
        
        mSliders[si] =
            mControlP5.addSlider("mTextWeight", 0, 10, mTextWeight, left, top + posY + 0, len, 15);
        mSliders[si++].setLabel("text weight");
        posY += 50;

        mToggles[ti] = mControlP5.addToggle("mShowArcs", mShowArcs, left + 0, top + posY, 15, 15);
        mToggles[ti++].setLabel("show Arcs");
        mToggles[ti] = mControlP5.addToggle("mShowLines", mShowLines, left + 0, top + posY + 20, 15, 15);
        mToggles[ti++].setLabel("show Lines");
        mToggles[ti] =
            mControlP5.addToggle("mUseBezierLine", mUseBezierLine, left + 0, top + posY + 40, 15, 15);
        mToggles[ti++].setLabel("Bezier / Line");
        mToggles[ti] = mControlP5.addToggle("mUseArc", mUseArc, left + 0, top + posY + 60, 15, 15);
        mToggles[ti++].setLabel("Arc / Rect");

        style(si, ri, ti);
    }

    /**
     * Style menu.
     * 
     * @param paramSi
     *            Last slider index.
     * @param paramRi
     *            Last ranges index.
     * @param paramTi
     *            Last toggles index.
     */
    private void style(final int paramSi, final int paramRi, final int paramTi) {
        final ControlGroup ctrl = mControlP5.addGroup("menu", 15, 25, 35);
        ctrl.setColorLabel(mParent.color(255));
        ctrl.close();

        for (int i = 0; i < paramSi; i++) {
            mSliders[i].setGroup(ctrl);
            mSliders[i].setId(i);
            mSliders[i].captionLabel().toUpperCase(true);
            mSliders[i].captionLabel().style().padding(4, 0, 1, 3);
            mSliders[i].captionLabel().style().marginTop = -4;
            mSliders[i].captionLabel().style().marginLeft = 0;
            mSliders[i].captionLabel().style().marginRight = -14;
            mSliders[i].captionLabel().setColorBackground(0x99ffffff);
            mSliders[i].plugTo(this);
        }

        for (int i = 0; i < paramRi; i++) {
            mRanges[i].setGroup(ctrl);
            mRanges[i].setId(i);
            mRanges[i].captionLabel().toUpperCase(true);
            mRanges[i].captionLabel().style().padding(4, 0, 1, 3);
            mRanges[i].captionLabel().style().marginTop = -4;
            mRanges[i].captionLabel().setColorBackground(0x99ffffff);
            mRanges[i].plugTo(this);
        }

        for (int i = 0; i < paramTi; i++) {
            mToggles[i].setGroup(ctrl);
            // mToggles[i].setColorValue(mParent.color(50));
            mToggles[i].captionLabel().style().padding(4, 3, 1, 3);
            mToggles[i].captionLabel().style().marginTop = -19;
            mToggles[i].captionLabel().style().marginLeft = 18;
            mToggles[i].captionLabel().style().marginRight = 5;
            mToggles[i].captionLabel().setColorBackground(0x99ffffff);
            mToggles[i].plugTo(this);
        }
        
        mParent.colorMode(PConstants.HSB, 360, 100, 100);
        mFont = mParent.createFont("Arial", 14);
        mParent.textFont(mFont, 12);
        mParent.textLeading(14);
        mParent.textAlign(PConstants.LEFT, PConstants.TOP);
        mParent.cursor(PConstants.CROSS);
    }

    /** Draw controlP5 GUI. */
    void drawGUI() {
        mControlP5.show();
        mControlP5.draw();
    }

    /**
     * Called on every change of the GUI.
     * 
     * @param paramControlEvent
     *            The {@link ControlEvent}, which is happening.
     */
    public void controlEvent(final ControlEvent paramControlEvent) {
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

        final List<SunburstItem> items = getItems();

        for (final SunburstItem item : items) {
            item.update(mMappingMode);
        }
    }

    /**
     * Implements the {@link PApplet} draw() method.
     */
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
        final float x = mParent.mouseX - mParent.width / 2;
        final float y = mParent.mouseY - mParent.height / 2;
        float angle = PApplet.atan2(y - 0, x - 0);
        final float radius = PApplet.dist(0, 0, x, y);

        if (angle < 0) {
            angle = PApplet.map(angle, -PConstants.PI, 0, PConstants.PI, PConstants.TWO_PI);
        } else {
            angle = PApplet.map(angle, 0, PConstants.PI, 0, PConstants.PI);
        }
        // Calc mouse depth with mouse radius ... transformation of calcEqualAreaRadius()
        final int depthMax = (Integer)mController.get("DepthMax");
        final int depth =
            PApplet.floor(PApplet.pow(radius, 2) * (depthMax + 1) / PApplet.pow(mParent.height * 0.5f, 2));

        // Draw the vizualization items.
        int index = 0;
        final List<SunburstItem> items = getItems();
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
            if (item.getDepth() == depth && angle > item.getAngleStart() && angle < item.getAngleEnd()) {
                hitTestIndex = index;
            }

            index++;
        }

        for (final SunburstItem item : items) {
            if (mShowLines) {
                if (mUseBezierLine) {
                    item.drawRelationBezier();
                } else {
                    item.drawRelationLine();
                }
            }
        }

        for (final SunburstItem item : items) {
            item.drawDot();
        }

        // Mouse rollover.
        if (!mShowGUI) {
            // Depth level focus.
            if (depth <= depthMax) {
                final float firstRad = calcEqualAreaRadius(depth, depthMax);
                final float secondRad = calcEqualAreaRadius(depth + 1, depthMax);
                mParent.stroke(0, 0, 0, 30);
                mParent.strokeWeight(5.5f);
                mParent.ellipse(0, 0, firstRad, firstRad);
                mParent.ellipse(0, 0, secondRad, secondRad);
            }
            // Rollover text.
            if (hitTestIndex != -1) {
                final String text = items.get(hitTestIndex).toString();
                final float texW = mParent.textWidth(text) * 1.2f;
                mParent.fill(0, 0, 0);
                final int offset = 5;
                mParent.rect(x + offset, y + offset, texW + 4, mParent.textAscent() * 3.6f);
                mParent.fill(0, 0, 100);
                mParent.text(text.toUpperCase(), x + offset + 2, y + offset + 2);
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
     * Get sunburst items.
     * 
     * @return {@link List} of {@link SunburstItem}s.
     */
    @SuppressWarnings("unchecked")
    List<SunburstItem> getItems() {
        if (mItems == null) {
            mItems = (List<SunburstItem>)mController.get("Items");
        }
        return mItems;
    }

    /**
     * Is getting called from processings keyRealeased-method and implements it.
     * 
     * @see controlP5.PAppletWindow#keyReleased().
     */
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
            final List<SunburstItem> items = getItems();
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
        final List<SunburstItem> items = getItems();
        for (final SunburstItem item : items) {
            item.update(mGUI.getMappingMode());
        }
        draw();
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
