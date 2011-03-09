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
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;
import com.treetank.exception.TTIOException;
import com.treetank.gui.ReadDB;
import com.treetank.gui.view.sunburst.SunburstView.Embedded;
import com.treetank.utils.LogWrapper;

import controlP5.*;

import org.gicentre.utils.move.ZoomPan;
import org.gicentre.utils.move.ZoomPanListener;
import org.slf4j.LoggerFactory;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

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
final class SunburstGUI implements PropertyChangeListener, ControlListener {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4747210906900567484L;

    /** {@link LogWrapper}. */
    private static final LogWrapper LOGWRAPPER = new LogWrapper(LoggerFactory.getLogger(SunburstGUI.class));

    /** Path to save visualization as a PDF or PNG file. */
    private static final String SAVEPATH = "target" + File.separator;

    /** Amount of effect in the fisheye transformation. */
    private static final float EFFECT_AMOUNT = 0.9f;

    /** The GUI of the Sunburst view. */
    private static volatile SunburstGUI mGUI;

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

    /** Modification weight. */
    transient float mModificationWeight = 0.7f;

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

    /** Maximum depth in the tree. */
    volatile int mDepthMax;

    /** Determines if arcs should be used (Default: true). */
    transient boolean mUseArc = true;

    /** Determines if diff view is used. */
    transient boolean mUseDiffView;

    /** Determines if bezier lines for connections between parent/child should be used (Default: true). */
    transient boolean mUseBezierLine = true;

    /** Determines if line connextions between parent/child should be drawn. */
    transient boolean mShowLines = true;

    /** Buffered image. */
    transient PGraphics mBuffer;

    /** Leaf node arc scale. */
    transient float mLeafArcScale = 1.0f;

    /** {@link AbsModel}. */
    transient volatile AbsModel mModel;

    /** Current angle of the mouse cursor to y axis. */
    transient float mAngle;

    /** Current depth of the mouse cursor. */
    transient int mDepth;

    /** {@link Sempahore} to block re-initializing sunburst item list until draw() is finished(). */
    private volatile Semaphore mLock = new Semaphore(1);

    /** Determines if zooming or panning endefinald. */
    private transient boolean mZoomPanEnded;

    /** Image to draw. */
    private volatile PImage mImg;

    /** Background brightness. */
    private transient float mBackgroundBrightness = 100f;

    /** Color mapping mode. */
    private transient int mMappingMode = 1;

    /** Determines if fisheye should be used. */
    private transient boolean mFisheye;

    /** Determines if current state should be saved as a PDF-file. */
    private transient boolean mSavePDF;

    /** Zoom into or out. */
    private volatile ZoomPan mZoomer;

    /** X position of the mouse cursor. */
    private transient float mX;

    /** Y position of the mouse cursor. */
    private transient float mY;

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

    /** Drawing strategy. */
    private volatile EDraw mDraw;

    // /** {@link ControlP5} listboxes. */
    // private transient List<ListBox> mBoxes;

    /** {@link ControlP5} text field. */
    private transient Textfield mXPathField;

    /** Parent {@link PApplet}. */
    final Embedded mParent;

    /** {@link ExecutorService} to parallelize painting. */
    private transient ExecutorService mService;

    private transient boolean mFirst;

    /** Determines if model has done the work. */
    volatile boolean mDone;

    /** Item, which is currently clicked. */
    private transient SunburstItem mHitItem;

    /** Hit test index. */
    transient int mHitTestIndex = -1;

    /** {@link ReadDB} reference. */
    private volatile ReadDB mDb;

    private volatile IWriteTransaction mWtx;

    private volatile DropdownList mRevisions;

    volatile int mSelectedRev;

    volatile int mOldDepthMax;

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
    private SunburstGUI(final PApplet paramParentApplet, final AbsModel paramModel, final ReadDB paramReadDB) {
        mDb = paramReadDB;
        mParent = (Embedded)paramParentApplet;
        mModel = paramModel;
        mZoomer = new ZoomPan(paramParentApplet);
        mZoomer.setMouseMask(PConstants.CONTROL);
        mZoomer.addZoomPanListener(new MyListener());
    }

    /**
     * Factory method (Singleton). Note that it's always called from the animation thread, thus it doesn't
     * need to be synchronized. Uses double checked locking to improve performance.
     * 
     * @param paramParentApplet
     *            parent processing applet
     * @param paramModel
     *            the model
     * @param paramReadDB
     *            read database
     * @return a {@link SunburstGUI} singleton
     */
    static synchronized SunburstGUI getInstance(final PApplet paramParentApplet, final AbsModel paramModel,
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
        final int activeColor = mParent.color(0, 130, 164);
        mControlP5 = new ControlP5(mParent);
        mControlP5.addListener(this);
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

        mRanges.add(ri++, mControlP5.addRange("leaf node hue range", 0, 360, mHueStart, mHueEnd, left, top
            + posY + 0, len, 15));
        mRanges.add(ri++, mControlP5.addRange("leaf node saturation range", 0, 100, mSaturationStart,
            mSaturationEnd, left, top + posY + 20, len, 15));
        mRanges.add(ri++, mControlP5.addRange("leaf node brightness range", 0, 100, mBrightnessStart,
            mBrightnessEnd, left, top + posY + 40, len, 15));
        posY += 70;

        mRanges.add(ri++, mControlP5.addRange("inner node brightness range", 0, 100,
            mInnerNodeBrightnessStart, mInnerNodeBrightnessEnd, left, top + posY + 0, len, 15));
        mRanges.add(ri++, mControlP5.addRange("inner node stroke brightness range", 0, 100,
            mInnerNodeStrokeBrightnessStart, mInnerNodeStrokeBrightnessEnd, left, top + posY + 20, len, 15));
        posY += 50;

        // name, minimum, maximum, default value (float), x, y, width, height
        mSliders.add(si, mControlP5.addSlider("mInnerNodeArcScale", 0, 1, mInnerNodeArcScale, left, top
            + posY + 0, len, 15));
        mSliders.get(si++).setLabel("innerNodeArcScale");
        mSliders.add(si,
            mControlP5.addSlider("mLeafArcScale", 0, 1, mLeafArcScale, left, top + posY + 20, len, 15));
        mSliders.get(si++).setLabel("leafNodeArcScale");
        posY += 50;
        mSliders.add(si, mControlP5.addSlider("mModificationWeight", 0, 1, mModificationWeight, left, top
            + posY + 0, len, 15));
        mSliders.get(si++).setLabel("modification weight");
        posY += 50;

        mRanges.add(
            ri++,
            mControlP5.addRange("stroke weight range", 0, 10, mStrokeWeightStart, mStrokeWeightEnd, left, top
                + posY + 0, len, 15));
        posY += 30;

        mSliders.add(si, mControlP5.addSlider("mDotSize", 0, 10, mDotSize, left, top + posY + 0, len, 15));
        mSliders.get(si++).setLabel("dotSize");
        mSliders.add(si,
            mControlP5.addSlider("mDotBrightness", 0, 100, mDotBrightness, left, top + posY + 20, len, 15));
        mSliders.get(si++).setLabel("dotBrightness");
        posY += 50;

        mSliders.add(si, mControlP5.addSlider("mBackgroundBrightness", 0, 100, mBackgroundBrightness, left,
            top + posY + 0, len, 15));
        mSliders.get(si++).setLabel("backgroundBrightness");
        posY += 30;

        mSliders.add(si,
            mControlP5.addSlider("mTextWeight", 0, 10, mTextWeight, left, top + posY + 0, len, 15));
        mSliders.get(si++).setLabel("text weight");
        posY += 50;

        mToggles.add(ti, mControlP5.addToggle("mShowArcs", mShowArcs, left + 0, top + posY, 15, 15));
        mToggles.get(ti++).setLabel("show Arcs");
        mToggles.add(ti, mControlP5.addToggle("mShowLines", mShowLines, left + 0, top + posY + 20, 15, 15));
        mToggles.get(ti++).setLabel("show Lines");
        mToggles.add(ti,
            mControlP5.addToggle("mUseBezierLine", mUseBezierLine, left + 0, top + posY + 40, 15, 15));
        mToggles.get(ti++).setLabel("Bezier / Line");
        mToggles.add(ti, mControlP5.addToggle("mUseArc", mUseArc, left + 0, top + posY + 60, 15, 15));
        mToggles.get(ti++).setLabel("Arc / Rect");
        mToggles.add(ti, mControlP5.addToggle("mFisheye", mFisheye, left + 0, top + posY + 80, 15, 15));
        mToggles.get(ti++).setLabel("Fisheye lense");
        mToggles.add(ti,
            mControlP5.addToggle("mUseDiffView", mUseDiffView, left + 0, top + posY + 100, 15, 15));
        mToggles.get(ti++).setLabel("Diff view");

        mXPathField = mControlP5.addTextfield("xpath", mParent.width - 250, top + 20, 200, 20);
        mXPathField.setLabel("XPath expression");
        mXPathField.setFocus(false);
        mXPathField.setAutoClear(false);
        mXPathField.plugTo(this);

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
     *            the {@link ControlEvent}
     */
    @Override
    public void controlEvent(final ControlEvent paramControlEvent) {
        if (paramControlEvent.isController()) {
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

            update();
        } else if (paramControlEvent.isGroup()) {
            if (paramControlEvent.group().name().equals("Compare revision")) {
                mSelectedRev = (int)paramControlEvent.group().value();
                mModel = new SunburstCompareModel(mParent, mDb);
                mModel.traverseTree(new SunburstContainer().setRevision(mSelectedRev).setModWeight(
                    mModificationWeight));
            }
        }
    }

    /**
     * XPath expression.
     * 
     * @param paramXPath
     *            the XPath expression
     */
    public void xpath(final String paramXPath) {
        mModel.evaluateXPath(paramXPath);
    }

    /**
     * Implements the {@link PApplet} draw() method.
     */
    void draw() {
        if (mControlP5 != null && mDone) {
            mParent.pushMatrix();

            // This enables zooming/panning.
            if (mFirst) {
                mFirst = false;
            } else {
                mZoomer.transform();
            }

            mParent.colorMode(PConstants.HSB, 360, 100, 100, 100);
            mParent.noFill();
            mParent.ellipseMode(PConstants.RADIUS);
            mParent.strokeCap(PConstants.SQUARE);
            mParent.textLeading(14);
            mParent.textAlign(PConstants.LEFT, PConstants.TOP);
            mParent.smooth();

            if (mZoomer.isZooming() || mZoomer.isPanning() || (mSavePDF && !mZoomPanEnded) || mFisheye) {
                LOGWRAPPER.debug("Without buffered image!");
                mParent.background(0, 0, mBackgroundBrightness);
                mParent.translate((float)mParent.width / 2f, (float)mParent.height / 2f);
                mDraw = EDraw.DRAW;
                drawItems();
            } else {
                LOGWRAPPER.debug("Buffered image!");
                try {
                    mLock.acquire();
                    mParent.image(mImg, 0, 0);
                } catch (final InterruptedException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                } finally {
                    mLock.release();
                    LOGWRAPPER.debug("[draw()]: Available permits: " + mLock.availablePermits());
                }
                mParent.translate(mParent.width / 2, mParent.height / 2);
            }

            // Mouse rollover, arc hittest vars.
            rollover();

            // Mouse rollover.
            if ((!mZoomPanEnded || mZoomer.isZooming() || mZoomer.isPanning()) && !mShowGUI) {
                // Depth level focus.
                if (mDepth <= mDepthMax) {
                    final float firstRad = calcEqualAreaRadius(mDepth, mDepthMax);
                    final float secondRad = calcEqualAreaRadius(mDepth + 1, mDepthMax);
                    mParent.stroke(0, 0, 0, 30);
                    mParent.strokeWeight(5.5f);
                    mParent.ellipse(0, 0, firstRad, firstRad);
                    mParent.ellipse(0, 0, secondRad, secondRad);
                }
                // Rollover text.
                if (mHitTestIndex != -1) {
                    final String text = mHitItem.toString();

                    int lines = 1;
                    for (final char c : text.toCharArray()) {
                        if (c == '\n') {
                            lines++;
                        }
                    }

                    final int offset = 5;
                    final float texW = mParent.textWidth(text) * 1.2f;// + 2f * offset + 4f;
                    mParent.fill(0, 0, 0);
                    mParent.rect(mX + offset, mY + offset, texW,
                        (mParent.textAscent() + mParent.textDescent()) * lines + 4);
                    mParent.fill(0, 0, 100);
                    mParent.text(text.toUpperCase(), mX + offset + 2, mY + offset + 2);
                }
            }

            // Fisheye view.
            if (mFisheye) {
                fisheye(mParent.mouseX, mParent.mouseY, 120);
            }

            mParent.popMatrix();

            if (mSavePDF) {
                mSavePDF = false;
                mParent.endRecord();
                PApplet.println("saving to pdf – done");
            }
            // } finally {
            // if (acquired) {
            // mLock.release();
            // LOGWRAPPER.debug("[draw()]: Available permits: " + mLock.availablePermits());
            // }
            // }

            drawGUI();
        }
    }

    /** Initialize rollover method. */
    private void rolloverInit() {
        mHitTestIndex = -1;
        if (mZoomer.isZooming() || mZoomer.isPanning() || mZoomPanEnded) {
            final PVector mousePosition = mZoomer.getMouseCoord();
            mX = mousePosition.x - mParent.width / 2;
            mY = mousePosition.y - mParent.height / 2;

        } else {
            mX = mParent.mouseX - mParent.width / 2;
            mY = mParent.mouseY - mParent.height / 2;
        }
        mAngle = PApplet.atan2(mY - 0, mX - 0);
        final float radius = PApplet.dist(0, 0, mX, mY);

        if (mAngle < 0) {
            mAngle = PApplet.map(mAngle, -PConstants.PI, 0, PConstants.PI, PConstants.TWO_PI);
        } else {
            mAngle = PApplet.map(mAngle, 0, PConstants.PI, 0, PConstants.PI);
        }
        // Calc mouse depth with mouse radius ... transformation of calcEqualAreaRadius()
        mDepth = PApplet.floor(PApplet.pow(radius, 2) * (mDepthMax + 1) / PApplet.pow(getInitialRadius(), 2));
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
            case 'c':
            case 'C':
                update();
                mZoomPanEnded = false;
                break;
            case 's':
            case 'S':
                // Save PNG.
                mParent.saveFrame(SAVEPATH + timestamp() + "_##.png");
                break;
            case 'p':
            case 'P':
                // Save PDF.
                mSavePDF = true;
                PApplet.println("\n" + "saving to pdf – starting");
                mParent.beginRecord(PConstants.PDF, SAVEPATH + timestamp() + ".pdf");
                break;
            case '\b':
                // Backspace.
                mModel.undo();
                update();
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
            case 'o':
            case 'O':
                mUseDiffView = true;

                mRevisions =
                    mControlP5.addDropdownList("Compare revision", mParent.width - 250, 100, 100, 120);
                try {
                    for (long i = mDb.getRevisionNumber() + 1, newestRev =
                        mDb.getSession().beginReadTransaction().getRevisionNumber(); i <= newestRev; i++) {
                        mRevisions.addItem("Revision " + i, (int)i);
                    }
                } catch (final TTIOException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                } catch (final AbsTTException e) {
                    LOGWRAPPER.error(e.getMessage(), e);
                }
                break;
            default:
                break;
            }

            switch (mParent.key) {
            case '1':
            case '2':
            case '3':
                update();
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
     * Implements processing mouseMoved.
     * 
     * @param paramEvent
     *            The {@link MouseEvent}.
     * 
     * @see processing.core.PApplet#mouseMoved
     */
    void mouseMoved(final MouseEvent paramEvent) {
        // draw();
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
        mZoomer.mouseEvent(paramEvent);

        mShowGUI = mControlP5.group("menu").isOpen();

        // Mouse rollover.
        if (!mParent.keyPressed) {
            rollover();

            if (!mShowGUI && mHitTestIndex != -1) {
                switch (mParent.mouseButton) {
                case PConstants.LEFT:
                    System.out.println("LEFT");
                    mModel.update(new SunburstContainer().setKey(mModel.getItem(mHitTestIndex).getNode()
                        .getNodeKey()));
                    break;
                case PConstants.RIGHT:
                    System.out.println("RIGHT");
                    try {
                        if (mWtx != null && !mWtx.isClosed()) {
                            mWtx.close();
                        }
                        mWtx = mDb.getSession().beginWriteTransaction();
                        // wtx.revertTo(..getRevisionNumber());
                        mWtx.moveTo(mModel.getItem(mHitTestIndex).mNode.getNodeKey());
                        final SunburstPopupMenu menu = new SunburstPopupMenu(mParent, mWtx, mDb);
                        menu.show(paramEvent.getComponent(), paramEvent.getX(), paramEvent.getY());
                    } catch (final AbsTTException e) {
                        LOGWRAPPER.error(e.getMessage(), e);
                    }

                    break;
                default:
                    // Take no action.
                }
            }
        }
    }

    /**
     * Calculate area so that radiuses have equal areas in each depth.
     * 
     * @param paramDepth
     *            the actual depth
     * @param paramDepthMax
     *            the maximum depth
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

    /** Update items as well as the buffered offscreen image. */
    void update() {
        LOGWRAPPER.debug("[update()]: Available permits: " + mLock.availablePermits());
        LOGWRAPPER.debug("parent width: " + mParent.width + " parent height: " + mParent.height);
        mBuffer = mParent.createGraphics(mParent.width, mParent.height, PConstants.JAVA2D);
        mBuffer.beginDraw();
        updateBuffer();
        mBuffer.endDraw();
        mParent.noLoop();

        try {
            mLock.acquire();
            mImg = mBuffer.get(0, 0, mBuffer.width, mBuffer.height);
        } catch (final InterruptedException e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
            mParent.loop();
        }
    }

    /**
     * Draws into an off-screen buffer.
     */
    private void updateBuffer() {
        mBuffer.pushMatrix();
        mBuffer.colorMode(PConstants.HSB, 360, 100, 100, 100);
        mBuffer.background(0, 0, mBackgroundBrightness);
        mBuffer.noFill();
        mBuffer.ellipseMode(PConstants.RADIUS);
        mBuffer.strokeCap(PConstants.SQUARE);
        // mBuffer.textLeading(14);
        // mBuffer.textAlign(PConstants.LEFT, PConstants.TOP);
        mBuffer.smooth();

        // Add menubar height (21 pixels).
        mBuffer.translate((float)mParent.width / 2f, (float)mParent.height / 2f);

        // Draw items.
        // mService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        mDraw = EDraw.UPDATEBUFFER;
        drawItems();

        // mService.submit(new UpdateBuffer(item));
        // }
        // mService.shutdown();
        // try {
        // mService.awaitTermination(5, TimeUnit.SECONDS);
        // } catch (final InterruptedException e) {
        // LOGWRAPPER.error(e.getMessage(), e);
        // }
        mBuffer.popMatrix();
    }

    /** Updates the buffered offscreen image after a zoom or pan. */
    private void updateImage() {
        LOGWRAPPER.debug("Available permits: " + mLock.availablePermits());
        mParent.noLoop();

        mParent.pushMatrix();

        // This enables zooming/panning.
        mZoomer.transform();
        mFirst = true;

        mParent.colorMode(PConstants.HSB, 360, 100, 100, 100);
        mParent.noFill();
        mParent.ellipseMode(PConstants.RADIUS);
        mParent.strokeCap(PConstants.SQUARE);
        mParent.textLeading(14);
        mParent.textAlign(PConstants.LEFT, PConstants.TOP);
        mParent.smooth();
        mParent.background(0, 0, mBackgroundBrightness);
        mParent.translate((float)mParent.width / 2f, (float)mParent.height / 2f);
        mDraw = EDraw.DRAW;
        drawItems();
        mParent.popMatrix();

        try {
            mLock.acquire();
            mImg = mParent.get(0, 0, mParent.width, mParent.height);
        } catch (final InterruptedException e) {
            LOGWRAPPER.warn(e.getMessage(), e);
        } finally {
            mLock.release();
            mParent.loop();
        }
    }

    // /**
    // * {@inheritDoc}
    // */
    // @SuppressWarnings("unchecked")
    // @Override
    // public void propertyChange(final PropertyChangeEvent paramEvent) {
    // boolean modifiedDepth = false;
    // try {
    // mLock.acquire();
    // if (paramEvent.getPropertyName().equals("items")) {
    // assert paramEvent.getNewValue() instanceof List;
    // mItems = (List<SunburstItem>)paramEvent.getNewValue();
    // } else if (paramEvent.getPropertyName().equals("maxDepth")) {
    // assert paramEvent.getNewValue() instanceof Integer;
    // mDepthMax = (Integer)paramEvent.getNewValue();
    // modifiedDepth = true;
    // }
    // } catch (final InterruptedException e) {
    // LOGWRAPPER.warn(e.getMessage(), e);
    // } finally {
    // mLock.release();
    // }
    // if (modifiedDepth) {
    // update();
    // }
    // }

    /** Class for responding to the end of a zoom or pan action. */
    private final class MyListener implements ZoomPanListener {
        @Override
        public void panEnded() {
            LOGWRAPPER.debug("Pan ended!");
            mZoomPanEnded = true;
            updateImage();
        }

        @Override
        public void zoomEnded() {
            LOGWRAPPER.debug("Zoom ended!");
            mZoomPanEnded = true;
            updateImage();
        }
    }

    // /** Update buffer concurrently. */
    // private final class UpdateBuffer implements Runnable {
    // /** @see SunburstItem */
    // private final SunburstItem mItem;
    //
    // /**
    // * Constructor.
    // *
    // * @param paramItem
    // * {@link SunburstItem}
    // */
    // private UpdateBuffer(final SunburstItem paramItem) {
    // mItem = paramItem;
    // }
    //
    // @Override
    // public void run() {
    // mItem.update(mGUI.getMappingMode(), mBuffer);
    // mDraw.drawStrategy(mGUI, mItem);
    // }
    // }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        if (paramEvent.getPropertyName().equals("maxDepth")) {
            assert paramEvent.getNewValue() instanceof Integer;
            mDepthMax = (Integer)paramEvent.getNewValue();
        } else if (paramEvent.getPropertyName().equals("oldMaxDepth")) {
            assert paramEvent.getNewValue() instanceof Integer;
            mOldDepthMax = (Integer)paramEvent.getNewValue();
        } else if (paramEvent.getPropertyName().equals("done")) {
            update();
            assert paramEvent.getNewValue() instanceof Boolean;
            mDone = true;
        }
    }

    /**
     * Rollover test.
     * 
     * @return true, if found, false otherwise
     */
    private boolean rollover() {
        boolean retVal = false;

        rolloverInit();
        int index = 0;
        for (final SunburstItem item : mModel) {
            // Hittest, which arc is the closest to the mouse.
            if (item.getDepth() == mDepth && mAngle > item.getAngleStart() && mAngle < item.getAngleEnd()) {
                mHitTestIndex = index;
                mHitItem = item;
                retVal = true;
                break;
            }
            index++;
        }

        return retVal;
    }

    /**
     * Draw items.
     */
    private void drawItems() {
        if (mUseDiffView) {
            mDraw.drawRevision(this);
        }

        for (final SunburstItem item : mModel) {
            item.update(getMappingMode(), null);
            mDraw.drawStrategy(this, item);
        }
    }
}
