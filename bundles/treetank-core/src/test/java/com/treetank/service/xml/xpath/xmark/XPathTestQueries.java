package com.treetank.service.xml.xpath.xmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.access.Database;
import com.treetank.access.DatabaseConfiguration;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TTException;
import com.treetank.exception.TTIOException;
import com.treetank.exception.TTUsageException;
import com.treetank.service.xml.shredder.EShredderInsert;
import com.treetank.service.xml.shredder.XMLShredder;
import com.treetank.service.xml.xpath.XPathAxis;
import com.treetank.settings.EDatabaseSetting;
import com.treetank.utils.TypedValue;

public class XPathTestQueries {

    final private static String XMLFILE = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "factbook.xml";

    final private static File OUTPUTFILE = new File(File.separatorChar + "tmp" + File.separatorChar + "tt"
        + File.separatorChar + "factbook.tnk");

    final private static String QUERY = "//country/name";

    public void executeQuery() {
        IReadTransaction rtx = null;
        IDatabase db = null;
        ISession session = null;

        try {
            db = Database.openDatabase(OUTPUTFILE);
            session = db.getSession();
            rtx = session.beginReadTransaction();

            IAxis axis = new XPathAxis(rtx, QUERY);
            int resultSize = 0;
            while (axis.hasNext()) {
                axis.next();
                resultSize++;
                System.out.println(rtx.getNode().getRawValue());
            }
            System.out.println("Gefundene Ergebnisse: " + resultSize);
            rtx.close();
            session.close();
            db.close();

        } catch (TTException e) {
            e.printStackTrace();
        }

    }

    public void shredXML() {
        long startTime = System.currentTimeMillis();

        IWriteTransaction wtx = null;
        IDatabase database = null;
        ISession session = null;

        try {

            final Properties dbProps = new Properties();
            dbProps.setProperty(EDatabaseSetting.REVISION_TO_RESTORE.name(), "1");
            final DatabaseConfiguration conf = new DatabaseConfiguration(OUTPUTFILE, dbProps);

            Database.createDatabase(conf);
            database = Database.openDatabase(OUTPUTFILE);
            session = database.getSession();
            wtx = session.beginWriteTransaction();
            wtx.moveToDocumentRoot();
            final boolean exist = wtx.moveToFirstChild();
            if (exist) {
                wtx.remove();
                wtx.commit();
            }

            final XMLEventReader reader = createReader(new File(XMLFILE));
            final XMLShredder shredder = new XMLShredder(wtx, reader, EShredderInsert.ADDASFIRSTCHILD);
            shredder.call();

            wtx.close();
            session.close();
            database.close();
            long endTime = System.currentTimeMillis();

            System.out
                .println("Datenbank in " + ((endTime - startTime) / 1000) + " sec erfolgreich angelegt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized XMLEventReader createReader(final File paramFile) throws IOException,
        XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        final InputStream in = new FileInputStream(paramFile);
        return factory.createXMLEventReader(in);
    }

    public static void main(String[] args) {

        XPathTestQueries xptq = new XPathTestQueries();
        xptq.shredXML();
        xptq.executeQuery();
    }

}
