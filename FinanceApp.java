import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FinanceApp extends JFrame {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bank";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "tiger";

    private JTextField tfAmount, tfDate;
    private JComboBox<String> cbType, cbCategory;
    private JButton btnAdd;
    private JLabel lblIncome, lblExpense, lblBalance; // Dashboard labels

    public FinanceApp() {
        // 🎯 FRAME SETTINGS
        setTitle("💰 Personal Finance Tracker");
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 🎨 HEADER PANEL
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(46, 82, 240));
        JLabel titleLabel = new JLabel("Personal Finance Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // 📊 DASHBOARD PANEL (SUMMARY CARDS)
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dashboardPanel.setBackground(new Color(240, 243, 255));

        lblIncome = createSummaryCard("Total Income", new Color(0, 153, 76));   // Green
        lblExpense = createSummaryCard("Total Expense", new Color(204, 0, 0));  // Red
        lblBalance = createSummaryCard("Balance", new Color(255, 153, 0));      // Orange

        dashboardPanel.add(lblIncome);
        dashboardPanel.add(lblExpense);
        dashboardPanel.add(lblBalance);

        add(dashboardPanel, BorderLayout.BEFORE_FIRST_LINE);

        // 🎨 MAIN INPUT PANEL
        // 🎨 MAIN INPUT PANEL (Rearranged Layout)
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        mainPanel.setBackground(new Color(245, 247, 255));

// 🔹 Row 1 – Type
        JLabel lblType = new JLabel("Type:");
        cbType = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        mainPanel.add(lblType);
        mainPanel.add(cbType);

// 🔹 Row 2 – Category
        JLabel lblCategory = new JLabel("Category:");
        cbCategory = new JComboBox<>();
        mainPanel.add(lblCategory);
        mainPanel.add(cbCategory);

// 🔹 Row 3 – Amount
        JLabel lblAmount = new JLabel("Amount:");
        tfAmount = new JTextField();
        mainPanel.add(lblAmount);
        mainPanel.add(tfAmount);

// 🔹 Row 4 – Date
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        tfDate = new JTextField();
        mainPanel.add(lblDate);
        mainPanel.add(tfDate);

// 🔹 Row 5 – Buttons
        btnAdd = createStyledButton("➕ Add Transaction");
        btnAdd.addActionListener(e -> {
            addTransaction();
            updateDashboard(); // Refresh summary
        });
        
        JButton btnShow = createStyledButton("📋 Show Transactions");
        btnShow.addActionListener(e -> showTransactions());

        mainPanel.add(lblAmount);
        mainPanel.add(tfAmount);
        mainPanel.add(lblType);
        mainPanel.add(cbType);
        mainPanel.add(lblCategory);
        mainPanel.add(cbCategory);
        mainPanel.add(lblDate);
        mainPanel.add(tfDate);
        mainPanel.add(btnAdd);
        mainPanel.add(btnShow);

        add(mainPanel, BorderLayout.CENTER);
        loadCategories();
        updateDashboard(); // Load summary on startup

        setVisible(true);
    }

    // 🎨 Create Dashboard Summary Card
    private JLabel createSummaryCard(String title, Color color) {
        JLabel label = new JLabel(title + ": ₹0.00", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        return label;
    }

    // 🔄 Update Dashboard (fetch totals)
    private void updateDashboard() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {

            double totalIncome = 0, totalExpense = 0;

            // Get total income
            ResultSet rs = st.executeQuery("SELECT SUM(amount) AS total_income FROM transactions WHERE type='INCOME'");
            if (rs.next()) totalIncome = rs.getDouble("total_income");

            // Get total expense
            rs = st.executeQuery("SELECT SUM(amount) AS total_expense FROM transactions WHERE type='EXPENSE'");
            if (rs.next()) totalExpense = rs.getDouble("total_expense");

            double balance = totalIncome - totalExpense;

            lblIncome.setText("Total Income: ₹" + String.format("%.2f", totalIncome));
            lblExpense.setText("Total Expense: ₹" + String.format("%.2f", totalExpense));
            lblBalance.setText("Balance: ₹" + String.format("%.2f", balance));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating dashboard: " + e.getMessage());
        }
    }

    // 🎨 Styled Button
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(46, 82, 240));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(30, 70, 230));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(46, 82, 240));
            }
        });
        return button;
    }

    // 🧩 Load Categories
    private void loadCategories() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {

            ResultSet rs = st.executeQuery("SELECT name FROM categories");
            cbCategory.removeAllItems();
            while (rs.next()) {
                cbCategory.addItem(rs.getString("name"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load categories from database.");
        }
    }

    // 🚀 Add Transaction
    private void addTransaction() {
        String amountText = tfAmount.getText().trim();
        String type = (String) cbType.getSelectedItem();
        String category = (String) cbCategory.getSelectedItem();
        String date = tfDate.getText().trim();

        if (amountText.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format.");
            return;
        }

        String sql = "INSERT INTO transactions (amount, category, type, date) VALUES ('"
                + amount + "', '"
                + category + "', '"
                + type + "', '"
                + date + "')";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {

            int rows = st.executeUpdate(sql);
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Transaction added successfully!");
                tfAmount.setText("");
                tfDate.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add transaction.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + e.getMessage());
        }
    }

    // 📋 Show Transactions
    private void showTransactions() {
        JFrame frame = new JFrame("All Transactions");
        frame.setSize(900, 400);
        frame.setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Amount", "Category", "Type", "Date"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM transactions ORDER BY date DESC")) {

            while (rs.next()) {
                int id = rs.getInt("transaction_id");
                double amount = rs.getDouble("amount");
                String category = rs.getString("category");
                String type = rs.getString("type");
                Date date = rs.getDate("date");

                model.addRow(new Object[]{id, amount, category, type, date});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        JButton btnEdit = createStyledButton("✏️ Edit Selected");
        JButton btnDelete = createStyledButton("🗑 Delete Selected");

        JPanel panelBottom = new JPanel();
        panelBottom.add(btnEdit);
        panelBottom.add(btnDelete);
        frame.add(panelBottom, BorderLayout.SOUTH);

        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a transaction to edit.");
                return;
            }
            int id = (int) model.getValueAt(selectedRow, 0);
            double amount = (double) model.getValueAt(selectedRow, 1);
            String category = (String) model.getValueAt(selectedRow, 2);
            String type = (String) model.getValueAt(selectedRow, 3);
            String date = model.getValueAt(selectedRow, 4).toString();

            editTransaction(id, amount, category, type, date, model, selectedRow);
            updateDashboard(); // Refresh dashboard
        });

        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a transaction to delete.");
                return;
            }
            int id = (int) model.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteTransaction(id);
                model.removeRow(selectedRow);
                updateDashboard(); // Refresh dashboard
            }
        });

        frame.setVisible(true);
    }

    private void editTransaction(int id, double amount, String category, String type, String date,
                                 DefaultTableModel model, int rowIndex) {
        JFrame editFrame = new JFrame("Edit Transaction");
        editFrame.setSize(400, 300);
        editFrame.setLayout(new GridLayout(6, 2, 5, 5));

        JTextField tfAmount = new JTextField(String.valueOf(amount));
        JComboBox<String> cbCategory = new JComboBox<>();
        loadCategories(cbCategory);
        cbCategory.setSelectedItem(category);

        JComboBox<String> cbType = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        cbType.setSelectedItem(type);

        JTextField tfDate = new JTextField(date);
        JButton btnUpdate = createStyledButton("💾 Update");

        editFrame.add(new JLabel("Amount:"));
        editFrame.add(tfAmount);
        editFrame.add(new JLabel("Category:"));
        editFrame.add(cbCategory);
        editFrame.add(new JLabel("Type:"));
        editFrame.add(cbType);
        editFrame.add(new JLabel("Date (YYYY-MM-DD):"));
        editFrame.add(tfDate);
        editFrame.add(new JLabel());
        editFrame.add(btnUpdate);

        btnUpdate.addActionListener(e -> {
            try {
                double newAmount = Double.parseDouble(tfAmount.getText().trim());
                String newCategory = (String) cbCategory.getSelectedItem();
                String newType = (String) cbType.getSelectedItem();
                String newDate = tfDate.getText().trim();

                String sql = "UPDATE transactions SET amount='" + newAmount +
                        "', category='" + newCategory +
                        "', type='" + newType +
                        "', date='" + newDate +
                        "' WHERE transaction_id=" + id;

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                     Statement st = conn.createStatement()) {

                    int rows = st.executeUpdate(sql);
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(editFrame, "Transaction updated successfully!");
                        model.setValueAt(newAmount, rowIndex, 1);
                        model.setValueAt(newCategory, rowIndex, 2);
                        model.setValueAt(newType, rowIndex, 3);
                        model.setValueAt(newDate, rowIndex, 4);
                        editFrame.dispose();
                        updateDashboard();
                    }
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(editFrame, "Error: " + ex.getMessage());
            }
        });

        editFrame.setVisible(true);
    }

    private void loadCategories(JComboBox<String> cbCategory) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM categories ORDER BY name")) {

            while (rs.next()) {
                cbCategory.addItem(rs.getString("name"));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage());
        }
    }

    private void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE transaction_id = " + id;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement st = conn.createStatement()) {

            st.executeUpdate(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting transaction: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FinanceApp::new);
    }
}
