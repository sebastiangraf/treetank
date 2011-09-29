package org.treetank.encryption;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.treetank.exception.TTEncryptionException;

public class KeyManagerHandlerTest {


    @Before
    public void setUp() throws Exception {
        new EncryptionController().clear();
        new EncryptionController().setEncryptionOption(true);
        new EncryptionController().init();
    }

    @After
    public void tearDown() {
        new EncryptionController().setEncryptionOption(false);
    }

    @Test
    public void testJoinAndLeave() throws TTEncryptionException {
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

        // after all joins and leaves and join/leave updates database size must be 31.
        assertEquals(new EncryptionController().getSelDb().count(),
            35);
        assertEquals(new EncryptionController().getDAGDb().count(),
            7);
        assertEquals(new EncryptionController().getManDb().count(),
            2);

    }
}
