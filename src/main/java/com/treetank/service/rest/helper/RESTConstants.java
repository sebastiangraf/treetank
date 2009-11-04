package com.treetank.service.rest.helper;

/**
 * Enum to store all params for the REST-Layer
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum RESTConstants {

    /** Content type of a Rest request */
    CONTENT_TYPE("application/xml"),
    /** Crossdomain policy */
    CROSSDOMAIN("<?xml version='1.0'?>"
            + "<!DOCTYPE cross-domain-policy SYSTEM "
            + "'http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd'>"
            + "<cross-domain-policy><allow-access-from domain='*' />"
            + "</cross-domain-policy>");

    /**
     * content of the constant
     */
    private final String content;

    /**
     * Constructor, just taking the string
     * 
     * @param param
     *            the string
     */
    private RESTConstants(final String param) {
        this.content = param;
    }

    /**
     * Getter for the content.
     * 
     * @return the content of this enum-type
     */
    public String getContent() {
        return content;
    }
}
