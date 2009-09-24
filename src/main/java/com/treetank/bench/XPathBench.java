package com.treetank.bench;

import java.io.File;
import java.io.IOException;

import com.treetank.api.IAxis;
import com.treetank.api.IReadTransaction;
import com.treetank.api.ISession;
import com.treetank.service.xml.XMLShredder;
import com.treetank.service.xml.xpath.XPathParser;
import com.treetank.session.Session;
import com.treetank.session.SessionConfiguration;

public class XPathBench {

	private final static String FILENAME = "shakespeare";
	private final static File XML = new File("src" + File.separator + "test"
			+ File.separator + "resources" + File.separator + FILENAME + ".xml");
	private final static File TNK = new File("target" + File.separator + "TNK"
			+ File.separator + FILENAME + ".tnk");
	private final static File INDEXTNK = new File("target" + File.separator
			+ "TNK" + File.separator + "index" + FILENAME + ".tnk");

	public XPathBench() {
	}

	public void query() {
		final String query = "//ACT";

		INDEXTNK.delete();
		if (!TNK.exists()) {
			XMLShredder.shred(XML.getAbsolutePath(), new SessionConfiguration(
					TNK.getAbsolutePath()));
		}
		try {
			final ISession session = Session.beginSession(TNK);
			final IReadTransaction rtx = session.beginReadTransaction();

			final XPathParser parser = new XPathParser(rtx, query);
			parser.parseQuery();
			final IAxis axis = parser.getQueryPipeline();
			for (final long key : axis) {
				System.out.println(rtx.getNameOfCurrentNode());
			}

			rtx.close();
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Starting query");
		new XPathBench().query();
		System.out.println("Ending query");
	}
}
