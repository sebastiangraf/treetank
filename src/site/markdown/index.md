Treetank - Secure Treebased Storage
=============

Treetank stores data securely by applying different layers on the stored data. Flexible handling of flat as well as tree-based data is supported by a native encoding of the tree-structures ongoing with suitable paging supporting integrity and confidentiality to provide throughout security.

Secure in this context includes integrity-checks, confidentiality with tree-aware key handling, versioning to provide accountability.
Furthermore, different backends are provided while a binding to different cloud-infrastructures is in progress.

What components does Treetank consists out of?
------------

Treetank consists out of three main components:

* Interfaces enabling data mappings to be stored are located in the interfacemodules
* The core layer containing the page layer and security measures are located in the coremodules. The corelayer furthermore contains the binding to different backends.
* Student projects are located in the studentmodules. Note that student projects are mainly well-tested and reviewed but neither feature-complete nor maintained.

What is Treetank ?
------------

Treetank is: 

* a complete Java-based library entirely mavenized
* acting as base for various thesis and papers
* offering flexible interfaces for storing various data (ranging from blocks to REST-reosurces)
* for a university project well-document and tested

Publications:
-------------

Treetank acted as a base in various thesis and papers namely:

* A Secure Cloud Gateway based upon XML and Web Services; ECOWS'11, PhD Symposium: http://kops.ub.uni-konstanz.de/handle/urn:nbn:de:bsz:352-154112 
* Treetank, Designing a Versioned XML Storage; XMLPrague'11: http://kops.ub.uni-konstanz.de/handle/urn:nbn:de:bsz:352-opus-126912
* Rolling Boles, Optimal XML Structure Integrity for Updating Operations; WWW'11, Poster: http://kops.ub.uni-konstanz.de/handle/urn:nbn:de:bsz:352-126226
* Hecate, Managing Authorization with RESTful XML; WS-REST'11: http://kops.ub.uni-konstanz.de/handle/urn:nbn:de:bsz:352-126237 
* Integrity Assurance for RESTful XML; WISM'10: http://kops.ub.uni-konstanz.de/handle/urn:nbn:de:bsz:352-opus-123507
* JAX-RX - Unified REST Access to XML Resources; TechReport'10: http://kops.ub.uni-konstanz.de/handle/urn:nbn:de:bsz:352-opus-120511
* Distributing XML with focus on parallel evaluation; DBISP2P'08: http://kops.ub.uni-konstanz.de/handle/urn:nbn:de:bsz:352-opus-84487

License
-------

This work is released in the public domain under the BSD 3-clause license


Involved People
-------

Treetank is maintained by:

* Sebastian Graf (Treetank Core & Project Lead)

Current subprojects are:

* Johannes Lichtenberger (Visualization of temporal trees)
* Wolfgang Miller (Binding of Treetank to cloud backends)

Concluded and adopted subprojects were:

* Patrick Lang (Encryption layer)
* Nuray Gürler (Maven Websites)
* Johannes Lichtenberger (Evaluation of several versioning approaches)
* Patrick Lang (Encryption layer)
* Lukas Lewandowski (Jax-RX binding)
* Tina Scherer (native XPath2 engine)
* Marc Kramis (first drafts of Treetank core)