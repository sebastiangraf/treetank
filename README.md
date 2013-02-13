#Treetank - Secure Treebased Storage

Treetank stores data securely by applying different layers on the stored data. Flexible handling of flat as well as tree-based data is supported by a native encoding of the tree-structures ongoing with suitable paging supporting integrity and confidentiality to provide throughout security.

Secure in this context includes integrity-checks, confidentiality with tree-aware key handling, versioning to provide accountability.
Furthermore, different backends are provided while a binding to different cloud-infrastructures is in progress.

[![Build Status](https://secure.travis-ci.org/disy/treetank.png)](http://travis-ci.org/disy/treetank)

##Content

* README:					this readme file
* LICENSE:	 				license file
* coremodules:				Bundles containing main treetank functionality
* interfacemodules:			Bundles implementing third-party interfaces
* studentmodules:			Bundles containing student projects
* scripts:					bash scripts for syncing against disy-internal repo.
* pom.xml:					Simple pom (yes we do use Maven)

##Further information

The documentation so far is accessible under http://treetank.org (pointing to http://disy.github.com/treetank/).

The framework was presented at various conferences and acted as base for multiple publications and reports:

* Versatile Key Management for Secure Cloud Storage; DISCCO'12: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-200971)
* A legal and technical perspective on secure cloud Storage; DFN Forum'12: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-192389)
* A Secure Cloud Gateway based upon XML and Web Services; ECOWS'11, PhD Symposium: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-154112)
* Treetank, Designing a Versioned XML Storage; XMLPrague'11: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-126912)
* Hecate, Managing Authorization with RESTful XML; WS-REST'11: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-126237)
* Rolling Boles, Optimal XML Structure Integrity for Updating Operations; WWW'11, Poster: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-126226)
* JAX-RX - Unified REST Access to XML Resources; TechReport'10: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-120511)
* Integrity Assurance for RESTful XML; WISM'10: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-123507)
* Distributing XML with focus on parallel evaluation; DBISP2P'08: [PDF](http://nbn-resolving.de/urn:nbn:de:bsz:352-opus-84487)

Any questions, just contact sebastian.graf AT uni-konstanz.de

##License

This work is released in the public domain under the BSD 3-clause license

##Involved People

Treetank is maintained by:

* Sebastian Graf (Treetank Core & Project Lead)

Current subprojects are:

* Andreas Rain (Binding to Filesystems and iSCSI (over jSCSI) and Implementing graphical user interface)

Concluded subprojects were:

* Nuray Gürler (Maven Websites)
* Patrick Lang (Encryption layer)
* Johannes Lichtenberger (Visualization of temporal trees and Evaluation of several versioning approaches)
* Wolfgang Miller (Binding of Treetank to cloud backends)
* Lukas Lewandowski (Jax-RX binding)
* Tina Scherer (native XPath2 engine)
* Marc Kramis (first draft1 of Treetank core)
