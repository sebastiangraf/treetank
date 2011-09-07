package org.treetank.encryption;

import java.io.File;

import org.treetank.EncryptionHelper;
import org.treetank.Holder;
import org.treetank.TestHelper;
import org.treetank.TestHelper.PATHS;
import org.treetank.axis.AbsAxis;
import org.treetank.encrpytion.exception.TTEncryptionException;
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
          
            enHelper = new EncryptionHelper();
            enHelper.setEncryption(true);
            enHelper.start();
            XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
            holder = Holder.generateRtx();
            
            enHelper.setSessionUser(holder.getSession().getUser());
        }

        @After
        public void tearDown() throws AbsTTException {
            holder.close();
            enHelper.setEncryption(false);
            enHelper.close();
            TestHelper.closeEverything();
        }


        @Test
        public void executeEncryption() throws AbsTTException, TTEncryptionException  {
            
            enHelper.getManager().join(new String[]{"User1"}, new String[]{"Inf", "Disy"}, "ALL");
            enHelper.getManager().join(new String[]{"User2"}, new String[]{"Inf", "Disy"}, "ALL");
            enHelper.getManager().leave(new String[]{"User2"}, new String[]{"Disy"});
            enHelper.getManager().join(new String[]{"User3"}, new String[]{"TT"}, "Disy");   
            enHelper.getManager().leave(new String[]{}, new String[]{"Disy"});
            enHelper.getManager().join(new String[]{"User4"}, new String[]{"Inf", "Dbis"}, "ALL");
            
            AbsAxis axis =
                new XPathAxis(holder.getRtx(),
                    "/site/people/person[@id=\"person0\"]/name/text()");

            XPathStringChecker.testIAxisConventions(axis, new String[] {
                "Sinisa Farrel"
            });

        }

    

}
