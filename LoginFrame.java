import java.sql.*;

import javax.naming.spi.DirStateFactory.Result;
import javax.swing.*;
import java.awt.*;

// LOGIN UI PAGE
/**
 * LoginFrame Creates the Login Gui for the POS.
 * 
 * The LoginFrame class allows users to login either as Employee or Manager. 
 * The class verifies if the EmployeeId is in the database and then opens 
 * up the correct GUI based on the user's role.
 * 
 * @author Caden Guillot
 * @author Yuki Noda 
 * @author Anibal Gomez
 */
public class LoginFrame extends JFrame {
    private final Connection conn;
    private int signedInEmployeeId;
    private String signedInRole;

    /**
     * This is the constructer function for LoginFrame.
     * 
     * Creates the Main Login page and initializes the buttons
     * for Mmployee Login and Manager Login.
     * 
     * @param conn active database connection
    */
    public LoginFrame(Connection conn) {
        this.conn = conn;
        setTitle("Login Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 300);
        setLocationRelativeTo(null);
        
        /// INITIALIZE BUTTONS
        JButton employeeLoginButton = new JButton("Employee Login");
        JButton managerLoginButton = new JButton("Manager Login");
        JPanel content = new JPanel(new GridLayout(2, 1, 10, 10));

        /// ADD BUTTONS TO DISPLAY
        content.add(employeeLoginButton);
        content.add(managerLoginButton);
        setContentPane(content);


        /// ADD ACTIONS TO BUTTONS
        employeeLoginButton.addActionListener(e -> employeePermissionCheck(content,false));
        managerLoginButton.addActionListener(e -> employeePermissionCheck(content,true));
    }

    /**
     * The function Displays the login and checks employee permission
     * 
     * The method asks the user for an employee ID. It then verfies if 
     * the Id is in the database, and opens the correct right GUI depending 
     * if they are logining for the employee or manager GUI. 
     * 
     * @param content main panel content
     * @param managerButtonClicked true if manager login was clicked
     */
    // HELPER FUNCTIONS
    /// HELPER FUNCTION TO CHECK EMPLOYEE PERMSISSIONS
    private void employeePermissionCheck(JPanel content, boolean managerButtonClicked) {
        
        JPanel employeeLoginPanel = new JPanel();
        employeeLoginPanel.setLayout(new BoxLayout(employeeLoginPanel, BoxLayout.Y_AXIS));
        employeeLoginPanel.add(Box.createVerticalGlue());

        JLabel text = new JLabel("Enter Employee ID: ");
        text.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        employeeLoginPanel.add(text);

        JTextField employeeIdField = new JTextField(15);
        employeeIdField.setMaximumSize(new Dimension(200,30));
        employeeIdField.setAlignmentX(Component.CENTER_ALIGNMENT);
        employeeLoginPanel.add(employeeIdField);

        JButton enterButton = new JButton("Enter");
        JButton returnButton = new JButton("Return");

        employeeLoginPanel.add(Box.createRigidArea(new Dimension(0,10)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,10,0));
        buttonPanel.add(enterButton);
        buttonPanel.add(returnButton);
        
        employeeLoginPanel.add(buttonPanel);
        employeeLoginPanel.add(Box.createVerticalGlue());

        setContentPane(employeeLoginPanel);
        revalidate();
        repaint();

        returnButton.addActionListener(e->{ dispose(); LoginFrame newFrame = new LoginFrame(conn); newFrame.setVisible(true);});

        enterButton.addActionListener(e -> {
            String textInput = employeeIdField.getText().trim();
            if(textInput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an Employee ID.");
                return;
            }
            try{int employeeId = Integer.parseInt(textInput);
                Statement query = conn.createStatement();
                ResultSet result = query.executeQuery(
                "SELECT employeePosition FROM Employees " +
                "WHERE employeeId = " + employeeId + ";");
                
                if(!employeeIdValid(employeeId)) {
                    JOptionPane.showMessageDialog(null, "Enter a valid Employee ID.");
                }
                else if(result.next()) {
                    String position = result.getString("employeePosition");
                    signedInEmployeeId = employeeId;
                    signedInRole = position;
                    //need because would send straight to manager regardless of option
                    if(managerButtonClicked){
                        if(position.equals("Manager")) {
                            openGUIManager();
                        }
                        else {
                        JOptionPane.showMessageDialog(this, "Incorrect Permissions:\n");
                        //content.setVisible(true);
                }
                    }
                    else{
                        openGUIEmployee();
                    }
                }
                result.close();
                query.close();
            }
            catch (Exception ex){
                JOptionPane.showMessageDialog(this, "Error accessing Database:\n" + ex.getMessage());
            }
        });
    }

    /**
     * This Function checks if the employee ID given is part of the system.
     * 
     * @param enteredId EmployeeId entered by the user.
     * @return true if employee ID exist in the database.
     */
    /// HELPER FUNCTION TO CHECK IF EMPLOYEEID ENTERED EXISTS IN THE EMPLOYEE TABLE
    public boolean employeeIdValid(int enteredId) {
        String retrieveAllEId = "SELECT employeeId FROM Employees WHERE employeeId = ?";

        try { PreparedStatement ps = conn.prepareStatement(retrieveAllEId);
            ps.setInt(1, enteredId);
            ResultSet results = ps.executeQuery();
            if(!results.next()) {
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error validating Employee Id:\n" + e.getMessage());
        }
        return true;
    }

    /**
     * Opens the Manager GUI after login.
     */
    /// HELPER FUNCTION TO OPEN MANAGER GUI
    private void openGUIManager() {
        dispose();            
        GUI_Manager main = new GUI_Manager(conn, signedInEmployeeId, signedInRole);  
        main.setVisible(true);
    }

    /**
     * Opens the Employee GUI after login. 
     */
    /// CLOSE 
    private void openGUIEmployee() {
        dispose();
        GUI_Employee main = new GUI_Employee(conn, signedInEmployeeId, signedInRole);
        main.setVisible(true);
    }

}
