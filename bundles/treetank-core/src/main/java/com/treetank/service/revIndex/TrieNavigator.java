package com.treetank.service.revIndex;

import javax.xml.namespace.QName;

import com.treetank.api.IReadTransaction;
import com.treetank.api.IWriteTransaction;
import com.treetank.exception.TreetankException;
import com.treetank.node.IStructuralNode;
import com.treetank.settings.ENodes;
import com.treetank.utils.NamePageHash;

final class TrieNavigator {

	/**
	 * Moving with a writetransaction into the trie and adapting it if the term
	 * hasn't been stored in the structure
	 * 
	 * @param wtx
	 *            with which should be worked, is moved to the root of the trie
	 *            at the beginning
	 * @param term
	 *            to be inserted
	 */
	static void adaptTrie(final IWriteTransaction wtx, final String term)
			throws TreetankException {
		moveToTrieRoot(wtx);
		int endIndexOfTerm = 1;
		String current = "";
		// for each letter in the term to be indexed...
		while (endIndexOfTerm <= term.length()) {
			// ..create a node in the structure and...
			current = term.substring(0, endIndexOfTerm);
			// ..navigate through the trie by inserting directly the prefix
			// if no firstchild is present in the trie...
			if (!wtx.moveToFirstChild()) {
				wtx.insertElementAsFirstChild(new QName(RevIndex.TRIE_ELEMENT));
				wtx.insertAttribute(
						new QName(RevIndex.TRIE_PREFIX_ATTRIBUTEKEY), current);
				wtx.moveToParent();
			}
			boolean found = false;
			// ..or checking against the sibling axis of the trie and
			// checking of the prefix is present as a sibling of the
			// firstchild...
			do {
				if (wtx.getNode().getNameKey() == NamePageHash
						.generateHashForString(RevIndex.TRIE_ELEMENT)) {
					if (!wtx.moveToAttribute(0)) {
						throw new IllegalStateException();
					}
					if (wtx.getValueOfCurrentNode().hashCode() == current
							.hashCode()) {
						found = true;
					}
					wtx.moveToParent();
					if (found) {
						break;
					}
				}
			} while (wtx.moveToRightSibling());
			// ...of not, insert it...
			if (!found) {
				wtx
						.insertElementAsRightSibling(new QName(
								RevIndex.TRIE_ELEMENT));
				wtx.insertAttribute(
						new QName(RevIndex.TRIE_PREFIX_ATTRIBUTEKEY), current);
				wtx.moveToParent();
			}
			// ..and adapt the prefix with the next letter from the trie..
			current = "";
			endIndexOfTerm++;
		}
	}

	static long getDocRootInTrie(final IReadTransaction rtx, final String term)
			throws TreetankException {
		moveToTrieRoot(rtx);
		long returnVal = ENodes.UNKOWN_KIND.getNodeIdentifier();
		StringBuilder toSearch = new StringBuilder();
		for (int i = 0; i < term.length(); i++) {
			if (rtx.moveToFirstChild()) {
				toSearch.append(term.charAt(i));
				do {
					if (rtx.getNode().getNameKey() == NamePageHash
							.generateHashForString(RevIndex.TRIE_ELEMENT)) {
						if (!rtx.moveToAttribute(0)) {
							throw new IllegalStateException();
						}
						if (rtx.getValueOfCurrentNode().hashCode() == toSearch
								.toString().hashCode()) {
							rtx.moveToParent();
							break;
						} else {
							rtx.moveToParent();
						}

					}
				} while (rtx.moveToRightSibling());

			} else {
				break;
			}
		}
		if (rtx.getNode().getNameKey() == NamePageHash
				.generateHashForString(RevIndex.TRIE_ELEMENT)) {
			if (!rtx.moveToAttribute(0)) {
				throw new IllegalStateException();
			}
			if (rtx.getValueOfCurrentNode().hashCode() == toSearch.toString()
					.hashCode()) {
				rtx.moveToParent();
				returnVal = rtx.getNode().getNodeKey();
			} else {
				rtx.moveToParent();
			}
		}
		return returnVal;

	}

	/**
	 * Private method to the root of the trie. Inserting basic structure if not
	 * avaliable.
	 */
	private static void moveToTrieRoot(final IReadTransaction rtx)
			throws TreetankException {
		rtx.moveToDocumentRoot();
		if (!((IStructuralNode) rtx.getNode()).hasFirstChild()) {
			RevIndex.initialiseBasicStructure(rtx);
			rtx.moveToLeftSibling();
		} else {
			rtx.moveToFirstChild();
			rtx.moveToRightSibling();
		}
	}

}
