package org.treetank.page;

import java.util.HashMap;
import java.util.Map;

import org.treetank.io.EStorage;
import org.treetank.io.ITTSource;
import org.treetank.page.interfaces.IPage;

public enum EPage {

    Node(1, NodePage.class) {

    },

    Name(2, NamePage.class) {

    },
    Uber(3, UberPage.class) {
    },
    Indirect(4, IndirectPage.class) {
    },
    Revision(5, RevisionRootPage.class) {
    };

    /** Getting identifier mapping. */
    private static final Map<Integer, EPage> INSTANCEFORID = new HashMap<Integer, EPage>();
    private static final Map<Class<? extends IPage>, EPage> INSTANCEFORCLASS =
        new HashMap<Class<? extends IPage>, EPage>();
    static {
        for (EPage page : values()) {
            INSTANCEFORID.put(page.mIdent, page);
            INSTANCEFORCLASS.put(page.mClass, page);
        }
    }

    /** Identifier for the storage. */
    final int mIdent;

    /** Class for Key. */
    final Class<? extends IPage> mClass;

    /**
     * Constructor.
     * 
     * @param paramIdent
     *            identifier to be set.
     */
    EPage(final int paramIdent, final Class<? extends IPage> paramClass) {
        mIdent = paramIdent;
        mClass = paramClass;
    }

    /**
     * Getting an instance of this enum for the identifier.
     * 
     * @param paramId
     *            the identifier of the enum.
     * @return a concrete enum
     */
    public static final EPage getInstance(final Integer paramId) {
        return INSTANCEFORID.get(paramId);
    }

    /**
     * Getting an instance of this enum for the identifier.
     * 
     * @param paramKey
     *            the identifier of the enum.
     * @return a concrete enum
     */
    public static final EPage getInstance(final Class<? extends IPage> paramKey) {
        return INSTANCEFORCLASS.get(paramKey);
    }

    public static final PageReference[] deserializeRef(final int paramRefSize, final ITTSource paramIn) {
        final PageReference[] refs = new PageReference[paramRefSize];
        for (int offset = 0; offset < refs.length; offset++) {
            refs[offset] = new PageReference();
            final EStorage storage = EStorage.getInstance(paramIn.readInt());
            if (storage != null) {
                refs[offset].setKey(storage.deserialize(paramIn));
            }
        }
        return refs;
    }

}
