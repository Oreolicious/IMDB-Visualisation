package imdbvisuals2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
        bc.setTitle("Movie summary");
        xAxis.setLabel("Year");       
        yAxis.setLabel("Movies");
        
        //Create label, textfield and button element
        Label countrylabel = new Label("Country:");
        TextField countryfield = new TextField();
        Button submit = new Button("Submit");
        //Button eventhandler to update the chart
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
        		//Get a new database connection
        		Connection c = getConnection();
        		try {
        			//SQL Statement for getting the data based on the country
        			String statement = "SELECT movie.release_yr, COUNT(movie.title) as movies FROM movie,movielocation WHERE movie.id = movielocation.movieid AND movielocation.country = ? GROUP BY movie.release_yr ORDER BY movie.release_yr DESC;";
        			//SQL Injection safe statement
            		PreparedStatement stmt = c.prepareStatement(statement);
            		stmt.setString(1, countryfield.getText());
            		ResultSet rs = stmt.executeQuery();
            		Series<String, Number> series = new XYChart.Series<String, Number>();
            		//Set the name of the chart to the country
            		series.setName(countryfield.getText());
            		while ( rs.next() ) {
            			//Add a new bar for each year
            			series.getData().add(new XYChart.Data<String, Number>(rs.getString("release_yr"),rs.getInt("movies")));
            			System.out.println(rs.getInt("movies"));
            		}
            		//Close the database connection
            		rs.close();
            		stmt.close();
            		c.close();
            		//Update the chart
            		bc.getData().clear();
            		bc.getData().add(series);
        		} catch(Exception e1) {
        			e1.printStackTrace();
        			System.err.println(e1.getClass().getName()+": "+e1.getMessage());
        			System.exit(0);
        		}
            }
        });

        //VBox styling used by JAVAFX to stack the chart and slider
        VBox root = new VBox();
        root.setPadding(new Insets(20));
        
        //Render the window
        root.getChildren().addAll(bc,countrylabel,countryfield,submit);
        Scene scene  = new Scene(root,1750,500);
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
