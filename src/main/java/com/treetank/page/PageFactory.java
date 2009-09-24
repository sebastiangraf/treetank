package com.treetank.page;

import com.treetank.io.ITTSource;

public class PageFactory {

	public final static int NODEPAGE = 1;
	public final static int NAMEPAGE = 2;
	public final static int UBERPAGE = 3;
	public final static int INDIRCTPAGE = 4;
	public final static int REVISIONROOTPAGE = 5;

	public final static AbstractPage createPage(final ITTSource source) {
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

}
