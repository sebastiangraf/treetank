Native XPath 2.0 Engine
=============

Based on the inlying tree-encoding of Treetank, XPath 2.0 as query engine was chosen in 2007.
The implementation was performed by the master thesis of Tina Scherer.

Implementation Nodes
-------
Important to know:

* the document order is NOT stable (the XPath engine does not return the result sequence in document order.) This is due to the XML encoding in treetank, which makes efficient node ordering impossible.
* node comparison << and >> is not supported due to the same reason
* namespace axis is not supported, because it is not supported in Treetank yet. This makes the engine not compatible with XPath 1.0, but has no	effect on the XPath 2.0 functionality, because the namespace axis is deprecated in the XPath 2.0 spec.
* XMLSchema supported is implemented in the engine, but is not yet supported by Treetank, so every node' type is either "xs:untyped" or "xs:untypedAtomic"
* functions and types can only be used with their namespace prefix.	e.g "fn:position()"
				
Not yet implemented things:

* functions: only a few very important functions are supported at the moment
* cast and treat as
* node constructors
* ordered sequences 
* nilled property
* SchemaAttribute() / SchemaElement() node tests (no Schema support in Treetank)
* interrogation in the element() node test (nilled property)
* wildcards in the name of attributes (ns:name -> *:name or ns:*, because Treetank does not allow to access these easily)
* NamespaceAxis
* prefix optional (at the moment no support for missing prefixes)
* processing instructions (no treetank support)

Things that have to be improved:

* XPath error messages	
* scanner: recognize maskings like &lt; ('<')
* handling of untyped and untypedAtomic (casting)
* quote signs within a stringliteral are ignored
	
Optimization ideas:

* use threads
* use fulltext indices
* convert queries with backward axis in queries only using forward axes				 


Further information
-------

The documentation so far is accessible as master thesis of Tina (German only):


Any questions, just contact sebastian.graf AT uni-konstanz.de

License
-------

This work is released in the public domain under the BSD 3-clause license


Involved People
-------

* Tina Scherer (Implementation)
* Marc Kramis (Supervision of first version)
* Sebastian Graf (Maintenance)
* Patrick Lang (Concurrent XPath 2.0 prototype)
