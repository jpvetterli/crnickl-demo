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
 * Type: DemoConstants
 * Version: 1.1.0
 */
package ch.agent.crnickl.demo.stox;

import java.util.ResourceBundle;

import ch.agent.core.KeyedException;
import ch.agent.core.KeyedMessage;
import ch.agent.core.MessageBundle;

/**
 * DemoConstants provides constants and keyed messages to the package. 
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 */
public class DemoConstants extends KeyedMessage {

	/**
	 * Constants and message symbols.
	 * <p>
	 * TODO: sort enums
	 */
	public enum K {
		
		// parameter names:
		
		CHART_HEIGHT_PARAM,
		CHART_OUTPUT1_PARAM,
		CHART_OUTPUT2_PARAM,
		CHART_OUTPUT3_PARAM,
		CHART_TEXT1_PARAM,
		CHART_TEXT2_PARAM,
		CHART_TEXT3_PARAM,
		CHART_WIDTH_PARAM,
		RANGE_PARAM,
		
		// names of database objects:
		
		NUM_TYPE,
		TEXT_TYPE,
		CURR_PROP,
		CURR_BOUGHT_PROP,
		TICKER_PROP,
		UNIT_PROP,
		UNIT_VALUE_CURRENCY,
		UNIT_VALUE_SHARES,
		FOREX_SCHEMA,
		STOCKS_SCHEMA,
		FOREX_CHRON,
		STOCK_CHRON,
		PRICE_SER,
		RATE_SER,
		SPLITS_SER,
		VOLUME_SER,
		
		// error message symbols:
		
		CHART_EXRATE_RANGE_ERR,
		CHART_SUBPLOT_ERR,
		CHART_SUPPORT_ERR,
		COL_COUNT_ERR,
		DATE_COL_ERR,
		DATE_COL_NEG_ERR,
		FILE_READ_ERR,
		FILE_READ_LINE_ERR,
		JFC_OUTPUT_ERR,
		JFC_PERIOD_ERR,
		JFC_TIMECLASS_ERR,
		JFC_USEC_ERR,
		PARAMETER_ERR,
		PATTERN_ERR,
		RANGE_DATES_ERR,
		RANGE_ERR,
		SER_DOMAIN_ERR,
		SER_COL_ERR,
		SER_COL_NEG_ERR,
		SER_NUM_ERR,
		TOO_LATE_ERR,
		;
		
	    /**
	     * Return the value of the enum. 
	     * The value is from a message bundle.
	     * 
	     * @param arg zero or more arguments
	     * @return a string
	     */
	    public String val(Object... arg) {
	    	return new DemoConstants(this.name(), arg).getMessage();
	    }
	    
		/**
		 * Return a KeyedException.
		 * 
		 * @param arg zero or more arguments
		 * @return a keyed exception
		 */
		public KeyedException exception(Object... arg) {
			return new KeyedException(new DemoConstants(this.name(), arg));
		}

		/**
		 * Return a keyed exception with link to the cause chain.
		 * 
		 * @param cause the exception's cause
		 * @param arg zero or more arguments
		 * @return a keyed exception
		 */
		public KeyedException exception(Throwable cause, Object... arg) {
			return new KeyedException(new DemoConstants(this.name(), arg), cause);
		}
	    
	}

	private static final String BUNDLE_NAME = ch.agent.crnickl.demo.stox.DemoConstants.class.getName();
	
	private static final MessageBundle BUNDLE = new MessageBundle("DEMO",
			ResourceBundle.getBundle(BUNDLE_NAME));

	/**
	 * Construct a keyed message.
	 * 
	 * @param key a key
	 * @param args zero or more arguments
	 */
	public DemoConstants(String key, Object... args) {
		super(key, BUNDLE, args);
	}

}
