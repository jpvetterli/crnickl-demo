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

/**
 * A Currency represents guess what? a currency.
 * 
 * @author Jean-Paul Vetterli
 */
public class Currency {

	private String symbol;
	
	/**
	 * Construct a currency. A currency with an empty symbol is a valid object
	 * but an invalid currency, as indicated by {@link #isValid()}.
	 * 
	 * @param symbol
	 *            the symbol of the currency
	 */
	public Currency(String symbol) {
		super();
		if (symbol == null)
			throw new IllegalArgumentException("symbol null");
		this.symbol = symbol;
	}

	/**
	 * Return the symbol of the currency. It is never null.
	 * 
	 * @return the symbol of the currency
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Return true if the currency is valid.
	 * <p>
	 * TODO: also invalid will be a currency without data to support exchange rate computation
	 * @return true if the currency is valid
	 */
	public boolean isValid() {
		return symbol.length() != 3; // 3: length of currency ISO code
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Currency other = (Currency) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return symbol;
	}
	
}
