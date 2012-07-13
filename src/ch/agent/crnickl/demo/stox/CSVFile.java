/*
 *   Copyright 2012 Hauser Olsson GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Package: ch.agent.crnickl.demo.stox
 * Type: CSVFile
 * Version: 1.1.0
 */
package ch.agent.crnickl.demo.stox;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.agent.core.KeyedException;
import ch.agent.crnickl.demo.stox.DemoConstants.K;

/**
 * CSVFile hides the details of spreadsheets saved as text. Such
 * spreadsheets are simple text files where the following structure is
 * assumed:
 * <ul>
 * <li>there is a single field separator,
 * <li>all lines have the same number of fields,
 * <li>the first line contains headings, and each heading is unique
 * </ul>
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 */
public class CSVFile {
	
	/**
	 * RowVisitor is used by CSVFile to pass data.
	 */
	public interface RowVisitor {
		/**
		 * The method is invoked for each row of data.
		 * 
		 * @param lineNr the line number
		 * @param column the content of the columns
		 * @throws Exception
		 */
		void visit(int lineNr, String... column) throws Exception;
	}
	
	private Pattern fieldSeparator;
	
	/**
	 * Construct a CSVFile.
	 * 
	 * @param fieldSeparator a pattern defining the field separator
	 * @param skipFirstRow true to skip the first row
	 * @param dateField the offset of the date field
	 * @param stuffField offsets of fields with interesting stuff
	 * @throws Exception
	 */
	public CSVFile(String fieldSeparator) throws KeyedException {
		try {
			this.fieldSeparator = Pattern.compile(fieldSeparator);
		} catch (PatternSyntaxException e) {
			throw K.PATTERN_ERR.exception(e, fieldSeparator);
		}
	}
	
	/**
	 * Scan a resource.
	 * 
	 * @param resource
	 *            the name of a resource on the class path or of a file in the file system
	 * @param visitor a visitor
	 * @throws KeyedException
	 */
	public void scan(String resource, RowVisitor visitor) throws KeyedException {
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
		if (inputStream == null)
			try {
				inputStream = new FileInputStream(resource);
			} catch (Exception e) {
				throw K.FILE_READ_ERR.exception(e, resource);
			}
		scan(inputStream, resource, visitor);
	}
	
	/**
	 * Scan an input stream.
	 * 
	 * @param input an input stream
	 * @param streamLabel a label to use in diagnostic messages
	 * @param visitor a visitor
	 * @throws KeyedException
	 */
	private void scan(InputStream input, String streamLabel, RowVisitor visitor) throws KeyedException {
		int lineNr = 0;
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(input));
			while (true) {
				String line = r.readLine();
				if (line == null)
					break;
				lineNr++;
				String[] fields = fieldSeparator.split(line);
				visitor.visit(lineNr, fields);
			}
			r.close();
		} catch (Exception e) {
			throw K.FILE_READ_LINE_ERR.exception(e, streamLabel, lineNr);
		}
	}
}
