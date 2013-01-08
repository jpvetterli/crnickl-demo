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

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.Database;
import ch.agent.crnickl.api.Property;
import ch.agent.crnickl.api.Schema;
import ch.agent.crnickl.api.UpdatableChronicle;
import ch.agent.crnickl.api.UpdatableSchema;
import ch.agent.crnickl.api.UpdatableValueType;
import ch.agent.crnickl.api.ValueType;
import ch.agent.crnickl.demo.stox.DemoConstants.K;
import ch.agent.t2.time.Workday;

/**
 * StocksAndForexSchema creates the CrNiCKL schema for the demo.
 *  
 * @author Jean-Paul Vetterli
 */
public class StocksAndForexSchema {
	private Database db;

	/**
	 * Constuct an object to set up the schema for the demo. The constructor
	 * creates two generally useful value types:
	 * <ul>
	 * <li>text
	 * <li>numeric
	 * </ul>
	 * Both use built-in value scanners specified by
	 * StandardValueType keywords. Like other methods in this
	 * class, the constructor applies updates but does not commit.
	 * <p>
	 * The constructor also adds the value type <em>numeric</em> to the values
	 * of the built-in value type <em>type</em>. This is the value type used for 
	 * series, and the demo will make use of numeric series.
	 * 
	 * @param db
	 */
	public StocksAndForexSchema(Database db) throws T2DBException {
		super();
		this.db = db;
		// define the text type
		db.createValueType(K.TEXT_TYPE.val(), false, ValueType.StandardValueType.TEXT.name())
			.applyUpdates();
		
		// define the numeric type
		db.createValueType(K.NUM_TYPE.val(), false, ValueType.StandardValueType.NUMBER.name())
			.applyUpdates();
		
		// add the numeric type to the list of allowed series types
		@SuppressWarnings("rawtypes")
		UpdatableValueType<ValueType> uvtvt = 
			db.getTypeBuiltInProperty().getValueType().typeCheck(ValueType.class).edit();
		uvtvt.addValue(uvtvt.getScanner().scan(K.NUM_TYPE.val()), null);
		uvtvt.applyUpdates();
	}
	
	/**
	 * Create the schema using methods in the correct sequence.
	 * <p>
	 * Updates are applied but not committed.
	 * 
	 * @throws T2DBException
	 */
	public void createSchema() throws T2DBException {
		createCurrencyValueTypeAndProperty();
		createSeriesUnitValueTypeAndProperty();
		createTickerProperty();
		createStocksSchema();
		createExchangeRatesSchema();
		createTopLevelChronicles();
	}
	
	/**
	 * Create a value type and a property for currencies. Use the same
	 * label for both.
	 * <p>
	 * The currency is created as a "non-indexed" property.
	 * This means that it is usually a bad idea to search for chronicles "by currency".
	 * Imagine searching a database of 10000 US stocks for all stocks in
	 * dollars. You would just get a list of 10000 stocks. However, it is
	 * not forbidden to search by currency. For example, you would search the much
	 * smaller database of foreign exchange rates for all dollar exchange rates. The
	 * search result would be small enough to display as result in a small window.
	 * <p>
	 * The method applies updates but does not commit.  
	 * 
	 * @throws T2DBException
	 */
	public void createCurrencyValueTypeAndProperty() throws T2DBException {
		// create the value type
		UpdatableValueType<Currency> uvt = 
				db.createValueType(K.CURR_PROP.val(), true, ch.agent.crnickl.demo.stox.CurrencyValueScanner.class.getName());

		// add some values
		uvt.addValue(new Currency("CNY"), "Yuan renminbi");
		uvt.addValue(new Currency("GBP"), "Pound sterling");
		uvt.addValue(new Currency("JPY"), "Japanese yen");
		uvt.addValue(new Currency("FUM"), "Funny money");
		uvt.addValue(new Currency("USD"), "US dollar");
		// add an empty currency which will be used as default value in schemas
		uvt.addValue(new Currency(""), "");
		
		uvt.applyUpdates();
		
		// create the property
		db.createProperty(K.CURR_PROP.val(), uvt, false).applyUpdates();
	}
	
	/**
	 * Create a value type and a property for series units. Use the same label
	 * for both.
	 * <p>
	 * The series units is created as a "non-indexed" property. Series attribute
	 * play a purely informative role and there is no support for searching by
	 * series attribute value in CrNiCKL.
	 * <p>
	 * Unlike currencies, series units do not play an important role in the
	 * demo. There is no class for them. The value type is a straightforward
	 * text type.
	 * <p>
	 * The method applies updates but does not commit.
	 * 
	 * @throws T2DBException
	 */
	public void createSeriesUnitValueTypeAndProperty() throws T2DBException {
		// create the value type
		UpdatableValueType<String> uvt = 
				db.createValueType(K.UNIT_PROP.val(), true, ValueType.StandardValueType.TEXT.name());

		// add some values
		uvt.addValue("currency", "price in currency");
		uvt.addValue("shares", "number of shares traded");
		uvt.addValue("", "(dimension-less)"); 
		// hint: dimension-less series can alternatively leave the unit unspecified

		uvt.applyUpdates();
		
		// create the property
		db.createProperty(K.UNIT_PROP.val(), uvt, false).applyUpdates();
	}
	
	/**
	 * Create a property for tickers.
	 * <p>
	 * The ticker is a straightforward textual property and is created as "indexed".
	 * This means that it is meaningful to search for chronicles "by ticker symbol".
	 * Imagine searching a database of 1 million worldwide securities for a given ticker.
	 * Tickers are not unique across different financial markets but have a relatively high
	 * "resolution". The result of the search is likely to include only a few securities.
	 * <p>
	 * The method applies updates but does not commit.  
	 * 
	 * @throws T2DBException
	 */
	public void createTickerProperty() throws T2DBException {
		ValueType<String> vt = db.getValueType(K.TEXT_TYPE.val()).typeCheck(String.class);
		// create the property
		db.createProperty(K.TICKER_PROP.val(), vt, false).applyUpdates();
	}
	
	/**
	 * The Stocks schema defines 3 series and 2 attributes for Stocks chronicles.
	 * The series are:
	 * <ul>
	 * <li>price
	 * <li>volume
	 * <li>split
	 * </ul>
	 * All series are <em>numeric</em> with data on <em>working days</em>. 
	 * <p>
	 * The attributes are:
	 * <ul>
	 * <li>ticker
	 * <li>currency
	 * </ul>
	 * Ticker is a <em>text</em> attribute, and currency is a <em>currency</em> attribute.
	 * <p>
	 * The method applies updates but does not commit.  
	 * <p>
	 * @throws T2DBException
	 */
	public void createStocksSchema() throws T2DBException {
		UpdatableSchema schema = db.createSchema(K.STOCKS_SCHEMA.val(), null);
		schema.addAttribute(1);
		schema.setAttributeProperty(1, db.getProperty(K.TICKER_PROP.val(), true));
		schema.addAttribute(2);
		Property<?> prop = db.getProperty(K.CURR_PROP.val(), true);
		schema.setAttributeProperty(2, prop);
		schema.setAttributeDefault(2, prop.scan(""));
		
		schema.addSeries(1);
		schema.setSeriesName(1, K.PRICE_SER.val());
		schema.setSeriesDescription(1, "close price");
		schema.setSeriesType(1, db.getValueType(K.NUM_TYPE.val()));
		schema.setSeriesTimeDomain(1, Workday.DOMAIN);
		schema.addAttribute(1, 5);
		schema.setAttributeProperty(1, 5, db.getProperty(K.UNIT_PROP.val(), true));
		schema.setAttributeDefault(1, 5, K.UNIT_VALUE_CURRENCY.val());
		
		schema.addSeries(2);
		schema.setSeriesName(2, K.VOLUME_SER.val());
		schema.setSeriesDescription(2, "total volume traded");
		schema.setSeriesType(2, db.getValueType(K.NUM_TYPE.val()));
		schema.setSeriesTimeDomain(2, Workday.DOMAIN);
		schema.addAttribute(2, 5);
		schema.setAttributeProperty(2, 5, db.getProperty(K.UNIT_PROP.val(), true));
		schema.setAttributeDefault(2, 5, K.UNIT_VALUE_SHARES.val());
		
		schema.addSeries(3);
		schema.setSeriesName(3, K.SPLITS_SER.val());
		schema.setSeriesDescription(3, "stock splits");
		schema.setSeriesType(3, db.getValueType(K.NUM_TYPE.val()));
		schema.setSeriesTimeDomain(3, Workday.DOMAIN);
		schema.setSeriesSparsity(3, true);
		// no series unit
		
		schema.applyUpdates();
	}
 	
	/**
	 * The Forex schema defines 1 series and 2 attributes for Forex chronicles.
	 * The series is <em>rate</em>. It is <em>numeric</em> with data on <em>working days</em>. 
	 * <p>
	 * The attributes are:
	 * <ul>
	 * <li>currency sold
	 * <li>currency bought
	 * </ul>
	 * Both are <em>currency</em> attributes. For the "currency sold", the schema
	 * uses the currency property already defined for stocks, since it describes the "price
	 * paid". For the other currency, a new property is required. 
	 * <p>
	 * The method applies updates but does not commit.  
	 * <p>
	 * @throws T2DBException
	 */
	public void createExchangeRatesSchema() throws T2DBException {
		
		// create the second currency property (defined as "indexed", but non-indexed would also work)
		db.createProperty(K.CURR_BOUGHT_PROP.val(), db.getValueType(K.CURR_PROP.val()), true)
			.applyUpdates();
		
		UpdatableSchema schema = db.createSchema(K.FOREX_SCHEMA.val(), null);
		schema.addAttribute(1);
		Property<?> prop = db.getProperty(K.CURR_PROP.val(), true);
		schema.setAttributeProperty(1, prop);
		schema.setAttributeDefault(1, prop.getValueType().scan(""));
		schema.addAttribute(2);
		schema.setAttributeProperty(2, db.getProperty(K.CURR_BOUGHT_PROP.val(), true));
		schema.setAttributeDefault(2, prop.getValueType().scan(""));
		
		schema.addSeries(1);
		schema.setSeriesName(1, K.RATE_SER.val());
		schema.setSeriesDescription(1, "exchange rate");
		schema.setSeriesType(1, db.getValueType(K.NUM_TYPE.val()));
		schema.setSeriesTimeDomain(1, Workday.DOMAIN);
		// no series unit
		
		schema.applyUpdates();
	}
	
	/**
	 * Create 2 top level chronicles for stocks and exchange rates.
	 * <p>
	 * The method applies updates but does not commit.  
	 * 
	 * @throws T2DBException
	 */
	public void createTopLevelChronicles() throws T2DBException {
		Schema stocksSchema = db.getSchemas(K.STOCKS_SCHEMA.val()).iterator().next();
		UpdatableChronicle stocks = db.getTopChronicle().edit()
				.createChronicle(K.STOCK_CHRON.val(), false, "Stock market data", null, stocksSchema);
		stocks.applyUpdates();
		Schema forexSchema = db.getSchemas(K.FOREX_SCHEMA.val()).iterator().next();
		UpdatableChronicle forex = db.getTopChronicle().edit()
				.createChronicle(K.FOREX_CHRON.val(), false, "Exchange rate data", null, forexSchema);
		forex.applyUpdates();
	}
	
}
