package org.treetank.encryption;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for parsing the initial given right tree and storing
 * all data into the databases.
 * 
 * @author Patrick Lang, University of Konstanz
 */
public class EncryptionTreeParser extends DefaultHandler {

    /**
     * Path of initial right tree XML file.
     */
    private static final String FILENAME = "src" + File.separator + "test"
        + File.separator + "resources" + File.separator
        + "righttreestructure.xml";

    /**
     * Linked list for storing all parsed right nodes.
     */
    private List<RightNode> mNodeList = new LinkedList<RightNode>();

    /**
     * Helper to identifying current tree level.
     */
    private List<Integer> mLevelList = new LinkedList<Integer>();

    /**
     * Stack for storing all parsed right nodes.
     */
    private List<String> mNodeStack = new LinkedList<String>();

    /**
     * Current tree level.
     */
    private int mLevel = -1;

    /**
     * Root right node instance.
     */
    private RightNode mRoot = null;

    /**
     * Instance for {@link KeySelectorDatabase}.
     */
    private static KeySelectorDatabase mSelectorDb;

    /**
     * Instance for {@link KeyMaterialDatabase}.
     */
    private static KeyMaterialDatabase mMaterialDb;

    /**
     * Instance for {@link KeyManagerDatabase}.
     */
    private static KeyManagerDatabase mManagerDb;

    /**
     * Helper for storing the parsed node types (group or user).
     */
    private final Map<String, String> mNodeTypeMap =
        new HashMap<String, String>();

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
    public final void init(final KeySelectorDatabase selDb,
        final KeyMaterialDatabase matDb, final KeyManagerDatabase manDb) {

        mSelectorDb = selDb;
        mMaterialDb = matDb;
        mManagerDb = manDb;

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse(FILENAME, new EncryptionTreeParser());

            new RightTree(mRoot);
            // RightTree tree = new RightTree(mRoot);
            // tree.traverse();

        } catch (final Exception mExp) {
            mExp.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void startElement(final String namespaceURI,
        final String localName, final String qName, final Attributes atts)
        throws SAXException {
        mNodeTypeMap.put(qName, atts.getValue(0));
        mLevel++;

        final String parentNode;
        if (mNodeStack.size() > 0) {
            int pos = mNodeStack.size() - 1;
            parentNode = mNodeStack.get(pos);
        } else {
            parentNode = null;
        }

        mNodeStack.add(qName);
        KeySelector mSelector = new KeySelector(qName, parentNode);

        mSelectorDb.putPersistent(mSelector);
        mMaterialDb.putPersistent(mSelector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void endElement(final String namespaceURI,
        final String localName, final String qName) throws SAXException {

        int mLevelSize = mLevelList.size();
        RightNode mCurNode;

        if (mLevelSize >= 2
            && (mLevelList.get(mLevelSize - 1) == mLevelList
                .get(mLevelSize - 2))) {

            mLevelList.remove(mLevelSize - 1);
            mLevelList.remove(mLevelSize - 2);

            int lastObjPos = mNodeList.size() - 1;
            RightNode mRightChild = mNodeList.remove(lastObjPos);
            lastObjPos = mNodeList.size() - 1;
            RightNode mLeftChild = mNodeList.remove(lastObjPos);

            mCurNode = new RightNode(qName, mLeftChild, mRightChild);
            mNodeStack.remove(qName);

        } else {
            // Node is a leaf node
            mCurNode = new RightNode(qName, null, null);

            mNodeStack.remove(qName);
            if (mNodeTypeMap.get(qName).equals("user")) {
                // get node ids of key trace
                List<Long> keyTrail = new LinkedList<Long>();
                final SortedMap<Long, KeySelector> sMap =
                    mSelectorDb.getEntries();

                for (String name : mNodeStack) {
                    Iterator iter = sMap.keySet().iterator();
                    while (iter.hasNext()) {
                        KeySelector selector = sMap.get(iter.next());
                        if (selector.getName().equals(name)) {
                            keyTrail.add(selector.getKeyId());
                            break;
                        }

                    }
                }
                mManagerDb.putPersistent(qName, keyTrail, keyTrail.get(0));
            }
        }
        mNodeList.add(mCurNode);
        mLevelList.add(mLevel);
        mRoot = mCurNode;
        mLevel--;
    }

}
