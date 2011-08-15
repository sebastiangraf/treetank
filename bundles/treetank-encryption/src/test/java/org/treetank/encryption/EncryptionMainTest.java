package org.treetank.encryption;

import java.io.File;

import org.treetank.EncryptionHelper;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.axis.AbsAxis;
import org.treetank.exception.AbsTTException;
import org.treetank.service.xml.shredder.XMLShredder;
import org.treetank.service.xml.xpath.XPathAxis;
import org.treetank.service.xml.xpath.XPathStringChecker;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EncryptionMainTest {


        private static final String XML = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "auction.xml";

        private static Holder holder;
        
        private static EncryptionHelper enHelper;

        @Before
        public void setUp() throws Exception {
            TestHelper.deleteEverything();
          
            enHelper = EncryptionHelper.start();
            XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
            holder = Holder.generateRtx();
        }

        @After
        public void tearDown() throws AbsTTException {
            holder.close();
            enHelper.close();
            TestHelper.closeEverything();
        }

        
        @Test
        public void executeEncryption() throws AbsTTException {
            
            enHelper.getManager().joinGroup("U5", "C");     
            enHelper.getManager().leaveGroup("U1", "D");
            enHelper.getManager().joinGroup("U6", "D");
            enHelper.getManager().leaveGroup("U5", "C");
            enHelper.getManager().joinGroup("U7", "F");

            AbsAxis axis =
                new XPathAxis(holder.getRtx(),
                    "/site/people/person[@id=\"person0\"]/name/text()");

            XPathStringChecker.testIAxisConventions(axis, new String[] {
                "Sinisa Farrel"
            });
        }

    

}
