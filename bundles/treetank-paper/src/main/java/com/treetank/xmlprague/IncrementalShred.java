package com.treetank.xmlprague;

import java.io.File;
import java.util.Properties;

import com.treetank.access.Database;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.AbsTTException;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;

import org.perfidix.AbstractConfig;
import org.perfidix.Benchmark;
import org.perfidix.annotation.Bench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.ouput.AbstractOutput;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

public class IncrementalShred {

    public static File XMLFile = new File("");
    public static File TNKFolder = new File("tnk");


//    @Bench
//    public void benchNormal() {
//        try {
//            System.out.println("0 started");
//            final IDatabase database = Database.openDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
//            final ISession session = database.getSession();
//            final IWriteTransaction wtx = session.beginWriteTransaction();
//            final XMLShredderWithCommit shredderNone = new XMLShredderWithCommit(wtx, XMLShredder.createReader(XMLFile),0.0d);
//            shredderNone.call();
//            wtx.commit();
//            Database.forceCloseDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
//            System.out.println("0 ended");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Bench
//    public void bench100Commit() {
//        try {
//            System.out.println("100 started");
//            final IDatabase database = Database.openDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
//            final ISession session = database.getSession();
//            final IWriteTransaction wtx = session.beginWriteTransaction();
//            final XMLShredderWithCommit shredderNone = new XMLShredderWithCommit(wtx, XMLShredder.createReader(XMLFile),1.0d);
//            shredderNone.call();
//            wtx.commit();
//            Database.forceCloseDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
//            System.out.println("100 ended");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
    @Bench
    public void bench75Commit() {
        try {
            System.out.println("75 started");
            final IDatabase database = Database.openDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            final XMLShredderWithCommit shredderNone = new XMLShredderWithCommit(wtx, XMLShredder.createReader(XMLFile),0.75d);
            shredderNone.call();
            wtx.commit();
            Database.forceCloseDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
            System.out.println("75 ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bench
    public void bench25Commit() {
        try {
            System.out.println("25 started");
            final IDatabase database = Database.openDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            final XMLShredderWithCommit shredderNone = new XMLShredderWithCommit(wtx, XMLShredder.createReader(XMLFile),0.25d);
            shredderNone.call();
            wtx.commit();
            Database.forceCloseDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
            System.out.println("25 ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Bench
    public void bench50Commit() {
        try {
            System.out.println("50 started");
            final IDatabase database = Database.openDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
            final ISession session = database.getSession();
            final IWriteTransaction wtx = session.beginWriteTransaction();
            final XMLShredderWithCommit shredderNone = new XMLShredderWithCommit(wtx, XMLShredder.createReader(XMLFile),0.50d);
            shredderNone.call();
            wtx.commit();
            Database.forceCloseDatabase(new File(TNKFolder, XMLFile.getName() + ".tnk"));
            System.out.println("50 ended");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   

    public static void main(final String[] args) {

        if (args.length != 2) {
            System.out
                .println("Please use java -jar JAR \"folder with xmls to parse\" \"folder to write csv\"");
            System.exit(-1);
        }

        // Argument is a folder with only XML in there. For each XML one benchmark should be executed.
        final File filetoshred = new File(args[0]);
        final File[] files = filetoshred.listFiles();
        final File filetoexport = new File(args[1]);
        for (final File currentFile : files) {
            XMLFile = currentFile;
            System.out.println("Starting benchmark for " + XMLFile.getName());
            final int index = currentFile.getName().lastIndexOf(".");
            final File folder = new File(filetoexport, currentFile.getName().substring(0, index));
            folder.mkdirs();
            final FilesizeMeter meter =
                new FilesizeMeter(new File(new File(new File(TNKFolder, XMLFile.getName() + ".tnk"), "tt"),
                    "tt.tnk"));

            final Benchmark bench = new Benchmark(new AbstractConfig(1, new AbstractMeter[] {
                meter, new TimeMeter(Time.MilliSeconds)
            }, new AbstractOutput[0], KindOfArrangement.SequentialMethodArrangement, 1.0d) {
            });

            bench.add(IncrementalShred.class);
            final BenchmarkResult res = bench.run();
            new TabularSummaryOutput(System.out).visitBenchmark(res);
            new CSVOutput(folder).visitBenchmark(res);
            System.out.println("Finished benchmark for " + XMLFile.getName());
        }

    }

}
