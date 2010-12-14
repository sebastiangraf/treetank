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
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.SunburstView.Embedded;
import com.treetank.utils.LogWrapper;

import org.slf4j.LoggerFactory;

import controlP5.ControlEvent;
import controlP5.ControlGroup;
import controlP5.ControlP5;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Textfield;
import controlP5.Toggle;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

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
final class SunburstGUI extends AbsGUI implements PropertyChangeListener {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4747210906900567484L;

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstGUI.class));

    /** Path to save visualization as a PDF or PNG file. */
    private static final String SAVEPATH = "target" + File.separator + timestamp();

    /** Amount of effect in the fisheye transformation. */
    private static final float EFFECT_AMOUNT = 0.9f;

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
    transient float mBrightnessStart = 30;

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

    /** {@link List} of {@link SunburstItem}s. */
    transient List<SunburstItem> mItems;

    /** Maximum depth in the tree. */
    transient int mDepthMax;

    /** {@link Sempahore} to block re-initializing sunburst item list until draw() is finished(). */
    transient Semaphore mLock = new Semaphore(1);

    /** Leaf node arc scale. */
    private transient float mLeafArcScale = 1.0f;

    /** Background brightness. */
    private transient float mBackgroundBrightness = 100f;

    /** Color mapping mode. */
    private transient int mMappingMode = 1;

    /** Determines if fisheye should be used. */
    private transient boolean mFisheye;

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
    private transient boolean mShowGUI;

    /** {@link ControlP5} sliders. */
    private transient List<Slider> mSliders;

    /** {@link ControlP5} ranges. */
    private transient List<Range> mRanges;

    /** {@link ControlP5} toggles. */
    private transient List<Toggle> mToggles;

    // /** {@link ControlP5} listboxes. */
    // private transient List<ListBox> mBoxes;

    /** {@link ControlP5} text field. */
    private transient Textfield mXPathField;

    /** Determines the update state. */
    private enum State {
        /** Drawing "event". */
        DRAW,

        /** Click event. */
        CLICK
    };

    /**
     * Temporary {@link List} of {@link List}s of {@link SunburstItem}s.
     */
    private final List<List<SunburstItem>> mLastItems = new ArrayList<List<SunburstItem>>();

    /** Parent {@link PApplet}. */
    private final Embedded mParent;

    /** {@link SunburstModel}. */
    private final SunburstModel mModel;

    /**
     * Private constructor.
     * 
     * @param paramParentApplet
     *            parent processing applet
     * @param paramModel
     *            the model
     * @param paramReadDB
     *            read database
     */
    private SunburstGUI(final PApplet paramParentApplet, final SunburstModel paramModel,
        final ReadDB paramReadDB) {
        mParent = (Embedded)paramParentApplet;
        mModel = paramModel;
    }

    /**
     * Factory method (Singleton). Note that it's always called from the animation thread, thus it doesn't
     * need to be synchronized.
     * 
     * @param paramParentApplet
     *            parent processing applet
     * @param paramModel
     *            the model
     * @param paramReadDB
     *            read database
     * @return a GUI singleton
     */
    static SunburstGUI getInstance(final PApplet paramParentApplet, final SunburstModel paramModel,
        final ReadDB paramReadDB) {
        if (mGUI == null) {
            mGUI = new SunburstGUI(paramParentApplet, paramModel, paramReadDB);
            mGUI.setupGUI();
        }
        return mGUI;
    }

    /** Initial setup of the GUI. */
    private void setupGUI() {
        mParent.noLoop();
        try {
            mLock.acquire();
            final int activeColor = mParent.color(0, 130, 164);
            mControlP5 = new ControlP5(mParent);
            mControlP5.setColorActive(activeColor);
            mControlP5.setColorBackground(mParent.color(170));
            mControlP5.setColorForeground(mParent.color(50));
            mControlP5.setColorLabel(mParent.color(50));
            mControlP5.setColorValue(mParent.color(255));

            mSliders = new LinkedList<Slider>();
            mRanges = new LinkedList<Range>();
            mToggles = new LinkedList<Toggle>();
            // mBoxes = new LinkedList<ListBox>();

            final int left = 0;
            final int top = 5;
            final int len = 300;

            int si = 0;
            int ri = 0;
            int ti = 0;
            int posY = 0;

            assert mControlP5 != null;

            mRanges.add(ri++, mControlP5.addRange("leaf node hue range", 0, 360, mHueStart, mHueEnd, left,
                top + posY + 0, len, 15));
            mRanges.add(ri++, mControlP5.addRange("leaf node saturation range", 0, 100, mSaturationStart,
                mSaturationEnd, left, top + posY + 20, len, 15));
            mRanges.add(ri++, mControlP5.addRange("leaf node brightness range", 0, 100, mBrightnessStart,
                mBrightnessEnd, left, top + posY + 40, len, 15));
            posY += 70;

            mRanges.add(ri++, mControlP5.addRange("inner node brightness range", 0, 100,
                mInnerNodeBrightnessStart, mInnerNodeBrightnessEnd, left, top + posY + 0, len, 15));
            mRanges.add(ri++, mControlP5.addRange("inner node stroke brightness range", 0, 100,
                mInnerNodeStrokeBrightnessStart, mInnerNodeStrokeBrightnessEnd, left, top + posY + 20, len,
                15));
            posY += 50;

            // name, minimum, maximum, default value (float), x, y, width, height
            mSliders.add(si, mControlP5.addSlider("mInnerNodeArcScale", 0, 1, mInnerNodeArcScale, left, top
                + posY + 0, len, 15));
            mSliders.get(si++).setLabel("innerNodeArcScale");
            mSliders.add(si,
                mControlP5.addSlider("mLeafArcScale", 0, 1, mLeafArcScale, left, top + posY + 20, len, 15));
            mSliders.get(si++).setLabel("leafNodeArcScale");
            posY += 50;

            mRanges.add(ri++, mControlP5.addRange("stroke weight range", 0, 10, mStrokeWeightStart,
                mStrokeWeightEnd, left, top + posY + 0, len, 15));
            posY += 30;

            mSliders
                .add(si, mControlP5.addSlider("mDotSize", 0, 10, mDotSize, left, top + posY + 0, len, 15));
            mSliders.get(si++).setLabel("dotSize");
            mSliders.add(si, mControlP5.addSlider("mDotBrightness", 0, 100, mDotBrightness, left, top + posY
                + 20, len, 15));
            mSliders.get(si++).setLabel("dotBrightness");
            posY += 50;

            mSliders.add(
                si,
                mControlP5.addSlider("mBackgroundBrightness", 0, 100, mBackgroundBrightness, left, top + posY
                    + 0, len, 15));
            mSliders.get(si++).setLabel("backgroundBrightness");
            posY += 30;

            mSliders.add(si,
                mControlP5.addSlider("mTextWeight", 0, 10, mTextWeight, left, top + posY + 0, len, 15));
            mSliders.get(si++).setLabel("text weight");
            posY += 50;

            mToggles.add(ti, mControlP5.addToggle("mShowArcs", mShowArcs, left + 0, top + posY, 15, 15));
            mToggles.get(ti++).setLabel("show Arcs");
            mToggles.add(ti,
                mControlP5.addToggle("mShowLines", mShowLines, left + 0, top + posY + 20, 15, 15));
            mToggles.get(ti++).setLabel("show Lines");
            mToggles.add(ti,
                mControlP5.addToggle("mUseBezierLine", mUseBezierLine, left + 0, top + posY + 40, 15, 15));
            mToggles.get(ti++).setLabel("Bezier / Line");
            mToggles.add(ti, mControlP5.addToggle("mUseArc", mUseArc, left + 0, top + posY + 60, 15, 15));
            mToggles.get(ti++).setLabel("Arc / Rect");
            mToggles.add(ti, mControlP5.addToggle("mFisheye", mFisheye, left + 0, top + posY + 80, 15, 15));
            mToggles.get(ti++).setLabel("Fisheye lense");

            mXPathField = mControlP5.addTextfield("xpath", left + 800, top + 20, 200, 20);
            mXPathField.setLabel("XPath expression");
            mXPathField.setFocus(true);
            mXPathField.setAutoClear(false);
            mXPathField.plugTo(this);

            style(si, ri, ti);
        } catch (final Exception e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
            mParent.loop();
        }
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

        int i = 0;
        for (final Slider slider : mSliders) {
            slider.setGroup(ctrl);
            slider.setId(i);
            slider.captionLabel().toUpperCase(true);
            slider.captionLabel().style().padding(4, 0, 1, 3);
            slider.captionLabel().style().marginTop = -4;
            slider.captionLabel().style().marginLeft = 0;
            slider.captionLabel().style().marginRight = -14;
            slider.captionLabel().setColorBackground(0x99ffffff);
            slider.plugTo(this);
            i++;
        }

        i = 0;
        for (final Range range : mRanges) {
            range.setGroup(ctrl);
            range.setId(i);
            range.captionLabel().toUpperCase(true);
            range.captionLabel().style().padding(4, 0, 1, 3);
            range.captionLabel().style().marginTop = -4;
            range.captionLabel().setColorBackground(0x99ffffff);
            range.plugTo(this);
            i++;
        }

        i = 0;
        for (final Toggle toggle : mToggles) {
            toggle.setGroup(ctrl);
            toggle.setId(i);
            toggle.captionLabel().style().padding(4, 3, 1, 3);
            toggle.captionLabel().style().marginTop = -19;
            toggle.captionLabel().style().marginLeft = 18;
            toggle.captionLabel().style().marginRight = 5;
            toggle.captionLabel().setColorBackground(0x99ffffff);
            toggle.plugTo(this);
            i++;
        }

        mParent.colorMode(PConstants.HSB, 360, 100, 100);
        mParent.textLeading(14);
        mParent.textAlign(PConstants.LEFT, PConstants.TOP);
        mParent.cursor(PConstants.CROSS);
    }

    /** Draw controlP5 GUI. */
    private void drawGUI() {
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
            firePropertyChange("strokeWeightStart", mStrokeWeightStart, f[0]);
            mStrokeWeightStart = f[0];
            mStrokeWeightEnd = f[1];
        }

        for (final SunburstItem item : mItems) {
            item.update(mMappingMode);
        }
    }

    /**
     * XPath expression.
     * 
     * @param paramXPath
     *            The XPath expression.
     */
    public void xpath(final String paramXPath) {
        mModel.evaluateXPath(paramXPath);
    }

    /**
     * Implements the {@link PApplet} draw() method.
     */
    void draw() {
        if (mItems != null && mControlP5 != null) {
            try {
                mLock.tryAcquire();
                mParent.pushMatrix();
                mParent.colorMode(PConstants.HSB, 360, 100, 100, 100);
                mParent.background(0, 0, mBackgroundBrightness);
                mParent.noFill();
                mParent.ellipseMode(PConstants.RADIUS);
                mParent.strokeCap(PConstants.SQUARE);
                mParent.textLeading(14);
                mParent.textAlign(PConstants.LEFT, PConstants.TOP);
                mParent.smooth();

                // Add menubar height (21 pixels).
                mParent.translate(mParent.width / 2, mParent.height / 2 + 21);

                // Draw the vizualization items.
                for (final SunburstItem item : mItems) {
                    // Draw arcs or rects.
                    if (mShowArcs) {
                        if (mUseArc) {
                            item.drawArc(mInnerNodeArcScale, mLeafArcScale);
                        } else {
                            item.drawRect(mInnerNodeArcScale, mLeafArcScale);
                        }
                    }
                }

                for (final SunburstItem item : mItems) {
                    if (mShowLines) {
                        if (mUseBezierLine) {
                            item.drawRelationBezier();
                        } else {
                            item.drawRelationLine();
                        }
                    }
                }

                for (final SunburstItem item : mItems) {
                    item.drawDot();
                }

                // Rollover test.
                rollover(State.DRAW);

                mParent.popMatrix();

                if (mSavePDF) {
                    mSavePDF = false;
                    mParent.endRecord();
                    PApplet.println("saving to pdf – done");
                }

                drawGUI();

                if (mFisheye) {
                    fisheye(mParent.mouseX, mParent.mouseY, 120);
                }
            } finally {
                mLock.release();
            }
        }
    }

    /**
     * Mouse rollover test.
     * 
     * @param paramState
     *            Determines the state, if item info should be printed or not.
     * @return Index of the {@link SunburstItem}, which is currently hovered. -1 if no matching item can be
     *         found.
     */
    private int rollover(final State paramState) {
        // Mouse rollover, arc hittest vars.
        int hitTestIndex = -1;
        final float x = mParent.mouseX - mParent.width / 2;
        final float y = mParent.mouseY - (mParent.height / 2 + 21);
        float angle = PApplet.atan2(y - 0, x - 0);
        final float radius = PApplet.dist(0, 0, x, y);

        if (angle < 0) {
            angle = PApplet.map(angle, -PConstants.PI, 0, PConstants.PI, PConstants.TWO_PI);
        } else {
            angle = PApplet.map(angle, 0, PConstants.PI, 0, PConstants.PI);
        }
        // Calc mouse depth with mouse radius ... transformation of calcEqualAreaRadius()
        final int depth =
            PApplet.floor(PApplet.pow(radius, 2) * (mDepthMax + 1) / PApplet.pow(getInitialRadius(), 2));

        int index = 0;
        for (final SunburstItem item : mItems) {
            // Hittest, which arc is the closest to the mouse.
            if (item.getDepth() == depth && angle > item.getAngleStart() && angle < item.getAngleEnd()) {
                hitTestIndex = index;
            }

            index++;
        }

        // Mouse rollover.
        if (!mShowGUI && paramState == State.DRAW) {
            // Depth level focus.
            if (depth <= mDepthMax) {
                final float firstRad = calcEqualAreaRadius(depth, mDepthMax);
                final float secondRad = calcEqualAreaRadius(depth + 1, mDepthMax);
                mParent.stroke(0, 0, 0, 30);
                mParent.strokeWeight(5.5f);
                mParent.ellipse(0, 0, firstRad, firstRad);
                mParent.ellipse(0, 0, secondRad, secondRad);
            }
            // Rollover text.
            if (hitTestIndex != -1) {
                final String text = mItems.get(hitTestIndex).toString();
                final float texW = mParent.textWidth(text) * 1.2f;
                mParent.fill(0, 0, 0);
                final int offset = 5;
                mParent.rect(x + offset, y + offset, texW + 4, mParent.textAscent() * 3.6f);
                mParent.fill(0, 0, 100);
                mParent.text(text.toUpperCase(), x + offset + 2, y + offset + 2);
            }
        }

        return hitTestIndex;
    }

    /**
     * Fisheye transformation.
     * 
     * @param paramXPos
     *            X position of middle point of the transformation
     * @param paramYPos
     *            Y position of middle point of the transformation
     * @param paramRadius
     *            the radius to use
     */
    void fisheye(final int paramXPos, final int paramYPos, final int paramRadius) {
        // Start point of rectangle to grab.
        final int tlx = paramXPos - paramRadius;
        final int tly = paramYPos - paramRadius;
        // Rectangle with pixels.
        final PImage pi = mParent.get(tlx, tly, paramRadius * 2, paramRadius * 2);
        for (int x = -paramRadius; x < paramRadius; x++) {
            for (int y = -paramRadius; y < paramRadius; y++) {
                // Rescale cartesian coords between -1 and 1.
                final float cx = (float)x / paramRadius;
                final float cy = (float)y / paramRadius;

                // Outside of the sphere -> skip.
                final float square = PApplet.sq(cx) + PApplet.sq(cy);
                if (square >= 1) {
                    continue;
                }

                // Compute cz from cx & cy.
                final float cz = PApplet.sqrt(1 - square);

                // Cartesian coords cx, cy, cz -> spherical coords sx, sy, still in -1, 1 range.
                final float sx = PApplet.atan(EFFECT_AMOUNT * cx / cz) * 2 / PConstants.PI;
                final float sy = PApplet.atan(EFFECT_AMOUNT * cy / cz) * 2 / PConstants.PI;

                // Spherical coords sx & sy -> texture coords.
                final int tx = tlx + (int)((sx + 1) * paramRadius);
                final int ty = tly + (int)((sy + 1) * paramRadius);

                // Set pixel value.
                pi.set(paramRadius + x, paramRadius + y, mParent.get(tx, ty));
            }
        }
        mParent.set(tlx, tly, pi);
    }

    /**
     * Get initial radius.
     * 
     * @return initial radius
     */
    private float getInitialRadius() {
        return mParent.height / 2f;
    }

    /**
     * Is getting called from processings keyRealeased-method and implements it.
     * 
     * @see processing.core.PApplet#keyReleased()
     */
    void keyReleased() {
        if (!mXPathField.isFocus()) {
            switch (mParent.key) {
            case 's':
            case 'S':
                // Save PNG.
                mParent.saveFrame(SAVEPATH + "_##.png");
                break;
            case 'p':
            case 'P':
                // Save PDF.
                mSavePDF = true;
                PApplet.println("\n" + "saving to pdf – starting");
                mParent.beginRecord(PConstants.PDF, SAVEPATH + ".pdf");
                break;
            case '\b':
                // Backspace.
                mParent.noLoop();
                try {
                    mLock.acquire();

                    if (!mItems.isEmpty()) {
                        // Go back one index in history list.
                        final int lastItemIndex = mLastItems.size() - 1;

                        mItems = mLastItems.get(lastItemIndex);
                        mLastItems.remove(lastItemIndex);
                        for (final SunburstItem item : mItems) {
                            item.update(mMappingMode);
                        }
                    }
                } catch (final Exception e) {
                    LOGWRAPPER.warn(e.getMessage(), e);
                } finally {
                    mLock.release();
                    mParent.loop();
                }

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

            switch (mParent.key) {
            case '1':
            case '2':
            case '3':
                for (final SunburstItem item : mItems) {
                    item.update(mMappingMode);
                }
                break;
            case 'm':
            case 'M':
                mShowGUI = mControlP5.group("menu").isOpen();
                mShowGUI = !mShowGUI;
                break;
            default:
                // No action.
            }

            if (mShowGUI) {
                mControlP5.group("menu").open();
            } else {
                mControlP5.group("menu").close();
            }
        }
    }

    /**
     * Implements processing mouseEntered.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     * 
     * @see processing.core.PApplet#mouseEntered
     */
    void mouseEntered(final MouseEvent paramEvent) {
        mParent.loop();
    }

    /**
     * Implements processing mouseExited.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     * 
     * @see processing.core.PApplet#mouseExited
     */
    void mouseExited(final MouseEvent paramEvent) {
        mParent.noLoop();
    }

    /**
     * Implements processing mousePressed.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     * 
     * @see processing.core.PApplet#mousePressed
     */
    void mousePressed(final MouseEvent paramEvent) {
        mControlP5.controlWindow.mouseEvent(paramEvent);

        // Mouse rollover.
        final int hitTestIndex = rollover(State.CLICK);
        if (!mShowGUI && hitTestIndex != -1) {
            switch (paramEvent.getClickCount()) {
            case 1:
                // System.out.println("SINGLE CLICK");
                // switch (mParent.mouseButton) {
                // case PConstants.LEFT:
                // System.out.println("LEFT");
                // break;
                // case PConstants.RIGHT:
                // System.out.println("RIGHT");
                // IWriteTransaction wtx = null;
                // try {
                // if (wtx != null && !wtx.isClosed()) {
                // wtx.close();
                // }
                // wtx = mReadDB.getSession().beginWriteTransaction();
                // wtx.revertTo(mReadDB.getRtx().getRevisionNumber());
                // wtx.moveTo(mItems.get(hitTestIndex).mNode.getNodeKey());
                // final SunburstPopupMenu menu = new SunburstPopupMenu(mParent, wtx, mReadDB);
                // menu.show(paramEvent.getComponent(), paramEvent.getX(), paramEvent.getY());
                // } catch (final TreetankException e) {
                // // TODO
                // }
                //
                // break;
                // default:
                // // Take no action.
                // }
                // break;
            case 2:
                switch (mParent.mouseButton) {
                case PConstants.LEFT:
                    mLastItems.add(new ArrayList<SunburstItem>(mItems));
                    final long nodeKey = mItems.get(hitTestIndex).mNode.getNodeKey();
                    mModel.traverseTree(nodeKey, mTextWeight);
                    break;
                case PConstants.RIGHT:
                    break;
                default:
                    // Take no action.
                }
                break;
            default:
                // Take no action.
            }
        }
    }

    /**
     * Calculate area so that radiuses have equal areas in each depth.
     * 
     * @param paramDepth
     *            The actual depth.e
     * @param paramDepthMax
     *            The maximum depth.
     * @return calculated area
     */
    float calcEqualAreaRadius(final int paramDepth, final int paramDepthMax) {
        float retVal = 0f;

        if (paramDepth == 0) {
            retVal = PApplet.sqrt(PApplet.pow(getInitialRadius(), 2) / (paramDepthMax + 1));
            // System.out.println("LAAAAAAAAA: " + retVal);
        } else {
            retVal = PApplet.sqrt(paramDepth * PApplet.pow(getInitialRadius(), 2) / (paramDepthMax + 1));
        }

        return retVal;
    }

    /**
     * Calculate area radius in a linear way.
     * 
     * @param paramDepth
     *            The actual depth.
     * @param paramDepthMax
     *            The maximum depth.
     * @return calculated area
     */
    float calcAreaRadius(final int paramDepth, final int paramDepthMax) {
        return PApplet.map(paramDepth, 0, paramDepthMax + 1, 0, getInitialRadius());
    }

    /**
     * Get mapping mode.
     * 
     * @return mappingMode
     */
    int getMappingMode() {
        return mMappingMode;
    }

    /**
     * Format a timestamp.
     * 
     * @return Formatted timestamp.
     */
    private static String timestamp() {
        return String.format("%1$ty%1$tm%1$td_%1$tH%1$tM%1$tS", Calendar.getInstance());
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        mParent.noLoop();
        try {
            mLock.acquire();

            if (paramEvent.getPropertyName().equals("items")) {
                assert paramEvent.getNewValue() instanceof List;
                mItems = (List<SunburstItem>)paramEvent.getNewValue();
            } else if (paramEvent.getPropertyName().equals("maxDepth")) {
                assert paramEvent.getNewValue() instanceof Integer;
                mDepthMax = (Integer)paramEvent.getNewValue();
            }

            for (final SunburstItem item : mItems) {
                item.update(mGUI.getMappingMode());
            }
        } catch (final Exception e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
            mParent.loop();
        }
    }
}
