/**
 * 
 */
package org.treetank.gui.view.sunburst.control;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import javax.xml.stream.XMLStreamException;

import controlP5.ControlEvent;
import controlP5.ControlListener;

import org.treetank.gui.view.controls.IControl;

/**
 * Interface for SunburstControllers which adds specific methods to {@link IControl}.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public interface ISunburstControl extends IControl, ControlListener {
    /**
     * Method to process event for submit-button.
     * 
     * @param paramValue
     *            change value
     * @throws XMLStreamException
     *             if the XML fragment isn't well formed
     */
   void submit(final int paramValue) throws XMLStreamException;
   
   /**
    * Method to process event for commit-button.
    * 
    * @param paramValue
    *            change value
    * @throws XMLStreamException
    *             if the XML fragment isn't well formed
    */
   void commit(final int paramValue) throws XMLStreamException;
   
   /**
    * Method to process event for cancel-button.
    * 
    * @param paramValue
    *            change value
    */
   void cancel(final int paramValue);
}
