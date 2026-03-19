import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.naming.spi.ResolveResult;
import javax.swing.*;
import java.util.*;
import java.util.*;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.Arrays;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

// EMPLOYEE UI PAGE

/**
 * GUI for Employees view of the POS system.
 * 
 * This class creates the Employee view of the POS. This is 
 * where products can be selected, orders can be created and 
 * payments can be processed. It connects to the database to 
 * retrieve products update inventory and store orders.
 *  
 * @author Caden Guillot
 * @author Yuki Noda 
 * @author Anibal Gomez
 */

public class GUI_Employee extends JFrame implements ActionListener {

    private final Connection conn;
    //Products table
    private Vector<String> columnNames;
    private Vector<Vector<String>> col;

    private JScrollPane tableScroll;

    private int signedInEmployeeId;
    private String signedInRole;

    // CALLS INITCOMPONENTS, BUILDING THE PAGE WITH THE CONNECTION
    public GUI_Employee(Connection conn, int employeeId, String role) {
        this.conn = conn;
        this.signedInEmployeeId = employeeId;
        this.signedInRole = role;
        initComponents();  
    }

    // "global" variables used in initComponents() and other funcs below 
    private JTextArea orderDetails;
    double totalCost = 0;
    JLabel totalCostLabel;

    private int currOrderId;
    private int currCustomerId;

    /** INIT COMPONENTS - MAIN FUNCTION OF THIS FILE. ADDS / EDITS ELEMENTS TO THE JFrame
     * 
     * 
     */
    private void initComponents() {
        setTitle("POS System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setSize(1000, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        JPanel p = new JPanel(new BorderLayout());
        JPanel centerDrinks = new JPanel(new GridLayout (5,5));
        centerDrinks.setBorder(BorderFactory.createLineBorder(Color.black));
        tableScroll = new JScrollPane(centerDrinks);

        currOrderId = 0;
        currCustomerId = currCustomerId();

        drawFromProductsTable(); 
        addToJPanel(centerDrinks);


        // PRODUCT DEFAULTS (for Project 3)
        // String[] currSize = {"Medium"};
        // int[] currToppings = {0, 0, 0};
        //     //[Tapioca Pearl int 1/0, Milk Foam 1/0, Extra Sugar 1/0]. Used in addToOrder func 


        /// INITIALIZE BUTTONS
        // JButton closeButton = new JButton("Close");
        JButton managerLoginButton = new JButton("Manager View");
        JButton logoutButton = new JButton("Log Out");
        JButton payCardButton = new JButton("Pay Card");
        JButton payCashButton = new JButton("Pay Cash");

        //drink options, abbreviated

        //PROJECT 3
        // //TODO: MAKE EDIT PRODUCT PANEL WITH SIZE AND TOPPING BUTTONS
        // //size options, default Medium
        // JButton smallSizeButton = new JButton("Small");
        // JButton mediumSizeButton = new JButton("Medium");
        // JButton largeSizeButton = new JButton("Large");
        // //topping options
        // JToggleButton pearlToppButton = new JToggleButton("Add Tapioca");
        // JToggleButton milkFoamToppButton = new JToggleButton("Add Milk Foam");
        // JToggleButton sugarToppButton = new JToggleButton("Add Sugar");

        // JButton addToOrdButton = new JButton("Add To \n Order");
        //drink options, abbreviated


        /// ADD ACTIONS TO BUTTONS
        // closeButton.addActionListener(this);
        managerLoginButton.addActionListener(e -> employeePermissionCheck(p));
        logoutButton.addActionListener(e -> System.exit(0));

        // ********************************************************************************************* TODO: Have pay buttons clear orderDetails & update database


        //FOR PROJECT 3
        // smallSizeButton.addActionListener(e -> {currSize[0] = "Small";});
        // mediumSizeButton.addActionListener(e -> {currSize[0] = "Medium";});
        // largeSizeButton.addActionListener(e -> {currSize[0] = "Large";});
        // addToOrdButton.addActionListener(e -> {drawFromProductsTable(); addToJPanel(p);});

        // // TODO: TOGGLE TOPPINGS BY CHANGING currToppings[x] TO 1/0
        // /* pearlToppButton = new JToggleButton("Add Tapioca");
        // milkFoamToppButton = new JToggleButton("Add Milk Foam");
        // sugarToppButton = new JToggleButton("Add Sugar"); */



        

        //LABELS
        JLabel orderNumLabel = new JLabel("Order Number: ");
        JLabel orderDetailsLabel = new JLabel("Order Details: ");
        JLabel drinksLabel = new JLabel("Drinks");
        totalCostLabel = new JLabel("Total: $0");
        orderDetails = new JTextArea("");
            orderDetails.setEditable(false);


        payCardButton.addActionListener(e -> finishOrderCard(totalCost));
        payCashButton.addActionListener(e -> finishOrder(totalCost));

        // ********************************************************************************************* TODO: Have pay buttons clear orderDetails & update database

        /// CREATE PAGE PANELS
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createLineBorder(Color.black));
        // PHASE 3 TODO: MAKE INCREMENTING ORDER #
        //right.add(orderNumLabel, BorderLayout.NORTH);
        right.add(orderDetailsLabel, BorderLayout.NORTH);
        JPanel orderDetailPanel = new JPanel(new BorderLayout());
            orderDetailPanel.add(orderDetails);
            right.add(orderDetailPanel, BorderLayout.CENTER);
        JPanel payPanel = new JPanel(new BorderLayout());
            payPanel.add(totalCostLabel, BorderLayout.NORTH);
            payPanel.add(payCardButton, BorderLayout.WEST);
            payPanel.add(payCashButton, BorderLayout.EAST);
            right.add(payPanel, BorderLayout.SOUTH);

        JPanel top = new JPanel();
        top.add(drinksLabel);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(logoutButton, BorderLayout.WEST);
        //  bottom.add(closeButton, BorderLayout.CENTER);
        bottom.add(managerLoginButton, BorderLayout.EAST);

        // centerDrinks.add(new JLabel("Teas"));
        //     JPanel teasPanel = new JPanel();
        // centerDrinks.add(new JLabel("Coffees"));
        //     JPanel coffeesPanel = new JPanel();
        // centerDrinks.add(new JLabel("Others"));
        //     JPanel othersPanel = new JPanel();

        p.add(top, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.SOUTH);
        p.add(right, BorderLayout.EAST);
        p.add(tableScroll, BorderLayout.CENTER);
        //p.add(centerDrinks, BorderLayout.CENTER);

        //PROJECT 3 TODO: MAKE EDIT PRODUCT PANEL
        /* JLayeredPane productInfoPane = new JLayeredPane();
        productInfoPane.setSize(100, 100);
        JLabel productNameLabel = new JLabel("Testing");
        productInfoPane.add(productNameLabel);
        //productInfoPane.setVisible(false);
        p.add(productInfoPane, BorderLayout.CENTER); */
        // PROJECT 3 TODO: ADD SIZE BUTTONS TO PRODUCT EDIT PANEL
        // PROJECT 3 TODO: ADD addToOrdButton TO PRODUCT EDIT PANEL 

        p.add(centerDrinks, BorderLayout.CENTER);
        setContentPane(p);
    }

    /// HELPER FUNCTION TO CHECK EMPLOYEE PERMISSIONS
    private void employeePermissionCheck(JPanel content) {
        
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

                    if(position.equals("Manager")) {
                        openGUIManager();
                    }
                    else {
                    JOptionPane.showMessageDialog(this, "Incorrect Permissions:\n");
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


    /** HELPER FUNCTION TO OPEN MANAGER GUI
     *
     * 
     */
    private void openGUIManager() {
        dispose();            
        GUI_Manager main = new GUI_Manager(conn, signedInEmployeeId, signedInRole);  
        main.setVisible(true);
    }


    /** HELPER FUNCTION TO GET PRICE FROM DATABASE 
    * 
    * @param productId is the product we are trying to pull the price of 
    * @return a string of a float no matter what. If no product can be found then the method returns "-1"
    */ 
    public String getPrice(String productId){
        String price = "-1";
        for (Vector<String> product : drawFromProductsTable()){
            if ((product.get(0).equals(productId))){
                price = product.get(3);
                break;
            }
        }
        return price;
    }


    /** HELPER FUNCTION TO ADD PRODUCT TO ORDER
     *
     * @param productId id of the product being added
     * @param productName name of the product being added
     */
    public void addToOrderProducts(String productId, String productName) {
        try { startNewOrderIfNeeded();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Could not start order:\n" + ex.getMessage());
            return;
        }
        double price = Double.parseDouble(getPrice(productId));
        String priceVal = String.format("%.2f", price);
        String saveOrderDetails = orderDetails.getText();
        orderDetails.append(productName + " : $" + priceVal + "\n");
        totalCost += price;
        totalCostLabel.setText("Total: $" + String.valueOf(totalCost));

        /// QUERY THAT DECREMENTS ALL INVENTORY ITEMS ATTACHED TO PRODUCT 
        totalCostLabel.setText("Total: $" + totalCost);
            try { conn.setAutoCommit(false);
                String query1String = "SELECT * FROM ProductIngredients WHERE productId = ?";
                try (PreparedStatement query = conn.prepareStatement(query1String)) {
                    query.setInt(1, Integer.parseInt(productId));
                    ResultSet result = query.executeQuery(); {

                    while (result.next()) {
                        String thisInventoryid = Integer.toString(result.getInt("inventoryId"));
                        String thisNumIngredients = Integer.toString(result.getInt("numIngredients"));
                        String query2String = "SELECT quantityHeld FROM InventoryStock WHERE inventoryId = ?";

                        try (PreparedStatement query2 = conn.prepareStatement(query2String)) {
                            query2.setInt(1, Integer.parseInt(thisInventoryid));
                            ResultSet result2 = query2.executeQuery(); {

                            while (result2.next()) {
                                int currQuantityHeld = result2.getInt("quantityHeld");
                                if ((currQuantityHeld - result.getInt("numIngredients")) < 0) {
                                    orderDetails.setText(saveOrderDetails);
                                    throw new SQLException("Not enough inventory stock to make this product");
                                }
                            }
                            }
                        }

                        String query3String = "UPDATE InventoryStock SET quantityHeld = quantityHeld - ? WHERE inventoryId = ?";
                        try (PreparedStatement query3 = conn.prepareStatement(query3String)) {
                            query3.setInt(1, Integer.parseInt(thisNumIngredients));
                            query3.setInt(2, Integer.parseInt(thisInventoryid));
                            query3.executeUpdate();
                        }
                    }
                }
            }
                String upsert = "INSERT INTO OrderProducts (orderId, productId, productQuantity) VALUES (?, ?, 1) ON CONFLICT (orderId, productId) DO UPDATE SET productQuantity = OrderProducts.productQuantity + 1 ";

                try (PreparedStatement ps = conn.prepareStatement(upsert)) {
                    ps.setInt(1, currOrderId);                
                    ps.setInt(2, Integer.parseInt(productId));
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (Exception e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                orderDetails.setText(saveOrderDetails);
                totalCost -= price;
                totalCostLabel.setText("Total: $" + totalCost);
                JOptionPane.showMessageDialog(null, "Error adding product:\n" + e.getMessage());
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
    }
    
    /** HELPER FUNCTION THAT RETURNS EACH ROW OF THE PRODUCTS TABLE AS A VECTOR OF VECTOR OF STRINGS
     * 
     * @return vector of vectors of strings 
     */
    public Vector<Vector<String>> drawFromProductsTable() {
        columnNames = new Vector<>(Arrays.asList("productId", "productName", "productType", "productPrice"));
        col = new Vector<>();

        try (Statement query = conn.createStatement();
            ResultSet result = query.executeQuery(
                "SELECT * FROM PRODUCTS ORDER BY productId")) {
                while(result.next()) {
                    Vector<String> row = new Vector<>(4);
                    row.add(Integer.toString(result.getInt("productId")));
                    row.add(result.getString("productName"));
                    row.add(result.getString("productType"));
                    row.add(Double.toString(result.getDouble("productPrice")));
                    col.add(row);
                }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database:\n" + e.getMessage());
        }
        return(col);
    }


    /** HELPER FUNCTION TO MAKE BUTTONS FOR EACH ELEMENT IN THE VECTOR
     * 
     * @param currPanel JPanel the caller is added to
     * 
     */
    public void addToJPanel(JPanel currPanel) {
        for(int i = 0; i < col.size(); i++) {
            int productId = Integer.parseInt(col.elementAt(i).elementAt(0));
            String productName = col.elementAt(i).elementAt(1);
            String productPrice = col.elementAt(i).elementAt(3);
            JButton productButton = new JButton("<html><body style='text-align:center'>" + productName + "</body></html>");

            /// BUTTON STYLE FONT
            productButton.setFont(new Font("Arial", Font.BOLD, 15));

            /// GIVE EACH BUTTON FUNCTIONALITY: ADD TO ORDERPRODUCTS TABLE 
            productButton.addActionListener(e -> addToOrderProducts(Integer.toString(productId), productName));

            /// ADD EACH BUTTON TO JPANEL PASSED INTO AS ARG
            currPanel.add(productButton);
            
        }
        revalidate();
        repaint();
    }

    /** COMPLETES ORDER MADE BY CASH AND ADDS TO DATABASE
     * 
     * @param orderTotal the total of the current order. Typically from totalCost
     * 
     */
    public void finishOrder(double orderTotal) {
        if (orderTotal == 0) {
            JOptionPane.showMessageDialog(null, "Empty Order:\n");
            return;
        }
        if (currOrderId == 0) {
            JOptionPane.showMessageDialog(null, "No active order.");
            return;
        }

        Instant timestamp = (Instant.now()).truncatedTo(ChronoUnit.SECONDS);
        String finishOrderString = " UPDATE Orders SET orderTime = ?, orderTotal = ?, cardPayment = ? WHERE orderId = ?";

        try (PreparedStatement ps = conn.prepareStatement(finishOrderString)) {
            ps.setTimestamp(1, Timestamp.from(timestamp));
            ps.setDouble(2, orderTotal);
            ps.setBoolean(3, false);
            ps.setInt(4, currOrderId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Succssfully Added Order!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error finishing order:\n" + e.getMessage());
            return;
        }

        // REFERSH GUI
        totalCost = 0;
        totalCostLabel.setText("Total: $0");
        orderDetails.setText("");

        // REFRESH FOR THE NEXT ORDER
        currOrderId = 0;             
        currCustomerId += 100;      
    }

    /** COMPLETES ORDER MADE BY CARD AND ADDS TO DATABASE
     * 
     * @param orderTotal the total of the current order. Typically from totalCost
     * 
     */
    public void finishOrderCard(double orderTotal) {
        if (orderTotal == 0) {
            JOptionPane.showMessageDialog(null, "Empty Order:\n");
            return;
        }
        if (currOrderId == 0) {
            JOptionPane.showMessageDialog(null, "No active order.");
            return;
        }

        Instant timestamp = (Instant.now()).truncatedTo(ChronoUnit.SECONDS);
        String finishOrderString = " UPDATE Orders SET orderTime = ?, orderTotal = ?, cardPayment = ? WHERE orderId = ?";

        try (PreparedStatement ps = conn.prepareStatement(finishOrderString)) {
            ps.setTimestamp(1, Timestamp.from(timestamp));
            ps.setDouble(2, orderTotal);
            ps.setBoolean(3, true);
            ps.setInt(4, currOrderId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Succssfully Added Order!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error finishing order:\n" + e.getMessage());
            return;
        }

        // REFERSH GUI
        totalCost = 0;
        totalCostLabel.setText("Total: $0");
        orderDetails.setText("");

        // REFRESH FOR THE NEXT ORDER
        currOrderId = 0;             
        currCustomerId += 100;      
    }

    /** HELPER FUNCTION THAT RETRIEVES THE MOST RECENT CUSTOMERID 
     * 
     * @return int that is the next customer id 
     */
    public int currCustomerId() {
        int customerId = 0;
        try (Statement query = conn.createStatement();
            ResultSet result = query.executeQuery(
            "SELECT customerId FROM Orders ORDER BY customerId DESC LIMIT 1")) {
                    while(result.next()) {
                        customerId = result.getInt("customerId") + 100;
                    }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing:\n" + e.getMessage());
        }
        return customerId;
    }


    /** HELPER FUNCTION THAT RETRIEVES THE CURRENT ORDERID
     * 
     * @return int that is the next order id
     */
    public int currOrderId() {
        int orderId = 0;
        try (Statement query = conn.createStatement();
            ResultSet result = query.executeQuery(
            "SELECT orderId FROM Orders ORDER BY orderId DESC LIMIT 1")) {
                    while(result.next()) {
                        orderId = result.getInt("orderId") + 1;
                    }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database:\n" + e.getMessage());
        }
        return orderId;
    }

    /** HELPER FUNCTION TO START NEW ORDER 
     * 
     * 
     */
    private void startNewOrderIfNeeded() throws SQLException {
        if (currOrderId != 0) return; 

        Instant timestamp = (Instant.now()).truncatedTo(ChronoUnit.SECONDS);

        String sql = "INSERT INTO Orders (employeeId, customerId, orderTime, orderTotal) VALUES (?, ?, ?, ?) RETURNING orderId ";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, signedInEmployeeId);
            ps.setInt(2, currCustomerId);
            ps.setTimestamp(3, Timestamp.from(timestamp));
            ps.setDouble(4, 0.0);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Failed to create order (no orderId returned).");
                currOrderId = rs.getInt(1); 
            }
        }
    }

    /** HELPER FUNCTION TO CHECK IF EMPLOYEEID ENTERED EXISTS IN THE EMPLOYEE TABLE
     * 
     * @param enteredId employee ID pulled from text input
     * @return boolean of True if the entered employee ID already exists in the database, False if not
     */
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

    /** CLOSE
     * 
     * @param e An action event triggered when the close button is pressed. 
     * 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Close".equals(e.getActionCommand())) {
            dispose(); 
        }
    }
}