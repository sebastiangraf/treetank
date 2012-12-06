package org.treetank.access.conf;

/**
 * 
 * Properties for handing values over the constructor through guice.
 * The properties are core-wide and influence different packages. 
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public interface ContructorProps {

    public static final String STORAGEPATH = "FILENAME";
    
    public static final String RESOURCE = "RESOURCE";

    public static final String JCLOUDSTYPE = "JCLOUDS";
    
    public static final String NUMBERTORESTORE = "NUMBERSTORESTORE";
    
}
