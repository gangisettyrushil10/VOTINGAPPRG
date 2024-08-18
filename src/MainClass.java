

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;


//simple main class establishes a connection between java and the sqlite3 database from the given path 
public class MainClass {
    public static void main(String[] args) {
        // Example usage
        String dbURL = "jdbc:sqlite:/Users/rushilgangisetty/dbsproject_rushilg";
        SwingUtilities.invokeLater(() -> new VotingWindow(dbURL));
    }
}




