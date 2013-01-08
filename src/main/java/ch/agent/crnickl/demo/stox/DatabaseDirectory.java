/*
 *   Copyright 2011-2013 Hauser Olsson GmbH
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
 */
package ch.agent.crnickl.demo.stox;

import java.io.PrintStream;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.AttributeDefinition;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.SeriesDefinition;
import ch.agent.crnickl.api.ValueType;
import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;

/**
 * A database directory has methods to print the contents of a database.
 * It is meant for tiny demo database. Don't use on a real database.
 * 
 * @author Jean-Paul Vetterli
 */
public class DatabaseDirectory {

	private Database db;

	/**
	 * Construct a database directory.
	 * 
	 * @param db a database
	 */
	public DatabaseDirectory(Database db) {
		super();
		this.db = db;
	}
	
	/**
	 * List all {@link Property}s.
	 * 
	 * @param out a print stream
	 * @throws T2DBException
	 */
	public void properties(PrintStream out) throws T2DBException {
		out.println ("Properties (with value types) : ");
		for (Property<?> prop : db.getProperties("*")) {
			out.println(String.format("+-- %s (%s)", prop,  prop.getValueType()));
		}
	}
	
	/**
	 * List all {@link ValueType}s.
	 * 
	 * @param out a print stream
	 * @throws T2DBException
	 */
	public void valueTypes(PrintStream out) throws T2DBException {
		out.println ("Value types (with base type) : ");
		for (ValueType<?> vt : db.getValueTypes("*")) {
			out.println(String.format("+-- %s (%s)", vt,  vt.getType().getSimpleName()));
			if (vt.isRestricted()) {
				for (String value : vt.getValues(null)) {
					out.println(String.format("|   +-- %s", value));
				}
			}
		}
	}

	/**
	 * List all {@link Schema}s.
	 * 
	 * @param out a print stream
	 * @throws T2DBException
	 */
	public void schemas(PrintStream out) throws T2DBException {
		out.println ("Schemas : ");
		for (Schema schema : db.getSchemas("*")) {
			out.println(String.format("+-- %s", schema));
			for (AttributeDefinition<?> def : schema.getAttributeDefinitions()) {
				out.println(String.format("|   +-- attribute %s (default: %s)", def.getName(), def.getValue()));
			}
			for (SeriesDefinition sdef : schema.getSeriesDefinitions()) {
				TimeDomain timeDom = sdef.getTimeDomain();
				out.println(String.format("|   +-- series %s (\"%s\", type: %s, time domain: %s, sparse: %b)", 
						sdef.getName(), sdef.getDescription(), 
						sdef.getValueType(), 
						timeDom == null ? null : timeDom.getLabel(),
						sdef.isSparse()));
				for (AttributeDefinition<?> adef : sdef.getCustomAttributeDefinitions()) {
					out.println(String.format("|   |   +-- attribute %s (default: %s)", adef.getName(), adef.getValue()));
				}
				
			}
		}
	}

	/**
	 * List all {@link Chronicle}s, recursively. THIS CAN PRODUCE A HUGE OUTPUT.
	 * 
	 * @param out a print stream
	 * @throws T2Exception
	 * @throws T2DBException
	 */
	public void chronicles(PrintStream out) throws T2Exception, T2DBException {
		out.println ("Chronicles : ");
		chronicles(out, db.getTopChronicle());
	}
	
	/**
	 * List all {@link Chronicle}s in a collection, recursively. THIS CAN PRODUCE A HUGE OUTPUT.
	 * 
	 * @param out a print stream
	 * @param chronicle a chronicle defining a collection
	 * @throws T2Exception
	 * @throws T2DBException
	 */
	public void chronicles(PrintStream out, Chronicle chronicle) throws T2Exception, T2DBException {
		if (!chronicle.isTopChronicle()) {
			Schema schema = chronicle.getSchema(false);
			if (schema != null)
				out.println(String.format("%s (\"%s\", schema: %s)", chronicle.getName(true), chronicle.getDescription(false), schema));
			else
				out.println(String.format("%s (\"%s\")", chronicle.getName(true), chronicle.getDescription(false)));
		}
		for (Attribute<?> a : chronicle.getAttributes()) {
			out.println(String.format("  +-- attribute %s ", a));
		}
		for (Series<?> s : chronicle.getSeries()) {
			Range range = s.getRange();
			out.println(String.format("  +-- series %s (%s, range: %s)", s.getName(false), s.getDescription(false), range));
			if (!range.isEmpty())
				out.println(String.format("      +-- sample %s", s.getValues(null).toString()));
		}
		for (Chronicle c : chronicle.getMembers()) {
			chronicles(out, c);
		}
	}

}
