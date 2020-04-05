package imdbvisuals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import imdbvisuals.Window;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Window extends Application{
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		//Enable basic JAVAFX functionality and set the title of the window
		stage.setTitle("IMDB Project graph application");
		stage.show();
		
		//Create the chart element
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc = 
            new BarChart<String,Number>(xAxis,yAxis);
        bc.setAnimated(false);
        bc.setTitle("Genre Summary");
        xAxis.setLabel("Genre");       
        yAxis.setLabel("Movies");
        
        //Create the onscreen slider element
        Slider slider = new Slider();
        slider.setMin(1870);
        slider.setMax(2020);
        slider.setValue(2000);
        slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(9);
        slider.setBlockIncrement(1);
        
        //Slider value observer
        slider.valueProperty().addListener(new ChangeListener<Number>() {
        	public void changed(ObservableValue<? extends Number> ov,
        			Number old_val, Number new_val) {
        		//Get a new database connection
        		Connection c = getConnection();
        		try {
        			//SQL Statement for getting the data based on the year
        			String statement = "SELECT genre, COUNT(genre) as c FROM moviegenre, movie WHERE movie.ID = moviegenre.movieid AND movie.release_yr = " + Math.round((double) new_val) + " GROUP BY genre ORDER BY c DESC";
            		var stmt = c.createStatement();
            		ResultSet rs = stmt.executeQuery( statement );
            		Series<String, Number> series = new XYChart.Series<String, Number>();
            		//Set the name of the chart to the year
            		series.setName(Long.toString(Math.round((double) new_val)));
            		while ( rs.next() ) {
            			//Add a new bar for each genre
            			series.getData().add(new XYChart.Data<String, Number>(rs.getString("genre"),rs.getInt("c")));
            		}
            		//Close the database connection
            		rs.close();
            		stmt.close();
            		c.close();
            		//Update the chart
            		bc.getData().clear();
            		bc.getData().add(series);
        		} catch(Exception e) {
        			e.printStackTrace();
        			System.err.println(e.getClass().getName()+": "+e.getMessage());
        			System.exit(0);
        		}
        	}
        });
        
        Series<String, Number> series1 = new XYChart.Series<String, Number>();
        
        //VBox styling used by JAVAFX to stack the chart and slider
        VBox root = new VBox();
        root.setPadding(new Insets(20));
        
        //Render the window
        root.getChildren().addAll(bc,slider);
        Scene scene  = new Scene(root,1750,500);
        bc.getData().add(series1);
        stage.setScene(scene);
        stage.show();
	}
	
	//Open a connection to the database
	public static Connection getConnection() {
		Connection c = null;
		try {
			Class.forName("org.postgresql.Driver");
		    c = DriverManager
		    .getConnection("jdbc:postgresql://localhost:5432/IMDB",
		    Secret.user, Secret.password);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
		return c;
	}
}
