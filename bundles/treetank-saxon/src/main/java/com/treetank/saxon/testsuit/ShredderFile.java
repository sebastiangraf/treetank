package com.treetank.saxon.testsuit;

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
 * <h1>Shredder file.</h1>
 * 
 * <p>Shredder a single file.</p>
 * 
 * @author johannes
 *
 */
public final class ShredderFile {

  /** Logger. */
  private static final Log LOGGER = LogFactory.getLog(ShredderFile.class);
  
  /**
   * Main method. 
   * 
   * @param args <ul>
   *               <li>First param: Source directory.</li>
   *               <li>Second param: Target directory.</li>
   *             </ul>
   */
  public static void main(final String[] args) {
    if (args.length != 2) {
      throw new IllegalStateException(
          "You have to specify source file and the target TNK!");
    }
    
    final File source = new File(args[0]);
    final File target = new File(args[1]);
    
    shredder(source, target);
  }
  
  /**
   * Shredder an XML file into a Treetank storage.
   * 
   * @param source XML source file.
   * @param target TNK target file/directory.
   */
  public static void shredder(final File source, final File target) {
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
