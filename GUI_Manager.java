import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import java.util.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

import java.io.File;
import java.io.PrintWriter;
import java.time.*;

/**
 * GUI_Manager creates the manager interface for the system
 * 
 * This class allows managers to view, edit, insert and delete
 * database data for employees, products, and inventory. It also
 * allows to viewing reports and exporting tables.
 * 
 * @author Caden Guillot
 * @author Yuki Noda
 * @author Anibal Gomez 
 */
// MANAGER UI PAGE 
public class GUI_Manager extends JFrame implements ActionListener {

    private final Connection conn;
    private Vector<String> columnNames;
    private Vector<Vector<String>> col;

    private String activeInsertSql;
    private Set<Integer> activeNewRows;

    private String activeDeleteSql;
    private Set<Integer> activeDeletedRows;

    private JScrollPane tableScroll;
    private JButton employeesMenuButton;
    private JButton inventoryStatsButton;
    private JButton productMenuButton;
    private JButton inventoryStockButton;

    private JButton saveButton;

    private JButton exportCsvButton;

    private DefaultTableModel tableModel;
    private JTable activeTable;                 
    private String activeUpdateSql;            
    private int activeIdColumnIndex = -1;      
    private Set<Integer> activeDirtyRows;       
    private Set<Integer> activeNonDbColumnIndexes = Collections.emptySet(); 
    private Integer activePriceColumnIndex = null;
    private Set<Integer> activeIntColumnIndexes = Collections.emptySet();

    private int signedInEmployeeId;
    private String signedInRole;

    private boolean addButtonClicked;
    private boolean deleteButtonClicked;
    
    /**
     * Creates the Manager GUI.
     * 
     * Initializes the manager interface using the database 
     * connection and signed in employee information. 
     * 
     * @param conn database connection
     * @param employeeId id of signed in employee
     * @param role role of signed in employee
     */
    // CALLS INITCOMPONENTS, BUILDING THE PAGE WITH THE CONNECTION
    public GUI_Manager(Connection conn, int employeeId, String role) {
        this.signedInEmployeeId = employeeId;
        this.signedInRole = role;
        this.conn = conn;
        initComponents();
    }

    /**
     * Initialize all GUI components for the manager Page
     * 
     * Creates buttons, panels, tables, and layouts. 
     */
    // INIT COMPONENTS - MAIN FUNCTION OF THIS FILE. ADDS / EDITS ELEMENTS TO THE JFrame
    private void initComponents() {
        setTitle("Management Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        JPanel p = new JPanel(new BorderLayout());
        tableScroll = new JScrollPane();

        /// INITIALIZE BUTTONS
        JButton closeButton = new JButton("Close");
        employeesMenuButton = new JButton("Employees Menu");
        JButton employeeLoginButton = new JButton("Employee View");
        inventoryStatsButton = new JButton("Inventory Item Stats");
        productMenuButton = new JButton("Product Menu");
        inventoryStockButton = new JButton("Inventory Stock");
        saveButton = new JButton("Save Changes");
        JButton addRowButton = new JButton("Add Row");
        JButton deleteRowButton = new JButton("Delete Row");
        JButton rushButton = new JButton("Sales Today");
        JButton IngredientUsageButton = new JButton("Change Dates");
        JButton productInventoryviewButton = new JButton("Edit Product");
        exportCsvButton = new JButton("Export CSV");

        /// ADD ACTIONS TO BUTTONS
        closeButton.addActionListener(this);
        productInventoryviewButton.addActionListener(e -> editSelectedProduct());
        employeesMenuButton.addActionListener(e -> {
            activeButtonColorChange(employeesMenuButton);
            showTable(makeEmployeesMenuTable(), p, tableScroll);
            saveButton.setVisible(true);
            addRowButton.setVisible(true);
            deleteRowButton.setVisible(true);
            exportCsvButton.setVisible(false);
            productInventoryviewButton.setVisible(false);
            rushButton.setVisible(false);
            IngredientUsageButton.setVisible(false);

        });
        employeeLoginButton.addActionListener(e -> openGUIEmployee());
        inventoryStatsButton.addActionListener(e -> {
            activeButtonColorChange(inventoryStatsButton);
            showTable(makeInventoryStatsTable(false), p, tableScroll);
            saveButton.setVisible(false);
            addRowButton.setVisible(false);
            deleteRowButton.setVisible(false);
            exportCsvButton.setVisible(true);
            productInventoryviewButton.setVisible(false);
            rushButton.setVisible(true);
            IngredientUsageButton.setVisible(true);

        });
        productMenuButton.addActionListener(e -> {
            activeButtonColorChange(productMenuButton);
            showTable(makeProductMenu(), p, tableScroll);
            saveButton.setVisible(true);
            addRowButton.setVisible(true);
            deleteRowButton.setVisible(true);
            exportCsvButton.setVisible(false);
            productInventoryviewButton.setVisible(true);
            rushButton.setVisible(false);
            IngredientUsageButton.setVisible(false);

        });
        inventoryStockButton.addActionListener(e -> {
            activeButtonColorChange(inventoryStockButton);
            showTable(makeInventoryStockTable(), p, tableScroll);
            saveButton.setVisible(true);
            addRowButton.setVisible(true);
            deleteRowButton.setVisible(true);
            exportCsvButton.setVisible(false);
            productInventoryviewButton.setVisible(false);
            rushButton.setVisible(false);
            IngredientUsageButton.setVisible(false);
        });
        saveButton.addActionListener(e -> saveDbChanges());
        addRowButton.addActionListener(e-> addRowtoActiveTable());
        deleteRowButton.addActionListener(e -> deleteSelectedRow());
        exportCsvButton.addActionListener(e -> exportActiveTableToCSV());
        rushButton.addActionListener(e->ShowRushReport());
        IngredientUsageButton.addActionListener(e->{JTable table = makeInventoryStatsTable(true); if(table!=null){showTable(table,p,tableScroll);}});
        
        /// CREATE PAGE MAIN BUTTONS
        JPanel top = new JPanel();
        top.add(employeesMenuButton);
        top.add(inventoryStockButton);
        top.add(productMenuButton);
        top.add(inventoryStatsButton);
        JPanel bottom = new JPanel(new BorderLayout(1, 4));
        bottom.add(closeButton, BorderLayout.WEST);
        bottom.add(employeeLoginButton, BorderLayout.EAST);
       
        // CREATE SIDE ACTION BUTTONS
        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        rightSide.add(saveButton);
        rightSide.add(exportCsvButton);
        rightSide.add(Box.createVerticalStrut(10));
        rightSide.add(addRowButton);
        rightSide.add(rushButton);
        rightSide.add(Box.createVerticalStrut(10));
        rightSide.add(deleteRowButton);
        rightSide.add(IngredientUsageButton);
        rightSide.add(Box.createVerticalStrut(10));
        rightSide.add(productInventoryviewButton);
        rightSide.add(Box.createVerticalStrut(10));
        exportCsvButton.setVisible(false);
        rushButton.setVisible(false);
        IngredientUsageButton.setVisible(false);

        // ADD PANELS TO MAIN PANEL
        p.add(top, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.SOUTH);
        p.add(rightSide, BorderLayout.EAST);
        p.add(tableScroll, BorderLayout.CENTER);
        setContentPane(p);
    }

    /**
     * Displays tables in the scroll panel.
     * 
     * @param table table to show
     * @param p main panel
     * @param tableScroll scroll pane
     */
    // INITCOMPONENTS HELPER FUNCTIONS
    /// HELPER FUNCTION TO CLEAN & DISPLAY NEW TABLES
    public void showTable(JTable table, JPanel p, JScrollPane tableScroll) {
        this.activeTable = table;
        table.setFillsViewportHeight(true);
        tableScroll.setViewportView(table);
        p.revalidate();
        p.repaint();
    }

    /**
     * This Function Makes the Empoyee Table from Employees in the database.
     * 
     * @return JTable cotaining employee data
     */
    /// HELPER FUNCTION TO MAKE EMPLOYEES TABLE
    public JTable makeEmployeesMenuTable() {
        columnNames = new Vector<>(Arrays.asList("employeeId", "employeeName", "employeePosition"));
        col = new Vector<>();
        activeNonDbColumnIndexes = Collections.emptySet();
        activeIntColumnIndexes = Collections.emptySet();
        activePriceColumnIndex = null;

        try (Statement query = conn.createStatement();
            ResultSet result = query.executeQuery(
                    "SELECT employeeId, employeeName, employeePosition FROM EMPLOYEES " +
                        "ORDER BY employeeId")) {
            while (result.next()) {
                Vector<String> row = new Vector<>(3);
                row.add(Integer.toString(result.getInt("employeeId")));
                row.add(result.getString("employeeName"));
                row.add(result.getString("employeePosition"));
                col.add(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database:\n" + e.getMessage());
        }

        activeDirtyRows = new HashSet<>();
        activeUpdateSql = "UPDATE EMPLOYEES SET employeeName = ?, employeePosition = ? WHERE employeeId = ?";
        activeIdColumnIndex = 0;
        activeNewRows = new HashSet<>();
        activeInsertSql = "INSERT INTO EMPLOYEES (employeeId, employeeName, employeePosition) VALUES (?, ?, ?)";
        activeDeletedRows = new HashSet<>();
        activeDeleteSql = "DELETE FROM EMPLOYEES WHERE employeeId = ?";

        tableModel = new DefaultTableModel(col, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                Object old = getValueAt(row, column);
                if (Objects.equals(old, aValue)) return;

                super.setValueAt(aValue, row, column);
                activeDirtyRows.add(row);
            }
        };

        return stripedTables(tableModel);
    }

    /**
     * Creates the product table from the products in the Database. 
     * 
     * @return JTable that contains product data
     */
    /// HELPER FUNCTION TO MAKE PRODUCT MENU
    public JTable makeProductMenu() {
        columnNames = new Vector<>(Arrays.asList("productId", "productName", "productType", "productPrice"));
        col = new Vector<>();
        activeNonDbColumnIndexes = Collections.emptySet();
        activeIntColumnIndexes = Collections.emptySet();
        activePriceColumnIndex = 3;

        try (Statement query = conn.createStatement();
            ResultSet result = query.executeQuery(
                    "SELECT productId, productName, productType, productPrice FROM Products ORDER BY productId")) {
            while (result.next()) {
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

        activeDirtyRows = new HashSet<>();
        activeUpdateSql = "UPDATE Products SET productName=?, productType=?, productPrice=? WHERE productId=?";
        activeIdColumnIndex = 0;
        activeNewRows= new HashSet<>();
        activeInsertSql = "INSERT INTO Products (productId, productName, productType, productPrice) VALUES (?, ?, ?, ?)";
        activeDeletedRows = new HashSet<>();
        activeDeleteSql = "DELETE FROM Products WHERE productId = ?";

        tableModel = new DefaultTableModel(col, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                Object old = getValueAt(row, column);
                if (Objects.equals(old, aValue)) return;

                super.setValueAt(aValue, row, column);
                activeDirtyRows.add(row);
            }
        };

        return stripedTables(tableModel);
    }

    /**
     * Creates the Inventory Stock table from inventoryItems and inventoryStock in the Database.
     * 
     * @return JTable that has Inventory data
     */
    /// HELPER FUNCTION TO MAKE INVENTORYSTOCK TABLE
    public JTable makeInventoryStockTable() {
        columnNames = new Vector<>(Arrays.asList("inventoryId", "inventoryName", "quantityHeld", "quantityInTransit"));
        col = new Vector<>();
        activeNonDbColumnIndexes = Set.of(1);
        activeIntColumnIndexes = Set.of(2, 3);
        activePriceColumnIndex = null;

        try (Statement query = conn.createStatement();
            ResultSet result = query.executeQuery(
                    "SELECT istock.inventoryId, iitem.inventoryName, istock.quantityHeld, istock.quantityInTransit " +
                            "FROM InventoryStock istock " +
                            "JOIN InventoryItems iitem ON istock.inventoryId = iitem.inventoryId " +
                            "ORDER BY istock.inventoryId")) {

            while (result.next()) {
                Vector<String> row = new Vector<>(4);
                row.add(Integer.toString(result.getInt("inventoryId")));
                row.add(result.getString("inventoryName"));
                row.add(Integer.toString(result.getInt("quantityHeld")));
                row.add(Integer.toString(result.getInt("quantityInTransit")));
                col.add(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database:\n" + e.getMessage());
        }

        activeDirtyRows = new HashSet<>();
        activeUpdateSql = "UPDATE InventoryStock SET quantityHeld=?, quantityInTransit=? WHERE inventoryId=?";
        activeIdColumnIndex = 0;
        activeNewRows = new HashSet<>();
        activeInsertSql = "INSERT INTO InventoryStock (inventoryId, quantityHeld, quantityInTransit) VALUES (?, ?, ?)";
        activeDeletedRows = new HashSet<>();
        activeDeleteSql = "DELETE FROM InventoryItems WHERE inventoryId = ?";

        tableModel = new DefaultTableModel(col, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 1;
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                Object old = getValueAt(row, column);
                if (Objects.equals(old, aValue)) return;
                super.setValueAt(aValue, row, column);
                activeDirtyRows.add(row);
            }
        };

        return stripedTables(tableModel);
    }

    /**
     * Creates the inventory stats table.
     * 
     * Shows ingredient's usage between 2 dates
     * 
     * @param popUp if true, asks user for date range.
     * @return JTable with statistics or nothing if given wrong information
     */
    /// HELPER FUNCTION TO MAKE INVENTORY STATS TABLE THIS WILL BECOME THE STATS PAGE
    public JTable makeInventoryStatsTable(boolean popUp) {
        LocalDate start_date = LocalDate.parse("2024-01-05");
        LocalDate end_date = LocalDate.now();
        LocalDate firstDay = LocalDate.parse("2024-01-05");
        LocalDate currentDay = LocalDate.now();

        //Conditional for when change dates button is pressed so that a popup appears. 
        if(popUp){
            JTextField startField = new JTextField(10);
            JTextField endField = new JTextField(10);
            
            //this is the popUp to ask for date start and end
            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
            inputPanel.add(startField);
            inputPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
            inputPanel.add(endField);
            int userResult =JOptionPane.showConfirmDialog(this, inputPanel, "Select date Range",JOptionPane.OK_CANCEL_OPTION);

            //Checks that the correct input has been given to the system
            if(userResult != JOptionPane.OK_OPTION){
                return null;
            }
            try{
                start_date = LocalDate.parse(startField.getText());
                end_date = LocalDate.parse(endField.getText());
                if(start_date.isAfter(end_date)){
                    JOptionPane.showMessageDialog(this, "Start date cannot be after end date");
                    return null;
                }
                if(start_date.isBefore(firstDay)||start_date.isAfter(currentDay)){
                    JOptionPane.showMessageDialog(this, "Start and end date can not begin before "+firstDay+" and after "+currentDay);
                    return null;
                }
            }
            catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Invalid Input. Enter date in YYYY-MM-DD Format.");
   
            }
        }

        columnNames = new Vector<>(Arrays.asList("timePeriod", "inventoryName", "totalUsed"));
        col = new Vector<>();
        activeUpdateSql = null;
        activeDirtyRows = null;
        activeIdColumnIndex = -1;
        activeNonDbColumnIndexes = Collections.emptySet();
        activeIntColumnIndexes = Collections.emptySet();
        activePriceColumnIndex = null;

        try (Statement query = conn.createStatement();
            ResultSet result = query.executeQuery(
                    //Selecting the start and end dates the inventory name and quantity of ingredient used
                    "SELECT '"+start_date+"', '" + end_date + "',i.inventoryName, SUM(op.productquantity* pi.numingredients) as TotalUsed "+
                    //Combines orders with there products and ingreients from their inventory.
                    //works like order->orderProduct->productingredients->inventoryitems
                    "From Orders o " + 
                    "JOIN OrderProducts op ON o.orderid = op.orderId " +
                    "JOIN productingredients pi ON op.productId = pi.productid "+ 
                    "JOIN inventoryitems i ON pi.inventoryid = i.inventoryid " +  
                    //only between start date and end date are chosen
                    "WHERE OrderTime " + 
                    "BETWEEN '"+start_date+"' AND '" + end_date + "' Group By inventoryName;")) {
            
            //for each row in the vectorfill in the information into the row and add in the column
            while (result.next()) {
                Vector<String> row = new Vector<>(3);
                row.add(start_date+" to "+end_date);
                row.add(result.getString("inventoryName"));
                row.add(Integer.toString(result.getInt("totalUsed")));
                col.add(row);
            }

        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error accessing Database:\n" + e.getMessage());
        }

        DefaultTableModel model = new DefaultTableModel(col, columnNames){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        JTable table = stripedTables(model);
        return table;
    }

    /**
     * Opens the Employee GUI.
     */
    /// HELPER FUNCTION TO CHANGE THE EMPLOYEE VIEW 
    private void openGUIEmployee() {
        dispose();
        GUI_Employee main = new GUI_Employee(conn, signedInEmployeeId, signedInRole);
        main.setVisible(true);
    }
    /**
     * This function highlights the selected menu button.
     * 
     */
    /// HELPER FUNCTION THAT ALLOWS FOR BUTTON TO CHANGE COLOR
    private void activeButtonColorChange(JButton active) {
        employeesMenuButton.setBackground(null);
        inventoryStatsButton.setBackground(null);
        productMenuButton.setBackground(null);
        inventoryStockButton.setBackground(null);
        active.setBackground(new Color(100, 150, 255));
    }

    /**
     * Makes the tables stripped for ease of viewing.
     *
     * @param model table model
     * @return formatted JTable 
     */ 
    /// HELPER FUNCTION TO COLOR TABLES FOR EASE OF VIEWING 
    private JTable stripedTables(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(255, 255, 255));
                    } else {
                        c.setBackground(new Color(240, 240, 240));
                    }
                }
                return c;
            }
        };

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(120, 0, 0));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);

        /// TABLE DATA VALIDATION 
        if (activeIntColumnIndexes != null && !activeIntColumnIndexes.isEmpty()) {
            TableCellEditor intEditor = new IntegerOnlyEditor(true);
            for (Integer colIndex : activeIntColumnIndexes) {
                if (colIndex != null && colIndex >= 0 && colIndex < table.getColumnCount()) {
                    table.getColumnModel().getColumn(colIndex).setCellEditor(intEditor);
                }
            }
        }
        if (activePriceColumnIndex != null
                && activePriceColumnIndex >= 0
                && activePriceColumnIndex < table.getColumnCount()) {
            table.getColumnModel()
                .getColumn(activePriceColumnIndex)
                .setCellEditor(new NonNegativeDoubleEditor());
        }

        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        return table;
    }

    /**
     * Cell editor that only allows integer input.
     */
    /// HELPER FUNCTION TO VALIDATE ENTRIES INTO INTEGER FIELDS 
    public static class IntegerOnlyEditor extends DefaultCellEditor {
        private final boolean nonNegative;

        IntegerOnlyEditor(boolean nonNegative) {
            super(new JTextField());
            this.nonNegative = nonNegative;
            ((JTextField) getComponent()).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public boolean stopCellEditing() {
            String currEntry = ((JTextField) getComponent()).getText();
            String readyToCheck = (currEntry == null) ? "" : currEntry.trim();

            if (readyToCheck.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Cannot leave table entry empty.");
                return false;
            }

            if (!readyToCheck.matches("^-?\\d+$")) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, "Please enter a whole number (no decimals).");
                return false;
            }

            try {
                int thisValue = Integer.parseInt(readyToCheck);
                if (nonNegative && thisValue < 0) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null, "Value cannot be negative.");
                    return false;
                }
            } catch (NumberFormatException ex) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, "Number is out of range.");
                return false;
            }

            return super.stopCellEditing();
        }
    }
    /**
     * Cell editor that only allows non-negative double Input
     */
    /// HELPER FUNCTION TO VALIDATE ENTRIES INTO DOUBLE FIELDS 
    public static class NonNegativeDoubleEditor extends DefaultCellEditor {
        NonNegativeDoubleEditor() {
            super(new JTextField());
            ((JTextField) getComponent()).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public boolean stopCellEditing() {
            String currEntry = ((JTextField) getComponent()).getText();
            String readyToCheck = (currEntry == null) ? "" : currEntry.trim();

            if (readyToCheck.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Cannot leave table entry empty.");
                return false;
            }

            try {
                double thisValue = Double.parseDouble(readyToCheck);
                if (thisValue < 0) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null, "Price cannot be negative.");
                    return false;
                }
                ((JTextField) getComponent()).setText(Double.toString(thisValue));
            } catch (NumberFormatException ex) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null, "Please enter a valid number.");
                return false;
            }

            return super.stopCellEditing();
        }
    }

    /**
     * Saves all changes made in the active table to the database. 
     */
    /// HELPER FUNCTION TO SAVE ALL CHANGES MADE TO THE DB
    private void saveDbChanges() {
        if (activeUpdateSql == null || activeTable == null || tableModel == null) {
            JOptionPane.showMessageDialog(null, "No tables active.");
            return;
        }

        if (activeTable.isEditing()) {
            activeTable.getCellEditor().stopCellEditing();
        }

        if ((activeDirtyRows.isEmpty() || activeDirtyRows == null) && (activeNewRows == null || activeNewRows.isEmpty()) && (activeDeletedRows == null || activeDeletedRows.isEmpty())) {
            JOptionPane.showMessageDialog(null, "No changes to save.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(activeUpdateSql)) {
            conn.setAutoCommit(false);

            for (int row : activeDirtyRows) {
                Object idValue = tableModel.getValueAt(row, activeIdColumnIndex);
                int param = 1;

                for (int colIndex = 0; colIndex < tableModel.getColumnCount(); colIndex++) {
                    if (colIndex == activeIdColumnIndex) continue;
                    if (activeNonDbColumnIndexes.contains(colIndex)) continue;

                    Object value = tableModel.getValueAt(row, colIndex);

                    if (activePriceColumnIndex != null && colIndex == activePriceColumnIndex) {
                        String s = Objects.toString(value, "").trim().replace("$", "");
                        if (s.contains(",") && !s.contains(".")) s = s.replace(",", ".");
                        else s = s.replace(",", "");
                        if (s.isEmpty()) s = "0";
                        ps.setDouble(param++, Double.parseDouble(s));
                    } else if (activeIntColumnIndexes.contains(colIndex)) {
                        String s = Objects.toString(value, "").trim();
                        if (s.isEmpty()) s = "0";
                        ps.setInt(param++, Integer.parseInt(s));
                    } else {
                        ps.setObject(param++, value);
                    }
                }

                // keep type-correct ID binding
                ps.setInt(param, Integer.parseInt(idValue.toString()));
                ps.addBatch();

            }

            ps.executeBatch();
            conn.commit();
            activeDirtyRows.clear();

            addButtonClicked = false;
            deleteButtonClicked = false;

            JOptionPane.showMessageDialog(null, "Saved changes.");
        } catch (Exception ex) {
            try { conn.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(null, "Save failed:\n" + ex.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }

        ///
        /**
         * This block functions as an row-insertion helper as different behavior was needed compared to `activeUpdateSql`. 
         */
                if(activeInsertSql != null && activeNewRows != null && !activeNewRows.isEmpty()) {
                    try {
                    conn.setAutoCommit(false);

                    boolean isInventoryStock = activeInsertSql.toLowerCase().contains("inventorystock");

                    for(int row : activeNewRows) {
                        int id = Integer.parseInt(tableModel.getValueAt(row, activeIdColumnIndex).toString());

                        //Determines if the current insert is in itemStock
                        if(isInventoryStock) {
                            //Initializes the query paramaters and placeholders (?), then executes the insert.
                            try(PreparedStatement psItems = conn.prepareStatement( "INSERT INTO InventoryItems (inventoryId, inventoryName) VALUES (?, ?)")){
                                psItems.setInt(1, id);
                                psItems.setString(2,tableModel.getValueAt(row, 1).toString());
                                psItems.executeUpdate();
                            }
                        
                            //Perfroms the same function as the previous codeblock but instead for InventoryStock. 
                            try(PreparedStatement psStock = conn.prepareStatement( "INSERT INTO InventoryStock (inventoryId, quantityHeld, quantityInTransit) VALUES (?, ?, ?)")) {
                                psStock.setInt(1, id);
                                psStock.setInt(2, parseIntSafe(tableModel.getValueAt(row, 2).toString()));
                                psStock.setInt(3, parseIntSafe(tableModel.getValueAt(row, 3).toString()));
                                psStock.executeUpdate();
                            }

                        }

                        //This code section performs the row insertation for employeeMenu and ProductMenu.
                        else {

                            //Creates the prepared stament using the query string in 'makeEmployeeTable' so it closes after use.
                            try (PreparedStatement ps = conn.prepareStatement(activeInsertSql)) {

                                //Tracks the  current paramater index. 
                                int param = 1;

                                //Loops through each column in the table model to set the corresponding prepared statements parameters.
                                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                                    Object value = tableModel.getValueAt(row, col);
                                
                                    //Checks to see if this column is supposed to take integers and checks if the integers are safe to use.
                                    if (activeIntColumnIndexes.contains(col) || col == activeIdColumnIndex) {
                                        ps.setInt(param++, parseIntSafe(value.toString()));
                                    } 
                                    //this code block checks if the column is supposed to take in a price.
                                    else if (activePriceColumnIndex != null && col == activePriceColumnIndex) {
                                        String s = value.toString().replace("$", "").replace(",", "");
                                        ps.setDouble(param++, Double.parseDouble(s));
                                    } 
                                    else {
                                        //Puts in value into the next ? and moves to the next parameter position.
                                        ps.setObject(param++, value);
                                    }
                                }
                                ps.executeUpdate();
                            }
                        }
                    }
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Changes saved successfully.");
                    boolean isProductsInsert = activeInsertSql != null &&
                            activeInsertSql.toLowerCase().contains("insert into products");
                    if (isProductsInsert) {
                        for (int row : activeNewRows) {
                            Object idValue = tableModel.getValueAt(row, activeIdColumnIndex);
                            int productId = Integer.parseInt(idValue.toString());
                            openProductInventory(productId);
                        }
                    }
                    activeNewRows.clear();
                    JOptionPane.showMessageDialog(null, "Inserted new rows successfully!!!");
                    addButtonClicked = false;
                    deleteButtonClicked = false;
                    } 
                    catch (Exception ex) {
                        try {
                            conn.rollback();
                        } 
                        catch (SQLException rollbackEx) {
                            rollbackEx.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(this, "Insert failed:\n" + ex.getMessage());

                    } 
                    finally {
                        try {
                            conn.setAutoCommit(true);
                        } 
                        catch(SQLException e) {
                        e.printStackTrace();
                        }
                    }
                }

        //delete row thing kinda like the rest
        //checks if we have the sql script and that any row is marked for execution
        //This code block checks that if there is a delete sql statement and if a row is selected.
        if(activeDeleteSql != null && activeDeletedRows != null && !activeDeletedRows.isEmpty()){

            //Determines if the deletion invloves the 'inventoryStock' table.
            boolean isInventoryStock = activeDeleteSql.toLowerCase().contains("inventorystock");

            //Initializes the query paramaters and placeholders (?).
            try(PreparedStatement psDelete = conn.prepareStatement(activeDeleteSql)){
                
                //Turns off auto-commit to allow batch execution.
                conn.setAutoCommit(false);
                
                //Converts the set of deleted row Ids to an array for iteration.
                Integer[] deleteArr = activeDeletedRows.toArray(new Integer[0]);

                //Loops through all Ids marked for deletion and adds them into a batch deletion statement.
                for(int i = 0; i < deleteArr.length; i++){
                    int idValue =  deleteArr[i];
                    psDelete.setInt(1, idValue);
                    psDelete.addBatch();
                }   
                
                //Executes the batch delete statments in a single call.
                psDelete.executeBatch();

                //Confirm and save all deletions to the database.
                conn.commit();
                
                //Clear the set of rows marked for deletion. 
                activeDeletedRows.clear();


                JOptionPane.showMessageDialog(null,"Deleted row succesfully!!!");

            } catch (Exception ex) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                JOptionPane.showMessageDialog(null, "Save failed:\n" + ex.getMessage());
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }

        }
    }
    /**
     * This Function adds a new Row to the active table.
     */
    //HELPER FUNCTION TO ADD A ROW IN GUI_MANAGER NOT THE DATABASE
    private void addRowtoActiveTable() {
        
        //checks that a table is currently active.
        if (activeTable == null || tableModel == null) {
            JOptionPane.showMessageDialog(null, "No table to edit selected.");
            return;
        }

        //
        //This code chunk automatically
        int nextId = 1;
        if(activeIdColumnIndex >= 0){
            int maxId = 0;
            for(int i = 0; i < tableModel.getRowCount(); i++){
                Object value = tableModel.getValueAt(i, activeIdColumnIndex);
                if(value!=null){
                    try{int id = Integer.parseInt(value.toString());
                        if(id > maxId){
                            maxId = id;
                        }
                   }catch(NumberFormatException ignored){}
                }
            }
            nextId = maxId + 100;
        }

        //created input dialog so user can fill in what they want in the table 
        JPanel inputPanel = new JPanel(new GridLayout(0,2,5,5));
        JTextField[] fields = new JTextField[tableModel.getColumnCount()];

        for(int i = 0; i < tableModel.getColumnCount(); i++){
            if(i == activeIdColumnIndex){
                continue;
            }
            inputPanel.add(new JLabel(tableModel.getColumnName(i) + ":"));
            fields[i] = new JTextField(15);
            inputPanel.add(fields[i]);
        }
        //show the dailog
        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Enter new Row Data",JOptionPane.OK_CANCEL_OPTION);

        //If user clicks ok creates the new row and table
        if(result == JOptionPane.OK_OPTION){

            //CHECK IF ANY ARE LEFT EMPTY
            boolean validityCheck = true;
            for(int i = 0; i < tableModel.getColumnCount(); i++) {
                if(i != activeIdColumnIndex) {
                    if(fields[i].getText().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Cannot leave any fields in new entry empty.");
                        validityCheck = false;
                        break;
                    }
                }
            }
            if(validityCheck) {
                //loops through every column
                for(int i =0; i<tableModel.getColumnCount();i++) {
                    if(i==activeIdColumnIndex){
                        continue;
                    }

                    //gets input for column and the column it was supposed to be inputted in
                    String inputValues = fields[i].getText().trim();
                    String columnNames = tableModel.getColumnName(i);

                    //this is the check for the integer columns
                    if(activeIntColumnIndexes.contains(i)) {
                        try {
                            Integer.parseInt(inputValues);
                            if(!(Integer.parseInt(inputValues)>-1)) {
                                JOptionPane.showMessageDialog(null,"Error: Negative number inputted");
                                return;
                            }
                        }
                        catch(NumberFormatException e) {
                            JOptionPane.showMessageDialog(null, "Error: Non-number inputted into numeric");
                            return;
                        }
                    }

                    //checks for the price comuns
                    if(activePriceColumnIndex != null && activePriceColumnIndex == i) {
                        try {
                            Double.parseDouble(inputValues);
                            if(!(Double.parseDouble(inputValues)>-1)) {
                                JOptionPane.showMessageDialog(null, "Error: Negative number inputted");
                                return;
                            }
                        }
                        catch(NumberFormatException e){
                            JOptionPane.showMessageDialog(null, "Error: Non-number inputted into numeric");
                            return;
                        }
                    }

                    //checks for the employeePosition
                    if(columnNames.equals("employeePosition")) {
                        if(!inputValues.equals("Employee") && !inputValues.equals("Manager")) {
                            JOptionPane.showMessageDialog(null, "Employee Position must be 'Employee' or 'Manager'");
                            return;
                        }
                    }

                }

                if(deleteButtonClicked) {
                    JOptionPane.showMessageDialog(null, "Cannot add row after deletion until save is pressed.");
                    return;
                }
                addButtonClicked = true;

                Vector<Object > newRow = new Vector<>();
                for(int i = 0; i < tableModel.getColumnCount(); i++){
                    if(i == activeIdColumnIndex){
                        newRow.add(nextId);
                    }else{
                        newRow.add(fields[i].getText());
                    }
                }

                //keeps track
                tableModel.addRow(newRow);
                if(activeDirtyRows == null){
                    activeDirtyRows = new HashSet<>();
                }
                if(activeNewRows == null){
                    activeNewRows = new HashSet<>();
                }
                activeNewRows.add(tableModel.getRowCount()-1);
                JOptionPane.showMessageDialog(null,"Row added in. Click Save.");
            }
        }
    }

    //HELPER FUNCTION THAT CHECKS IF A STRING IS AN INTEGER
    private int parseIntSafe(String s) {
    try { return Integer.parseInt(s); }
    catch (NumberFormatException e) { return 0; }
    }
    
    /**
     * This function deletes a selected row in the active table.
     */
    ///Helper function that deletes rows
    private void deleteSelectedRow(){
        if(activeTable ==null||tableModel==null||activeDeleteSql==null){
            JOptionPane.showMessageDialog(null,"No editable table selected.");
            return;
        }
        int selectedRow = activeTable.getSelectedRow();
        //alert no row selected
        if(selectedRow ==-1){
            JOptionPane.showMessageDialog(null,"Select a row to delete.");
            return;
        }
        //if the row is a row that hasn't been saved yet to the db just removes it from the add row tracker that is right above
        if(activeNewRows!=null&& activeNewRows.contains(selectedRow)){
            activeNewRows.remove(selectedRow);
            tableModel.removeRow(selectedRow);
            return;
        }
        Object idValue =tableModel.getValueAt(selectedRow,activeIdColumnIndex);
        if(activeDeletedRows==null){
            activeDeletedRows= new HashSet<>();
        }
                if(addButtonClicked) {
            JOptionPane.showMessageDialog(null, "Cannot add row after deletion until 'Save' is pressed.");
            return;
        }
        deleteButtonClicked = true;
        //gets id of row being delete. used to avoid errors due to shifting rows
        activeDeletedRows.add(Integer.parseInt(idValue.toString()));
        tableModel.removeRow(selectedRow);
        JOptionPane.showMessageDialog(null,"Row marked for deletion. Click 'Save'.");
    }

    /**
     * This functions exports the active table to a csv file.
     */
    ///HELPER FUNCTION THAT EXPORTS STATS TABLE TO CSV
    private void exportActiveTableToCSV(){
        JFileChooser fileChooser = new JFileChooser();
        if(activeTable == null){
            JOptionPane.showMessageDialog(this, "No table to Export selected");
            return;
        }
        fileChooser.setDialogTitle("Save CSV File"); 
        int userSelection =fileChooser.showSaveDialog(this);
        if(userSelection != JFileChooser.APPROVE_OPTION){
            return;
        }
        File fileToSave = fileChooser.getSelectedFile();
        if(!fileToSave.getName().toLowerCase().endsWith(".csv")){
            fileToSave = new File(fileToSave.getAbsolutePath()+".csv");
        }
        try(PrintWriter pw = new PrintWriter(fileToSave)){
            DefaultTableModel model = (DefaultTableModel) activeTable.getModel();
            for(int col = 0; col < model.getColumnCount(); col++){
                pw.print(model.getColumnName(col));
                if(col < model.getColumnCount()-1){
                    pw.print(",");
                }
            }
            pw.println();
            for(int row = 0; row < model.getRowCount(); row++){
                for(int col = 0; col < model.getColumnCount(); col++){
                    Object value = model.getValueAt(row, col);
                    pw.print(value != null ? value.toString(): "");
                    if(col < model.getColumnCount() - 1){
                        pw.print(",");
                    }
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "CSV exported Successfully");
        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Export failed:\n"+ ex.getMessage());
        }
    }

    /**
     * Opens up productInventory GUI for the selected Product. 
     */
    /// Helper function that edits rows
    private void editSelectedProduct(){
        if(activeTable ==null||tableModel==null||activeDeleteSql==null){
            JOptionPane.showMessageDialog(null,"No table to edit selected.");
            return;
        }
        int selectedRow = activeTable.getSelectedRow();

        //alert no row selected
        if(selectedRow ==-1){
            JOptionPane.showMessageDialog(null,"Select a product to edit.");
            return;
        }
        else {
            String productId = activeTable.getValueAt(selectedRow, 0).toString();
            openProductInventory(Integer.parseInt(productId));
        }
    }

    /** 
     * Opens the product inventory page.
     * 
     * @param productId selected products id
    */
    /// HELPER FUNCTION TO OPEN PRODUCT INVENTORY WITH THE CURRENTLY SELECTED PRODUCT ID
    private void openProductInventory(int productId) {
        dispose();            
        GUI_productInventory main = new GUI_productInventory(conn, signedInEmployeeId, signedInRole, productId);  
        main.setVisible(true);
    }

    /**
     * This funtion shows today's x-report.
     */
    //HELPER FUNCTION SHOWS RUSH REPORT
    private void ShowRushReport(){
        columnNames= new Vector<>(Arrays.asList("HourInterval","TotalOrders","NumCardPayments","NumCashPayments","TotalProfit"));
        col = new Vector<>();
        //prepares statement to pull the table and puts the values into the table
        try(PreparedStatement ps = conn.prepareStatement(
            "SELECT DATE_TRUNC('hour', ordertime) AS hourly_int, COUNT(orderId) AS total_orders, " +
            "SUM(CASE WHEN cardPayment = true THEN 1 ELSE 0 END) AS card_Payments, SUM(CASE WHEN cardPayment = false THEN 1 ELSE 0 END) AS cash_payments, " +
            "SUM(orderTotal) AS hourly_sum " +
            "FROM ORDERS WHERE OrderTime BETWEEN Current_date + Time '11:00:00' AND current_timestamp GROUP BY hourly_int ORDER BY hourly_int")){
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Vector<String> row = new Vector<>();
                row.add(rs.getTimestamp("hourly_int").toString());
                row.add(Integer.toString(rs.getInt("total_orders")));
                row.add(Integer.toString(rs.getInt("card_payments")));
                row.add(Integer.toString(rs.getInt("cash_payments")));
                row.add(Double.toString(rs.getDouble("hourly_sum")));
                col.add(row);
            }

        }
        catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage());
            return;
        }
         DefaultTableModel model = new DefaultTableModel(col, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = stripedTables(model);
        //Has the table appear inside stats page 
        showTable(table, (JPanel) getContentPane(), tableScroll);
    }

    /**
     * Closes application. 
     */
    /// CLOSE
    @Override
    public void actionPerformed(ActionEvent e) {
        if ("Close".equals(e.getActionCommand())) {
            dispose();
        }
    }
}
