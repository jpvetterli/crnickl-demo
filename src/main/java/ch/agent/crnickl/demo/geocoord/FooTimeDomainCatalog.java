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

import ch.agent.t2.applied.DateTime;
import ch.agent.t2.applied.Month;
import ch.agent.t2.applied.Week;
import ch.agent.t2.applied.Workday;
import ch.agent.t2.applied.Year;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.ImmutableTimeDomainCatalog;

/**
 * A time domain catalog supporting yearly, monthly, daily, weekly, workweek, datetime and footime.
 * 
 * @author Jean-Paul Vetterli
 */
public class FooTimeDomainCatalog extends ImmutableTimeDomainCatalog {
	
	/**
	 * Construct a catalog.
	 */
	public FooTimeDomainCatalog() {
		super(Year.DOMAIN, Month.DOMAIN, Day.DOMAIN, Week.DOMAIN, Workday.DOMAIN, DateTime.DOMAIN, FooTime.DOMAIN);
	}

}
