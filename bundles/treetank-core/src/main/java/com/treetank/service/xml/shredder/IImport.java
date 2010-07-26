package com.treetank.service.xml.shredder;

/**
 * <h1>IImport</h1>
 * 
 * <p>
 * Interface to provide convenient methods which all classes have to implement which shredder revisioned
 * databases into Treetank.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public interface IImport {
    /**
     * Check one database for one or more timestamp objects and shredder several
     * revision.
     * 
     * @param database
     *            Database to import.
     * @param obj
     *            Timestamp object.
     */
    void check(final Object database, final Object obj);
}
