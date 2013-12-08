package org.treetank.iscsi.data;

import java.io.DataInput;
import java.io.DataOutput;

import org.treetank.api.IMetaEntry;
import org.treetank.api.IMetaEntryFactory;
import org.treetank.exception.TTIOException;

import com.google.common.hash.Funnel;

/**
 * Implementation for iSCSI Meta information, at the moment not really used.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ISCSIMetaPageFactory implements IMetaEntryFactory {

    @Override
    public IMetaEntry deserializeEntry(DataInput pData) {
        return new IMetaEntry() {
            @Override
            public void serialize(DataOutput pOutput) throws TTIOException {
            }

            @Override
            public Funnel<IMetaEntry> getFunnel() {
                return null;
            }
        };
    }

}
