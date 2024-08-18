import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.*;

public class VotingWindow extends JFrame {
	
	//variables that we will be displaying using a lot
    private JTextField voterIDField;
    private final String dbURL;
    private String voterName;
    private String voterLocality;
    private String voterState;

    //making the voting window
    public VotingWindow(String mydbURL) {
        // setting the title of the window
        dbURL = mydbURL;
        setTitle("Enter Voter ID");

        // create a text field for entering voter ID
        voterIDField = new JTextField(20);

        // create a button to submit the voter ID
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> submitVoterID());

        // create a panel for the components
        JPanel panel = new JPanel();
        panel.add(new JLabel("Voter ID:"));
        panel.add(voterIDField);
        panel.add(submitButton);

        // setting size and adding panel to pane
        getContentPane().add(panel);
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    //submit voter id here
    private void submitVoterID() {
        // get the voter ID from the text field
        String voterID = voterIDField.getText();
        	//testing to see if we got it
            System.out.println("Voter ID: " + voterID);
            
            //using voter id: 0 to be how we enter manager mode (asssuming no one can have 0)
            if (voterID.equals("0")) {
            	// close the current window
                dispose(); 
             // Open ManagerMode window
                SwingUtilities.invokeLater(ManagerMode::new); 
                return; 
            }
            
            //this will be copy and pasted a lot bc i was dumb and didnt use prepared statments
            try (Connection conn = DriverManager.getConnection(dbURL);
                 Statement stmt = conn.createStatement();) {
            } catch (SQLException e) {
                System.err.println(e);
            }
        
      //copy pasted
        try (Connection conn = DriverManager.getConnection(dbURL);
                Statement stmt = conn.createStatement();) {
            System.out.println("Executing query...");
            
            //this query gets us the voter's name, locality(city) and state and stores it in a more convenient variable name
            ResultSet rset = stmt.executeQuery(
                    "select Person.name as voter_name, Locality.name as locality, States.name as state " +
                    "from Person " + 
                    "join Lives_In on Person.id = Lives_In.person_id " +
                    "join Locality on Lives_In.locality_id = Locality.id " +
                    "join States on Locality.locState_id = States.id " +
                    "where Person.id = " + voterID);
            if (rset.next()) {
               //storing the query results in variables
                voterName = rset.getString("voter_name");
                voterLocality = rset.getString("locality");
                voterState = rset.getString("state");
                //testing by printing off values
                System.out.println("Voter Name: " + voterName);
                System.out.println("Locality: " + voterLocality);
                System.out.println("State: " + voterState);

                // get contest IDs for the voter, have to make voterID an int
                ArrayList<Integer> contestIds = getContestIdsForVoter(Integer.parseInt(voterID));

                // testing by printing contest IDs to console
                System.out.println("Contest IDs for Voter " + voterID + ": " + contestIds);

                
                // initializing candidate arrays for each position 
                ArrayList<Integer> mayorCandidates = new ArrayList<>();
                ArrayList<Integer> governorCandidates = new ArrayList<>();
                ArrayList<Integer> presidentCandidates = new ArrayList<>();

                // iterating over contest IDs to get all of the candidates for each position 
                for (Integer contestId : contestIds) {
                    // get candidates for the contest and sort them into appropriate arrays
                	//calling a sort method that is defined later
                    sortCandidatesByPlatform(contestId, mayorCandidates, governorCandidates, presidentCandidates);
                }

                // close current window and open the new CastVotes window
                dispose();
                //feeding CastVotes window the voter information and candidate arrays
                SwingUtilities.invokeLater(() -> new CastVotes(voterName, voterLocality, voterState, mayorCandidates, governorCandidates, presidentCandidates, dbURL));

            } else {
                // ff no voter information is found, display an error message
                JOptionPane.showMessageDialog(this, "Invalid Voter ID", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println(e);
        }
    }
    
    //array to store contests the voter can vote in
    private ArrayList<Integer> getContestIdsForVoter(int voterID) {
        ArrayList<Integer> contestIds = new ArrayList<>();

        // query to get contest IDs that the voter can vote in
        String contestQuery = "select distinct id " +
                              "from Contest " +
                              "inner join (" +
                                  "select contest_id from Local_Contest where locality_id in (" +
                                      "select locality_id from Lives_In where person_id = " + voterID +
                                  ") " +
                              "union " +
                                  "select contest_id from State_Contest where state_id in (" +
                                  "select locState_id from Locality where id in (" +
                                  "select locality_id from Lives_In where person_id = " + voterID + ")" + ") " +
                                  "union " +
                                  "select contest_id from National_Contest" + ") " + 
                                  "as AvailableContests on Contest.id = AvailableContests.contest_id";

        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(contestQuery)) {
            while (rs.next()) {
            	//adding the contest ids to contest id array
                int contestId = rs.getInt("id");
                contestIds.add(contestId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contestIds;
    }
    
    //method to sort candidates from contest into positions
    private void sortCandidatesByPlatform(int contestId, ArrayList<Integer> mayorCandidates, ArrayList<Integer> governorCandidates, ArrayList<Integer> presidentCandidates) {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement()) {
            // query that gets the candidates position based on the contest id 
            String candidateQuery = "select Candidate.id, Candidate.platform " +
                                    "from Candidate " +
                                    "join Runs_In on Candidate.id = Runs_In.candidate_id " +
                                    "where Runs_In.contest_id = " + contestId +
                                    " and Candidate.platform in ('Mayor', 'Governor', 'President')";
            ResultSet rs = stmt.executeQuery(candidateQuery);
            while (rs.next()) {
                int candidateId = rs.getInt("id");
                String platform = rs.getString("platform");
                
                if ("Mayor".equals(platform)) {
                    mayorCandidates.add(candidateId);
                } else if ("Governor".equals(platform)) {
                    governorCandidates.add(candidateId);
                } else if ("President".equals(platform)) {
                    presidentCandidates.add(candidateId);
                } else {
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("mayors:" + mayorCandidates);
        System.out.println("governors: " + governorCandidates);
        System.out.println("presidents: " + presidentCandidates);
    }

}




    