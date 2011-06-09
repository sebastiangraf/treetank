/**
 * 
 */
package org.treetank.gui.view.sunburst.control;

import javax.xml.stream.XMLStreamException;

import controlP5.ControlEvent;
import controlP5.ControlListener;

import org.treetank.gui.controls.IControl;

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
