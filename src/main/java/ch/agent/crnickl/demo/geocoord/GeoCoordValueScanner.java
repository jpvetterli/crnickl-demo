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
package ch.agent.crnickl.demo.geocoord;

import ch.agent.crnickl.T2DBException;
import ch.agent.crnickl.api.ValueScanner;
import ch.agent.crnickl.api.ValueType;

/**
 * A value scanner for {@link GeoCoord}.
 * 
 * @author Jean-Paul Vetterli
 */
public class GeoCoordValueScanner implements ValueScanner<GeoCoord> {

	private ValueType<GeoCoord> geoValueType;
	
	public GeoCoordValueScanner(ValueType<GeoCoord> geoValueType) {
		super();
		this.geoValueType = geoValueType;
	}

	@Override
	public Class<GeoCoord> getType() {
		return GeoCoord.class;
	}

	@Override
	public GeoCoord scan(String value) throws T2DBException {
		return new CartesianGeoCoord(value);
	}

	@Override
	public void check(GeoCoord value) throws T2DBException {
		geoValueType.check(value);
	}

	@Override
	public String toString(GeoCoord value) throws T2DBException {
		if (value == null)
			throw new IllegalArgumentException("value null");
		return value.toString();
	}
	
}
