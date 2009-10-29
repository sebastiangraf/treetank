package com.treetank.io;

import com.treetank.page.AbstractPage;
import com.treetank.page.IndirectPage;
import com.treetank.page.NamePage;
import com.treetank.page.NodePage;
import com.treetank.page.RevisionRootPage;
import com.treetank.page.UberPage;

public final class PagePersistenter {

    public final static int NODEPAGE = 1;
    public final static int NAMEPAGE = 2;
    public final static int UBERPAGE = 3;
    public final static int INDIRCTPAGE = 4;
    public final static int REVISIONROOTPAGE = 5;

    /**
     * Empty constructor, not needed since access occures with static methods.
     */
    private PagePersistenter() {
        // Not needed over here
    }

    public static AbstractPage createPage(final ITTSource source) {
        final int kind = source.readInt();
        AbstractPage returnVal = null;
        switch (kind) {
        case NODEPAGE:
            returnVal = new NodePage(source);
            break;
        case NAMEPAGE:
            returnVal = new NamePage(source);
            break;
        case UBERPAGE:
            returnVal = new UberPage(source);
            break;
        case INDIRCTPAGE:
            returnVal = new IndirectPage(source);
            break;
        case REVISIONROOTPAGE:
            returnVal = new RevisionRootPage(source);
            break;
        }
        return returnVal;
    }

    public static void serializePage(final ITTSink sink, final AbstractPage page) {

        if (page instanceof NodePage) {
            sink.writeInt(PagePersistenter.NODEPAGE);

        } else if (page instanceof IndirectPage) {
            sink.writeInt(PagePersistenter.INDIRCTPAGE);

        } else if (page instanceof NamePage) {
            sink.writeInt(PagePersistenter.NAMEPAGE);

        } else if (page instanceof RevisionRootPage) {
            sink.writeInt(PagePersistenter.REVISIONROOTPAGE);

        } else if (page instanceof UberPage) {
            sink.writeInt(PagePersistenter.UBERPAGE);

        } else {
            throw new IllegalStateException(new StringBuilder("Page ").append(
                    page.getClass()).append(" cannot be serialized").toString());
        }

    }

}
