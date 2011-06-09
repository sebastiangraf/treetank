/**
 * 
 */
package org.treetank.gui.view.sunburst;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import controlP5.*;

import org.gicentre.utils.move.ZoomPan;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.sunburst.SunburstView.Embedded;
import org.treetank.gui.view.sunburst.control.ISunburstControl;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Abstract Processing GUI.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public abstract class AbsSunburstGUI implements IProcessingGUI, PropertyChangeListener, ControlListener {

    /** {@link ControlP5} sliders. */
    protected transient List<Slider> mSliders;

    /** {@link ControlP5} ranges. */
    protected transient List<Range> mRanges;

    /** {@link ControlP5} toggles. */
    protected transient List<Toggle> mToggles;
    
    /** Hue start value. */
    private transient float mHueStart = 323;

    /** Hue end value. */
    protected transient float mHueEnd = 273;

    /** Saturation start value. */
    private transient float mSaturationStart = 100;

    /** Saturation end value. */
    private transient float mSaturationEnd = 73;

    /** Brightness start value. */
    private transient float mBrightnessStart = 77;

    /** Brightness end value. */
    private transient float mBrightnessEnd = 33;

    /** Inner node brightness start value. */
    private transient float mInnerNodeBrightnessStart = 90;

    /** Inner node brightness end value. */
    private transient float mInnerNodeBrightnessEnd = 20;

    /** Inner node stroke brightness start value. */
    private transient float mInnerNodeStrokeBrightnessStart = 90;

    /** Inner node stroke brightness end value. */
    private transient float mInnerNodeStrokeBrightnessEnd = 20;

    /** Inner node arc scale. */
    protected transient float mInnerNodeArcScale = 0.9f;

    /** Modification weight. */
    protected transient float mModificationWeight = 0.7f;

    /** Stroke weight start. */
    private transient float mStrokeWeightStart = 0.2f;

    /** Stroke weight end. */
    private transient float mStrokeWeightEnd = 4.0f;

    /** Dot size. */
    protected transient float mDotSize = 3f;

    /** Dot brightness. */
    protected transient float mDotBrightness = 80f;

    /** Show arcs. */
    protected transient boolean mShowArcs = true;

    /** Maximum depth in the tree. */
    protected transient int mDepthMax;

    /** Determines if arcs should be used (Default: true). */
    protected transient boolean mUseArc = true;

    /** {@link ZoomPan} instance to zoom in or out. */
    private transient ZoomPan mZoomer;

    /** Background brightness. */
    protected transient float mBackgroundBrightness = 100f;

    /** Leaf node arc scale. */
    protected transient float mLeafArcScale = 1.0f;

    /** {@link PGraphics} offscreen buffer. */
    protected transient PGraphics mBuffer;

    /** {@link PApplet} instance. */
    protected final PApplet mParent;

    /** Semaphore to lock draw() during updates. */
    protected final Semaphore mLock = new Semaphore(1);

    /** Image to write into. */
    protected transient PImage mImg;

    /** Used to define the rotation. */
    protected transient int mRad;

    /** Determines if bezier lines for connections between parent/child should be used (Default: true). */
    protected transient boolean mUseBezierLine = true;

    /** Determines if line connextions between parent/child should be drawn. */
    protected transient boolean mShowLines = true;
    
    /** {@link ControlP5} instance. */
    private final ControlP5 mControlP5;

    /** {@link ReadDB} instance. */
    protected transient ReadDB mDb;

    /** {@link ISunburstControl} implementation. */
    protected final ISunburstControl mControl;

    /**
     * Constructor.
     * 
     * @param paramApplet
     *            {@link PApplet} instance
     * @param paramControl
     *            {@link ISunburstControl} implementation
     * @param paramDb
     *            {@link ReadDB} instance
     */
    public AbsSunburstGUI(final PApplet paramApplet, final ISunburstControl paramControl, final ReadDB paramDb) {
        assert paramApplet != null;
        assert paramControl != null;
        assert paramDb != null;
        mParent = paramApplet;
        mControl = paramControl;
        mDb = paramDb;
        mZoomer = new ZoomPan(mParent);
        mZoomer.setMouseMask(PConstants.CONTROL);
        mControlP5 = new ControlP5(mParent);
    }
    
    /** Initial setup of the GUI. */
    abstract void setupGUI();

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
    protected void style(final int paramSi, final int paramRi, final int paramTi) {
        final ControlGroup ctrl = getControlP5().addGroup("menu", 15, 25, 35);
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

    /** Update items as well as the buffered offscreen image. */
    @Override
    public void update() {
        // LOGWRAPPER.debug("[update()]: Available permits: " + mLock.availablePermits());
        // LOGWRAPPER.debug("parent width: " + mParent.width + " parent height: " + mParent.height);
        getZoomer().reset();
        mBuffer = mParent.createGraphics(mParent.width, mParent.height, PConstants.JAVA2D);
        mBuffer.beginDraw();
        updateBuffer();
        mBuffer.endDraw();
        mParent.noLoop();

        try {
            mLock.acquire();
            mImg = mBuffer.get(0, 0, mBuffer.width, mBuffer.height);
        } catch (final InterruptedException exc) {
            exc.printStackTrace();
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
        // mZoomer.transform();
        mBuffer.colorMode(PConstants.HSB, 360, 100, 100, 100);
        mBuffer.background(0, 0, mBackgroundBrightness);
        mBuffer.noFill();
        mBuffer.ellipseMode(PConstants.RADIUS);
        mBuffer.strokeCap(PConstants.SQUARE);
        mBuffer.smooth();
        mBuffer.translate((float)mParent.width / 2f, (float)mParent.height / 2f);
        mBuffer.rotate(PApplet.radians(mRad));

        // Draw items.
        drawItems(EDraw.UPDATEBUFFER);

        mBuffer.stroke(0);
        mBuffer.strokeWeight(2f);
        mBuffer.line(0, 0, mParent.width, 0);

        mBuffer.popMatrix();
    }
    
    /**
     * Set database instance.
     * 
     * @param paramDb
     *            the {@link ReadDB} instance to set
     */
    void updateDb(final ReadDB paramDb) {
        mDb.close();
        mDb = paramDb;
    }

    /**
     * Called on every change of the GUI.
     * 
     * @param paramControlEvent
     *            the {@link ControlEvent}
     */
    @Override
    public void controlEvent(final ControlEvent paramControlEvent) {
        mControl.controlEvent(paramControlEvent);
    }

    /**
     * Draw {@link IVisualItem} instances on the screen.
     * 
     * @param paramDraw
     *            drawing strategy
     */
    abstract void drawItems(final EDraw paramDraw);

    // =============================== Setter and Getter ================================
    
    /**
     * @param mHueStart the mHueStart to set
     */
    public void setHueStart(float mHueStart) {
        this.mHueStart = mHueStart;
    }

    /**
     * @return the mHueStart
     */
    public float getHueStart() {
        return mHueStart;
    }

    /**
     * @param f
     */
    public void setHueEnd(float f) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param mSaturationStart the mSaturationStart to set
     */
    public void setSaturationStart(float mSaturationStart) {
        this.mSaturationStart = mSaturationStart;
    }

    /**
     * @return the mSaturationStart
     */
    public float getSaturationStart() {
        return mSaturationStart;
    }

    /**
     * @param mBrightnessStart the mBrightnessStart to set
     */
    public void setBrightnessStart(float mBrightnessStart) {
        this.mBrightnessStart = mBrightnessStart;
    }

    /**
     * @return the mBrightnessStart
     */
    public float getBrightnessStart() {
        return mBrightnessStart;
    }

    /**
     * @param mSaturationEnd the mSaturationEnd to set
     */
    public void setSaturationEnd(float mSaturationEnd) {
        this.mSaturationEnd = mSaturationEnd;
    }

    /**
     * @return the mSaturationEnd
     */
    public float getSaturationEnd() {
        return mSaturationEnd;
    }

    /**
     * @param mInnerNodeBrightnessStart the mInnerNodeBrightnessStart to set
     */
    public void setInnerNodeBrightnessStart(float mInnerNodeBrightnessStart) {
        this.mInnerNodeBrightnessStart = mInnerNodeBrightnessStart;
    }

    /**
     * @return the mInnerNodeBrightnessStart
     */
    public float getInnerNodeBrightnessStart() {
        return mInnerNodeBrightnessStart;
    }

    /**
     * @param mInnerNodeStrokeBrightnessStart the mInnerNodeStrokeBrightnessStart to set
     */
    public void setInnerNodeStrokeBrightnessStart(float mInnerNodeStrokeBrightnessStart) {
        this.mInnerNodeStrokeBrightnessStart = mInnerNodeStrokeBrightnessStart;
    }

    /**
     * @return the mInnerNodeStrokeBrightnessStart
     */
    public float getInnerNodeStrokeBrightnessStart() {
        return mInnerNodeStrokeBrightnessStart;
    }

    /**
     * @param mStrokeWeightStart the mStrokeWeightStart to set
     */
    public void setStrokeWeightStart(float mStrokeWeightStart) {
        this.mStrokeWeightStart = mStrokeWeightStart;
    }

    /**
     * @return the mStrokeWeightStart
     */
    public float getStrokeWeightStart() {
        return mStrokeWeightStart;
    }

    /**
     * @param mStrokeWeightEnd the mStrokeWeightEnd to set
     */
    public void setStrokeWeightEnd(float mStrokeWeightEnd) {
        this.mStrokeWeightEnd = mStrokeWeightEnd;
    }

    /**
     * @return the mStrokeWeightEnd
     */
    public float getStrokeWeightEnd() {
        return mStrokeWeightEnd;
    }

    /**
     * @param mInnerNodeStrokeBrightnessEnd the mInnerNodeStrokeBrightnessEnd to set
     */
    public void setInnerNodeStrokeBrightnessEnd(float mInnerNodeStrokeBrightnessEnd) {
        this.mInnerNodeStrokeBrightnessEnd = mInnerNodeStrokeBrightnessEnd;
    }

    /**
     * @return the mInnerNodeStrokeBrightnessEnd
     */
    public float getInnerNodeStrokeBrightnessEnd() {
        return mInnerNodeStrokeBrightnessEnd;
    }

    /**
     * @param mBrightnessEnd the mBrightnessEnd to set
     */
    public void setBrightnessEnd(float mBrightnessEnd) {
        this.mBrightnessEnd = mBrightnessEnd;
    }

    /**
     * @return the mBrightnessEnd
     */
    public float getBrightnessEnd() {
        return mBrightnessEnd;
    }

    /**
     * @param mInnerNodeBrightnessEnd the mInnerNodeBrightnessEnd to set
     */
    public void setInnerNodeBrightnessEnd(float mInnerNodeBrightnessEnd) {
        this.mInnerNodeBrightnessEnd = mInnerNodeBrightnessEnd;
    }

    /**
     * @return the mInnerNodeBrightnessEnd
     */
    public float getInnerNodeBrightnessEnd() {
        return mInnerNodeBrightnessEnd;
    }

    /**
     * @return the mControlP5
     */
    public ControlP5 getControlP5() {
        return mControlP5;
    }

    /**
     * @return the mZoomer
     */
    public ZoomPan getZoomer() {
        return mZoomer;
    }
}
