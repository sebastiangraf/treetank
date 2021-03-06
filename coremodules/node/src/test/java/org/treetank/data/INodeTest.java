package org.treetank.data;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.treetank.api.IDataFactory;
import org.treetank.data.delegates.NameNodeDelegate;
import org.treetank.data.delegates.NodeDelegate;
import org.treetank.data.delegates.StructNodeDelegate;
import org.treetank.data.delegates.ValNodeDelegate;
import org.treetank.data.interfaces.ITreeData;
import org.treetank.data.interfaces.ITreeNameData;
import org.treetank.data.interfaces.ITreeStructData;
import org.treetank.data.interfaces.ITreeValData;
import org.treetank.exception.TTByteHandleException;
import org.treetank.exception.TTIOException;
import org.treetank.testutil.CoreTestHelper;
import org.treetank.utils.NamePageHash;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class INodeTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
        CoreTestHelper.deleteEverything();

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        CoreTestHelper.closeEverything();
        CoreTestHelper.deleteEverything();
    }

    /**
     * Test method for {@link ITreeData#getHash()}, {@link ITreeData#getDataKey()},
     * {@link ITreeData#serialize(java.io.DataOutput)},
     * 
     * @param pNodeClass
     *            class for node to test
     * @param pNodes
     *            nodes to test
     * @param pNodeCheckerClass
     *            class for node-tester
     * @param pNodeChecker
     *            checker for nodes
     * @throws TTIOException
     * 
     */
    @Test(dataProvider = "instantiateNode")
    public void testNode(Class<ITreeData> pNodeClass, ITreeData[] pNodes, Class<INodeChecker> pNodeCheckerClass,
        INodeChecker[] pNodeChecker) throws TTIOException {

        // be sure you have enough checkers for the revisioning to check
        assertEquals(pNodes.length, pNodeChecker.length);
        IDataFactory fac = new TreeNodeFactory();

        for (int i = 0; i < pNodes.length; i++) {
            pNodeChecker[i].checkNode(pNodes[i]);
            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            pNodes[i].serialize(output);
            final byte[] firstSerialized = output.toByteArray();

            ByteArrayDataInput input = ByteStreams.newDataInput(firstSerialized);
            final ITreeData serializedNode = (ITreeData)fac.deserializeData(input);
            output = ByteStreams.newDataOutput();
            serializedNode.serialize(output);
            byte[] secondSerialized = output.toByteArray();

            pNodeChecker[i].checkNode(serializedNode);
            assertTrue(new StringBuilder("Check for ").append(pNodeChecker[i].getClass()).append(" failed.")
                .toString(), Arrays.equals(firstSerialized, secondSerialized));
        }
    }

    /**
     * Providing different implementations of the {@link ITreeData} as Dataprovider to the test class.
     * 
     * @return different classes of the {@link ITreeData} and <code>INodeChecker</code>
     * @throws TTByteHandleException
     *             if something weird happens
     */
    @DataProvider(name = "instantiateNode")
    public Object[][] instantiateNode() throws TTByteHandleException {
        // bootstrap for values
        final byte[] value = {
            (byte)17, (byte)18
        };

        // generating delegates
        final NodeDelegate del = new NodeDelegate(99, 13, 0);
        final NameNodeDelegate nameDel = new NameNodeDelegate(del, 14, 15);
        final ValNodeDelegate valDel = new ValNodeDelegate(del, value);
        final StructNodeDelegate strucDel = new StructNodeDelegate(del, 24, 36, 48, 77);

        // bootstraping Elementnode, necessary due to attribute/namespacekeys
        final ArrayList<Long> attrKeys = new ArrayList<Long>();
        final ArrayList<Long> namespaceKeys = new ArrayList<Long>();
        final ElementNode elemNode = new ElementNode(del, strucDel, nameDel, attrKeys, namespaceKeys);
        elemNode.insertAttribute(97);
        elemNode.insertAttribute(98);
        elemNode.insertNamespace(99);
        elemNode.insertNamespace(100);

        Object[][] returnVal =
            {
                {
                    ITreeData.class,
                    new ITreeData[] {
                        new AttributeNode(del, nameDel, valDel), new DocumentRootNode(del, strucDel),
                        elemNode, new NamespaceNode(del, nameDel), new TextNode(del, strucDel, valDel)

                    }, INodeChecker.class, new INodeChecker[] {
                        // Checker for AttributeNode
                        new INodeChecker() {
                            @Override
                            public void checkNode(ITreeData treeData) {
                                checkPlainNode(treeData);
                                checkNameNode(treeData);
                                checkValNode(treeData);
                                assertEquals("Check for AttributeNode failed: ", IConstants.ATTRIBUTE, treeData
                                    .getKind());

                            }
                        },
                        // Checker for DocumentRootNode
                        new INodeChecker() {
                            @Override
                            public void checkNode(ITreeData treeData) {
                                checkPlainNode(treeData);
                                checkStrucNode(treeData);
                                assertEquals(IConstants.ROOT, treeData.getKind());

                            }
                        },// Checker for ElementNode
                        new INodeChecker() {
                            @Override
                            public void checkNode(ITreeData treeData) {
                                ElementNode elemNode = (ElementNode)treeData;
                                checkPlainNode(treeData);
                                checkNameNode(treeData);
                                checkStrucNode(treeData);
                                assertEquals("Check for ElementNode failed: ", 2, elemNode
                                    .getAttributeCount());
                                assertEquals("Check for ElementNode failed: ", 2, elemNode
                                    .getNamespaceCount());
                                assertEquals("Check for ElementNode failed: ", IConstants.ELEMENT, elemNode
                                    .getKind());
                                assertEquals("Check for ElementNode failed: ", true, elemNode.hasFirstChild());
                                assertEquals("Check for ElementNode failed: ", 97L, elemNode
                                    .getAttributeKey(0));
                                assertEquals("Check for ElementNode failed: ", 98L, elemNode
                                    .getAttributeKey(1));
                                assertEquals("Check for ElementNode failed: ", 99L, elemNode
                                    .getNamespaceKey(0));
                                assertEquals("Check for ElementNode failed: ", 100L, elemNode
                                    .getNamespaceKey(1));
                            }
                        },// Checker for NamespaceNode
                        new INodeChecker() {
                            @Override
                            public void checkNode(ITreeData treeData) {
                                checkPlainNode(treeData);
                                checkNameNode(treeData);
                                assertEquals("Check for NamespaceNode failed: ", IConstants.NAMESPACE, treeData
                                    .getKind());

                            }
                        }, // Checker for TextNode
                        new INodeChecker() {
                            @Override
                            public void checkNode(ITreeData treeData) {
                                checkPlainNode(treeData);
                                checkValNode(treeData);
                                checkStrucNode(treeData);
                                assertEquals("Check for TextNode failed: ", IConstants.TEXT, treeData.getKind());
                            }
                        }
                    }
                }
            };
        return returnVal;
    }

    private static void checkPlainNode(ITreeData treeData) {
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 99L, treeData.getDataKey());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 13L, treeData.getParentKey());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), true, treeData.hasParent());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), NamePageHash.generateHashForString("xs:untyped"), treeData.getTypeKey());
    }

    private static void checkNameNode(ITreeData treeData) {
        ITreeNameData treeNameData = (ITreeNameData)treeData;
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 15, treeNameData.getURIKey());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 14, treeNameData.getNameKey());
    }

    private static void checkValNode(ITreeData treeData) {
        ITreeValData treeValData = (ITreeValData)treeData;
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 2, treeValData.getRawValue().length);

    }

    private static void checkStrucNode(ITreeData treeData) {
        ITreeStructData strucNode = (ITreeStructData)treeData;
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 24L, strucNode.getFirstChildKey());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 48L, strucNode.getLeftSiblingKey());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 36L, strucNode.getRightSiblingKey());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), true, strucNode.hasLeftSibling());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), true, strucNode.hasRightSibling());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), true, strucNode.hasFirstChild());
        assertEquals(new StringBuilder("Check for ").append(treeData.getClass().getName()).append(" failed: ")
            .toString(), 77, strucNode.getChildCount());
    }

    /**
     * Interface to check nodes.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    interface INodeChecker {
        void checkNode(ITreeData treeData);

    }

}
