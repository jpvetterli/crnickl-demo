/*
 *   Copyright 2011-2017 Hauser Olsson GmbH
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
package ch.agent.crnickl.demo.geocoord;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.hsqldb.cmdline.SqlFile;

import ch.agent.core.KeyedMessage;
import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.MessageListener;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.SimpleDatabaseManager;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueType;
import ch.agent.crnickl.demo.geocoord.Constants.K;
import ch.agent.crnickl.jdbc.JDBCDatabase;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;

/**
 * This is the "main" class for GeoCoord demo.
 * The demo shows two completely unrelated things:
 * <ol>
 * <li>How to extend a base CrNiCKL system with custom value series types.
 * <li>How to set up unusual time domains.
 * </ol>
 * 
 * @author Jean-Paul Vetterli
 */
public class GeoCoordDemo {

	/**
	 * The main method takes exactly one parameter. The parameter
	 * is the name of a file in the file system or a file resource 
	 * on the class path.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) { 
			System.err.println("This program wants a single parameter string\n" +
			"containing a list of comma-separated key-value pairs.\n" +
			"Example: 'file=geo.parm' which names a file in the\n" + 
			"file system or on the class path.");
			System.exit(1);
		}
		try {
			GeoCoordDemo demo = new GeoCoordDemo(args[0]);
			demo.setUpHyperSQLDatabase();
			demo.declareFooTimeToBeOkay();
			String[] ids = demo.setUpSchemaForGeoCoord();
			Range range = new Range(FooTime.DOMAIN, 
					"2012-04-09T12:15:00", "2012-07-09T17:23:42", 
					Adjustment.UP);
			demo.inventSomeData(range, ids);
			demo.playWithData(System.out, new CartesianGeoCoord("0:0:0"), ids);
			System.exit(0);
		} catch (Exception e) {
			System.err.println("There is a problem ...");
			e.printStackTrace(System.err);
			System.exit(2);
		}
	}

	private Random random;
	private Database db;
	
	/**
	 * Construct the demo using parameters from a file.
	 * 
	 * @param parameterFile the name of a parameter file
	 * @throws Exception
	 */
	public GeoCoordDemo(String parameterFile) throws Exception {
		SimpleDatabaseManager sdm = new SimpleDatabaseManager(parameterFile);
		db = sdm.getDatabase();
		setupLogging(db);
		// we'll invent data
		random = new Random();
	}
	
	/**
	 * Create tables and indexes of CrNiCKL. Also 
	 * create a few built-in value types and properties.
	 *  
	 * @throws Exception
	 */
	public void setUpHyperSQLDatabase() throws Exception {
		// create tables and indexes of CrNiCKL
		sql(((JDBCDatabase) db).getConnection(), "sql/HyperSQL_DDL_base.sql");
		// create table for geocoord values
		sql(((JDBCDatabase) db).getConnection(), "sql/HyperSQL_DDL_geocoord.sql");
	}
	
	/**
	 * FooTime is not supported out of the box. Add it to the list of 
	 * allowed time domains.
	 * 
	 * @throws Exception
	 */
	public void declareFooTimeToBeOkay() throws Exception {
		UpdatableValueType<TimeDomain> uvtvt = 
				db.getTimeDomainBuiltInProperty().getValueType().typeCheck(TimeDomain.class).edit();
		uvtvt.addValue(uvtvt.getScanner().scan(FooTime.DOMAIN.getLabel()), null);
		uvtvt.applyUpdates();
	}
	
	/**
	 * Setup the schema. Database tables and indexes must have been defined
	 * with {@link #setUpHyperSQLDatabase()}.
	 * 
	 * @return an array of ID attribute values
	 * @throws Exception
	 */
	public String[] setUpSchemaForGeoCoord() throws Exception {
		createValueTypes();
		createProperties();
		createSchemas();
		createCollection();
		String[] ids = createThings();
		// commit all changes
		db.commit();
		return ids;
	}
	
	/**
	 * Create a value type for {@link GeoCoord} and add it to the list of valid
	 * value types for series. This simple demo does not even need numeric
	 * types. But it needs a textual type for an attribute we'll use for easy
	 * identification.
	 * 
	 * @throws T2DBException
	 */
	protected void createValueTypes() throws T2DBException {
		// define type "geo" for geographical coordinates
		db.createValueType("geo", false, GeoCoordValueScanner.class.getName()).applyUpdates();
		
		// add the "geo" type to the list of allowed series types
		@SuppressWarnings("rawtypes")
		UpdatableValueType<ValueType> uvtvt = 
				db.getTypeBuiltInProperty().getValueType().typeCheck(ValueType.class).edit();
		uvtvt.addValue(uvtvt.getScanner().scan("geo"), null);
		uvtvt.applyUpdates();
		
		// define a "text" type
		db.createValueType("text", false, ValueType.StandardValueType.TEXT.name()).applyUpdates();
		
	}
	
	/**
	 * The demo defines a single property used to help identifying things.
	 * 
	 * @throws T2DBException
	 */
	protected void createProperties() throws T2DBException {
		db.createProperty("ID", db.getValueType("text"), true).applyUpdates();
	}
	
	/**
	 * The demo defines a single schema "Moving things" with just one
	 * attribute "ID" and a single series "position".
	 * 
	 * @throws T2DBException
	 */
	protected void createSchemas() throws T2DBException {
		UpdatableSchema schema = db.createSchema("Moving things", null);
		
		schema.addAttribute(1);
		schema.setAttributeProperty(1, db.getProperty("ID", true));
		
		schema.addSeries(1);
		schema.setSeriesName(1, "position");
		schema.setSeriesDescription(1, "last reported position");
		schema.setSeriesType(1, db.getValueType("geo"));
		schema.setSeriesTimeDomain(1, FooTime.DOMAIN);
		
		schema.applyUpdates();
	}
	
	/**
	 * Create a collection for moving things.
	 * 
	 * @throws T2DBException
	 */
	protected void createCollection() throws T2DBException {
		Schema schema = db.getSchemas("Moving things").iterator().next();
		UpdatableChronicle things = db.getTopChronicle().edit()
				.createChronicle("things", false, "The moving things collection", null, schema);
		things.applyUpdates();
	}

	/**
	 * Create a few things in the "things" collection. In this simple demo, there is only
	 * one collection, and its members would easy to find by name. Because it's more natural
	 * to search by attribute, the things all have an attribute used for identification.
	 * 
	 * @return an array of ID attribute values
	 * @throws Exception
	 */
	public String[] createThings() throws Exception {
		String[] ids = new String[]{"TRUC-42", "MACH-42", "BIDL-42"};
		
		// get the parent chronicle for things 
		UpdatableChronicle things = db.getChronicle("things", true).edit();
		
		createThing(things, "truc", "Un truc", ids[0]);
		createThing(things, "machin", "Un machin", ids[1]);
		createThing(things, "bidule", "Un bidule", ids[2]); 
		return ids;
	}
	
	/**
	 * Helper method to create a chronicle.
	 * 
	 * @param parent the collection
	 * @param name a string naming the chronicle within its parent collection
	 * @param description a string describing the chronicle
	 * @param idAttribute a string identifying the chronicle, uniquely if possible (not mandatory)
	 * @throws Exception
	 */
	protected void createThing(UpdatableChronicle parent, String name, String description, String idAttribute) throws Exception {
		UpdatableChronicle thing = parent.createChronicle(name, false, description, null, null);
		Attribute<String> id = thing.getAttribute("ID", true).typeCheck(String.class);
		id.scan(idAttribute);
		thing.setAttribute(id);
		thing.applyUpdates();
	}
	
	/**
	 * Generate random geographical positions. 
	 * 
	 * @param range time range
	 * @param ids list of strings
	 * @throws Exception
	 */
	public void inventSomeData(Range range, String... ids) throws Exception {
		Chronicle[] things = chronicles(ids);
		for (Chronicle thing : things) {
			inventSomeData(thing.edit(), range);
		}
	}
	
	protected void inventSomeData(UpdatableChronicle thing, Range range) throws Exception {
		UpdatableSeries<GeoCoord> series = thing.updateSeries("position");
		if (series == null)
			series = thing.createSeries("position");
		for (TimeIndex t : range) {
			GeoCoord coord = new CartesianGeoCoord(ran(), ran(), ran());
			series.setValue(t, coord);
		}
		thing.applyUpdates();
	}
	
	/**
	 * In the common range of the things, print the shortest distance between a reference
	 * point and a list of things passed as an array.
	 *  
	 * @param out stream taking the output
	 * @param ref reference point
	 * @param ids array of a things
	 * @throws Exception
	 */
	public void playWithData(PrintStream out, GeoCoord ref, String... ids) throws Exception {
		Range range = null;
		Chronicle[] things = chronicles(ids);
		List<Series<GeoCoord>> positions = new ArrayList<Series<GeoCoord>>();
		for(Chronicle thing : things) {
			Series<GeoCoord> s = thing.getSeries("position");
			if (s != null) 
				positions.add(s);
		}
		for (Series<GeoCoord> position : positions) {
			if (range == null)
				range = position.getRange();
			else
				range = range.intersection(position.getRange());
		}
		if (range != null) {
			for (TimeIndex t : range) {
				double min = Double.MAX_VALUE;
				String winner = null;
				for (Series<GeoCoord> position : positions) {
					double dist = ref.distanceTo(position.getValue(t));
					if (dist < min) {
						min = dist;
						winner = position.getChronicle().getName(false);
					}
				}
				out.println(String.format("%s %6.0fkm (%s)", t.toString(), min/1000d, winner));
			}
		}
	}
	
	/**
	 * @return random double between plus and minus 10'000'000.
	 */
	private double ran() {
		return 20000000d * (random.nextDouble() - 0.5d);
	}

	/**
	 * Return chronicles identified by their ID attributes.
	 * Throw an exception when one is not found. Log an error
	 * when one is not unique.
	 * 
	 * @param ids array of attribute values
	 * @return array of chronicles
	 * @throws Exception
	 */
	private Chronicle[] chronicles(String... ids) throws Exception {
		List<Chronicle> chronicles = new ArrayList<Chronicle>();
		Property<String> idProp = db.getProperty("ID", true).typeCheck(String.class);
		for (String id : ids) {
			List<Chronicle> found = idProp.getChronicles(id, 0);
			switch (found.size()) {
			case 0:
				throw K.NO_SUCH_ID.exception(id);
			case 1:
				chronicles.add(found.get(0));
				break;
			default:
				Exception e = K.DUPLICATE_ID.exception(id);
				// don't throw it, log it
				db.getMessageListener().log(e);
			}
		}
		return chronicles.toArray(new Chronicle[chronicles.size()]);
	}

	/**
	 * Setup logging. Log only exceptions.
	 * 
	 * @param db the database
	 */
	private void setupLogging(Database db) {
		db.setMessageListener(new MessageListener() {

			@Override
			public void setFilterLevel(Level level) {
			}

			@Override
			public void log(Level level, String text) {
			}

			@Override
			public void log(Level level, KeyedMessage msg) {
			}

			@Override
			public void log(Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace(System.err);
			}

			@Override
			public boolean isListened(Level level) {
				return false;
			}
		});

	}
	
	/**
	 * Execute the SQL in a resource. This is HyperSQL specific but handy when
	 * testing or playing.
	 * 
	 * @param c JDBC connection
	 * @param resource string naming resource
	 * @throws Exception
	 */
	private static void sql(Connection c, String resource) throws Exception {
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
		if (inputStream == null)
		 	inputStream = new FileInputStream(resource);
		SqlFile sqlf = new SqlFile(new InputStreamReader(inputStream), resource, null, null, false, null);
		sqlf.setConnection(c);
		sqlf.execute();
	}

	
}
