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
 * Type: StocksAndForexDemo
 * Version: 1.1.0
 */
package ch.agent.crnickl.demo.stox;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.hsqldb.cmdline.SqlFile;

import ch.agent.core.KeyedException;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.Chronicle;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Series;
import ch.agent.crnickl.api.SimpleDatabaseManager;
import ch.agent.crnickl.demo.stox.Chart.ChartSeries;
import ch.agent.crnickl.demo.stox.DemoConstants.K;
import ch.agent.crnickl.jdbc.JDBCDatabase;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;

/**
 * Main class for demo.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 */
public class StocksAndForexDemo {

	/**
	 * The main method takes exactly one parameter. The parameter
	 * is the name of a file in the file system or a file resource 
	 * on the class path.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) { 
			System.err.println("This program wants the name of a parameter file, " +
			"which can reside in the file system or on the class path.");
			System.exit(1);
		}
		try {
			StocksAndForexDemo demo = new StocksAndForexDemo(args[0]);
			demo.setUpHyperSQLDatabase();
			demo.setUpSchema();
			demo.parseRange();
			demo.loadData();
			demo.displayDatabase(System.out);
			demo.makeChart1();
			demo.makeChart2();
			demo.makeChart3();
			System.exit(0);
		} catch (Exception e) {
			System.err.println("There is a problem ...");
			e.printStackTrace(System.err);
			System.exit(2);
		}
	}

	private Database db;
	private Map<String, String> parameters;
	private Range range;
	
	/**
	 * Construct the demo using parameters from a file.
	 * 
	 * @param parameterFile the name of a parameter file
	 * @throws Exception
	 */
	public StocksAndForexDemo(String parameterFile) throws Exception {
		SimpleDatabaseManager sdm = new SimpleDatabaseManager("file=" + parameterFile);
		db = sdm.getDatabase();
		parameters = sdm.getParameters();
	}
	
	/**
	 * Create tables and indexes of CrNiCKL. Also 
	 * create a few built-in value types and properties.
	 *  
	 * @throws Exception
	 */
	public void setUpHyperSQLDatabase() throws Exception {
		// create tables and indexes of CrNiCKL
		sql(((JDBCDatabase) db).getConnection(), "Resources/HyperSQL_DDL_base.sql");
	}
	
	/**
	 * Setup the schema. Database tables and indexes must have been defined
	 * with {@link #setUpHyperSQLDatabase()}.
	 * 
	 * @throws Exception
	 */
	public void setUpSchema() throws Exception {
		StocksAndForexSchema schema = new StocksAndForexSchema(db);
		schema.createSchema();
		// commit all changes
		db.commit();
	}
	
	/**
	 * Create 3 chronicles and load them with data from files.
	 * The full names of the 3 chronicles are:
	 * <ul>
	 * <li>stocks.fbi 
	 * <li>stocks.kgb
	 * <li>forex.fumusd
	 * </ul> 
	 * Before using this method, create the schema with {@link #setUpSchema()}.
	 * 
	 * @throws Exception
	 */
	public void loadData() throws Exception {
		StocksAndForexDataLoader loader = new StocksAndForexDataLoader(db);
		loader.createChronicles();
		loader.loadData(range);
	}
	
	/**
	 * Parse the range parameter. No parameter means null range, which should
	 * be interpreted as "range not restricted".
	 * 
	 * @throws Exception
	 */
	public void parseRange() throws KeyedException {
		String rangeString = parameters.get(K.RANGE_PARAM.val());
		range = null; 
		if (rangeString != null) {
			try {
				TimeDomain domain = db.getSchemas(K.STOCKS_SCHEMA.val()).iterator().next().getSeriesDefinition(1, true).getTimeDomain();
				String[] dates = rangeString.split(",", 2);
				if (dates.length == 2) {
					range = new Range(domain, dates[0], dates[1], Adjustment.DOWN);
				} else
					throw K.RANGE_DATES_ERR.exception();
			} catch (Exception e) {
				throw K.RANGE_ERR.exception(e, rangeString);
			}
		}
	}

	
	/**
	 * Print some information about what is in the database. Using this method
	 * when the database has not been setup throws an exception.
	 * 
	 * @throws Exception
	 */
	public void displayDatabase(PrintStream out) throws Exception {
		out.println (String.format("Database : %s", db));
		out.println ();
		DatabaseDirectory directory = new DatabaseDirectory(db);
		directory.valueTypes(out);
		out.println ();
		directory.properties(out);
		out.println ();
		directory.schemas(out);
		out.println ();
		directory.chronicles(out);
	}
	
	/**
	 * Display FBI vs KG unadjusted.
	 * 
	 * @throws KeyedException
	 */
	public void makeChart1() throws KeyedException {
		String output = parameters.get(K.CHART_OUTPUT1_PARAM.val());
		if (output == null || output.length() == 0)
			return;
		
		Chart chart = new Chart();
		
		Property<String> ticker = db.getProperty(K.TICKER_PROP.val(), true).typeCheck(String.class);
		Chronicle fbi = ticker.getChronicles(ticker.scan("FBI"), 1).get(0);
		Series<Double> price = fbi.getSeries(K.PRICE_SER.val()).typeCheck(Double.class);
		Series<Double> volume = fbi.getSeries(K.VOLUME_SER.val()).typeCheck(Double.class);
		
		String title = parameters.get(K.CHART_TEXT1_PARAM.val());
		int width = asInteger(K.CHART_WIDTH_PARAM.val());
		int height = asInteger(K.CHART_HEIGHT_PARAM.val());
		
		chart.setTitle(title);
		chart.setWithLegend(true);
		chart.setRange(range);

		ChartSeries chartSeries = new ChartSeries(price.getValues(range), price.getName(true));
		chartSeries.setWeight(3);
		chart.addChartSeries(chartSeries);
		
		chartSeries = new ChartSeries(volume.getValues(range), volume.getName(true));
		chartSeries.setLine(false);
		chart.addChartSeries(chartSeries);
		
		Chronicle kgb = ticker.getChronicles(ticker.scan("KGB"), 1).get(0);
		price = kgb.getSeries(K.PRICE_SER.val()).typeCheck(Double.class);
		chartSeries = new ChartSeries(price.getValues(range), price.getName(true));
		chartSeries.setSubPlotIndex(1);
		chart.addChartSeries(chartSeries);

		chart.save(output, width, height);
		System.out.println(output);
	}
	
	/**
	 * Display FBI vs KG in dollars.
	 * 
	 * @throws KeyedException
	 */
	public void makeChart2() throws KeyedException {
		String output = parameters.get(K.CHART_OUTPUT2_PARAM.val());
		if (output == null || output.length() == 0)
			return;

		Chart chart = new Chart();
		
		Property<String> ticker = db.getProperty(K.TICKER_PROP.val(), true).typeCheck(String.class);
		Chronicle fbi = ticker.getChronicles(ticker.scan("FBI"), 1).get(0);
		Series<Double> price = fbi.getSeries(K.PRICE_SER.val()).typeCheck(Double.class);
		
		String title = parameters.get(K.CHART_TEXT2_PARAM.val());
		int width = asInteger(K.CHART_WIDTH_PARAM.val());
		int height = asInteger(K.CHART_HEIGHT_PARAM.val());
		
		chart.setTitle(title);
		chart.setWithLegend(true);
		chart.setRange(range);

		ChartSeries chartSeries = new ChartSeries(price.getValues(range), price.getName(true));
		chartSeries.setWeight(3);
		chart.addChartSeries(chartSeries);
		
		Chronicle kgb = ticker.getChronicles(ticker.scan("KGB"), 1).get(0);
		price = kgb.getSeries(K.PRICE_SER.val()).typeCheck(Double.class);
		
		Property<Currency> currency = db.getProperty(K.CURR_BOUGHT_PROP.val(), true).typeCheck(Currency.class);
		List<Chronicle> result = currency.getChronicles(currency.scan("USD"), 1000);
		Chronicle usdfum = null;
		Attribute<Currency> kgbCurrency = kgb.getAttribute("Currency", true).typeCheck(Currency.class);
		for (Chronicle c : result) {
			if (c.getAttribute("Currency", true).equals(kgbCurrency)) {
				usdfum = c;
				break;
			}
		}
		Series<Double> rate = usdfum.getSeries(K.RATE_SER.val());
		Range rateRange = rate.getRange();
		if (!rateRange.isInRange(range))
			throw K.CHART_EXRATE_RANGE_ERR.exception(rate.getChronicle().getDescription(false), rateRange, range);

		TimeAddressable<Double> priceTS = price.getValues(range);
		TimeAddressable<Double> rateTS = rate.getValues(range);
		for (Observation<Double> obs : priceTS) {
			priceTS.put(obs.getIndex(), obs.getValue() * rateTS.get(obs.getIndex()));
		}
		chartSeries = new ChartSeries(priceTS, price.getName(true));
		chartSeries.setSubPlotIndex(1);
		chart.addChartSeries(chartSeries);

		chart.save(output, width, height);
		System.out.println(output);
	}

	/**
	 * Display FBI adjusted for splits vs KG in dollars.
	 * 
	 * @throws KeyedException
	 */
	public void makeChart3() throws KeyedException {
		String output = parameters.get(K.CHART_OUTPUT3_PARAM.val());
		if (output == null || output.length() == 0)
			return;
		
		Chart chart = new Chart();
		
		Property<String> ticker = db.getProperty(K.TICKER_PROP.val(), true).typeCheck(String.class);
		Chronicle fbi = ticker.getChronicles(ticker.scan("FBI"), 1).get(0);
		Series<Double> price = fbi.getSeries(K.PRICE_SER.val()).typeCheck(Double.class);
		
		String title = parameters.get(K.CHART_TEXT3_PARAM.val());
		int width = asInteger(K.CHART_WIDTH_PARAM.val());
		int height = asInteger(K.CHART_HEIGHT_PARAM.val());
		
		chart.setTitle(title);
		chart.setWithLegend(true);
		chart.setRange(range);

		// adjust FBI price for splits
		Series<Double> splits = fbi.getSeries(K.SPLITS_SER.val()).typeCheck(Double.class);
		
		// step 1 : compound splits
		TimeAddressable<Double> splitsTS = splits.getValues(range);
		TimeAddressable<Double> compoundSplits = splitsTS.makeEmptyCopy();
		Double current = 1d;
		for (Observation<Double> obs : splitsTS) {
			if (!splitsTS.isMissing(obs.getValue())) { 
				current = current * obs.getValue();
				compoundSplits.put(obs.getIndex(), current);
			}
		}
		
		// step 2 : apply
		TimeAddressable<Double> priceTS = price.getValues(range, true);
		for (Observation<Double> obs : priceTS) {
			Observation<Double> ratio = compoundSplits.getLast(obs.getTime());
			if (ratio != null) {
				priceTS.put(obs.getIndex(), obs.getValue() * ratio.getValue());
			}
		}
		
		ChartSeries chartSeries = new ChartSeries(priceTS, price.getName(true));
		chartSeries.setWeight(3);
		chart.addChartSeries(chartSeries);
		
		Chronicle kgb = ticker.getChronicles(ticker.scan("KGB"), 1).get(0);
		price = kgb.getSeries(K.PRICE_SER.val()).typeCheck(Double.class);
		
		Property<Currency> currency = db.getProperty(K.CURR_BOUGHT_PROP.val(), true).typeCheck(Currency.class);
		List<Chronicle> result = currency.getChronicles(currency.scan("USD"), 1000);
		Chronicle usdfum = null;
		Attribute<Currency> kgbCurrency = kgb.getAttribute("Currency", true).typeCheck(Currency.class);
		for (Chronicle c : result) {
			if (c.getAttribute("Currency", true).equals(kgbCurrency)) {
				usdfum = c;
				break;
			}
		}
		Series<Double> rate = usdfum.getSeries(K.RATE_SER.val());
		Range rateRange = rate.getRange();
		if (!rateRange.isInRange(range))
			throw K.CHART_EXRATE_RANGE_ERR.exception(rate.getChronicle().getDescription(false), rateRange, range);

		priceTS = price.getValues(range);
		TimeAddressable<Double> rateTS = rate.getValues(range);
		for (Observation<Double> obs : priceTS) {
			priceTS.put(obs.getIndex(), obs.getValue() * rateTS.get(obs.getIndex()));
		}
		chartSeries = new ChartSeries(priceTS, price.getName(true));
		chartSeries.setSubPlotIndex(1);
		chart.addChartSeries(chartSeries);

		chart.save(output, width, height);
		System.out.println(output);
	}
	
	private static void sql(Connection c, String resource) throws Exception {
		InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);
		if (inputStream == null)
		 	inputStream = new FileInputStream(resource);
		SqlFile sqlf = new SqlFile(new InputStreamReader(inputStream), resource, null, null, false, null);
		sqlf.setConnection(c);
		sqlf.execute();
	}

	private int asInteger(String name) throws KeyedException {
		String val = parameters.get(name);
		try {
			return new Integer(val);
		} catch (Exception e) {
			throw K.PARAMETER_ERR.exception(name, val);
		}
	}
	
	protected void dump(TimeAddressable<Double> ts, String file) {
		try {
			 PrintStream out = new PrintStream(new File(file));
			 for (Observation<Double> obs : ts)
				 out.println(obs);
			 out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
