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

/**
 * GeoCoord represents a geographical position.
 * 
 * @author Jean-Paul Vetterli
 */
public interface GeoCoord {

	/**
	 * Return true if the distance to the geographical position is less than 
	 * a default.
	 * <p>
	 * Return false when the distance is a NaN.
	 * 
	 * @param coord a geographical position
	 * @return true if the distance is less than a default
	 */
	boolean isNear(GeoCoord coord);
	
	/**
	 * Return true if the distance to the geographical position is less than 
	 * the distance specified.
	 * <p>
	 * Return false when the distance is a NaN.
	 * 
	 * @param coord a geographical position
	 * @param distance a distance in meters
	 * @return true if the distance is less than a default
	 */
	boolean isNear(GeoCoord coord, double distance);
	
	/**
	 * Return the distance to the geographical position.
	 * 
	 * @param coord a geographical position
	 * @return the distance to the geographical position
	 */
	double distanceTo(GeoCoord coord);
	
}
