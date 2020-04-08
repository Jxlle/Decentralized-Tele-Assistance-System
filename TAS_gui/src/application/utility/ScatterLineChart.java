package application.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.shape.Line;

/**
 * Scatter chart with horizontal/vertical line functionality
 * Part of the source: https://stackoverflow.com/questions/24232931/adding-a-line-in-a-javafx-chart
 *
 * @param <X> generic value parameter
 * @param <Y> generic value parameter
 */
public class ScatterLineChart<X, Y> extends ScatterChart<X, Y> {
	
	// Available colors
	public static List<String> colors = Arrays.asList("red", "blue", "green", "yellow", "orange", "black");

    // data defining horizontal markers, xValues are ignored
    private ObservableList<Data<X, Y>> horizontalMarkers;
    
    // data defining vertical markers, yValues are ignored
    private ObservableList<Data<X, Y>> verticalMarkers;

    public ScatterLineChart(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
        
        // a list that notifies on change of the yValue property
        horizontalMarkers = FXCollections.observableArrayList(d -> new Observable[] {d.YValueProperty()});
        
        // listen to list changes and re-plot
        horizontalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
        
        // a list that notifies on change of the xValue property
        verticalMarkers = FXCollections.observableArrayList(d -> new Observable[] {d.XValueProperty()});
        
        // listen to list changes and re-plot
        verticalMarkers.addListener((InvalidationListener)observable -> layoutPlotChildren());
    }

    /**
     * Add horizontal value marker. The marker's Y value is used to plot a
     * horizontal line across the plot area, its X value is ignored.
     * 
     * @param marker must not be null.
     * @param colorIndex the index of the used color for the line
     */
    public void addHorizontalValueMarker(Data<X, Y> marker, int colorIndex) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (horizontalMarkers.contains(marker)) return;
        Line line = new Line();
        line.getStrokeDashArray().addAll(25d, 10d);
        line.setStyle("-fx-stroke: " + colors.get(colorIndex) + ";");
        marker.setNode(line);
        getPlotChildren().add(line);
        horizontalMarkers.add(marker);
    }
    
    /**
     * Add vertical value marker. The marker's X value is used to plot a
     * vertical line across the plot area, its Y value is ignored.
     * 
     * @param marker must not be null.
     * @param colorIndex the index of the used color for the line
     */
    public void addVerticalValueMarker(Data<X, Y> marker, int colorIndex) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        line.getStrokeDashArray().addAll(25d, 10d);
        line.setStyle("-fx-stroke: " + colors.get(colorIndex) + ";");
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
    }

    /**
     * Remove horizontal value marker.
     * 
     * @param horizontal marker must not be null
     */
    public void removeHorizontalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        
        horizontalMarkers.remove(marker);
    }
    
    /**
     * Remove vertical value marker.
     * 
     * @param vertical marker must not be null
     */
    public void removeVerticalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        
        if (marker.getNode() != null) {
            getPlotChildren().remove(marker.getNode());
            marker.setNode(null);
        }
        
        verticalMarkers.remove(marker);
    }

    /**
     * Overridden to layout the value markers.
     */
    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        
        for (Data<X, Y> horizontalMarker : horizontalMarkers) {
            double lower = ((ValueAxis<?>) getXAxis()).getLowerBound();
            X lowerX = getXAxis().toRealValue(lower);
            double upper = ((ValueAxis<?>) getXAxis()).getUpperBound();
            X upperX = getXAxis().toRealValue(upper);
            Line line = (Line) horizontalMarker.getNode();
            line.setStartX(getXAxis().getDisplayPosition(lowerX));
            line.setEndX(getXAxis().getDisplayPosition(upperX));
            line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()));
            line.setEndY(line.getStartY());

        }
        
        for (Data<X, Y> verticalMarker : verticalMarkers) {
            double lower = ((ValueAxis<?>) getYAxis()).getLowerBound();
            Y lowerY = getYAxis().toRealValue(lower);
            double upper = ((ValueAxis<?>) getYAxis()).getUpperBound();
            Y upperY = getYAxis().toRealValue(upper);
            Line line = (Line) verticalMarker.getNode();
            line.setStartY(getYAxis().getDisplayPosition(lowerY));
            line.setEndY(getYAxis().getDisplayPosition(upperY));
            line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()));
            line.setEndX(line.getStartX());

        }
        
        
    }
}
