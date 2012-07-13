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
 * Type: Chart
 * Version: 1.1.0
 */
package ch.agent.crnickl.demo.stox;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import ch.agent.core.KeyedException;
import ch.agent.crnickl.demo.stox.DemoConstants.K;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeutil.JavaDateUtil;

/**
 * Draw charts using JFreeChart.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.0
 */
public class Chart {
	
	/**
	 * A ChartSeries packs a time series and some important chart parameters.
	 *
	 */
	public static class ChartSeries {
		private TimeAddressable<Double> series;
		private String name;
		private boolean line;
		private int subPlotIndex;
		private int weight;
		/**
		 * Construct a ChartSeries.
		 * 
		 * @param series a time series
		 * @param name a short string describing the time series uniquely 
		 * @param line if true display as line chart else as bar chart
		 * @param weight a positive number giving the relative weight of the series
		 * @param subPlot a number indicating a plot area
		 */
		public ChartSeries(TimeAddressable<Double> series, String name) {
			if (series == null)
				throw new IllegalArgumentException("series null");
			this.series = series;
			this.name = name;
			this.line = true;
			this.weight = 1;
			this.subPlotIndex = -1;
		}
		/**
		 * Return the time series.
		 * @return a time series
		 */
		public TimeAddressable<Double> getTimeSeries() {
			return series;
		}
		/**
		 * Return the name
		 * @return a string
		 */
		public String getName() {
			return name;
		}
		/**
		 * Set the line mode.
		 * 
		 * @param line true for a line chart, false for a bar chart
		 */
		public void setLine(boolean line) {
			this.line = line;
		}
		/**
		 * Return true for a line chart and false for a bar chart. Default: true.
		 * @return
		 */
		public boolean isLine() {
			return line;
		}
		/**
		 * Return the relative vertical size of the subplot area.
		 * Ignored when using an existing subplot.
		 * Default: 1.
		 * 
		 * @return a number
		 */
		public int getWeight() {
			return weight;
		}
		/**
		 * Set the relative vertical size of the subplot area.
		 * 
		 * @param weight a number
		 */
		public void setWeight(int weight) {
			this.weight = weight;
		}
		/**
		 * Return the subplot index. If non positivee, put the series in a new area.
		 * Else put it in the subplot indicated. It is a 1-based index. Default value is 0.
		 * 
		 * @return a number
		 */
		public int getSubPlotIndex() {
			return subPlotIndex;
		}
		/**
		 * Set the subplot index for this series.
		 * 
		 * @param subPlotIndex a number
		 */
		public void setSubPlotIndex(int subPlotIndex) {
			this.subPlotIndex = subPlotIndex;
		}
	}

	private String title;
	private Range range;
	private boolean withLegend;
	private List<ChartSeries> chartSeries;
	private JFreeChart chart;
	
	/**
	 * Construct a StockChart object. Set up the map for runtime arguments and
	 * initialize it with valid names and default values.
	 */
	public Chart() {
		this.chartSeries = new ArrayList<Chart.ChartSeries>();
		/* eliminate time zone and DST effects */
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	/**
	 * Set the range of data to display. If null or if not specified, display 
	 * full range. 
	 * Using this method clears the current chart and the series already added.
	 * 
	 * @param range a range or null for full range
	 */
	public void setRange(Range range) {
		this.range = range;
		this.chart = null;
		this.chartSeries.clear();
	}

	/**
	 * Set the chart title.
	 * Using this method clears the current chart.
	 * 
	 * @param title a string
	 */
	public void setTitle(String title) {
		this.title = title;
		this.chart = null;
	}
	
	/**
	 * Set legend visibility. By default the legend is not displayed.
	 * Using this method clears the current chart.
	 * 
	 * @param withLegend if true legend will be displayed
	 */
	public void setWithLegend(boolean withLegend) {
		this.withLegend = withLegend;
		this.chart = null;
	}
	
	/**
	 * Add a ChartSeries.
	 * Using this method clears the current chart.
	 * 
	 * @param chartSeries a ChartSeries
	 */
	public void addChartSeries(ChartSeries chartSeries) {
		this.chartSeries.add(chartSeries);
		this.chart = null;
	}

	/**
	 * Save a chart into a file. It is possible to save into multiple
	 * files or with various dimensions without recompiling the chart.
	 * 
	 * @param outputFile a file name
	 * @param chartWidth a positive number
	 * @param chartHeight a positive number
	 * @throws KeyedException
	 */
	public void save(String outputFile, int chartWidth,	int chartHeight) throws KeyedException {
		saveChart(getChart(), outputFile, chartWidth, chartHeight);
	}
	
	private JFreeChart getChart() throws KeyedException {
		if (chart == null) 
			chart = makeChart();
		return chart;
	}
	
	private JFreeChart makeChart() throws KeyedException {
		
		if (chartSeries.size() == 0)
			throw new IllegalStateException("addChartSeries() not called");

		if (range == null) {
			for (ChartSeries s : chartSeries) {
				if (range == null)
					range = s.getTimeSeries().getRange();
				else
					range = range.union(s.getTimeSeries().getRange());
			}
		}
		
		// use number axis for dates, with special formatter
    	DateAxis dateAxis = new DateAxis();
    	dateAxis.setDateFormatOverride(new CustomDateFormat("M/d/y"));
    	if (range.getTimeDomain().getLabel().equals("workweek"))
    		dateAxis.setTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());

		// combined plot with shared date axis
    	CombinedDomainXYPlot plot = new CombinedDomainXYPlot(dateAxis);

		for (ChartSeries s : chartSeries) {
			makeSubPlot(plot, s);
		}
		
		// make the chart, remove the legend, set the title
		JFreeChart chart = new JFreeChart(plot);
		if (!withLegend)
			chart.removeLegend();
    	chart.setBackgroundPaint(Color.white);
		chart.setTitle(new TextTitle(title));
		
		return chart;
	}
	
	private void makeSubPlot(CombinedDomainXYPlot container, ChartSeries series) throws KeyedException {
		int index = series.getSubPlotIndex();
		int nextDatasetOffset = 0;
		XYPlot plot = null;
		try {
			if (index > 0) {
				plot = (XYPlot) container.getSubplots().get(index - 1);
				nextDatasetOffset = plot.getDatasetCount();
			}
		} catch (Exception e) {
			throw K.CHART_SUBPLOT_ERR.exception(e, index);
		}
		if (plot == null)
			plot = series.isLine() ? getLinePlot() : getBarPlot();
		XYItemRenderer renderer = series.isLine() ? getLineRenderer() : getBarRenderer();
		plot.setRenderer(nextDatasetOffset, renderer);
		plot.setDataset(nextDatasetOffset, getDataset(series.getTimeSeries(), series.getName()));
		if (index < 1)
			container.add(plot, series.getWeight());
	}
	
	private XYItemRenderer getLineRenderer() throws KeyedException {
		XYLineAndShapeRenderer lineRenderer = new XYLineAndShapeRenderer();
		lineRenderer.setDrawSeriesLineAsPath(true);
		lineRenderer.setSeriesStroke(0, new BasicStroke(getStrokeWidth(), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
		lineRenderer.setBaseShapesVisible(false);
		return lineRenderer;
	}

	private float getStrokeWidth() {
		// determine the stroke width from the "density" of the data
		return Math.max(1f, 3f -0.4f * (range.getSize() / 100));
	}
	
	private XYItemRenderer getBarRenderer() throws KeyedException {
		return new XYBarRenderer();
	}
	
	private XYPlot getLinePlot() throws KeyedException {
    	// use a number axis on the left side (default)
    	NumberAxis axis = new NumberAxis();
    	axis.setAutoRangeIncludesZero(false);
		XYPlot plot = new XYPlot(null, null, axis, null);
		return plot;
	}
	
	private XYPlot getBarPlot() throws KeyedException {
		// use a number axis on the right side with a special formatter for millions
		NumberAxis axis = new NumberAxis();
		axis.setAutoRangeIncludesZero(false);
		axis.setNumberFormatOverride(new NumberFormatForMillions());
		XYPlot plot = new XYPlot(null, null, axis, null);
		plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
		return plot;
	}
	
	/**
	 * Save the chart in a file. Currently the image types supported are
	 * PNG and SVG. They are selected from the file extension.
	 * 
	 * @param chart a non-null {@link JFreeChart}
	 * @param fileName a non-null file name
	 * @param width a positive number
	 * @param height a positive number
	 * @throws Exception
	 */
	private void saveChart(JFreeChart chart, String fileName, int width, int height) throws KeyedException {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("width or height not positive");
       	if (fileName.toUpperCase().endsWith(".PNG")) {
       		saveChartAsPNG(chart, fileName, width, height);
       	} else if (fileName.toUpperCase().endsWith(".SVG"))
       		saveChartAsSVG(chart, fileName, width, height);
       	else
			throw K.CHART_SUPPORT_ERR.exception(fileName);
	}

	private void saveChartAsPNG(JFreeChart chart, String fileName, int width, int height) throws KeyedException {
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(fileName));
			BufferedImage bufferedImage = chart.createBufferedImage(width, height, null);
			ImageEncoder imageEncoder = ImageEncoderFactory.newInstance("png");
			imageEncoder.encode(bufferedImage, out);
		} catch (Exception e) {
			throw K.JFC_OUTPUT_ERR.exception(e, fileName);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
    }
	
	private void saveChartAsSVG(JFreeChart chart, String fileName, int width, int height) throws KeyedException {
		Writer out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
		    String svgNS = "http://www.w3.org/2000/svg";
			DOMImplementation di = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0");
			Document document = di.createDocument(svgNS, "svg", null);
			
			SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
			ctx.setEmbeddedFontsOn(true);
			SVGGraphics2D svgGenerator = new CustomSVGGraphics2D(ctx, true, 100, true);
		    svgGenerator.setSVGCanvasSize(new Dimension(width, height));
            chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height));
		    boolean useCSS = true;
		    svgGenerator.stream(out, useCSS);
		    svgGenerator.dispose();
		} catch (Exception e) {
			throw K.JFC_OUTPUT_ERR.exception(e, fileName);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
    }
	
	private TimeSeries convertToJFCTimeSeries(boolean interpolate, String name, TimeAddressable<Double> ts) throws KeyedException {
		Constructor<? extends RegularTimePeriod> constructor = getPeriodConstructor(ts.getTimeDomain());	
		TimeSeries timeSeries = new TimeSeries(name);
		for (Observation<Double> obs : ts) {
			Double value = obs.getValue();
			if (ts.isMissing(value)) {
				if (interpolate)
					continue;
			}
			Date date = JavaDateUtil.toJavaDate(obs.getTime());
			RegularTimePeriod period = null;
			try {
				period = constructor.newInstance(date);
			} catch (Exception e) {
				throw K.JFC_PERIOD_ERR.exception(e, date.toString());
			}
			timeSeries.add(period, value);
		}
		return timeSeries;
	}
	
	private Constructor<? extends RegularTimePeriod> getPeriodConstructor(TimeDomain timeDomain) throws KeyedException {
		Class<? extends RegularTimePeriod> timeClass = findTimeClass(timeDomain);
		try {
			return timeClass.getConstructor(Date.class);
		} catch (Exception e) {
			throw K.JFC_TIMECLASS_ERR.exception(e, Date.class.getSimpleName());
		}
	}
	
	private Class<? extends RegularTimePeriod> findTimeClass(TimeDomain timeDomain) throws KeyedException {
		Class<? extends RegularTimePeriod> jfcTimeClass = null;
		// determine JFreeChart type of time from the time domain
		switch (timeDomain.getResolution()) {
		case YEAR:
			jfcTimeClass = org.jfree.data.time.Year.class;
			break;
		case MONTH:
			jfcTimeClass = org.jfree.data.time.Month.class;
			break;
		case DAY:
			jfcTimeClass = org.jfree.data.time.Day.class;
			break;
		case HOUR:
			jfcTimeClass = org.jfree.data.time.Hour.class;
			break;
		case MIN:
			jfcTimeClass = org.jfree.data.time.Minute.class;
			break;
		case SEC:
			jfcTimeClass = org.jfree.data.time.Second.class;
			break;
		case MSEC:
			jfcTimeClass = org.jfree.data.time.Millisecond.class;
			break;
		case USEC:
			throw K.JFC_USEC_ERR.exception();
		default:
			throw new RuntimeException("bug: " + timeDomain.getResolution());
		}
		return jfcTimeClass;
	}
	
	private XYDataset getDataset(TimeAddressable<Double> ts, String name) throws KeyedException {
		return getDataset(name, convertToJFCTimeSeries(true, name, ts));
	}

	private XYDataset getDataset(String key, TimeSeries series) throws KeyedException {
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.setXPosition(TimePeriodAnchor.START); 
        dataset.addSeries(series);
		return dataset;
	}

	/* ======================================================================= */
	
	/**
	 * NumberFormatForMillions formats large numbers so that they take
	 * less space in tick labels.
	 */
	@SuppressWarnings("serial")
	public class NumberFormatForMillions extends DecimalFormat {
		@Override
		public StringBuffer format(double number, StringBuffer result,
				FieldPosition fieldPosition) {
			if (number > 1000000) {
				super.format(number / 1000000, result, fieldPosition);
				result.append("M");
				return result;
			} else
				return super.format(number, result, fieldPosition);
		}
		
	}

	/* ======================================================================= */
	
	/**
	 * CustomDateFormat avoids repeating identical dates for tick labels.
	 */
	@SuppressWarnings("serial")
	public class CustomDateFormat extends SimpleDateFormat {

		private String previous;
		
		public CustomDateFormat(String pattern) {
			super(pattern);
		}

		@Override
		public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
			StringBuffer result = super.format(date, toAppendTo, pos);
			if (previous == null || !result.toString().equals(previous)) {
				previous = result.toString();
			} else
				result.setLength(0);
			return result;
		}
		
	}
	
	/* ======================================================================= */

	/**
	 * CustomSVGGraphics2D is a extension of the Apache Batik
	 * {@link SVGGraphics2D} driver.
	 */
	public class CustomSVGGraphics2D extends SVGGraphics2D {

		private int geomPercentage = 0;
		private boolean preserveAspectRatio;
		
		protected CustomSVGGraphics2D(SVGGeneratorContext generatorCtx, boolean textAsShapes, int geomPercentage, boolean preserveAspectRatio) {
			super(generatorCtx, textAsShapes);
			this.geomPercentage = geomPercentage;
			this.preserveAspectRatio = preserveAspectRatio;
		}

		@Override
		public Element getRoot(Element svgRoot) {
	        svgRoot = domTreeManager.getRoot(svgRoot);
	        if (svgCanvasSize != null){
	        	// make the image scalable in various viewers
	        	// and preserve the aspect ratio
	        	if (geomPercentage > 0) {
	        		svgRoot.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, geomPercentage + "%");
	        		svgRoot.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, geomPercentage + "%");
	        	} else {
	        		svgRoot.setAttributeNS(null, SVG_WIDTH_ATTRIBUTE, svgCanvasSize.width + "");
	        		svgRoot.setAttributeNS(null, SVG_HEIGHT_ATTRIBUTE, svgCanvasSize.height + "");
	        	}
	            svgRoot.setAttributeNS(null, SVG_VIEW_BOX_ATTRIBUTE, 
	            		"0 0 " + svgCanvasSize.width + " " + svgCanvasSize.height);
	            svgRoot.setAttributeNS(null, SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE, 
	            		preserveAspectRatio ? SVG_MEET_VALUE : SVG_NONE_VALUE);
	        }
        	return svgRoot;
		}
	}
	
}
