package org.treetank.encryption;

import java.io.File;

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
import org.junit.Test;

public class EncryptionMainTest {


        private static final String XML = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator + "auction.xml";

        private static Holder holder;
        

        @Before
        public void setUp() throws Exception {
            TestHelper.deleteEverything();
          
            new EncryptionController().clear();
            new EncryptionController().setEncryptionOption(true);
            new EncryptionController().init();
            XMLShredder.main(XML, PATHS.PATH1.getFile().getAbsolutePath());
            holder = Holder.generateRtx();
            
            new EncryptionController().setUser((holder.getSession().getUser()));
        }

        @After
        public void tearDown() throws AbsTTException {
            holder.close();
            new EncryptionController().setEncryptionOption(false);
            //new EncryptionController().print();
            TestHelper.closeEverything();
        }


        @Test
        public void executeEncryption() throws AbsTTException, TTEncryptionException  {
            
            String [] nodes = new String[]{"Inf", "Disy", "TT", "Group1"};
            EncryptionOperator op = new EncryptionOperator();
            op.join("ROOT", nodes);

            
            String [] nodes2 = new String[]{"BaseX", "Group2"};
            EncryptionOperator op2 = new EncryptionOperator();
            op2.join("Inf", nodes2);
            
            String [] nodes3 = new String[]{"RZ", "Waldvogel"};
            EncryptionOperator op3 = new EncryptionOperator();
            op3.join("ROOT", nodes3);
           
            String [] nodes4 = new String[]{"Waldvogel"};
            EncryptionOperator op4 = new EncryptionOperator();
            op4.join("TT", nodes4);
            

            
          EncryptionOperator op10 = new EncryptionOperator();
          op10.leave("Group2", new String[]{"BaseX"});
          
        EncryptionOperator op9 = new EncryptionOperator();
        op9.leave("Waldvogel", new String[]{});
            
            AbsAxis axis =
                new XPathAxis(holder.getRtx(),
                    "/site/people/person[@id=\"person0\"]/name/text()");

            XPathStringChecker.testIAxisConventions(axis, new String[] {
                "Sinisa Farrel"
            });

        }

    

}
