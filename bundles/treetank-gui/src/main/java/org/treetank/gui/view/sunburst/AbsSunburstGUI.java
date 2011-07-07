/**
 * 
 */
package org.treetank.gui.view.sunburst;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import controlP5.*;

import org.gicentre.utils.move.ZoomPan;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.sunburst.EDraw.EDrawSunburst;
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
public abstract class AbsSunburstGUI implements IProcessingGUI, PropertyChangeListener {

    /** Pixels from the left border of the processing view. */
    public static final int LEFT = 0;
    
    /** Pixels from the top border of the processing view. */
    public static final int TOP = 5;

    /** Y-position from the top. */
    protected transient int mPosY;

    /** {@link List} of {@link Slider}s. */
    protected final List<Slider> mSliders;

    /** {@link List} of {@link Range}s. */
    protected final List<Range> mRanges;

    /** {@link List} of {@link Toggle}s. */
    protected final List<Toggle> mToggles;
    
    /** Color mapping mode. */
    private transient int mMappingMode = 3;

    /** Hue start value. */
    private transient float mHueStart = 323;

    /** Hue end value. */
    private transient float mHueEnd = 273;

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
    private transient float mInnerNodeArcScale = 0.9f;

    /** Modification weight. */
    private transient float mModificationWeight = 0.7f;

    /** Stroke weight start. */
    private transient float mStrokeWeightStart = 0.2f;

    /** Stroke weight end. */
    private transient float mStrokeWeightEnd = 4.0f;

    /** Dot size. */
    transient float mDotSize = 3f;

    /** Dot brightness. */
    private transient float mDotBrightness = 80f;

    /** Show arcs. */
    public boolean mShowArcs = true;

    /** Maximum depth in the tree. */
    protected transient int mDepthMax;

    /** Determines if arcs should be used (Default: true). */
    public transient boolean mUseArc = true;

    /** {@link ZoomPan} instance to zoom in or out. */
    private transient ZoomPan mZoomer;

    /** Background brightness. */
    private transient float mBackgroundBrightness = 100f;

    /** Leaf node arc scale. */
    private transient float mLeafArcScale = 1.0f;

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
    private transient boolean mUseBezierLine = true;

    /** Determines if line connextions between parent/child should be drawn. */
    private transient boolean mShowLines = true;

    /** {@link ControlP5} instance. */
    private final ControlP5 mControlP5;

    /** {@link ReadDB} instance. */
    protected transient ReadDB mDb;

    /** {@link ISunburstControl} implementation. */
    public final ISunburstControl mControl;

    /** Selected revision to compare. */
    protected transient long mSelectedRev;
    
    /** Selected revision to compare. */
    protected transient long mOldSelectedRev;

    /** Old maximum depth. */
    protected transient int mOldDepthMax;

    /** Determines if diff view should be used or not. */
    protected transient boolean mUseDiffView;
    
    /** Determines if current state should be saved as a PDF-file. */
    private transient boolean mSavePDF;
    
    /** Determines if SunburstGUI interface should be shown. */
    private transient boolean mShowGUI;
    
    /** Determines if model has done the work. */
    protected volatile boolean mDone;

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
    protected AbsSunburstGUI(final PApplet paramApplet, final ISunburstControl paramControl,
        final ReadDB paramDb) {
        assert paramApplet != null;
        assert paramControl != null;
        assert paramDb != null;
        mParent = paramApplet;
        mControl = paramControl;
        mDb = paramDb;
        mZoomer = new ZoomPan(mParent);
        mZoomer.setMouseMask(PConstants.CONTROL);
        mControlP5 = new ControlP5(mParent);
        mSliders = new LinkedList<Slider>();
        mRanges = new LinkedList<Range>();
        mToggles = new LinkedList<Toggle>();
        setupGUI();
    }

    /** Initial setup of the GUI. */
    private void setupGUI() {
        mParent.textFont(mParent.createFont("src" + File.separator + "main"
            + File.separator + "resources" + File.separator + "data" + File.separator
            + "miso-regular.ttf", 15));
        mParent.smooth();
        mParent.background(255f);

        mParent.textFont(mParent.createFont("src" + File.separator + "main" + File.separator + "resources"
            + File.separator + "data" + File.separator + "miso-regular.ttf", 15));

        final int activeColor = mParent.color(0, 130, 164);
        mControlP5.setColorActive(activeColor);
        mControlP5.setColorBackground(mParent.color(170));
        mControlP5.setColorForeground(mParent.color(50));
        mControlP5.setColorLabel(mParent.color(50));
        mControlP5.setColorValue(mParent.color(255));

        final int len = 300;

        final Range hueRange = mControlP5.addRange("leaf node hue range", 0, 360, getHueStart(), getHueEnd(), LEFT, TOP + mPosY
            + 0, len, 15);
        mRanges.add(hueRange);
        final Range saturationRange = mControlP5.addRange("leaf node saturation range", 0, 100, getSaturationStart(),
            getSaturationEnd(), LEFT, TOP + mPosY + 20, len, 15);
        mRanges.add(saturationRange);
        final Range brightnessRange =  mControlP5.addRange("leaf node brightness range", 0, 100, getBrightnessStart(),
            getBrightnessEnd(), LEFT, TOP + mPosY + 40, len, 15);
        mRanges.add(brightnessRange);

        mPosY += 70;

        final Range innerNodebrightnessRange = mControlP5.addRange("inner node brightness range", 0, 100,
            getInnerNodeBrightnessStart(), getInnerNodeBrightnessEnd(), LEFT, TOP + mPosY + 0, len, 15);
        mRanges.add(innerNodebrightnessRange);
        final Range innerNodeStrokeBrightnessRange = mControlP5.addRange("inner node stroke brightness range", 0, 100,
            getInnerNodeStrokeBrightnessStart(), getInnerNodeStrokeBrightnessEnd(), LEFT, TOP + mPosY + 20,
            len, 15);
        mRanges.add(innerNodeStrokeBrightnessRange);

        mPosY += 50;

        // name, minimum, maximum, default value (float), x, y, width, height
        final Slider innerNodeArcScale = mControlP5.addSlider("setInnerNodeArcScale", 0, 1, getInnerNodeArcScale(), LEFT, TOP
            + mPosY + 0, len, 15);
        innerNodeArcScale.setLabel("innerNodeArcScale");
        mSliders.add(innerNodeArcScale);
        final Slider leafNodeArcScale = mControlP5.addSlider("setLeafArcScale", 0, 1, getLeafArcScale(), LEFT, TOP + mPosY + 20, len, 15);
        leafNodeArcScale.setLabel("leafNodeArcScale");
        mSliders.add(leafNodeArcScale);
        mPosY += 50;
        final Slider modWeight = mControlP5.addSlider("setModificationWeight", 0, 1, getModificationWeight(), LEFT, TOP
            + mPosY + 0, len, 15);
        modWeight.setLabel("modification weight");
        mSliders.add(modWeight); 
        mPosY += 50;

        final Range strokeWeight = mControlP5.addRange("stroke weight range", 0, 10, getStrokeWeightStart(),
            getStrokeWeightEnd(), LEFT, TOP + mPosY + 0, len, 15);
        mRanges.add(strokeWeight);
        mPosY += 30;

        final Slider dotSize = mControlP5.addSlider("setDotSize", 0, 10, mDotSize, LEFT, TOP + mPosY + 0, len, 15);
        dotSize.setLabel("dot size");
        mSliders.add(dotSize); 
        final Slider dotBrightness = mControlP5.addSlider("setDotBrightness", 0, 100, mDotBrightness, LEFT, TOP + mPosY + 20,
            len, 15);
        dotBrightness.setLabel("dot brightness");
        mSliders.add(dotBrightness); 
        mPosY += 50;

        final Slider backgroundBrightness = mControlP5.addSlider("setBackgroundBrightness", 0, 100, getBackgroundBrightness(), LEFT, TOP
            + mPosY + 0, len, 15);
        backgroundBrightness.setLabel("background brightness");
        mSliders.add(backgroundBrightness); 
        mPosY += 50;

        final Toggle showArcs = mControlP5.addToggle("isShowArcs", isShowArcs(), LEFT + 0, TOP + mPosY, 15, 15);
        showArcs.setLabel("show arcs");
        mToggles.add(showArcs);
        final Toggle showLines = mControlP5.addToggle("isShowLines", isShowLines(), LEFT + 0, TOP + mPosY + 20, 15, 15);
        showLines.setLabel("show lines");
        mToggles.add(showLines);
        final Toggle useBezier = mControlP5.addToggle("isUseBezierLine", isUseBezierLine(), LEFT + 0, TOP + mPosY + 40, 15, 15);
        useBezier.setLabel("Bezier / Line");
        mToggles.add(useBezier);

        setup();

        style();
    }

    /**
     * Template method. Allows additional GUI setup.
     */
    protected abstract void setup();

    /**
     * Style menu.
     */
    protected void style() {
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
            slider.plugTo(mControl);
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
            range.plugTo(mControl);
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
            toggle.plugTo(mControl);
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
        setBuffer(mParent.createGraphics(mParent.width, mParent.height, PConstants.JAVA2D));
        getBuffer().beginDraw();
        updateBuffer();
        getBuffer().endDraw();
        mParent.noLoop();

        try {
            mLock.acquire();
            mImg = getBuffer().get(0, 0, getBuffer().width, getBuffer().height);
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
        getBuffer().pushMatrix();
        getBuffer().colorMode(PConstants.HSB, 360, 100, 100, 100);
        getBuffer().background(0, 0, getBackgroundBrightness());
        getBuffer().noFill();
        getBuffer().ellipseMode(PConstants.RADIUS);
        getBuffer().strokeCap(PConstants.SQUARE);
        getBuffer().smooth();
        getBuffer().translate((float)getBuffer().width / 2f, (float)getBuffer().height / 2f);
        getBuffer().rotate(PApplet.radians(mRad));

        // Draw items.
        System.out.println(mDepthMax);
        System.out.println(mBuffer.width);
        System.out.println(mBuffer.height);
        drawItems(EDraw.UPDATEBUFFER);

        getBuffer().stroke(0);
        getBuffer().strokeWeight(2f);
        getBuffer().line(0, 0, getBuffer().width, 0);

        getBuffer().popMatrix();
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
     * Draw {@link IVisualItem} instances on the screen.
     * 
     * @param paramDraw
     *            drawing strategy
     */
    protected void drawItems(final EDraw paramDraw) {
        if (!isShowArcs()) {
            paramDraw.drawRings(this);
        }

        @SuppressWarnings("unchecked")
        final Iterable<SunburstItem> items = (Iterable<SunburstItem>)mControl.getModel();
        for (final SunburstItem item : items) {
            paramDraw.update(this, item);

            if (mUseDiffView) {
                paramDraw.drawModificationRel(this, item);
                paramDraw.drawStrategy(this, item, EDrawSunburst.COMPARE);
            } else {
                paramDraw.drawStrategy(this, item, EDrawSunburst.NORMAL);
            }
        }

        if (mUseDiffView) {
            paramDraw.drawNewRevision(this);
            paramDraw.drawOldRevision(this);

            for (final SunburstItem item : items) {
                paramDraw.drawRelation(this, item);
            }

            for (final SunburstItem item : items) {
                paramDraw.drawDot(this, item);
                paramDraw.drawLabel(this, item);
            }
        }
    }
    
    /**
     * Get initial radius.
     * 
     * @return initial radius
     */
    protected float getInitialRadius() {
        return mParent.height / 2.2f;
    }

    /**
     * Calculate area so that radiuses have equal areas in each depth.
     * 
     * @param paramDepth
     *            the actual depth
     * @param paramDepthMax
     *            the maximum depth
     * @return calculated radius
     */
    protected float calcEqualAreaRadius(final int paramDepth, final int paramDepthMax) {
        return PApplet.sqrt(paramDepth * PApplet.pow(getInitialRadius(), 2) / (paramDepthMax + 1));
    }

    /**
     * Calculate area radius in a linear way.
     * 
     * @param paramDepth
     *            The actual depth.
     * @param paramDepthMax
     *            The maximum depth.
     * @return calculated radius
     */
    protected float calcAreaRadius(final int paramDepth, final int paramDepthMax) {
        return PApplet.map(paramDepth, 0, paramDepthMax + 1, 0, getInitialRadius());
    }

    /**
     * Get mapping mode.
     * 
     * @return mappingMode
     */
    public int getMappingMode() {
        return mMappingMode;
    }

    // =============================== Setter and Getter ================================

    /**
     * @param mHueStart
     *            the mHueStart to set
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
     * @return
     */
    public float getHueEnd() {
        return mHueEnd;
    }

    /**
     * @param paramHueEnd
     */
    public void setHueEnd(final float paramHueEnd) {
        mHueEnd = paramHueEnd;

    }

    /**
     * @param mSaturationStart
     *            the mSaturationStart to set
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
     * @param mBrightnessStart
     *            the mBrightnessStart to set
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
     * @param mSaturationEnd
     *            the mSaturationEnd to set
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
     * @param mInnerNodeBrightnessStart
     *            the mInnerNodeBrightnessStart to set
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
     * @param mInnerNodeStrokeBrightnessStart
     *            the mInnerNodeStrokeBrightnessStart to set
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
     * @param mStrokeWeightStart
     *            the mStrokeWeightStart to set
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
     * @param mStrokeWeightEnd
     *            the mStrokeWeightEnd to set
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
     * @param mInnerNodeStrokeBrightnessEnd
     *            the mInnerNodeStrokeBrightnessEnd to set
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
     * @param mBrightnessEnd
     *            the mBrightnessEnd to set
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
     * @param mInnerNodeBrightnessEnd
     *            the mInnerNodeBrightnessEnd to set
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

    /**
     * @param mModificationWeight
     *            the mModificationWeight to set
     */
    public void setModificationWeight(float mModificationWeight) {
        this.mModificationWeight = mModificationWeight;
    }

    /**
     * @return the mModificationWeight
     */
    public float getModificationWeight() {
        return mModificationWeight;
    }

    /**
     * @param mUseBezierLine
     *            the mUseBezierLine to set
     */
    public void setUseBezierLine(boolean mUseBezierLine) {
        this.mUseBezierLine = mUseBezierLine;
    }

    /**
     * @return the mUseBezierLine
     */
    public boolean isUseBezierLine() {
        return mUseBezierLine;
    }

    /**
     * @param mShowArcs
     *            the mShowArcs to set
     */
    public void setShowArcs(boolean mShowArcs) {
        this.mShowArcs = mShowArcs;
    }

    /**
     * @return the mShowArcs
     */
    public boolean isShowArcs() {
        return mShowArcs;
    }

    /**
     * @param mShowLines
     *            the mShowLines to set
     */
    public void setShowLines(boolean mShowLines) {
        this.mShowLines = mShowLines;
    }

    /**
     * @return the mShowLines
     */
    public boolean isShowLines() {
        return mShowLines;
    }

    /**
     * @param mInnerNodeArcScale
     *            the mInnerNodeArcScale to set
     */
    public void setInnerNodeArcScale(float mInnerNodeArcScale) {
        this.mInnerNodeArcScale = mInnerNodeArcScale;
    }

    /**
     * @return the mInnerNodeArcScale
     */
    public float getInnerNodeArcScale() {
        return mInnerNodeArcScale;
    }

    /**
     * @param mLeafArcScale
     *            the mLeafArcScale to set
     */
    public void setLeafArcScale(float mLeafArcScale) {
        this.mLeafArcScale = mLeafArcScale;
    }

    /**
     * @return the mLeafArcScale
     */
    public float getLeafArcScale() {
        return mLeafArcScale;
    }

    /**
     * @param mDotSize
     *            the mDotSize to set
     */
    public void setDotSize(float mDotSize) {
        this.mDotSize = mDotSize;
    }

    /**
     * @param mDotSize
     *            the mDotSize to set
     */
    public void mDotSize(float mDotSize) {
        this.mDotSize = mDotSize;
    }

    /**
     * @return the mDotSize
     */
    public float getDotSize() {
        return mDotSize;
    }

    /**
     * @param mDotBrightness
     *            the mDotBrightness to set
     */
    public void setDotBrightness(float mDotBrightness) {
        this.mDotBrightness = mDotBrightness;
    }

    /**
     * @return the mDotBrightness
     */
    public float getDotBrightness() {
        return mDotBrightness;
    }

    /**
     * @param mBackgroundBrightness
     *            the mBackgroundBrightness to set
     */
    public void setBackgroundBrightness(float mBackgroundBrightness) {
        this.mBackgroundBrightness = mBackgroundBrightness;
    }

    /**
     * @return the mBackgroundBrightness
     */
    public float getBackgroundBrightness() {
        return mBackgroundBrightness;
    }

    /**
     * @param mMappingMode the mMappingMode to set
     */
    public void setMappingMode(int mMappingMode) {
        this.mMappingMode = mMappingMode;
    }

    /**
     * @param mBuffer the mBuffer to set
     */
    public void setBuffer(PGraphics mBuffer) {
        this.mBuffer = mBuffer;
    }

    /**
     * @return the mBuffer
     */
    public PGraphics getBuffer() {
        return mBuffer;
    }
    
    public PApplet getParent() {
        return mParent;
    }

    /**
     * @param mSavePDF the mSavePDF to set
     */
    public void setSavePDF(boolean mSavePDF) {
        this.mSavePDF = mSavePDF;
    }

    /**
     * @return the mSavePDF
     */
    public boolean isSavePDF() {
        return mSavePDF;
    }

    /**
     * @param mShowGUI the mShowGUI to set
     */
    public void setShowGUI(boolean mShowGUI) {
        this.mShowGUI = mShowGUI;
    }

    /**
     * @return the mShowGUI
     */
    public boolean isShowGUI() {
        return mShowGUI;
    }
}
