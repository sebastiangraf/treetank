package com.treetank;

import java.io.File;

public interface ITestConstants {

	public static final String PATH1 = "/tmp" + File.separator + "tnk"
			+ File.separator + "path1";

	public static final String PATH2 = "/tmp" + File.separator + "tnk"
			+ File.separator + "path2";

	public static final String NON_EXISTING_PATH = "/tmp" + File.separator
			+ "tnk" + File.separator + "NonExistingSessionTest";

	public static final String TEST_INSERT_CHILD_PATH = "/tmp" + File.separator
			+ "tnk" + File.separator + "InsertChildSessionTest";

	public static final String TEST_REVISION_PATH = "/tmp" + File.separator
			+ "tnk" + File.separator + "RevisionSessionTest";

	public static final String TEST_SHREDDED_REVISION_PATH = "/tmp"
			+ File.separator + "tnk" + File.separator
			+ "ShreddedRevisionSessionTest";

	public static final String TEST_EXISTING_PATH = "/tmp" + File.separator
			+ "tnk" + File.separator + "ExistingSessionTest";
}
