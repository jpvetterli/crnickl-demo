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
 * Package: ch.agent.crnickl.demo.geocoord
 * Type: CartesianGeoCoord
 * Version: 1.0.0
 */
package ch.agent.crnickl.demo.geocoord;

import java.util.regex.Pattern;

/**
 * CartesianGeoCoord implements a geographical position.
 * As this is the demo, it just uses
 * <a href="http://en.wikipedia.org/wiki/Coordinates_(geographic)#Cartesian_coordinates">cartesian coordinates</a>
 * with the origin at the center of the earth. In an actual system, one would think a bit more about
 * the representation. An important factor to consider is the kind of support provided by 
 * database engines targeted like MongoDB or PostGIS.  
 * <p>
 * Methods are not documented. X, Y, and Z have the same interpretation in all constructors and methods.
 * <p>
 * X: meters in the equatorial plane, positive towards 0° longitude<br>
 * Y: meters in the equatorial plane, positive towards 90°E longitude<br>
 * Z: meters along the axis of rotation, positive towards N<br>
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class CartesianGeoCoord implements GeoCoord {

	private static class Singleton {
		private static Pattern splitter;
		static {
			splitter = Pattern.compile(":");
		};
	}
	
	private double x;
	private double y;
	private double z;
	
	/**
	 * Construct a cartesian geographical position from a string.
	 * The only valid format is three doubles, separated by colons.
	 * The three doubles represent x, y, and z, in that order.
	 *  
	 * @param colonSeparatedValues
	 */
	public CartesianGeoCoord(String colonSeparatedValues) {
		String[] xyz = Singleton.splitter.split(colonSeparatedValues);
		try {
			if (xyz.length != 3)
				throw new IllegalArgumentException("bad length:" + xyz.length);
			this.x = Double.parseDouble(xyz[0]); 
			this.y = Double.parseDouble(xyz[1]); 
			this.z = Double.parseDouble(xyz[2]); 
		} catch (Exception e) {
			throw new IllegalArgumentException("can't parse " + colonSeparatedValues);
		}
	}

	public CartesianGeoCoord(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	@Override
	public boolean isNear(GeoCoord coord) {
		return isNear(coord, 42d);
	}

	@Override
	public boolean isNear(GeoCoord coord, double distance) {
		return distanceTo(coord) <= distance;
	}

	@Override
	public double distanceTo(GeoCoord coord) {
		try {
			CartesianGeoCoord cart = (CartesianGeoCoord) coord;  
			return Math.sqrt(squareDiff(getX(), cart.getX()) + squareDiff(getY(), cart.getY())+ squareDiff(getZ(), cart.getZ()));
		} catch (Exception e) {
			throw new RuntimeException("coordinate system conversion support comming soon");
		}
	}
	
	@Override
	public String toString() {
		return String.format("%f:%f:%f", x, y, z);
	}

	private double squareDiff(double x1, double x2) {
		double d = x1 - x2;
		return d * d;
	}
	
}
