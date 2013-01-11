package org.treetank.iscsi.node;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;

/**
 * Implementation for iSCSI Meta information, at the moment not really used.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ISCSIMetaPageFactory implements IMetaEntryFactory {

    @Override
    public IMetaEntry deserializeEntry(byte[] pData) {
        return new IMetaEntry() {
            @Override
            public byte[] getByteRepresentation() {
                return new byte[0];
            }
        };
    }

}
