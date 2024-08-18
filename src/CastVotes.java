import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//so many imports...


	public class CastVotes extends JFrame implements ActionListener {
	//variables and arrays we will need to interact with
	//i think eclipse added this serial number thing
    private static final long serialVersionUID = 1L;
    private final String voterName;
    private final String voterLocality;
    private final String voterState;
    private final ArrayList<Integer> mayorCandidates;
    private final ArrayList<Integer> governorCandidates;
    private final ArrayList<Integer> presidentCandidates;
    private final String dbURL;

    //making vote casting window
    public CastVotes(String voterName, String voterLocality, String voterState, 
            ArrayList<Integer> mayorCandidates, ArrayList<Integer> governorCandidates, 
            ArrayList<Integer> presidentCandidates, String dbURL) {

    	//initializing variables for future usage
        this.voterName = voterName;
        this.voterLocality = voterLocality;
        this.voterState = voterState;
        this.mayorCandidates = mayorCandidates;
        this.governorCandidates = governorCandidates;
        this.presidentCandidates = presidentCandidates;
        this.dbURL = dbURL;
        
        //setting title of window
        setTitle("Cast Votes");
        JPanel panel = new JPanel(new GridLayout(0, 2));

        //showing the voter information (mostly for confirmation)
        panel.add(new JLabel("Voter Name:"));
        panel.add(new JLabel(voterName));

        panel.add(new JLabel("Locality:"));
        panel.add(new JLabel(voterLocality));

        panel.add(new JLabel("State:"));
        panel.add(new JLabel(voterState));

        //label for each drop down menu for voting
        addCandidates(panel, "Mayor", mayorCandidates);
        addCandidates(panel, "Governor", governorCandidates);
        addCandidates(panel, "President", presidentCandidates);

        //submit button that will trigger voting when clicked
        JButton submitButton = new JButton("Submit Votes");
        submitButton.addActionListener(this);
        panel.add(submitButton);

        getContentPane().add(panel);
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    //checks for if we have candidates 
    private void addCandidates(JPanel panel, String position, ArrayList<Integer> candidates) {
        if (!candidates.isEmpty()) {
            panel.add(new JLabel("Select " + position + " Candidate:"));
            JComboBox<String> comboBox = new JComboBox<>();
            populateComboBox(comboBox, candidates);
            panel.add(comboBox);
        } else {
            panel.add(new JLabel(position + " Candidates:"));
            panel.add(new JLabel("n/a"));
        }
    }
    
    //populating the drop box menu with candidate options
    private void populateComboBox(JComboBox<String> comboBox, ArrayList<Integer> candidateIds) {
        try (Connection conn = DriverManager.getConnection(dbURL);
             Statement stmt = conn.createStatement()) {
            for (Integer candidateId : candidateIds) {
            	//getting the candidate names 
                String query = "select Person.name from Person "
                			 + "join Candidate on Person.id = Candidate.person_id "
                			 + "where Candidate.id = " + candidateId;
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                	//storing the name
                    String candidateName = rs.getString("name");
                    comboBox.addItem(candidateName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //this block runs when the voter hits submit
    @Override
    public void actionPerformed(ActionEvent e) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to submit your votes?", "Confirm Submission", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection(dbURL)) {
                // calling the method to update the tally of votes for each selected candidate
                updateCandidateVotes(conn, "Mayor", mayorCandidates);
                updateCandidateVotes(conn, "Governor", governorCandidates);
                updateCandidateVotes(conn, "President", presidentCandidates);
                JOptionPane.showMessageDialog(this, "Votes submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to submit votes. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            //if user selects no on confirmation 
            JOptionPane.showMessageDialog(this, "Votes submission cancelled.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    //updates the candidate tallies after vote submission confirmation
    private void updateCandidateVotes(Connection conn, String position, ArrayList<Integer> candidates) throws SQLException {
        for (Integer candidateId : candidates) {
            //query to make the update
            String updateQuery = "update Runs_In set tally = tally + 1 where candidate_id = " + candidateId;
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(updateQuery);
            }
        }
    }
}
