import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/// NOTE TO SELF BEFORE START WORK: ALLOW MANAGERS TO ENTER PRODUCTINVENTORY JFRAME FROM PRODUCTS SCREEN -> PASS IN PRODUCTID AS ARG
public class GUI_productInventory extends JFrame implements ActionListener {
    private final Connection conn;
    private int signedInEmployeeId;
    private String signedInRole;
    private int productId;
    private JPanel decrementPanel;
    private JPanel allIngredientsPanel;

    /**SETS UP INVENTORY GUI
     * 
     * @param conn current connection to database
     * @param employeeId current employee ID logged in
     * @param role role connected to the current employee ID logged in
     * @param productID current product being edited
     */
    public GUI_productInventory(Connection conn, int employeeId, String role, int productId) {
        this.conn = conn;
        this.signedInEmployeeId = employeeId;
        this.signedInRole = role;
        this.productId = productId;
        initComponents();
    }

    private void initComponents() {
        setTitle("Edit Product Ingredients");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new BorderLayout());
        decrementPanel = new JPanel(new GridLayout(4,4));
        //decrementPanel.setLayout(new BoxLayout(decrementPanel, BoxLayout.Y_AXIS));
        allIngredientsPanel = new JPanel(new BorderLayout());
        allIngredientsPanel.setLayout(new BoxLayout(allIngredientsPanel, BoxLayout.Y_AXIS));
        getcurrProductIngredients();
        getAllIngredients();

        JButton closeButton = new JButton("Return");

        closeButton.addActionListener(e -> productValidationCheck());

        JPanel bottom = new JPanel(new BorderLayout(1, 4));
        bottom.add(closeButton, BorderLayout.WEST);

        p.add(bottom, BorderLayout.SOUTH);
        p.add(decrementPanel, BorderLayout.WEST);
        p.add(allIngredientsPanel, BorderLayout.EAST);  
        setContentPane(p);
    }

    // HELPER FUNCTION SECTION
    

    /** HELPER FUNCTION TO GET & BUILD INVENTORY ITEM DECREMENT BUTTONS
     * 
     * @return void
     */
    public void getcurrProductIngredients() {
        String drawCurrProductIngredients = "SELECT i.inventoryId, i.inventoryName, COALESCE(pi.numIngredients, 0) AS numIngredients " + 
                                            "FROM InventoryItems i " +
                                            "LEFT JOIN ProductIngredients pi ON pi.inventoryId = i.inventoryId " +
                                            "AND pi.productId = ?";

        try(PreparedStatement ps = conn.prepareStatement(drawCurrProductIngredients)) {
            ps.setInt(1, productId);
            ResultSet result = ps.executeQuery();
            while(result.next()) {
                int numOfIngredients = result.getInt("numIngredients");
                int inventoryId = result.getInt("inventoryId");
                JPanel ingredientPanel = new JPanel(new BorderLayout());
                JButton inventoryItemButton = new JButton("-");
                inventoryItemButton.setFont(new Font("Arial", Font.BOLD, 15));
                inventoryItemButton.setPreferredSize(new Dimension(100, 200));

                JTextArea ingredientQuantity = new JTextArea(Integer.toString(numOfIngredients));
                ingredientQuantity.setFont(new Font("Arial", Font.BOLD, 20));

                inventoryItemButton.addActionListener(e -> {removeThisIngredient(inventoryId); refreshDecrementPanel();});
                inventoryItemButton.setEnabled(numOfIngredients > 0);
                ingredientPanel.add(inventoryItemButton, BorderLayout.EAST);
                ingredientPanel.add(ingredientQuantity, BorderLayout.WEST);
                decrementPanel.add(ingredientPanel);
            }
            decrementPanel.revalidate();
            decrementPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to get ingredients in product:\n" + e.getMessage());
        }
    }   

    /** HELPER FUCNTION TO GET THE LIST OF ALL INVENTORY ITEMS 
     * 
     * return void
     */
    public void getAllIngredients() {
        String drawAllIngredients = "SELECT inventoryId, inventoryName FROM InventoryItems";

        try(PreparedStatement ps = conn.prepareStatement(drawAllIngredients)) {
            ResultSet result = ps.executeQuery();
            while(result.next()) {
                int inventoryId = result.getInt("inventoryId");
                JButton inventoryItemButton = new JButton(result.getString("inventoryName") + " +");
                inventoryItemButton.setPreferredSize(new Dimension(200, 150));
                inventoryItemButton.setFont(new Font("Arial", Font.BOLD, 15));
                inventoryItemButton.addActionListener(e -> {addThisIngredient(inventoryId); refreshDecrementPanel();});
                allIngredientsPanel.add(inventoryItemButton);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to get inventory items:\n" + e.getMessage());
        }
    }

    
    /** HELPER FUNCTION TO ADD NEW INVENTORYITEMS ASSOCIATED TO THE CURRENT PRODUCT
     * 
     * @param inventoryId id of the inventory item to add to product ingredients
     * @return void
     */
    public void addThisIngredient(int inventoryId) {

        // PREAPRED STATEMENT 1: ATTEMPT INSERTING INTO ProductIngredients
            // UPON CONFLICT, INSTEAD INCREMENT numIngredients BY 1
        String insertThisIngredient = "INSERT INTO ProductIngredients (productId, inventoryId, numIngredients) " +
            "VALUES (?, ?, 1) " +
            "ON CONFLICT (productId, inventoryId) " +
            "DO UPDATE SET numIngredients = ProductIngredients.numIngredients + 1";

            // ADDS INTO OR INCREMENTS ProductIngredients
            try(PreparedStatement ps2 = conn.prepareStatement(insertThisIngredient)) {
                ps2.setInt(1, productId);
                ps2.setInt(2, inventoryId);
                int rows = ps2.executeUpdate();
                JOptionPane.showMessageDialog(null, "Successfully added ingredient to product");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Failed to add ingredient to product:\n" + ex.getMessage());
            }
    }

    
    /** HELPER FUNCTION TO REMOVE INVENTORYITEMS ASSOCIATED WITH THE CURRENT PRODUCT
     * 
     * @param inventoryId id of the inventory item to remove from a product's ingredients
     * @return void
     */
    public void removeThisIngredient(int inventoryId) {

        // PREPRARED STATEMENT 1: GET numIngredients
        String getNumIngredients = "SELECT numIngredients FROM ProductIngredients WHERE productId = ? AND inventoryId = ?";

        // PREPARED STATEMENT 2: DELETE ENTRY FROM ProductIngredients
        String deleteThisIngredient = "DELETE FROM ProductIngredients " +
            "WHERE productId = ? AND inventoryId = ?";

        // PREPARED STATEMENT 3: DECREMENT numIngredients
        String decrementThisIngredient = "UPDATE ProductIngredients SET numIngredients = " +
            "? WHERE productId = ? AND inventoryId = ?";

        // FIRST GETS THE numIngredients OF THIS inventoryItem FROM ProductIngredient
        try(PreparedStatement ps1 = conn.prepareStatement(getNumIngredients)) {
            ps1.setInt(1, productId);
            ps1.setInt(2, inventoryId);
            ResultSet result = ps1.executeQuery(); 

            while(result.next()) {
                int newNumIngredient = result.getInt("numIngredients");

                // IF THE numIngredients IS INVALID (less than 1), BREAK LOOP
                if (newNumIngredient < 1) {
                    JOptionPane.showMessageDialog(null, "Attempting to remove invalid ingredient:\n");
                    return;
                }

                // IF THE numIngredients == 1, DELETE (productId, inventoryId) PK FROM ProductIngredients
                else if(newNumIngredient == 1) {
                    try(PreparedStatement ps2 = conn.prepareStatement(deleteThisIngredient)) {
                        ps2.setInt(1, productId);
                        ps2.setInt(2, inventoryId);
                        int rows = ps2.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Successfully removed this Ingredient\n");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Failed to remove this Ingredient:\n" + ex.getMessage());
                    }
                }

                // IF THE numIngredients > 1, DECREMENT INGREDIENT by 1
                else {
                    try(PreparedStatement ps3 = conn.prepareStatement(decrementThisIngredient)) {
                        ps3.setInt(1, newNumIngredient - 1);
                        ps3.setInt(2, productId);
                        ps3.setInt(3, inventoryId);
                        int rows = ps3.executeUpdate();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Failed to decrement this Ingredient:\n" + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Couldn't get numIngredients");
        }
        revalidate();
        repaint();
    }

    
    /** HELPER FUNCTION TO REFRESH ITEM DECREMENT PAGE (FOR ACCURATE COUNTS)
     * 
     * @return void
     */
    private void refreshDecrementPanel() {
        decrementPanel.removeAll();
        getcurrProductIngredients();     
        decrementPanel.revalidate();
        decrementPanel.repaint();
    }


    
    /** HELPER FUNCTION TO CHECK IF PRODUCT IS OKAY TO RETURN BACK TO MANAGER VIEW 
     * (ie: does it have at least 1 ingredient)
     * 
     * @return void
     */
    private void productValidationCheck() {
        String getNumIngredients = "SELECT numIngredients FROM ProductIngredients WHERE productId = ?";
        boolean notAllZeroes = false;
        try(PreparedStatement ps = conn.prepareStatement(getNumIngredients)) {
            ps.setInt(1, productId);
            ResultSet result = ps.executeQuery(); 
            while(result.next()) {
                if(result.getInt("numIngredients") > 0) {
                    notAllZeroes = true;
                }
            }

            if(notAllZeroes) {openGUIManager();}
            else {JOptionPane.showMessageDialog(null, "Cannot have products with 0 ingredients");}

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to get number of ingredients for this product:\n" + e.getMessage());
        }
    }
    

    /** HELPER FUNCTION TO RETURN BACK TO MANAGER GUI
     * 
     * @return void
     */
    private void openGUIManager() {
        dispose();            
        GUI_Manager main = new GUI_Manager(conn, signedInEmployeeId, signedInRole);  
        main.setVisible(true);
    }

    /** CLOSE
     * 
     * @return void
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Close".equals(e.getActionCommand())) {
            dispose(); 
        }
    }
}