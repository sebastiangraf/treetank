package com.treetank.saxon.testsuit;

import net.sf.saxon.query.ModuleURIResolver;
import net.sf.saxon.query.StandardModuleURIResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.type.Type;

import javax.xml.transform.stream.StreamSource;
import java.util.List;
import java.util.ArrayList;

/**
 * This ModuleURIResolver forms part of the test driver for the XQTS test suite. 
 * It locates library modules for a given namespace based on information in the 
 * XQTS catalog.
 */
public class XQTSModuleURIResolver implements ModuleURIResolver {

  /** Serialization UID. */
  private static final long serialVersionUID = 1L;

  /** TestCase node. */
  public final transient NodeInfo testCase;

  /**
   * Constructor
   * 
   * @param testCase 
   *                  TestCase node.
   */
  public XQTSModuleURIResolver(final NodeInfo testCase) {
    this.testCase = testCase;
  }

  /**
   * Resolve and identifying a query module, given the identifying URI and
   * a set of associated location hints.
   *
   * @param moduleURI the module URI of the module to be imported; or null when
   *                  loading a non-library module.
   * @param baseURI   The base URI of the module containing the "import module" declaration;
   *                  null if no base URI is known
   * @param locations The set of URIs specified in the "at" clause of "import module",
   *                  which serve as location hints for the module
   * @return an array of StreamSource objects each identifying the contents of a query module to be
   *         imported. Each StreamSource must contain a
   *         non-null absolute System ID which will be used as the base URI of the imported module,
   *         and either an InputSource or a Reader representing the text of the module. The method
   *         may also return null, in which case the system attempts to resolve the URI using the
   *         standard module URI resolver.
   * @throws net.sf.saxon.trans.XPathException
   *          if the module cannot be located, and if delegation to the default
   *          module resolver is not required.
   */

  public StreamSource[] resolve(
      final String moduleURI,
      final String baseURI,
      final String[] locations) throws XPathException {
    final NamePool pool = testCase.getNamePool();
    final int moduleNC =
        pool.allocate(
            "",
            "http://www.w3.org/2005/02/query-test-XQTSCatalog",
            "module");
    final int namespaceNC = pool.allocate("", "", "namespace");
    final AxisIterator iter =
        testCase.iterateAxis(Axis.CHILD, new NameTest(
            Type.ELEMENT,
            moduleNC,
            pool));
    final List<String> catalogLocations = new ArrayList<String>(5);
    while (true) {
      final NodeInfo m = (NodeInfo) iter.next();
      if (m == null)
        break;
      if (moduleURI.equals(m.getAttributeValue(namespaceNC))) {
        String moduleRef = m.getStringValue();
        // take a short cut here: hard code information from the catalog
        if (moduleRef.equals("module-defs")) {
          moduleRef = "moduleDefs-lib";
        }
        moduleRef = "../TestSources/" + moduleRef + ".xq";
        catalogLocations.add(moduleRef);
      }
    }
    if (catalogLocations.isEmpty()) {
      // in XQTS-extra we don't catalog modules, we just reference them directly
      final StandardModuleURIResolver r =
          StandardModuleURIResolver.getInstance();
      return r.resolve(moduleURI, baseURI, locations);
    } else {
      final String[] locs = new String[catalogLocations.size()];
      for (int i = 0; i < locs.length; i++) {
        locs[i] = catalogLocations.get(i);
      }
      final StandardModuleURIResolver r =
          StandardModuleURIResolver.getInstance();
      return r.resolve(moduleURI, testCase.getBaseURI(), locs);
    }
  }
}
