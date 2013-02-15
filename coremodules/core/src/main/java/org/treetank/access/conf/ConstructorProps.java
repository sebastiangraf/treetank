package org.treetank.access.conf;

/**
 * 
 * Properties for handing values over the constructor through guice.
 * The properties are core-wide and influence different packages. 
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public interface ConstructorProps {

    public static final String STORAGEPATH = "treetank.storagepath";
    
    public static final String RESOURCE = "treetank.resource";
    
    public static final String RESOURCEPATH = "treetank.resourcepath";

    public static final String JCLOUDSTYPE = "treetank.jclouds";
    
    public static final String NUMBERTORESTORE = "treetank.numbersofrestore";
    
}
