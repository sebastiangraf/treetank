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

package org.treetank.gui.view.sunburst;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlGroup;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.DropdownList;
import controlP5.Range;
import controlP5.Slider;
import controlP5.Textfield;
import controlP5.Toggle;

import org.gicentre.utils.move.ZoomPan;
import org.gicentre.utils.move.ZoomPanListener;
import org.treetank.diff.DiffFactory.EDiff;
import org.treetank.exception.AbsTTException;
import org.treetank.gui.ReadDB;
import org.treetank.gui.view.IProcessingGUI;
import org.treetank.gui.view.IVisualItem;
import org.treetank.gui.view.ViewUtilities;
import org.treetank.gui.view.sunburst.EDraw.EDrawSunburst;
import org.treetank.gui.view.sunburst.SunburstView.Embedded;
import org.treetank.gui.view.controls.IControl;
import org.treetank.gui.view.model.AbsModel;
import org.treetank.gui.view.model.IModel;
import org.treetank.gui.view.sunburst.control.ISunburstControl;
import org.treetank.gui.view.sunburst.model.SunburstCompareModel;
import org.treetank.gui.view.sunburst.model.SunburstModel;

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
public class SunburstGUI extends AbsSunburstGUI implements PropertyChangeListener {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -4747210906900567484L;

    /** Amount of effect in the fisheye transformation. */
    private static final float EFFECT_AMOUNT = 0.9f;

    /** The GUI of the Sunburst view. */
    private static volatile SunburstGUI mGUI;

    /** Current angle of the mouse cursor to y axis. */
    transient float mAngle;

    /** Current depth of the mouse cursor. */
    transient int mDepth;

    /** Determines if zooming or panning is resetted. */
    transient boolean mZoomPanReset;

    /** Determines if fisheye should be used. */
    transient boolean mFisheye;

    /** X position of the mouse cursor. */
    private transient float mX;

    /** Y position of the mouse cursor. */
    private transient float mY;

    /** {@link ControlP5} text field. */
    transient Textfield mXPathField;

    /** Determines if model has done the work. */
    volatile boolean mDone;

    /** Item, which is currently clicked. */
    private transient SunburstItem mHitItem;

    /** Hit test index. */
    transient int mHitTestIndex = -1;

    /** {@link DropdownList} of available revisions, which are newer than the currently opened revision. */
    volatile DropdownList mRevisions;

    /** {@link ControlGroup} to encapsulate the components to insert XML fragments. */
    transient ControlGroup mCtrl;

    /** {@link Textfield} to insert an XML fragment. */
    transient Textfield mTextArea;

    /** Determines if it is currently zooming or panning or has been in the past. */
    private transient boolean mIsZoomingPanning;

    /** Determines if pruning should be enabled or not. */
    transient boolean mUsePruning;

    /** Determines if GUI has been initialized. */
    transient boolean mInitialized;

    transient boolean mRadChanged;

    /**
     * Private constructor.
     * 
     * @param paramApplet
     *            parent processing applet
     * @param paramReadDB
     *            read database
     */
    private SunburstGUI(final PApplet paramApplet, final ISunburstControl paramControl,
        final ReadDB paramReadDB) {
        super(paramApplet, paramControl, paramReadDB);
        mDb = paramReadDB;
    }

    /**
     * Factory method (Singleton). Note that it's always called from the animation thread, thus it doesn't
     * need to be synchronized.
     * 
     * @param paramParentApplet
     *            parent processing applet
     * @param paramControl
     *            associated controller
     * @param paramReadDB
     *            read database
     * @return a {@link SunburstGUI} singleton
     */
    public static SunburstGUI getInstance(final PApplet paramParentApplet,
        final ISunburstControl paramControl, final ReadDB paramReadDB) {
        if (mGUI == null) {
            synchronized (SunburstGUI.class) {
                if (mGUI == null) {
                    mGUI = new SunburstGUI(paramParentApplet, paramControl, paramReadDB);
                }
            }
        }
        return mGUI;
    }

    /** {@inheritDoc} */
    @Override
    protected void setup() {
        final Toggle toggleArc =
            getControlP5().addToggle("mUseArc", mUseArc, LEFT + 0, TOP + mPosY + 60, 15, 15);
        toggleArc.setLabel("Arc / Rect");
        mToggles.add(toggleArc);
        final Toggle toggleFisheye =
            getControlP5().addToggle("mFisheye", mFisheye, LEFT + 0, TOP + mPosY + 80, 15, 15);
        toggleFisheye.setLabel("Fisheye lense");
        mToggles.add(toggleFisheye);
        final Toggle togglePruning =
            getControlP5().addToggle("mUsePruning", mUsePruning, LEFT + 0, TOP + mPosY + 100, 15, 15);
        togglePruning.setLabel("Pruning");
        mToggles.add(togglePruning);

        mXPathField = getControlP5().addTextfield("xpath", mParent.width - 250, TOP + 20, 200, 20);
        mXPathField.setLabel("XPath expression");
        mXPathField.setFocus(false);
        mXPathField.setAutoClear(false);
        mXPathField.setColorBackground(mParent.color(0)); // black
        mXPathField.setColorForeground(mParent.color(255)); // white
        mXPathField.plugTo(this);

        // Add textfield for XML fragment input.
        mCtrl = getControlP5().addGroup("add XML fragment", 150, 25, 115);
        mCtrl.setVisible(false);
        mCtrl.close();

        mTextArea = getControlP5().addTextfield("Add XML fragment", 0, 20, 400, 100);
        mTextArea.setColorBackground(mParent.color(0)); // black
        mTextArea.setColorForeground(mParent.color(255)); // white
        mTextArea.setGroup(mCtrl);

        final Button submit = getControlP5().addButton("submit", 20, 0, 140, 80, 19);
        submit.plugTo(this);
        submit.setGroup(mCtrl);
        final Button commit = getControlP5().addButton("commit", 20, 120, 140, 80, 19);
        commit.plugTo(this);
        commit.setGroup(mCtrl);
        final Button cancel = getControlP5().addButton("cancel", 20, 240, 140, 80, 19);
        cancel.plugTo(this);
        cancel.setGroup(mCtrl);
    }

    /**
     * Implements the {@link PApplet} draw() method.
     */
    @Override
    public void draw() {
        if (getControlP5() != null) {
            mParent.pushMatrix();

            if (getZoomer().isZooming() || getZoomer().isPanning()) {
                mIsZoomingPanning = true;
            }

            // This enables zooming/panning.
            getZoomer().transform();

            mParent.colorMode(PConstants.HSB, 360, 100, 100, 100);
            mParent.noFill();
            mParent.ellipseMode(PConstants.RADIUS);
            mParent.strokeCap(PConstants.SQUARE);
            mParent.textLeading(14);
            mParent.textAlign(PConstants.LEFT, PConstants.TOP);
            mParent.smooth();

            if (mIsZoomingPanning || isSavePDF() || mFisheye) {
                // LOGWRAPPER.debug("Without buffered image!");
                mParent.background(0, 0, getBackgroundBrightness());
                mParent.translate((float)mParent.width / 2f, (float)mParent.height / 2f);
                mParent.rotate(PApplet.radians(mRad));
                if (mDone) {
                    System.out.println(mParent.g.width);
                    System.out.println(mParent.g.height);
                    drawItems(EDraw.DRAW);
                }
                getBuffer().stroke(0);
                getBuffer().strokeWeight(2f);
                getBuffer().line(0, 0, mParent.width, 0);
            } else if (mDone) {
                // LOGWRAPPER.debug("Buffered image!");

                if (mRadChanged) {
                    mRadChanged = false;
                    update();
                }

                try {
                    mLock.acquire();
                    mParent.image(mImg, 0, 0);
                } catch (final InterruptedException exc) {
                    exc.printStackTrace();
                } finally {
                    mLock.release();
                    // LOGWRAPPER.debug("[draw()]: Available permits: " + mLock.availablePermits());
                }
                mParent.translate((float)mParent.width / 2f, (float)mParent.height / 2f);
            }

            // Mouse rollover.
            if (!isShowGUI() && !mCtrl.isVisible() && mDone) {
                boolean doMouseOver = true;
                if (mRevisions != null && mRevisions.isOpen()) {
                    doMouseOver = false;
                }

                if (doMouseOver) {
                    // Mouse rollover, arc hittest vars.
                    final boolean itemHit = rollover();
                    if (itemHit) {
                        ((Embedded) mParent).getView().hover(mControl.getModel().getItem(mHitTestIndex));
                    }
                    mParent.pushMatrix();
                    if (mHitItem != null) {
                        if (!mIsZoomingPanning && !isSavePDF() && !mFisheye) {
                            mParent.rotate(PApplet.radians(mRad));
                        }
                        EDraw.DRAW.drawHover(this, mHitItem);
                    }
                    mParent.popMatrix();

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
                    if (mIsZoomingPanning || isSavePDF() || mFisheye) {
                        mParent.rotate(-PApplet.radians(mRad));
                    }
                    textMouseOver();
                }
            }

            // Fisheye view.
            if (mDone && mFisheye && !isSavePDF()) { // In PDF mode cannot make pixel based transformations.
                // Fisheye transormation.
                fisheye(mParent.mouseX, mParent.mouseY, 120);
            }

            if (mZoomPanReset) {
                update();
                mZoomPanReset = false;
                mIsZoomingPanning = false;
            }

            mParent.popMatrix();

            mParent.translate(0, 0);
            mParent.strokeWeight(0);
            if (mUseDiffView) {
                ViewUtilities.compareLegend(this, mParent);
            } else {
                ViewUtilities.color(this, mParent);
                mParent.text("Press 'o' to get a list of revisions to compare!", mParent.width - 300f,
                    mParent.height - 50f);
            }

            ViewUtilities.legend(this, mParent);
            ViewUtilities.drawGUI(getControlP5());
        }
    }

    /**
     * Mouse over to display text.
     */
    private void textMouseOver() {
        if (mHitTestIndex != -1) {
            String text = mHitItem.toString();

            int lines = 1;
            int chars = 0;
            for (final char c : text.toCharArray()) {
                if (c == '\n') {
                    lines++;
                    chars = 0;
                }
                chars++;
            }

            if (chars > 80) {
                final StringBuilder builder = new StringBuilder().append("[Depth: ").append(mDepth);
                if (mHitItem.mQName != null) {
                    builder.append(" QName: ")
                        .append(ViewUtilities.qNameToString(mHitItem.mQName).substring(0, 20)).append("...");
                } else {
                    builder.append(" Text: ").append(mHitItem.mText.substring(0, 20)).append("...");
                }
                if (mUseDiffView) {
                    mHitItem.updated(builder);
                }
                builder.append(" NodeKey: ").append(mHitItem.getNode().getNodeKey()).append("]");
                text = builder.toString();
            }

            final int offset = 5;
            final float textW = mParent.textWidth(text) + 10f;
            mParent.fill(0, 0, 0);
            if (mX + offset + textW > mParent.width / 2) {
                // Exceeds right window border, thus align to the left of the current mouse location.
                mParent.rect(mX - textW + offset, mY + offset, textW,
                    (mParent.textAscent() + mParent.textDescent()) * lines + 4);
                mParent.fill(0, 0, 100);
                mParent.text(text, mX - textW + offset + 2, mY + offset + 2);
            } else {
                // Align to the right of the current mouse location.
                mParent.rect(mX + offset, mY + offset, textW, (mParent.textAscent() + mParent.textDescent())
                    * lines + 4);
                mParent.fill(0, 0, 100);
                mParent.text(text, mX + offset + 2, mY + offset + 2);
            }
        }
    }

    /** Initialize rollover method. */
    private void rolloverInit() {
        mHitTestIndex = -1;

        final PVector mousePosition = getZoomer().getMouseCoord();
        mX = mousePosition.x - mParent.width / 2;
        mY = mousePosition.y - mParent.height / 2;

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
    private void fisheye(final int paramXPos, final int paramYPos, final int paramRadius) {
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
     * Rollover test.
     * 
     * @return true, if found, false otherwise
     */
    boolean rollover() {
        boolean retVal = false;
        mHitTestIndex = -1;
        mHitItem = null;

        rolloverInit();
        int index = 0;
        @SuppressWarnings("unchecked")
        final Iterable<SunburstItem> items = (Iterable<SunburstItem>)mControl.getModel();
        for (final IVisualItem visualItem : items) {
            final SunburstItem item = (SunburstItem)visualItem;
            // Hittest, which arc is the closest to the mouse.
            if (item.getDepth() == mDepth && mAngle > item.getAngleStart() + PApplet.radians(mRad)
                && mAngle < item.getAngleEnd() + PApplet.radians(mRad)) {
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
     * Method to process event for cancel-button.
     * 
     * @param paramValue
     *            change value
     */
    public void cancel(final int paramValue) {
        mControl.cancel(paramValue);
    }

    /**
     * Method to process event for submit-button.
     * 
     * @param paramValue
     *            change value
     * @throws FactoryConfigurationError
     *             if something odd happens
     * @throws XMLStreamException
     *             if the XML fragment isn't well formed
     */
    public void submit(final int paramValue) throws XMLStreamException {
        mControl.submit(paramValue);
    }

    /**
     * Method to process event for submit-button.
     * 
     * @param paramValue
     *            change value
     * @throws XMLStreamException
     *             if the XML fragment isn't well formed
     */
    public void commit(final int paramValue) throws XMLStreamException {
        mControl.commit(paramValue);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(final PropertyChangeEvent paramEvent) {
        if (paramEvent.getPropertyName().equals("maxDepth")) {
            assert paramEvent.getNewValue() instanceof Integer;
            mDepthMax = (Integer)paramEvent.getNewValue();

            if (mUseDiffView) {
                mDepthMax += 2;
            }
        } else if (paramEvent.getPropertyName().equals("oldMaxDepth")) {
            assert paramEvent.getNewValue() instanceof Integer;
            mOldDepthMax = (Integer)paramEvent.getNewValue();
            mUseDiffView = true;
        } else if (paramEvent.getPropertyName().equals("done")) {
            update();
            assert paramEvent.getNewValue() instanceof Boolean;
            mDone = true;
        }
    }

    /**
     * Set fisheye lense.
     * 
     * @param paramState
     *            true if fisheye lense should be used, false otherwise
     */
    public void setFisheye(final boolean paramState) {
        mFisheye = paramState;
    }

    /**
     * Set use arcs.
     * 
     * @param paramState
     *            true if arcs should be used, false otherwise
     */
    public void setUseArc(final boolean paramState) {
        mUseArc = paramState;
    }

    /**
     * Set pruning.
     * 
     * @param paramState
     *            true if pruning should be used, false otherwise
     */
    public void setPruning(final boolean paramState) {
        mUsePruning = paramState;
    }
    
    /** {@inheritDoc} */
    @Override
    public void relocate() {
        mXPathField.setPosition(mParent.width - 250, TOP + 20);
        mRevisions.setPosition(mParent.width - 250, 100);
    }
}
