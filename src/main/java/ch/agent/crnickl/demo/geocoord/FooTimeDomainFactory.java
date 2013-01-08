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

import ch.agent.t2.time.DateTime;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Month;
import ch.agent.t2.time.Week;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;
import ch.agent.t2.time.engine.AbstractTimeDomainFactory;

/**
 * A time domain factory supporting the non standard time FooTime.
 * Configuration: pass the name of this class as the system property named
 * <em>TimeDomainFactory</em>
 * 
 * @author Jean-Paul Vetterli
 */
public class FooTimeDomainFactory extends AbstractTimeDomainFactory {
	private static class Singleton {
		private static FooTimeDomainFactory factory;
		static {
			factory = new FooTimeDomainFactory();
			factory.declareBuiltIn(Year.DEF.getLabel());
			factory.declareBuiltIn(Month.DEF.getLabel());
			factory.declareBuiltIn(Day.DEF.getLabel());
			factory.declareBuiltIn(Week.DEF.getLabel());
			factory.declareBuiltIn(Workday.DEF.getLabel());
			factory.declareBuiltIn(DateTime.DEF.getLabel());
			factory.declareBuiltIn(FooTime.DEF.getLabel());
			factory.lockBuiltIns();
		};
	}

	/**
	 * Return the TimeDomainFactory instance.
	 * @return the TimeDomainFactory instance
	 */
	public static FooTimeDomainFactory getInstance() {
		return Singleton.factory;
	}
	
	/**
	 * Construct a TimeDomainFactory.
	 */
	private FooTimeDomainFactory() {
	}

}
