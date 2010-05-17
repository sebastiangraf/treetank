package com.treetank.saxon.helper;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.exception.TreetankIOException;
import com.treetank.service.xml.XMLShredder;

/**
 * <h1>Shredder files.</h1>
 * 
 * <p>Shredder all XML files in a directory either recursively within 
 * subdirectories or not.</p>
 * 
 * @author lichtenb
 *
 */
public final class ShredderFiles {

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(ShredderFiles.class);

  /**
   * Main method. 
   * 
   * @param args <ul>
   *               <li>First param: Source directory.</li>
   *               <li>Second param: Shredder recursively?</li>
   *             </ul>
   */
  public static void main(final String[] args) {
    if (args.length != 1 && args.length != 2) {
      throw new IllegalStateException("Needs at least a source directory!");
    }
    
    final File source = new File(args[0]);
    boolean recursive = false;
    
    if (args.length == 2) {
      recursive = Boolean.parseBoolean(args[1]);
    }

    if (!source.isDirectory()) {
      throw new IllegalStateException("Source needs to be a directory!");
    }
    
    traverse(source, recursive);
  }

  /**
   * Traverse directory.
   * 
   * @param file Source directory.
   * @param recursive Recursivly process directory?
   */
  private static void traverse(final File file, final boolean recursive) {
    final File source = file;

    for (final File mFile : source.listFiles()) {
      if (mFile.isFile() && mFile.getAbsolutePath().endsWith(".xml")) {
        shredder(mFile);
      } else if (mFile.isDirectory()) {
        traverse(mFile, recursive);
      }
    }
  }

  /**
   * Shredder file into Treetank.
   * 
   * @param file File to shredder.
   */
  private static void shredder(final File file) {
    final File source = file.getAbsoluteFile();
    final File target =
        new File(source.toString().substring(0, source.toString().length()-4));

    Database.truncateDatabase(target);
    try {
      Database.createDatabase(new DatabaseConfiguration(target));

      final IDatabase db = Database.openDatabase(target);
      final ISession session = db.getSession();
      final IWriteTransaction wtx = session.beginWriteTransaction();
      final XMLEventReader reader = XMLShredder.createReader(source);
      final XMLShredder shredder = new XMLShredder(wtx, reader, true);
      shredder.call();
      wtx.close();
      session.close();
      db.close();
    } catch (TreetankIOException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (TreetankException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (XMLStreamException e) {
      LOGGER.error(e.getMessage(), e);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
  }
}
