Diff package for XML relying on Treetank node layer.
=============

Based on the inlying tree-encoding of Treetank, this package offers multiple possibilities to offer diffs.
The implementation was performed by the master thesis of Johannes Lichtenberger.

Implementation Nodes
-------
Doesn't work when inserting a node with a name/value, then reverting
and then inserting a node with the same name/value at the same level
and comparing these two revisions since they both have the same node key
and same QNames or text values and thus nodes are regarded as being deleted
between the old node and the new node (if the new node is a right sibling).

Proposed fix:
-------
Load maximum node key of newest revision and add 1 for the first added node.	 

License
-------

This work is released in the public domain under the BSD 3-clause license

Involved People
-------

* Tina Scherer (Implementation)
* Marc Kramis (Supervision of first version)
* Sebastian Graf (Maintenance)
* Patrick Lang (Concurrent XPath 2.0 prototype)
