package com.treetank.service.xml.shredder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treetank.TestHelper;
import com.treetank.TestHelper.PATHS;
import com.treetank.api.IDatabase;
import com.treetank.api.ISession;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.service.xml.serialize.XMLSerializer;
import com.treetank.service.xml.serialize.XMLSerializer.XMLSerializerBuilder;

/**
 * Test XMLUpdateShredder.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 */
public final class XMLUpdateShredderTest extends XMLTestCase {
    public static final String RESOURCES = "src" + File.separator + "test" + File.separator + "resources";

    public static final String XMLINSERT = RESOURCES + File.separator + "revXMLsInsert";

    public static final String XMLDELETE = RESOURCES + File.separator + "revXMLsDelete";

    public static final String XMLSAME = RESOURCES + File.separator + "revXMLsSame";

    public static final String XMLALL = RESOURCES + File.separator + "revXMLsAll";

    @Override
    @Before
    public void setUp() throws TreetankException {
        TestHelper.deleteEverything();
    }

    @Override
    @After
    public void tearDown() throws TreetankException {
        TestHelper.closeEverything();
    }

    @Test
    public void testSame() throws Exception {
        test(XMLSAME);
    }

    @Test
    public void testInserts() throws Exception {
        test(XMLINSERT);
    }

    @Test
    public void testDeletes() throws Exception {
        test(XMLDELETE);
    }

    @Test
    public void testAll() throws Exception {
        test(XMLALL);
    }

    private void test(final String FOLDER) throws Exception {
        final IDatabase database = TestHelper.getDatabase(PATHS.PATH1.getFile());
        final ISession session = database.getSession();
        final File folder = new File(FOLDER);
        int i = 1;
        final File[] filesList = folder.listFiles();
        final List<File> list = new ArrayList<File>();
        for (final File file : filesList) {
            if (file.getName().endsWith(".xml")) {
                list.add(file);
            }
        }

        // Sort files array according to file names.
        Collections.sort(list, new Comparator<Object>() {
            public int compare(final Object o1, final Object o2) {
                final String fileName1 =
                    ((File)o1).getName().toString()
                        .substring(0, ((File)o1).getName().toString().indexOf('.'));
                final String fileName2 =
                    ((File)o2).getName().toString()
                        .substring(0, ((File)o2).getName().toString().indexOf('.'));
                if (Integer.parseInt(fileName1) < Integer.parseInt(fileName2)) {
                    return -1;
                } else if (Integer.parseInt(fileName1) > Integer.parseInt(fileName2)) {
                    return +1;
                } else {
                    return 0;
                }
            }
        });

        boolean first = true;

        for (final File file : list) {
            if (file.getName().endsWith(".xml")) {
                final IWriteTransaction wtx = session.beginWriteTransaction();
                if (first) {
                    final XMLShredder shredder = new XMLShredder(wtx, XMLShredder.createReader(file), true);
                    shredder.call();
                    first = false;
                } else {
                    final XMLShredder shredder =
                        new XMLUpdateShredder(wtx, XMLShredder.createReader(file), true);
                    shredder.call();
                }
                assertEquals(i, wtx.getRevisionNumber());

                i++;
                wtx.moveToDocumentRoot();
                wtx.close();

                final OutputStream out = new ByteArrayOutputStream();
                final XMLSerializer serializer = new XMLSerializerBuilder(session, out).build();
                serializer.call();
                final StringBuilder sBuilder = TestHelper.readFile(file.getAbsoluteFile(), false);
                
                System.out.println(out.toString());
                
                assertXMLEqual(sBuilder.toString(), out.toString());
            }
        }
    }

}
