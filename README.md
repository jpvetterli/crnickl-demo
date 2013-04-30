crnickl-demo : CrNiCKL Database Demos
=====================================

	Copyright 2011-2013 Hauser Olsson GmbH.
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
    	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

***

CrNiCKL (pronounced "chronicle") is a database for time series written in 
Java running on top of SQL and NoSQL systems. This package
provides a few demos.

Distribution
------------

The distribution consists of three archives

	crnickl-demo-x.y.z.jar
	crnickl-demo-x.y.z-javadoc.jar
	crnickl-demo-x.y.z-sources.jar

with compiled classes, HTML documentation, and sources. The version number 
`x.y.z` follows the maven convention. The distribution also includes .asc 
files with detached cryptographic signatures.

Building the software
---------------------

The recommended way is to use [git](http://git-scm.com) for accessing the
source and [maven](<http://maven.apache.org/>) for building. The procedure 
is easy, as maven takes care of locating and downloading dependencies:

	$ git clone https://github.com/jpvetterli/crnickl-demo.git
	$ cd crnickl-demo
	$ mvn install

This builds and installs the distribution JARs in your local maven
repository. They can also be found in the `target` directory.

When building the software by other means, the following dependencies must be
addressed:

- `hsqldb-x.y.z.jar` [HyperSQL Database](http://hsqldb.org/)
- `sqltool-x.y.z.jar` [HyperSQL Database](http://hsqldb.org/)
- `batik-awt-util-x.y.z.jar` [Batik](http://xmlgraphics.apache.org/batik/)
- `batik-svggen-x.y.z.jar` [Batik](http://xmlgraphics.apache.org/batik/)
- `batik-util-x.y.z.jar` [Batik](http://xmlgraphics.apache.org/batik/)
- `jcommon-x.y.z.jar` [JCommon](http://www.jfree.org/jcommon/)
- `jfreechart-x.y.z.jar` [JFreeChart](http://www.jfree.org/jfreechart/) 
- `t2-x.y.z.jar` [Time2 Library](http://agent.ch/timeseries/t2/) 

Replace all `x.y.z` with the actual version numbers which can be found in the 
<q>POM</q> file included in the binary JAR:

	/META-INF/maven/ch.agent/crnickl-demo/pom.xml

Running the demos
-----------------

The following command executes the <q>default</q> demo:

	$ mvn -q exec:exec
	Database : sa@jdbc:hsqldb:mem:demodb
	
	Value types (with base type) : 
	+-- Currency (Currency)
	|   +-- 
	|   +-- CNY - Yuan renminbi
	[... lots of output removed ...]

(Everything after the first line is the output of the command.)
The same can be done with:

	$ mvn -q -Ddemo.mainClass=ch.agent.crnickl.demo.stox.StocksAndForexDemo \
		 -Ddemo.args="file=stox-text.parm,file=jdbc.parm" exec:exec
	[... output removed ...]

Note: this was executed in a Unix shell, which supports line continuation
with a backslash. In case your shell or command window does not do this, 
write the command as one long line.

Another demo produces some graphics:

	$ mvn -q -Ddemo.mainClass=ch.agent.crnickl.demo.stox.StocksAndForexDemo \
		 -Ddemo.args="file=stox-graphic.parm,file=jdbc.parm" exec:exec
	[... most output removed ...]
	/tmp/FBI_KGB_raw.svg
	/tmp/FBI_KGB_in_USD.svg
	/tmp/FBI_KGB_in_USD_adj.svg

A third demo deals with time series of geo locations:

	$ mvn -q -Ddemo.mainClass=ch.agent.crnickl.demo.geocoord.GeoCoordDemo \
		-Ddemo.args="file=geo.parm" exec:exec
	2012-04-09 09:00:00   5044km (machin)
	2012-04-09 15:11:00   5651km (machin)
	2012-04-09 21:33:20   8607km (truc)
	[... lots of output removed ...]

Browsing the source code
------------------------

The source is available on GitHub at 
<http://github.com/jpvetterli/crnickl-demo.git>.

Finding more information
------------------------

More information on CrNiCKL is available at 
<http://agent.ch/timeseries/crnickl>.
This README was updated on 2013-04-30 (jpv).

<link rel="stylesheet" type="text/css" href="README.css"/>

