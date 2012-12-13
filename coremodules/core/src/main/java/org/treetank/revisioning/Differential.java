/**
 * 
 */
package org.treetank.revisioning;

import java.util.Properties;

import org.treetank.access.conf.ContructorProps;
import org.treetank.cache.NodePageContainer;
import org.treetank.page.NodePage;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Differential versioning of {@link NodePage}s.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class Differential implements IRevisioning {

    /**
     * Parameter to determine the gap between two full-dumps
     */
    private final int mRevToRestore;

    /**
     * 
     * Constructor.
     * 
     * @param pRevToRestore
     *            to be set.
     */
    @Inject
    public Differential(@Assisted Properties pProperties) {
        mRevToRestore = Integer.parseInt(pProperties.getProperty(ContructorProps.NUMBERTORESTORE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePage combinePages(NodePage[] pages) {
        final long nodePageKey = pages[0].getPageKey();
        final NodePage returnVal = new NodePage(nodePageKey);
        final NodePage latest = pages[0];

        NodePage referencePage = pages[0];

        for (int i = 1; i < pages.length; i++) {
            if (i % mRevToRestore == 0) {
                referencePage = pages[i];
                break;
            }
        }
        assert latest.getPageKey() == nodePageKey;
        assert referencePage.getPageKey() == nodePageKey;
        for (int i = 0; i < referencePage.getNodes().length; i++) {
            if (latest.getNode(i) != null) {
                returnVal.setNode(i, latest.getNode(i));
            } else {
                returnVal.setNode(i, referencePage.getNode(i));
            }
        }
        return returnVal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodePageContainer combinePagesForModification(NodePage[] pages) {
        final long nodePageKey = pages[0].getPageKey();
        final NodePage[] returnVal = {
            new NodePage(nodePageKey), new NodePage(nodePageKey)
        };

        final NodePage latest = pages[0];
        NodePage fullDump = pages[0];
        int i = 1;
        for (; i < pages.length; i++) {
            if (i % mRevToRestore == 0) {
                fullDump = pages[i];
                break;
            }
        }

        // iterate through all nodes
        for (int j = 0; j < returnVal[0].getNodes().length; j++) {
            if (latest.getNode(j) != null) {
                returnVal[0].setNode(j, latest.getNode(j));
                returnVal[1].setNode(j, latest.getNode(j));
            } else {
                if (fullDump.getNode(j) != null) {
                    returnVal[0].setNode(j, fullDump.getNode(j));
                    if (i + 1 % mRevToRestore == 0) {
                        returnVal[1].setNode(j, fullDump.getNode(j));
                    }
                }
            }
        }

        final NodePageContainer cont = new NodePageContainer(returnVal[0], returnVal[1]);
        return cont;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRevisionsToRestore() {
        return mRevToRestore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mRevToRestore;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Differential [mRevToRestore=");
        builder.append(mRevToRestore);
        builder.append("]");
        return builder.toString();
    }

}
