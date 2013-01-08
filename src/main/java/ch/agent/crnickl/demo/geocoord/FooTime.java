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

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.BasePeriodPattern;
import ch.agent.t2.time.Cycle;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.SimpleSubPeriodPattern;
import ch.agent.t2.time.SubPeriodPattern;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeDomainManager;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.engine.Time2;

/**
 * FooTime has time points a few times some days, not even regularly spaced. The
 * days are Saturday, Monday, Tuesday, Friday. The times are 07:00, 09:00,
 * 15:11, and 21:33:20.
 * <p>
 * FooTime is not very useful.
 * 
 * @author Jean-Paul Vetterli
 */
public class FooTime extends Time2 {

	/**
	 * A constant holding the definition.
	 */
	public static final TimeDomainDefinition DEF = init();

	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = TimeDomainManager.getFactory().get(DEF, true);
	
	private static TimeDomainDefinition init() {
		BasePeriodPattern bpp = new Cycle(true, false, true, true, false, false, true); // Sat,,Mon,Tue,,,Fri
		SubPeriodPattern spp = null;
		try {
			spp = new SimpleSubPeriodPattern(Resolution.DAY, Resolution.SEC, 
					new int[]{7*3600, 9*3600, 15*3600 + 660, 21*3600 + 2000});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new TimeDomainDefinition("footime", Resolution.DAY, 0L, bpp, spp);
	}
	
	/**
	 * Construct a <q>footime</q> time from another time object. 
	 * @param time a non-null time in the domain <q>footime</q>
	 * @throws T2Exception
	 */
	public FooTime(TimeIndex time) throws T2Exception {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(K.T1073, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>footime</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @throws T2Exception
	 */
	public FooTime(String date) throws T2Exception {
		super(DOMAIN, date);
	}
	
	/**
	 * Construct a <q>footime</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @param adjust a non-null adjustment mode
	 * @throws T2Exception
	 */
	public FooTime(String date, Adjustment adjust) throws T2Exception {
		super(DOMAIN, date, adjust);
	}
	
}
