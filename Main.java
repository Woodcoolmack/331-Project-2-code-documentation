import javax.swing.*;
import java.sql.*;
/**
 * This is the Main class for the POS
 * 
 * This class establishes a connecction with the databse
 * and launches the loginFrame GUI.
 * 
 * This program connects to the database and, if the 
 * connection is successful, Opens up the LoginFrame.
 *      
 * @author Caden Guillot
 * @author Yuki Noda 
 * @author Anibal Gomez
 */
// MAIN FILE.
public class Main {
    /**
     * This is the where the program starts. 
     * 
     * the main function connects to the database using JBC. 
     * If the connection is successful then it opens the 
     * Login Frame GUI. If connectionfails stack trace is printed. 
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // ESTABLISH DATABASE CONNECTION
        Connection conn;

        String database_name = "team_cyg_db";
        String database_user = "team_cyg";
        String database_password = "";
        String database_url = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/" + database_name;

        try {
            conn = DriverManager.getConnection(database_url, database_user, database_password);
        } 
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // SEND VIEWER TO LOGIN GUI
        Connection finalConn = conn;
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame1 = new LoginFrame(finalConn);
            frame1.setVisible(true);
        });
    }
}