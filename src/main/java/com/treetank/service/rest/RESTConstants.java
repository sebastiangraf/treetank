package com.treetank.service.rest;

/**
 * Enum to store all params for the REST-Layer
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public enum RESTConstants {

    /** Content type of a Rest request */
    CONTENT_TYPE("application/xml", -1),
    /** Crossdomain policy */
    CROSSDOMAIN("<?xml version='1.0'?>"
            + "<!DOCTYPE cross-domain-policy SYSTEM "
            + "'http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd'>"
            + "<cross-domain-policy><allow-access-from domain='*' />"
            + "</cross-domain-policy>", -1),
    /** Size of return buffer */
    BUFFER_SIZE("", 8192),
    /** REST-GET Operator */
    GET("GET", -1),
    /** REST-POST Operator */
    POST("POST", -1),
    /** REST-PUT Operator */
    PUT("PUT", -1),
    /** REST-DELETE Operator */
    DELETE("DELETE", -1),
    /** PNG-Ending */
    PNG(".png", -1),
    /** JPEG-Ending */
    JPEG(".jpg", -1),
    /** GIF-Ending */
    GIF(".gif", -1),
    /** FLEX-Ending */
    FLEX(".swf", -1),
    /** JAVASCRIPT-Ending */
    JAVASCRIPT(".js", -1),
    /** STYLE-Ending */
    STYLE(".css", -1),
    /** Favicon-Path */
    FAVICONPATH("/favicon.ico", -1),
    /** CROSSDOMAIN-Path */
    CROSSDOMAINPATH("/crossdomain.xml", -1);

    /**
     * string content of the constant
     */
    private final String stringContent;

    /** int content of the constant */
    private final int intContent;

    /**
     * Constructor, just taking the string
     * 
     * @param stringParam
     *            the string
     * @param intParam
     *            integer parameter
     */
    private RESTConstants(final String stringParam, final int intParam) {
        this.stringContent = stringParam;
        this.intContent = intParam;
    }

    /**
     * Getter for the string content.
     * 
     * @return the string content of this enum-type
     */
    public String getStringContent() {
        return stringContent;
    }

    /**
     * Getter for the int content
     * 
     * @return the int content of this enum-type
     */
    public int getIntContent() {
        return intContent;
    }
}
