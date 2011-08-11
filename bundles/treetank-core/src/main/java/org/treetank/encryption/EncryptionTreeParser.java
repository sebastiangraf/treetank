/**
 * Copyright (c) 2011, Distributed Systems Group, University of Konstanz
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED AS IS AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * 
 */
package org.treetank.encryption;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.treetank.cache.KeyCache;

/**
 * Class for parsing the initial given encryption tree and storing
 * all data into the databases.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class EncryptionTreeParser extends DefaultHandler {

    /**
     * Path of initial right tree XML file.
     */
    private static final String FILENAME = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "righttreestructure.xml";

    /**
     * Instance for {@link KeySelectorDatabase}.
     */
    private static KeySelectorDatabase mKeySelectorDb;

    /**
     * Instance for {@link KeyManagerDatabase}.
     */
    private static KeyManagerDatabase mKeyManagerDb;

    private static KeyCache mKeyCache;

    /**
     * Node declaration in initial right tree XML file.
     */
    private final String mNodeDec = "NODE";

    /**
     * Edge declaration in initial right tree XML file.
     */
    private final String mEdgeDec = "EDGE";

    /**
     * Group type declaration in initial right tree XML file.
     */
    private final String mTypeGroup = "group";

    private static String mUser;

    /**
     * Stack holding all parsed nodes.
     */
    private final Stack<Long> mNodeStack = new Stack<Long>();

    /**
     * Map holding all node names and its corresponding node id.
     */
    private final Map<String, Long> mNodeMap = new HashMap<String, Long>();

    /**
     * List of all parsed user ids.
     */
    private final List<Long> mUserIdList = new LinkedList<Long>();

    /**
     * Just a helper map for user that has parent that hasn't been parsed
     * and written to the database yet.
     */
    private final Map<Long, List<String>> mUserParents = new HashMap<Long, List<String>>();

    /**
     * Start tree parsing process.
     * 
     * @param selDb
     *            key selector database instance.
     * @param matDb
     *            keying material database instance.
     * @param manDb
     *            key manager database instance.
     */
    public final void init() {

        mKeySelectorDb = EncryptionHandler.getInstance().getKeySelectorInstance();
        mKeyManagerDb = EncryptionHandler.getInstance().getKeyManagerInstance();
        mKeyCache = EncryptionHandler.getInstance().getKeyCacheInstance();
        mUser = EncryptionHandler.getInstance().getUser();

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse(FILENAME, new EncryptionTreeParser());

        } catch (final Exception mExp) {
            mExp.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void startElement(final String namespaceURI, final String localName, final String qName,
        final Attributes atts) throws SAXException {

        final String mNodeName = atts.getValue(0);
        final String mNodeType = atts.getValue(1);

        if (qName.equals(mNodeDec)) {
            if (mNodeStack.size() > 0) {
                mNodeStack.pop();
            }

            final KeySelector mSelector;
            if (mNodeType.equals(mTypeGroup)) {
                mSelector =
                    new KeySelector(mNodeName, new LinkedList<Long>(), new LinkedList<Long>(), 0, 0,
                        EntityType.GROUP);
            } else {
                mSelector =
                    new KeySelector(mNodeName, new LinkedList<Long>(), new LinkedList<Long>(), 0, 0,
                        EntityType.USER);
                mUserIdList.add(mSelector.getPrimaryKey());
            }

            mNodeStack.add(mSelector.getPrimaryKey());
            mNodeMap.put(mNodeName, mSelector.getPrimaryKey());

            mKeySelectorDb.putEntry(mSelector);

        } else if (qName.equals(mEdgeDec)) {
            long mNodeId = mNodeStack.peek();
            KeySelector mSelector = mKeySelectorDb.getEntry(mNodeId);

            if (mNodeMap.containsKey(mNodeName)) {
                mSelector.addParent(mNodeMap.get(mNodeName));
                mKeySelectorDb.putEntry(mSelector);
            } else {
                // put parent node that weren't parsed yet into a map.
                if (mUserParents.containsKey(mNodeId)) {
                    List<String> mNameList = mUserParents.get(mNodeId);
                    mNameList.add(mNodeName);
                } else {
                    List<String> mNameList = new LinkedList<String>();
                    mNameList.add(mNodeName);
                    mUserParents.put(mNodeId, mNameList);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void endDocument() throws SAXException {
        /*
         * add not yet stored edges as parents to the nodes.
         */
        Iterator iter = mUserParents.keySet().iterator();
        while (iter.hasNext()) {
            final long mMapKey = (Long)iter.next();
            final KeySelector mSelector = mKeySelectorDb.getEntry(mMapKey);
            final List<String> mNameList = mUserParents.get(mMapKey);

            for (int i = 0; i < mNameList.size(); i++) {
                final String mName = mNameList.get(i);

                final Iterator innerIter = mKeySelectorDb.getEntries().keySet().iterator();
                while (innerIter.hasNext()) {
                    final KeySelector mParentSelector = mKeySelectorDb.getEntries().get(innerIter.next());
                    if (mParentSelector.getName().equals(mName)) {
                        mSelector.addParent(mParentSelector.getPrimaryKey());
                        mKeySelectorDb.putEntry(mSelector);
                    }
                }
            }
        }

        /*
         * find and store children of each node.
         */

        // outer loop
        final SortedMap<Long, KeySelector> mOuterSelectorMap = mKeySelectorDb.getEntries();
        final Iterator outerIter = mOuterSelectorMap.keySet().iterator();
        while (outerIter.hasNext()) {
            final KeySelector mOuterSelector = mOuterSelectorMap.get(outerIter.next());
            final long mOuterId = mOuterSelector.getPrimaryKey();

            // inner loop
            final SortedMap<Long, KeySelector> mInnerSelectorMap = mKeySelectorDb.getEntries();
            final Iterator innerIter = mInnerSelectorMap.keySet().iterator();
            while (innerIter.hasNext()) {
                final KeySelector mInnerSelector = mInnerSelectorMap.get(innerIter.next());
                final long mInnerId = mInnerSelector.getPrimaryKey();
                final List<Long> mParents = mInnerSelector.getParents();

                if (mParents.size() > 0 && mParents.contains(mOuterId)) {
                    mOuterSelector.addChild(mInnerId);
                }
            }
            mKeySelectorDb.putEntry(mOuterSelector);
        }

        /*
         * build key manager database.
         */

        for (long userId : mUserIdList) {
            final Set<Long> mKeySet = new HashSet<Long>();
            final List<Long> mHelper = new LinkedList<Long>();
            final KeySelector mUserSelector = mKeySelectorDb.getEntry(userId);

            mKeySet.add(mUserSelector.getPrimaryKey());
            mHelper.add(mUserSelector.getPrimaryKey());

            for (int i = 0; i < mHelper.size(); i++) {
                final KeySelector mNodeSelector = mKeySelectorDb.getEntry(mHelper.get(i));
                final List<Long> mParents = mNodeSelector.getParents();
                if (mParents.size() > 0) {
                    for (long parentId : mParents) {
                        if (!mKeySet.contains(parentId)) {
                            mKeySet.add(parentId);
                        }
                        if (!mHelper.contains(parentId)) {
                            mHelper.add(parentId);
                        }
                    }
                }
            }
            mKeyManagerDb.putEntry(new KeyManager(mUserSelector.getName(), mKeySet));

            /*
             * put initial key set of current logged user into key cache.
             */
            if (mUserSelector.getName().equals(mUser)) {
                mKeyCache.put(mUser, new LinkedList<Long>(mKeySet));
            }
        }

    }

}
