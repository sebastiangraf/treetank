package com.treetank.saxon.helper;

import java.io.File;

import javax.xml.stream.XMLEventReader;

import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.service.xml.XMLShredder;

/**
 * Shredder XML files in a directory.
 * 
 * @author lichtenb
 *
 */
public final class CreateTNKs {
  /**
   * Main method. 
   * 
   * @param args First param: Source directory. Second param: Shredder recursively?
   */
  public static void main(final String[] args) {
    if (args.length != 1 || args.length != 2) {
      throw new IllegalStateException("Needs at least a source directory!");
    }

    final File source = new File(args[0]);

    if (source.isDirectory()) {
      throw new IllegalStateException("Source needs to be a directory!");
    }

    traverse(source);
  }

  /**
   * Recursively traverse directory.
   * 
   * @param file Source directory.
   */
  private static void traverse(final File file) {
    final File source = file;

    for (final File mFile : source.listFiles()) {
      if (mFile.getAbsolutePath().endsWith(".xml")) {
//        shredder(mFile);
      }
    }
  }

//  private static void shredder(final File file) {
//    
//    final File target = file.getAbsolutePath();
//    Database.truncateDatabase(target);
//    Database.createDatabase(new DatabaseConfiguration(target));
//    final IDatabase db = Database.openDatabase(target);
//    final ISession session = db.getSession();
//    final IWriteTransaction wtx = session.beginWriteTransaction();
//    final XMLEventReader reader = createReader(new File(args[0]));
//    final XMLShredder shredder = new XMLShredder(wtx, reader, true);
//    shredder.call();
//
//    wtx.close();
//    session.close();
//    db.close();
//  }
}
