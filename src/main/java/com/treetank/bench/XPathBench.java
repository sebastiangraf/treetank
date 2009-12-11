package com.treetank.bench;

import java.io.File;

import com.treetank.access.Database;
import com.treetank.api.IAxis;
import com.treetank.api.IDatabase;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.service.xml.XMLShredder;
import com.treetank.service.xml.xpath.XPathParser;

public class XPathBench {

    private final static String FILENAME = "shakespeare";
    private final static File XML = new File("src" + File.separator + "test"
            + File.separator + "resources" + File.separator + FILENAME + ".xml");
    private final static File TNK = new File("target" + File.separator + "TNK"
            + File.separator + FILENAME + ".tnk");
    private final static File INDEXTNK = new File("target" + File.separator
            + "TNK" + File.separator + "index" + FILENAME + ".tnk");

    public XPathBench() {
    }

    public void query() {
        final String query = "//ACT";
        try {
            INDEXTNK.delete();
            if (!TNK.exists()) {
                final String[] args = { XML.getAbsolutePath(),
                        TNK.getAbsolutePath() };
                XMLShredder.main(args);
            }

            final IDatabase db = Database.openDatabase(TNK);

            final ISession session = db.getSession();
            final IReadTransaction rtx = session.beginReadTransaction();

            final XPathParser parser = new XPathParser(rtx, query);
            parser.parseQuery();
            final IAxis axis = parser.getQueryPipeline();

            while (axis.hasNext()) {
                axis.next();
                System.out.println(rtx.getNameOfCurrentNode());
            }

            rtx.close();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting query");
        new XPathBench().query();
        System.out.println("Ending query");
    }
}
