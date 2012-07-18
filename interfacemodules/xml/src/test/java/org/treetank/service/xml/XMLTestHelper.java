package org.treetank.service.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.treetank.settings.ECharsForSerializing;

public class XMLTestHelper {

	/**
	 * Read a file into a StringBuilder.
	 * 
	 * @param paramFile
	 *            The file to read.
	 * @param paramWhitespaces
	 *            Retrieve file and don't remove any whitespaces.
	 * @return StringBuilder instance, which has the string representation of
	 *         the document.
	 * @throws IOException
	 *             throws an IOException if any I/O operation fails.
	 */
	public static StringBuilder readFile(final File paramFile,
			final boolean paramWhitespaces) throws IOException {
		final BufferedReader in = new BufferedReader(new FileReader(paramFile));
		final StringBuilder sBuilder = new StringBuilder();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			if (paramWhitespaces) {
				sBuilder.append(line + ECharsForSerializing.NEWLINE);
			} else {
				sBuilder.append(line.trim());
			}
		}

		// Remove last newline.
		if (paramWhitespaces) {
			sBuilder.replace(sBuilder.length() - 1, sBuilder.length(), "");
		}
		in.close();

		return sBuilder;
	}

}
