package com.treetank.service.xml;

import java.io.File;
import java.util.List;

/**
 * <h1>AbsXMLImport</h1>
 * 
 * <p>
 * XML specific methods an import class has to implement.
 * </p>
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 *
 */
public abstract class AbsXMLImport implements IImport {
  /**
   * Check several XML files for one or more timestamp objects and shredder 
   * every file into a new revision.
   * 
   * @param list
   *            List of files.
   */
  abstract void check(final List<File> list);
}
