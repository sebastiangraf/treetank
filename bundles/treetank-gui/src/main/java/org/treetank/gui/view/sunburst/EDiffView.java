/**
 * 
 */
package org.treetank.gui.view.sunburst;

/**
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public enum EDiffView {
    DIFF(false),
    
    NODIFF(false);
    
    private transient boolean mValue;

    EDiffView(final boolean paramValue) {
        mValue = paramValue;
    }
    
    public boolean getValue() {
        return mValue;
    }
    
    public void setValue(final boolean paramValue) {
        mValue = paramValue;
    }
}
