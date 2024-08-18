import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ManagerMode extends JFrame {
    private final String dbURL;

    //making the window
    public ManagerMode() {
        dbURL = "jdbc:sqlite:/Users/rushilgangisetty/dbsproject_rushilg";
        setTitle("Manaager Mode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //fetch contest results and display them in tables
        displayContestResults();

        setSize(800, 600);
        setVisible(true);
    }

    //querying results and then adding them to a table
    private void displayContestResults() {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement()) {
            // Query to fetch contest results with necessary joins 
            String query = "select C.id AS CandidateID, R.tally as VoteCount, C.platform as Platform, E.description as ElectionYear " +
                           "from Candidate C " +
                           "inner join Runs_in R on C.id = R.candidate_id " +
                           "inner join Contest Co on R.contest_id = Co.id " +
                           "inner join Held H on Co.id = H.contest_id " +
                           "inner join Election E on H.election_id = E.id " +
                           "order by R.tally desc";

            ResultSet rs = stmt.executeQuery(query);

            // create a table to hold the data 
            DefaultTableModel model = new DefaultTableModel();
            JTable table = new JTable(model);

            // adding columns with the information we want to display to the table
            model.addColumn("Candidate ID");
            model.addColumn("Vote Count");
            model.addColumn("Platform");
            model.addColumn("Election Year");

            //adding rows to the table and populating with data
            while (rs.next()) {
                Object[] rowData = new Object[4]; 
                rowData[0] = rs.getInt("CandidateID");
                rowData[1] = rs.getInt("VoteCount");
                rowData[2] = rs.getString("Platform");
                rowData[3] = rs.getString("ElectionYear");
                model.addRow(rowData);
            }

            // adding the table to the frame
            JScrollPane scrollPane = new JScrollPane(table);
            getContentPane().add(scrollPane);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

  