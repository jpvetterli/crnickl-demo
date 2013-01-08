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
import ch.agent.crnickl.api.ValueScanner;
import ch.agent.crnickl.api.ValueType;

/**
 * A value scanner for {@link Currency}.
 * 
 * @author Jean-Paul Vetterli
 */
public class CurrencyValueScanner implements ValueScanner<Currency> {

	private ValueType<Currency> currencyValueType;
	
	public CurrencyValueScanner(ValueType<Currency> currencyValueType) {
		super();
		this.currencyValueType = currencyValueType;
	}

	@Override
	public Class<Currency> getType() {
		return Currency.class;
	}

	@Override
	public Currency scan(String value) throws T2DBException {
		return new Currency(value);
	}

	@Override
	public void check(Currency value) throws T2DBException {
		currencyValueType.check(value);
	}

	@Override
	public String toString(Currency value) throws T2DBException {
		return value.getSymbol();
	}
	
}
