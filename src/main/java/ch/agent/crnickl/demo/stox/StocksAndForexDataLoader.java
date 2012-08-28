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
 * Type: StocksAndForexDataLoader
 * Version: 1.1.0
 */
package ch.agent.crnickl.demo.stox;

import ch.agent.core.KeyedException;
import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Attribute;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableSeries;
import ch.agent.crnickl.demo.stox.DemoConstants.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Workday;

/**
 * StocksAndForexDataLoader sets up the actual data used in the demo.
 * Some of the data comes from file resources. Some is made up on the fly.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 */
public class StocksAndForexDataLoader {
	
	private static final boolean SKIP_LINE_1 = true;
	
	/**
	 * A visitor to read rows from spreadsheets into series.
	 */
	public class Visitor implements CSVFile.RowVisitor {

		private Range range;
		private int dateColumnOffset;
		private int[] seriesColumnOffset;
		private UpdatableSeries<Double>[] series;
		private TimeDomain timeDomain;
		private int columnLength;
		
		/**
		 * Construct an object to process each row of the data file.
		 * 
		 * @param range range of data to keep 
		 * @param dateColumnOffset offset of the column containing the date
		 * @param seriesCount the number of series to extract from each row
		 */
		@SuppressWarnings("unchecked")
		public Visitor(Range range, int dateColumnOffset, int seriesCount) {
			super();
			if (dateColumnOffset < 0)
				throw new IllegalArgumentException(K.DATE_COL_NEG_ERR.val());
			this.range = range;
			if (range != null)
				timeDomain = range.getTimeDomain();
			this.dateColumnOffset = dateColumnOffset;
			seriesColumnOffset = new int[seriesCount];
			series = new UpdatableSeries[seriesCount];
			columnLength = -1;
		}

		@Override
		public void visit(int line, String... column) throws Exception {
			if (SKIP_LINE_1 && line == 1)
				return;
			if (columnLength < 0) {
				columnLength = column.length;
				if (dateColumnOffset >= columnLength)
					throw new IllegalStateException(K.DATE_COL_ERR.val());
				for (int i = 0; i < series.length; i++) {
					if (seriesColumnOffset[i] >= columnLength)
						throw new IllegalStateException(K.SER_COL_ERR.val(i, seriesColumnOffset[i]));
				}
			} else {
				if (column.length != columnLength)
					throw new RuntimeException(K.COL_COUNT_ERR.val(column.length, columnLength, line));
			}
			TimeIndex t = timeDomain.time(column[dateColumnOffset]);
			if (range == null || range.isInRange(t)) { 
				for (int i = 0; i < series.length; i++) {
					if (seriesColumnOffset[i] >= columnLength)
						throw new IllegalStateException(K.SER_COL_ERR.val(i, seriesColumnOffset[i]));
					series[i].scanValue(t, column[seriesColumnOffset[i]]);
				}
			}
		}
		
		/**
		 * Sets a series to be loaded.
		 * 
		 * @param index a number 
		 * @param offset a column offset
		 * @param series a series
		 * @throws KeyedException
		 */
		public void setSeries(int index, int offset, UpdatableSeries<Double> series) throws KeyedException {
			if (columnLength >= 0)
				throw new IllegalStateException(K.TOO_LATE_ERR.val());
			if (offset < 0)
				throw new IllegalArgumentException(K.SER_COL_NEG_ERR.val(index));
			if (index < 0 || index >= seriesColumnOffset.length)
				throw new IndexOutOfBoundsException(K.SER_NUM_ERR.val(index));
			this.seriesColumnOffset[index] = offset;
			this.series[index] = series.edit();
			if (timeDomain == null)
				timeDomain = series.getTimeDomain();
			else
				if (!timeDomain.equals(series.getTimeDomain()))
					throw K.SER_DOMAIN_ERR.exception(timeDomain.getLabel());
		}
		
	}
	
	private Database db;
	private UpdatableChronicle fbi, kgb, usdfum;
	
	/**
	 * Construct the demo data loader.
	 * 
	 * @param db a database
	 */
	public StocksAndForexDataLoader(Database db) {
		super();
		this.db = db;
	}

	/**
	 * Create chronicles for 2 stocks and 1 exchange rate.
	 * 
	 * @throws T2DBException
	 */
	public void createChronicles() throws T2DBException {
		
		// get the parent chronicle for stocks 
		UpdatableChronicle stocks = db.getChronicle(K.STOCK_CHRON.val(), true).edit();
		
		// create a stock chronicle for "FBI"
		fbi = stocks.createChronicle("fbi", false, "Foo & Bar, Inc", null, null);
		Attribute<Currency> currency = fbi.getAttribute(K.CURR_PROP.val(), true).typeCheck(Currency.class);
		currency.scan("USD");
		fbi.setAttribute(currency);
		Attribute<String> ticker = fbi.getAttribute(K.TICKER_PROP.val(), true).typeCheck(String.class);
		ticker.scan("FBI");
		fbi.setAttribute(ticker);
		fbi.applyUpdates();
		
		// create a stock chronicle for "KGB"
		kgb = stocks.createChronicle("kgb", false, "Kolossale Geschäftsbank", null, null);
		currency.scan("FUM");
		kgb.setAttribute(currency);
		ticker.scan("KGB");
		kgb.setAttribute(ticker);
		kgb.applyUpdates();

		// create an exchange rate for USDs into FUMs
		UpdatableChronicle forex = db.getChronicle(K.FOREX_CHRON.val(), true).edit();
		usdfum = forex.createChronicle("whatever", false, "USD/FUM", null, null);
		currency.scan("FUM");
		usdfum.setAttribute(currency);
		// note getting the attribute from forex; getting it from usdfum would also work
		Attribute<Currency> bcurrency = forex.getAttribute(K.CURR_BOUGHT_PROP.val(), true).typeCheck(Currency.class);
		bcurrency.scan("USD");
		usdfum.setAttribute(bcurrency);
		usdfum.applyUpdates();
	}
	
	/**
	 * Load the data in a given range.
	 * 
	 * @param range a range or null to load all the data found
	 * @throws KeyedException
	 */
	public void loadData(Range range) throws KeyedException {
		if (fbi == null)
			createChronicles();
		
		// load price and volume data for FBI
		CSVFile file = new CSVFile(",");
		Visitor visitor = new Visitor(range, 0, 2);
		UpdatableSeries<Double> price = fbi.updateSeries(K.PRICE_SER.val());
		if (price == null)
			price = fbi.createSeries(K.PRICE_SER.val());
		visitor.setSeries(0, 4, price);
		UpdatableSeries<Double> volume = fbi.updateSeries(K.VOLUME_SER.val());
		if (volume == null)
			volume = fbi.createSeries(K.VOLUME_SER.val());
		visitor.setSeries(1, 5, volume);
		file.scan("data/FBI.csv", visitor);
		price.applyUpdates();
		volume.applyUpdates();

		// load splits info for FBI
		visitor = new Visitor(range, 0, 1);
		UpdatableSeries<Double> splits = fbi.updateSeries(K.SPLITS_SER.val());
		if (splits == null)
			splits = fbi.createSeries(K.SPLITS_SER.val());
		visitor.setSeries(0, 1, splits);
		file.scan("data/FBISplits.csv", visitor);
		splits.applyUpdates();
	
		// load price data for KGB
		visitor = new Visitor(range, 0, 2);
		price = kgb.updateSeries(K.PRICE_SER.val());
		if (price == null)
			price = kgb.createSeries(K.PRICE_SER.val());
		visitor.setSeries(0, 4, price);
		/* Note: volume data consist only of "missing values", so volume series 
		 * will exist with an empty range. */
		volume = kgb.updateSeries(K.VOLUME_SER.val());
		if (volume == null)
			volume = kgb.createSeries(K.VOLUME_SER.val());
		visitor.setSeries(1, 5, volume);
		file.scan("data/KGB.csv", visitor);
		price.applyUpdates();
		volume.applyUpdates();
		
		// exchange rates will be invented
		UpdatableSeries<Double> rate = usdfum.updateSeries(K.RATE_SER.val());
		if (rate == null)
			rate = usdfum.createSeries(K.RATE_SER.val());
		Range generationRange = new Range(Workday.DOMAIN, "1990-01-01", "2010-12-31", Adjustment.DOWN);
		double value = 0.6;
		double dailyIncrease = 1.001;
		for (TimeIndex t : generationRange) {
			value = value * dailyIncrease;
			rate.setValue(t, value);
		}
		rate.applyUpdates();
	}

}
