package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminFrame extends JFrame {
    private UserDAO userDAO;
    private JTabbedPane tabbedPane;
    private JTable adminTable, managerTable, kasirTable, customerTable, allRoundTable;
    private DefaultTableModel adminTableModel, managerTableModel, kasirTableModel, customerTableModel, allRoundTableModel;
    private JTextField usernameField, namaField, searchField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JLabel totalAdminLabel, totalManagerLabel, totalKasirLabel, totalCustomerLabel, totalAllLabel;
    private int selectedUserId = -1;
    private String currentRole = "Admin";

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(64, 81, 181);
    private static final Color SECONDARY_COLOR = new Color(92, 107, 192);
    private static final Color ACCENT_COLOR = new Color(255, 64, 129);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);

    public AdminFrame() {
        userDAO = new UserDAO();
        initializeComponents();
        setupModernUI();
        setupLayout();
        setupEventListeners();
        loadAllUserData();
        updateStatistics();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Admin Dashboard - User Management System");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void initializeComponents() {
        // Initialize tables with modern styling
        String[] columnNames = {"ID", "Username", "Nama", "Role", "Actions"};
        
        adminTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        adminTable = createModernTable(adminTableModel);
        
        managerTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        managerTable = createModernTable(managerTableModel);
        
        kasirTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        kasirTable = createModernTable(kasirTableModel);

        customerTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = createModernTable(customerTableModel);
        
        allRoundTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        allRoundTable = createModernTable(allRoundTableModel);

        // Form fields with modern styling
        usernameField = createModernTextField();
        passwordField = new JPasswordField();
        stylePasswordField(passwordField);
        namaField = createModernTextField();
        roleComboBox = new JComboBox<>(new String[]{"Admin", "Manager", "Kasir", "Customer"});
        styleComboBox(roleComboBox);
        searchField = createModernTextField();

        // Modern buttons
        addButton = createModernButton("Tambah User", SUCCESS_COLOR);
        updateButton = createModernButton("Update User", PRIMARY_COLOR);
        deleteButton = createModernButton("Hapus User", ERROR_COLOR);
        clearButton = createModernButton("Clear Form", TEXT_SECONDARY);

        // Statistics labels
        totalAdminLabel = createStatLabel("0");
        totalManagerLabel = createStatLabel("0");
        totalKasirLabel = createStatLabel("0");
        totalCustomerLabel = createStatLabel("0");
        totalAllLabel = createStatLabel("0");

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        styleModernTabbedPane(tabbedPane);
    }

    private JTable createModernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 50));
        
        // Modern table header
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(BACKGROUND_COLOR);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR));
        table.getTableHeader().setReorderingAllowed(false);
        
        // Modern cell renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                } else {
                    c.setBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 30));
                }
                
                setBorder(new EmptyBorder(10, 15, 10, 15));
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        return table;
    }

    private JTextField createModernTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Smaller font
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10) // Smaller padding
        ));
        field.setPreferredSize(new Dimension(170, 28)); // Smaller size
        return field;
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setPreferredSize(new Dimension(170, 28));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        combo.setPreferredSize(new Dimension(170, 28));
        combo.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Smaller font
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10)); // Smaller padding
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(85, 28)); // Smaller size
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Smaller font
        label.setForeground(PRIMARY_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void styleModernTabbedPane(JTabbedPane pane) {
        pane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pane.setBackground(CARD_COLOR);
        pane.setForeground(TEXT_PRIMARY);
        pane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    private void setupModernUI() {
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        setBackground(BACKGROUND_COLOR);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Modern header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12)); // Smaller main panel padding

        // Left panel - Form and statistics
        JPanel leftPanel = createLeftPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Right panel - Tables
        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(0, 60)); // Smaller height
        panel.setBorder(new EmptyBorder(12, 20, 12, 20)); // Smaller padding

        JLabel titleLabel = new JLabel("Dashboard Admin - Manajemen User");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Smaller
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Kelola pengguna sistem dengan mudah");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Smaller
        subtitleLabel.setForeground(new Color(255, 255, 255, 180));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        panel.add(titlePanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setPreferredSize(new Dimension(240, 0)); // Smaller width

        // Statistics panel
        JPanel statsPanel = createStatisticsPanel();
        leftPanel.add(statsPanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = createFormPanel();
        leftPanel.add(formPanel, BorderLayout.CENTER);

        // Search panel
        JPanel searchPanel = createSearchPanel();
        leftPanel.add(searchPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 1, 4, 4)); // Smaller gaps
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 8, 8)); // Smaller padding

        // All Round stat card (shows total users)
        JPanel allCard = createStatCard("Total Users", totalAllLabel, PRIMARY_COLOR);
        panel.add(allCard);

        // Admin stat card
        JPanel adminCard = createStatCard("Admin", totalAdminLabel, SUCCESS_COLOR);
        panel.add(adminCard);

        // Manager stat card
        JPanel managerCard = createStatCard("Manager", totalManagerLabel, WARNING_COLOR);
        panel.add(managerCard);

        // Kasir stat card
        JPanel kasirCard = createStatCard("Kasir", totalKasirLabel, ACCENT_COLOR);
        panel.add(kasirCard);

        // Customer stat card
        JPanel customerCard = createStatCard("Customer", totalCustomerLabel, SECONDARY_COLOR);
        panel.add(customerCard);

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
            new EmptyBorder(8, 10, 8, 10) // Smaller padding
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Smaller font
        titleLabel.setForeground(TEXT_SECONDARY);

        // Smaller value label
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Smaller

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(valueLabel, BorderLayout.CENTER);

        // Accent bar
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(3, 0));

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(accentBar, BorderLayout.WEST);

        return card;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
            new EmptyBorder(12, 14, 12, 14) // Smaller padding
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 8, 0); // Smaller gap
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel formTitle = new JLabel("Form User");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Smaller
        formTitle.setForeground(TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(formTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 8, 0);

        // Username
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createFieldLabel("Username"), gbc);
        gbc.gridy = 2;
        panel.add(usernameField, gbc);

        // Password
        gbc.gridy = 3;
        panel.add(createFieldLabel("Password"), gbc);
        gbc.gridy = 4;
        panel.add(passwordField, gbc);

        // Nama
        gbc.gridy = 5;
        panel.add(createFieldLabel("Nama Lengkap"), gbc);
        gbc.gridy = 6;
        panel.add(namaField, gbc);

        // Role
        gbc.gridy = 7;
        panel.add(createFieldLabel("Role"), gbc);
        gbc.gridy = 8;
        panel.add(roleComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 4, 4)); // Smaller gaps
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridy = 9;
        gbc.insets = new Insets(12, 0, 0, 0);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Smaller
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
            new EmptyBorder(10, 14, 10, 14) // Smaller padding
        ));

        JLabel searchLabel = new JLabel("Cari User");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Smaller
        searchLabel.setForeground(TEXT_PRIMARY);

        searchField.setPreferredSize(new Dimension(0, 26)); // Smaller height
        
        JButton searchButton = createModernButton("Cari", PRIMARY_COLOR);
        searchButton.setPreferredSize(new Dimension(50, 26)); // Smaller size

        JPanel searchInputPanel = new JPanel(new BorderLayout(4, 0)); // Smaller gap
        searchInputPanel.setOpaque(false);
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);

        panel.add(searchLabel, BorderLayout.NORTH);
        panel.add(Box.createVerticalStrut(4), BorderLayout.CENTER); // Smaller gap
        panel.add(searchInputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Setup tabbed pane
        tabbedPane.addTab("📋 All Round", createTablePanel(allRoundTable));
        tabbedPane.addTab("👨‍💼 Admin", createTablePanel(adminTable));
        tabbedPane.addTab("🏢 Manager", createTablePanel(managerTable));
        tabbedPane.addTab("💰 Kasir", createTablePanel(kasirTable));
        tabbedPane.addTab("👥 Customer", createTablePanel(customerTable));

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablePanel(JTable table) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12)); // Smaller padding

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(224, 224, 224), 1));
        scrollPane.setBackground(CARD_COLOR);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void setupEventListeners() {
        // Table selection listeners
        adminTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection(adminTable, adminTableModel);
                currentRole = "Admin";
            }
        });

        managerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection(managerTable, managerTableModel);
                currentRole = "Manager";
            }
        });

        kasirTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection(kasirTable, kasirTableModel);
                currentRole = "Kasir";
            }
        });

        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection(customerTable, customerTableModel);
                currentRole = "Customer";
            }
        });

        allRoundTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection(allRoundTable, allRoundTableModel);
                currentRole = "All";
            }
        });

        // Button listeners
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        clearButton.addActionListener(e -> clearForm());

        // Search functionality
        searchField.addActionListener(e -> searchUser());
    }

    private void handleTableSelection(JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedUserId = (Integer) model.getValueAt(selectedRow, 0);
            usernameField.setText((String) model.getValueAt(selectedRow, 1));
            namaField.setText((String) model.getValueAt(selectedRow, 2));
            roleComboBox.setSelectedItem((String) model.getValueAt(selectedRow, 3));
            
            // Clear password field for security
            passwordField.setText("");
        }
    }

    private void loadAllUserData() {
        // Clear all tables
        adminTableModel.setRowCount(0);
        managerTableModel.setRowCount(0);
        kasirTableModel.setRowCount(0);
        customerTableModel.setRowCount(0);
        allRoundTableModel.setRowCount(0);

        List<User> users = userDAO.findAll();
        for (User user : users) {
            Object[] rowData = {
                user.getUserId(),
                user.getUsername(),
                user.getNama(),
                user.getRole(),
                "Actions"
            };

            // Add to All Round table
            allRoundTableModel.addRow(rowData);

            // Add to specific role table
            switch (user.getRole().toLowerCase()) {
                case "admin":
                    adminTableModel.addRow(rowData);
                    break;
                case "manager":
                    managerTableModel.addRow(rowData);
                    break;
                case "kasir":
                    kasirTableModel.addRow(rowData);
                    break;
                case "customer":
                    customerTableModel.addRow(rowData);
                    break;
            }
        }
    }

    private void updateStatistics() {
        List<User> users = userDAO.findAll();
        int adminCount = 0, managerCount = 0, kasirCount = 0, customerCount = 0;

        for (User user : users) {
            switch (user.getRole().toLowerCase()) {
                case "admin":
                    adminCount++;
                    break;
                case "manager":
                    managerCount++;
                    break;
                case "kasir":
                    kasirCount++;
                    break;
                case "customer":
                    customerCount++;
                    break;
            }
        }

        totalAllLabel.setText(String.valueOf(users.size()));
        totalAdminLabel.setText(String.valueOf(adminCount));
        totalManagerLabel.setText(String.valueOf(managerCount));
        totalKasirLabel.setText(String.valueOf(kasirCount));
        totalCustomerLabel.setText(String.valueOf(customerCount));
    }

    private void addUser() {
        if (validateInput()) {
            User user = new User(
                usernameField.getText().trim(),
                new String(passwordField.getPassword()),
                namaField.getText().trim(),
                (String) roleComboBox.getSelectedItem()
            );

            if (userDAO.create(user)) {
                showSuccessMessage("User berhasil ditambahkan!");
                clearForm();
                loadAllUserData();
                updateStatistics();
            } else {
                showErrorMessage("Gagal menambahkan user!");
            }
        }
    }

    private void updateUser() {
        if (selectedUserId == -1) {
            showWarningMessage("Pilih user yang akan diupdate!");
            return;
        }

        if (validateInput()) {
            User user = new User(
                usernameField.getText().trim(),
                new String(passwordField.getPassword()),
                namaField.getText().trim(),
                (String) roleComboBox.getSelectedItem()
            );
            user.setUserId(selectedUserId);

            if (userDAO.update(user)) {
                showSuccessMessage("User berhasil diupdate!");
                clearForm();
                loadAllUserData();
                updateStatistics();
            } else {
                showErrorMessage("Gagal mengupdate user!");
            }
        }
    }

    private void deleteUser() {
        if (selectedUserId == -1) {
            showWarningMessage("Pilih user yang akan dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menghapus user ini?",
            "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.delete(selectedUserId)) {
                showSuccessMessage("User berhasil dihapus!");
                clearForm();
                loadAllUserData();
                updateStatistics();
            } else {
                showErrorMessage("Gagal menghapus user!");
            }
        }
    }

    private void searchUser() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadAllUserData();
            return;
        }

        User user = userDAO.findByUsername(searchTerm);
        if (user != null) {
            // Clear all tables first
            adminTableModel.setRowCount(0);
            managerTableModel.setRowCount(0);
            kasirTableModel.setRowCount(0);
            customerTableModel.setRowCount(0);
            allRoundTableModel.setRowCount(0);

            Object[] rowData = {
                user.getUserId(),
                user.getUsername(),
                user.getNama(),
                user.getRole(),
                "Actions"
            };

            // Add to All Round table
            allRoundTableModel.addRow(rowData);

            // Add to appropriate table and switch to that tab
            switch (user.getRole().toLowerCase()) {
                case "admin":
                    adminTableModel.addRow(rowData);
                    tabbedPane.setSelectedIndex(1); // Admin tab
                    break;
                case "manager":
                    managerTableModel.addRow(rowData);
                    tabbedPane.setSelectedIndex(2); // Manager tab
                    break;
                case "kasir":
                    kasirTableModel.addRow(rowData);
                    tabbedPane.setSelectedIndex(3); // Kasir tab
                    break;
                case "customer":
                    customerTableModel.addRow(rowData);
                    tabbedPane.setSelectedIndex(4); // Customer tab
                    break;
            }
        } else {
            showInfoMessage("User tidak ditemukan!");
            loadAllUserData();
        }
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        namaField.setText("");
        roleComboBox.setSelectedIndex(0);
        selectedUserId = -1;
        adminTable.clearSelection();
        managerTable.clearSelection();
        kasirTable.clearSelection();
        customerTable.clearSelection();
        allRoundTable.clearSelection();
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty()) {
            showErrorMessage("Username tidak boleh kosong!");
            usernameField.requestFocus();
            return false;
        }

        if (passwordField.getPassword().length == 0) {
            showErrorMessage("Password tidak boleh kosong!");
            passwordField.requestFocus();
            return false;
        }

        if (namaField.getText().trim().isEmpty()) {
            showErrorMessage("Nama tidak boleh kosong!");
            namaField.requestFocus();
            return false;
        }

        return true;
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AdminFrame();
        });
    }
}