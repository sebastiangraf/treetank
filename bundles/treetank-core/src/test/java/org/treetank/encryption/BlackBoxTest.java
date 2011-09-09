package org.treetank.encryption;

import java.io.File;

import org.treetank.EncryptionHelper;
import org.treetank.Holder;
import org.treetank.TestHelper.PATHS;
import org.treetank.axis.AbsAxis;
import org.treetank.encrpytion.exception.TTEncryptionException;
import org.treetank.exception.TTUsageException;
import org.treetank.exception.TTXPathException;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathStringChecker;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class BlackBoxTest {
/*
    private static EncryptionHelper enHelper;

    private static Holder holder;

    private static final String XML = "src" + File.separator + "test"
        + File.separator + "resources" + File.separator + "encrypttest.xml";

    @Before
    public void setUp() throws Exception {
        enHelper = new EncryptionHelper();
        enHelper.setEncryption(true);
        enHelper.start();

        // add user1 ->ROOT->Disy->user1
        enHelper.getManager().join(new String[] {
            "User1"
        }, new String[] {
            "Disy"
        }, "ALL");

        // add user2 ->ROOT->Dbis->user2
        enHelper.getManager().join(new String[] {
            "User2"
        }, new String[] {
            "Dbis"
        }, "ALL");

    }

    @After
    public void tearDown() {
        enHelper.setEncryption(false);
        enHelper.close();
    }

    @Ignore
    @Test
    public void testUser1() throws Exception {

        enHelper.setSessionUser("User1");
        System.out.println("user: " + enHelper.getController().getUser());
        enHelper.setDek(2);

        XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
        holder = Holder.generateRtx();

        AbsAxis axis = new XPathAxis(holder.getRtx(), "/root/disy");

        XPathStringChecker.testIAxisConventions(axis, new String[] {
            "disy"
        });

    }

    @Ignore
    @Test(expected = TTUsageException.class)
    public void testUser2() throws TTEncryptionException, TTXPathException,
        Exception {

        enHelper.setSessionUser("User2");
        System.out.println("user: " + enHelper.getController().getUser());
        enHelper.setDek(2);

        XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
        holder = Holder.generateRtx();

        AbsAxis axis2 = new XPathAxis(holder.getRtx(), "/root/disy");

        XPathStringChecker.testIAxisConventions(axis2, new String[] {
            "disy"
        });

    }*/

}
