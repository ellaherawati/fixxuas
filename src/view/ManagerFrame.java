package view;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class ManagerFrame extends JFrame {
    // Color scheme - Modern Blue Theme
    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);      // Blue 500
    private static final Color PRIMARY_BLUE_DARK = new Color(37, 99, 235);  // Blue 600
    private static final Color PRIMARY_BLUE_LIGHT = new Color(147, 197, 253); // Blue 300
    private static final Color BACKGROUND_LIGHT = new Color(248, 250, 252);  // Slate 50
    private static final Color BACKGROUND_WHITE = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);         // Slate 900
    private static final Color TEXT_SECONDARY = new Color(71, 85, 105);      // Slate 600
    private static final Color BORDER_COLOR = new Color(226, 232, 240);      // Slate 200
    private static final Color SIDEBAR_BG = new Color(30, 41, 59);           // Slate 800
    private static final Color SIDEBAR_HOVER = new Color(51, 65, 85);        // Slate 700
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);       // Green 500
    private static final Color WARNING_ORANGE = new Color(249, 115, 22);     // Orange 500
    private static final Color ERROR_RED = new Color(239, 68, 68);           // Red 500
    
    // DAOs
    private MenuDAO menuDAO;
    private CustomerOrderDAO customerOrderDAO;
    private PembayaranDAO pembayaranDAO;
    private PesananDibatalkanDAO pesananDibatalkanDAO;
    private OrderDetailDAO orderDetailDAO;
    private UserDAO userDAO;
    
    // Main components
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;
    private CardLayout cardLayout;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    
    // Navigation buttons
    private JButton[] navButtons;
    private String[] navLabels = {
        "Dashboard", "Menu Management", "Sales Report", 
        "Order Tracking", "Cancelled Orders"
    };
    private String[] navIcons = {"📊", "🍽️", "📈", "📦", "❌"};
    
    // Dashboard components
    private JLabel totalRevenueLabel;
    private JLabel totalOrdersLabel;
    private JLabel totalCancelledLabel;
    private JLabel totalMenusLabel;
    
    // Menu management components
    private JTable menuTable;
    private DefaultTableModel menuTableModel;
    
    // Sales report components
    private JTable salesReportTable;
    private DefaultTableModel salesTableModel;
    private JComboBox<String> reportPeriodCombo;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    
    // Order tracking components
    private JTable orderTrackingTable;
    private DefaultTableModel orderTableModel;
    private JComboBox<String> orderStatusFilter;
    
    // Cancelled orders components
    private JTable cancelledOrdersTable;
    private DefaultTableModel cancelledTableModel;

    public ManagerFrame() {
        initializeDAOs();
        initializeFormatters();
        setupModernLookAndFeel();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDashboardData();
        
        setTitle("Dapur Arunika - Manager Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 1000);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Set app icon and modern appearance
        setBackground(BACKGROUND_LIGHT);
    }
    
    private void setupModernLookAndFeel() {
        try {
            // Use system look and feel as base
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            
            // Customize UI defaults for modern appearance
            UIManager.put("Button.background", BACKGROUND_WHITE);
            UIManager.put("Button.foreground", TEXT_PRIMARY);
            UIManager.put("Button.border", BorderFactory.createLineBorder(BORDER_COLOR, 1));
            UIManager.put("Panel.background", BACKGROUND_WHITE);
            UIManager.put("Table.background", BACKGROUND_WHITE);
            UIManager.put("Table.alternateRowColor", BACKGROUND_LIGHT);
            UIManager.put("Table.gridColor", BORDER_COLOR);
            UIManager.put("Table.selectionBackground", PRIMARY_BLUE_LIGHT);
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("TableHeader.background", BACKGROUND_LIGHT);
            UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void initializeDAOs() {
        menuDAO = new MenuDAO();
        customerOrderDAO = new CustomerOrderDAO();
        pembayaranDAO = new PembayaranDAO();
        pesananDibatalkanDAO = new PesananDibatalkanDAO();
        orderDetailDAO = new OrderDetailDAO();
        userDAO = new UserDAO();
    }
    
    private void initializeFormatters() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    }
    
    private void initializeComponents() {
        createSidebar();
        createMainContent();
    }
    
    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(280, 0));
        sidebarPanel.setBorder(new EmptyBorder(0, 0, 0, 1));
        
        // Logo/Header section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        
        JLabel logoLabel = new JLabel("🍽️ Dapur Arunika");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Manager Dashboard");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(PRIMARY_BLUE_LIGHT);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(SIDEBAR_BG);
        titlePanel.add(logoLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        sidebarPanel.add(headerPanel);
        
        // Navigation section
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(SIDEBAR_BG);
        navPanel.setBorder(new EmptyBorder(0, 10, 20, 10));
        
        navButtons = new JButton[navLabels.length];
        
        for (int i = 0; i < navLabels.length; i++) {
            navButtons[i] = createNavButton(navIcons[i] + "  " + navLabels[i], i);
            navPanel.add(navButtons[i]);
            navPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        // Set first button as active
        setActiveNavButton(0);
        
        sidebarPanel.add(navPanel);
        
        // Add flexible space
        sidebarPanel.add(Box.createVerticalGlue());
        
        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(SIDEBAR_BG);
        footerPanel.setBorder(new EmptyBorder(20, 20, 30, 20));
        
        JLabel userLabel = new JLabel("👤 Manager");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(PRIMARY_BLUE_LIGHT);
        
        JButton logoutButton = createModernButton("🚪 Logout", ERROR_RED);
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        
        footerPanel.add(userLabel, BorderLayout.NORTH);
        footerPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.CENTER);
        footerPanel.add(logoutButton, BorderLayout.SOUTH);
        
        sidebarPanel.add(footerPanel);
    }
    
    private JButton createNavButton(String text, int index) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(PRIMARY_BLUE_LIGHT);
        button.setBackground(SIDEBAR_BG);
        button.setBorder(new EmptyBorder(15, 20, 15, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getBackground().equals(PRIMARY_BLUE)) {
                    button.setBackground(SIDEBAR_HOVER);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.getBackground().equals(PRIMARY_BLUE)) {
                    button.setBackground(SIDEBAR_BG);
                }
            }
        });
        
        button.addActionListener(e -> {
            setActiveNavButton(index);
            showPanel(navLabels[index]);
        });
        
        return button;
    }
    
    private void setActiveNavButton(int activeIndex) {
        for (int i = 0; i < navButtons.length; i++) {
            if (i == activeIndex) {
                navButtons[i].setBackground(PRIMARY_BLUE);
                navButtons[i].setForeground(Color.WHITE);
            } else {
                navButtons[i].setBackground(SIDEBAR_BG);
                navButtons[i].setForeground(PRIMARY_BLUE_LIGHT);
            }
        }
    }
    
    private void createMainContent() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BACKGROUND_LIGHT);
        
        // Create all content panels
        mainContentPanel.add(createDashboardPanel(), "Dashboard");
        mainContentPanel.add(createMenuManagementPanel(), "Menu Management");
        mainContentPanel.add(createSalesReportPanel(), "Sales Report");
        mainContentPanel.add(createOrderTrackingPanel(), "Order Tracking");
        mainContentPanel.add(createCancelledOrdersPanel(), "Cancelled Orders");
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_LIGHT);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("Dashboard Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel("Real-time business insights");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_LIGHT);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        JLabel dateLabel = new JLabel("Updated: " + dateFormat.format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_SECONDARY);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        // Stats Cards
        JPanel statsPanel = createModernStatsPanel();
        
        // Quick Actions
        JPanel actionsPanel = createQuickActionsPanel();
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createModernStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(BACKGROUND_LIGHT);
        statsPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        
        // Revenue Card
        JPanel revenueCard = createModernStatsCard("Total Revenue", "Rp 0", 
            SUCCESS_GREEN, "💰", "vs last month");
        totalRevenueLabel = (JLabel) ((JPanel) revenueCard.getComponent(1)).getComponent(1);
        
        // Orders Card
        JPanel ordersCard = createModernStatsCard("Total Orders", "0", 
            PRIMARY_BLUE, "📦", "completed orders");
        totalOrdersLabel = (JLabel) ((JPanel) ordersCard.getComponent(1)).getComponent(1);
        
        // Cancelled Orders Card
        JPanel cancelledCard = createModernStatsCard("Cancelled Orders", "0", 
            ERROR_RED, "❌", "this month");
        totalCancelledLabel = (JLabel) ((JPanel) cancelledCard.getComponent(1)).getComponent(1);
        
        // Menu Items Card
        JPanel menuCard = createModernStatsCard("Menu Items", "0", 
            WARNING_ORANGE, "🍽️", "active items");
        totalMenusLabel = (JLabel) ((JPanel) menuCard.getComponent(1)).getComponent(1);
        
        statsPanel.add(revenueCard);
        statsPanel.add(ordersCard);
        statsPanel.add(cancelledCard);
        statsPanel.add(menuCard);
        
        return statsPanel;
    }
    
    private JPanel createModernStatsCard(String title, String value, Color accentColor, String icon, String subtitle) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BACKGROUND_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(accentColor, 12),
            new EmptyBorder(25, 25, 25, 25)
        ));
        
        // Header with icon and title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_WHITE);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        headerPanel.add(iconLabel, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Value panel
        JPanel valuePanel = new JPanel(new BorderLayout());
        valuePanel.setBackground(BACKGROUND_WHITE);
        valuePanel.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        valuePanel.add(valueLabel, BorderLayout.NORTH);
        valuePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createQuickActionsPanel() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionsPanel.setBackground(BACKGROUND_LIGHT);
        
        JButton refreshButton = createModernButton("🔄 Refresh Data", PRIMARY_BLUE);
        refreshButton.addActionListener(e -> loadDashboardData());
        
        JButton menuButton = createModernButton("🍽️ Manage Menu", SUCCESS_GREEN);
        menuButton.addActionListener(e -> {
            setActiveNavButton(1);
            showPanel("Menu Management");
        });
        
        JButton salesButton = createModernButton("📈 Sales Report", WARNING_ORANGE);
        salesButton.addActionListener(e -> {
            setActiveNavButton(2);
            showPanel("Sales Report");
        });
        
        JButton exportButton = createModernButton("💾 Export Data", TEXT_SECONDARY);
        exportButton.addActionListener(e -> exportSalesData());
        
        actionsPanel.add(refreshButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        actionsPanel.add(menuButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        actionsPanel.add(salesButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        actionsPanel.add(exportButton);
        
        return actionsPanel;
    }
    
    private JButton createModernButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(new RoundedBorder(backgroundColor, 8));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    private JPanel createMenuManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = createSectionHeader("Menu Management", "Manage your restaurant menu items");
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(BACKGROUND_LIGHT);
        controlPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JButton addButton = createModernButton("➕ Add Menu", SUCCESS_GREEN);
        JButton editButton = createModernButton("✏️ Edit Menu", PRIMARY_BLUE);
        JButton deleteButton = createModernButton("🗑️ Delete Menu", ERROR_RED);
        
        addButton.addActionListener(e -> openMenuDialog(null));
        editButton.addActionListener(e -> editSelectedMenu());
        deleteButton.addActionListener(e -> deleteSelectedMenu());
        
        controlPanel.add(addButton);
        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanel.add(editButton);
        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanel.add(deleteButton);
        
        // Table
        JPanel tablePanel = createModernTablePanel();
        String[] menuColumns = {"ID", "Menu Name", "Type", "Price", "Status", "Description", "Image"};
        menuTableModel = new DefaultTableModel(menuColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(menuTableModel);
        styleTable(menuTable);
        
        JScrollPane scrollPane = new JScrollPane(menuTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND_WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(controlPanel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSalesReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = createSectionHeader("Sales Report", "Analyze your sales performance");
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(BACKGROUND_WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 12),
            new EmptyBorder(20, 25, 20, 25)
        ));
        
        reportPeriodCombo = new JComboBox<>(new String[]{
            "Today", "Last 7 Days", "This Month", "Last Month", "Custom Range"
        });
        styleComboBox(reportPeriodCombo);
        
        startDateChooser = new JDateChooser();
        endDateChooser = new JDateChooser();
        
        JButton generateButton = createModernButton("📊 Generate", PRIMARY_BLUE);
        generateButton.addActionListener(e -> generateSalesReport());
        
        filterPanel.add(new JLabel("Period:"));
        filterPanel.add(reportPeriodCombo);
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(startDateChooser);
        filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(endDateChooser);
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(generateButton);
        
        // Table
        JPanel tablePanel = createModernTablePanel();
        String[] salesColumns = {"Date", "Order ID", "Customer", "Items", "Total", "Payment", "Status"};
        salesTableModel = new DefaultTableModel(salesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesReportTable = new JTable(salesTableModel);
        styleTable(salesReportTable);
        
        JScrollPane scrollPane = new JScrollPane(salesReportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createOrderTrackingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = createSectionHeader("Order Tracking", "Monitor all customer orders");
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(BACKGROUND_WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 12),
            new EmptyBorder(20, 25, 20, 25)
        ));
        
        orderStatusFilter = new JComboBox<>(new String[]{"All", "pending", "completed", "cancelled"});
        styleComboBox(orderStatusFilter);
        
        JButton refreshButton = createModernButton("🔄 Refresh", PRIMARY_BLUE);
        JButton viewButton = createModernButton("👁️ View Details", SUCCESS_GREEN);
        
        refreshButton.addActionListener(e -> loadOrderTracking());
        viewButton.addActionListener(e -> viewOrderDetails());
        
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(orderStatusFilter);
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(refreshButton);
        filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filterPanel.add(viewButton);
        
        // Table
        JPanel tablePanel = createModernTablePanel();
        String[] orderColumns = {"Order ID", "Customer", "Date", "Total", "Status", "Notes"};
        orderTableModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTrackingTable = new JTable(orderTableModel);
        styleTable(orderTrackingTable);
        
        JScrollPane scrollPane = new JScrollPane(orderTrackingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCancelledOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_LIGHT);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Header
        JPanel headerPanel = createSectionHeader("Cancelled Orders", "Analyze cancellation patterns");
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(BACKGROUND_LIGHT);
        controlPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JButton refreshButton = createModernButton("🔄 Refresh", PRIMARY_BLUE);
        JButton analyzeButton = createModernButton("📊 Analyze", WARNING_ORANGE);
        
        refreshButton.addActionListener(e -> loadCancelledOrders());
        analyzeButton.addActionListener(e -> analyzeCancellationReasons());
        
        controlPanel.add(refreshButton);
        controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        controlPanel.add(analyzeButton);
        
        // Table
        JPanel tablePanel = createModernTablePanel();
        String[] cancelledColumns = {"Order ID", "Customer", "Cancel Date", "Reason", "Amount Lost"};
        cancelledTableModel = new DefaultTableModel(cancelledColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cancelledOrdersTable = new JTable(cancelledTableModel);
        styleTable(cancelledOrdersTable);
        
        JScrollPane scrollPane = new JScrollPane(cancelledOrdersTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(controlPanel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSectionHeader(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_LIGHT);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_LIGHT);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createModernTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_WHITE);
        tablePanel.setBorder(new RoundedBorder(BORDER_COLOR, 12));
        tablePanel.setPreferredSize(new Dimension(0, 400));
        
        return tablePanel;
    }
    
    private void styleTable(JTable table) {
        table.setBackground(BACKGROUND_WHITE);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(35);
        table.setSelectionBackground(PRIMARY_BLUE_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        
        // Style header
        JTableHeader header = table.getTableHeader();
        header.setBackground(BACKGROUND_LIGHT);
        header.setForeground(TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setPreferredSize(new Dimension(0, 40));
        
        // Custom cell renderer for alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(BACKGROUND_WHITE);
                    } else {
                        c.setBackground(BACKGROUND_LIGHT);
                    }
                }
                
                setBorder(new EmptyBorder(8, 12, 8, 12));
                return c;
            }
        });
    }
    
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(BACKGROUND_WHITE);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        comboBox.setPreferredSize(new Dimension(150, 35));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        reportPeriodCombo.addActionListener(e -> {
            String selected = (String) reportPeriodCombo.getSelectedItem();
            boolean customRange = "Custom Range".equals(selected);
            startDateChooser.setEnabled(customRange);
            endDateChooser.setEnabled(customRange);
            
            if (!customRange) {
                setDateRangeFromPeriod(selected);
            }
        });
        
        orderStatusFilter.addActionListener(e -> loadOrderTracking());
    }
    
    private void showPanel(String panelName) {
        cardLayout.show(mainContentPanel, panelName);
    }
    
    private void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadStats();
                loadMenuData();
                loadOrderTracking();
                loadCancelledOrders();
                return null;
            }
            
            @Override
            protected void done() {
                showModernNotification("Dashboard refreshed successfully!", SUCCESS_GREEN);
            }
        };
        worker.execute();
    }
    
    /**
     * @param message
     * @param color
     */
    private void showModernNotification(String message, Color color) {
        JDialog notification = new JDialog(this);
        notification.setUndecorated(true);
        notification.setAlwaysOnTop(true);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(color);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        panel.add(messageLabel, BorderLayout.CENTER);
        notification.add(panel);
        notification.pack();
        
        // Position at top-right of parent
        Point parentLocation = this.getLocationOnScreen();
        Dimension parentSize = this.getSize();
        notification.setLocation(
            parentLocation.x + parentSize.width - notification.getWidth() - 30,
            parentLocation.y + 30
        );
        
        notification.setVisible(true);
        
        // Auto-hide after 3 seconds
        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> notification.dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void loadStats() {
        try {
            // Total Revenue
            List<Pembayaran> payments = pembayaranDAO.findAll();
            double totalRevenue = payments.stream()
                .filter(p -> "berhasil".equals(p.getStatusPembayaran()))
                .mapToDouble(Pembayaran::getJumlahPembayaran)
                .sum();
            
            SwingUtilities.invokeLater(() -> 
                totalRevenueLabel.setText(currencyFormat.format(totalRevenue)));
            
            // Total Orders
            List<CustomerOrder> orders = customerOrderDAO.findAll();
            SwingUtilities.invokeLater(() -> 
                totalOrdersLabel.setText(String.valueOf(orders.size())));
            
            // Cancelled Orders
            List<PesananDibatalkan> cancelledOrders = pesananDibatalkanDAO.findAll();
            SwingUtilities.invokeLater(() -> 
                totalCancelledLabel.setText(String.valueOf(cancelledOrders.size())));
            
            // Menu Items
            List<Menu> menus = menuDAO.findAll();
            SwingUtilities.invokeLater(() -> 
                totalMenusLabel.setText(String.valueOf(menus.size())));
            
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> 
                showModernNotification("Error loading stats: " + e.getMessage(), ERROR_RED));
        }
    }
    
    private void loadMenuData() {
        SwingUtilities.invokeLater(() -> {
            menuTableModel.setRowCount(0);
            List<Menu> menus = menuDAO.findAll();
            
            for (Menu menu : menus) {
                String status = "1".equals(menu.getKetersediaan()) ? "Available" : "Unavailable";
                String description = menu.getDeskripsi();
                if (description != null && description.length() > 50) {
                    description = description.substring(0, 47) + "...";
                }
                
                String hasImage = (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) ? "✓" : "✗";
                
                Object[] row = {
                    menu.getIdMenu(),
                    menu.getNamaMenu(),
                    menu.getJenisMenu(),
                    currencyFormat.format(menu.getHarga()),
                    status,
                    description != null ? description : "-",
                    hasImage
                };
                menuTableModel.addRow(row);
            }
        });
    }
    
    private void loadOrderTracking() {
        SwingUtilities.invokeLater(() -> {
            orderTableModel.setRowCount(0);
            List<CustomerOrder> orders = customerOrderDAO.findAll();
            
            String statusFilter = (String) orderStatusFilter.getSelectedItem();
            
            for (CustomerOrder order : orders) {
                if (!"All".equals(statusFilter) && !statusFilter.equals(order.getStatusPesanan())) {
                    continue;
                }
                
                String customerName = "Unknown";
                try {
                    User customer = userDAO.findById(order.getCustomerId());
                    if (customer != null) {
                        customerName = customer.getNama();
                    }
                } catch (Exception e) {
                    // Handle error silently
                }
                
                Object[] row = {
                    order.getIdPesanan(),
                    customerName,
                    dateFormat.format(order.getTanggalPesanan()),
                    currencyFormat.format(order.getTotalPesanan()),
                    order.getStatusPesanan(),
                    order.getCatatan() != null ? order.getCatatan() : "-"
                };
                orderTableModel.addRow(row);
            }
        });
    }
    
    private void loadCancelledOrders() {
        SwingUtilities.invokeLater(() -> {
            cancelledTableModel.setRowCount(0);
            List<PesananDibatalkan> cancelledOrders = pesananDibatalkanDAO.findAll();
            
            for (PesananDibatalkan cancelled : cancelledOrders) {
                CustomerOrder originalOrder = customerOrderDAO.findById(cancelled.getIdPesanan());
                String customerName = "Unknown";
                double amountLost = 0;
                
                if (originalOrder != null) {
                    amountLost = originalOrder.getTotalPesanan();
                    try {
                        User customer = userDAO.findById(originalOrder.getCustomerId());
                        if (customer != null) {
                            customerName = customer.getNama();
                        }
                    } catch (Exception e) {
                        // Handle error silently
                    }
                }
                
                Object[] row = {
                    cancelled.getIdPesanan(),
                    customerName,
                    dateFormat.format(cancelled.getTanggalBatal()),
                    cancelled.getAlasanBatal(),
                    currencyFormat.format(amountLost)
                };
                cancelledTableModel.addRow(row);
            }
        });
    }
    
    private void generateSalesReport() {
        try {
            salesTableModel.setRowCount(0);
            
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();
            
            if (startDate == null || endDate == null) {
                showModernNotification("Please select date range", ERROR_RED);
                return;
            }
            
            List<CustomerOrder> orders = customerOrderDAO.findAll();
            double totalSales = 0;
            int totalTransactions = 0;
            
            for (CustomerOrder order : orders) {
                Date orderDate = new Date(order.getTanggalPesanan().getTime());
                
                if (orderDate.compareTo(startDate) >= 0 && orderDate.compareTo(endDate) <= 0) {
                    if ("selesai".equals(order.getStatusPesanan())) {
                        String customerName = "Unknown";
                        try {
                            User customer = userDAO.findById(order.getCustomerId());
                            if (customer != null) {
                                customerName = customer.getNama();
                            }
                        } catch (Exception e) {
                            // Handle error silently
                        }
                        
                        String paymentMethod = "Cash";
                        try {
                            Pembayaran payment = pembayaranDAO.findByOrderId(order.getIdPesanan());
                            if (payment != null) {
                                paymentMethod = payment.getMetodePembayaran().toUpperCase();
                            }
                        } catch (Exception e) {
                            // Handle error silently
                        }
                        
                        List<OrderDetail> details = orderDetailDAO.findByOrderId(order.getIdPesanan());
                        int itemCount = details.stream().mapToInt(OrderDetail::getJumlah).sum();
                        
                        Object[] row = {
                            dateFormat.format(order.getTanggalPesanan()),
                            order.getIdPesanan(),
                            customerName,
                            itemCount + " items",
                            currencyFormat.format(order.getTotalPesanan()),
                            paymentMethod,
                            order.getStatusPesanan()
                        };
                        
                        salesTableModel.addRow(row);
                        totalSales += order.getTotalPesanan();
                        totalTransactions++;
                    }
                }
            }
            
            showModernNotification(String.format("Generated %d transactions, Total: %s", 
                totalTransactions, currencyFormat.format(totalSales)), SUCCESS_GREEN);
                
        } catch (Exception e) {
            e.printStackTrace();
            showModernNotification("Error generating sales report", ERROR_RED);
        }
    }
    
    private void setDateRangeFromPeriod(String period) {
        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end = now;
        
        switch (period) {
            case "Today":
                start = now;
                break;
            case "Last 7 Days":
                start = now.minusDays(7);
                break;
            case "This Month":
                start = now.withDayOfMonth(1);
                break;
            case "Last Month":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = now.withDayOfMonth(1).minusDays(1);
                break;
            default:
                return;
        }
        
        startDateChooser.setDate(java.sql.Date.valueOf(start));
        endDateChooser.setDate(java.sql.Date.valueOf(end));
    }
    
    private void openMenuDialog(Menu menu) {
        JDialog dialog = new JDialog(this, menu == null ? "Add Menu" : "Edit Menu", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(BACKGROUND_LIGHT);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_LIGHT);
        headerPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        JLabel titleLabel = new JLabel(menu == null ? "Add New Menu Item" : "Edit Menu Item");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 12),
            new EmptyBorder(25, 25, 25, 25)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Form fields
        JTextField nameField = createStyledTextField(30);
        JTextField typeField = createStyledTextField(30);
        JTextField priceField = createStyledTextField(30);
        JTextArea descArea = new JTextArea(4, 30);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        descScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        
        JComboBox<String> availabilityCombo = new JComboBox<>(new String[]{"1", "0"});
        styleComboBox(availabilityCombo);
        
        // Image preview
        JLabel imagePreviewLabel = new JLabel("No Image");
        imagePreviewLabel.setPreferredSize(new Dimension(150, 100));
        imagePreviewLabel.setBorder(new RoundedBorder(BORDER_COLOR, 8));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(BACKGROUND_LIGHT);
        
        JButton uploadImageButton = createModernButton("📷 Upload", PRIMARY_BLUE);
        JButton removeImageButton = createModernButton("🗑️ Remove", ERROR_RED);
        removeImageButton.setEnabled(false);
        
        final String[] currentImagePath = {null};
        
        if (menu != null) {
            nameField.setText(menu.getNamaMenu());
            typeField.setText(menu.getJenisMenu());
            priceField.setText(String.valueOf(menu.getHarga()));
            descArea.setText(menu.getDeskripsi());
            availabilityCombo.setSelectedItem(menu.getKetersediaan());
            
            if (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) {
                currentImagePath[0] = menu.getGambar();
                displayImagePreview(imagePreviewLabel, menu.getGambar());
                removeImageButton.setEnabled(true);
            }
        }
        
        // Layout form
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(createStyledLabel("Menu Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(createStyledLabel("Type:"), gbc);
        gbc.gridx = 1;
        formPanel.add(typeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createStyledLabel("Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createStyledLabel("Available:"), gbc);
        gbc.gridx = 1;
        formPanel.add(availabilityCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(createStyledLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(descScrollPane, gbc);
        
        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(BACKGROUND_WHITE);
        imagePanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 12),
            new EmptyBorder(25, 25, 25, 25)
        ));
        imagePanel.setPreferredSize(new Dimension(200, 0));
        
        JLabel imageTitleLabel = new JLabel("Menu Image");
        imageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        imageTitleLabel.setForeground(TEXT_PRIMARY);
        
        JPanel imageButtonPanel = new JPanel(new FlowLayout());
        imageButtonPanel.setBackground(BACKGROUND_WHITE);
        imageButtonPanel.add(uploadImageButton);
        imageButtonPanel.add(removeImageButton);
        
        imagePanel.add(imageTitleLabel, BorderLayout.NORTH);
        imagePanel.add(imagePreviewLabel, BorderLayout.CENTER);
        imagePanel.add(imageButtonPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_LIGHT);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton saveButton = createModernButton("💾 Save Menu", SUCCESS_GREEN);
        JButton cancelButton = createModernButton("❌ Cancel", TEXT_SECONDARY);
        
        saveButton.addActionListener(e -> {
            try {
                if (nameField.getText().trim().isEmpty()) {
                    showModernNotification("Menu name cannot be empty!", ERROR_RED);
                    nameField.requestFocus();
                    return;
                }
                
                if (typeField.getText().trim().isEmpty()) {
                    showModernNotification("Menu type cannot be empty!", ERROR_RED);
                    typeField.requestFocus();
                    return;
                }
                
                double price;
                try {
                    price = Double.parseDouble(priceField.getText().trim());
                    if (price < 0) {
                        showModernNotification("Price cannot be negative!", ERROR_RED);
                        priceField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showModernNotification("Please enter a valid price!", ERROR_RED);
                    priceField.requestFocus();
                    return;
                }
                
                Menu menuToSave = menu != null ? menu : new Menu();
                menuToSave.setNamaMenu(nameField.getText().trim());
                menuToSave.setJenisMenu(typeField.getText().trim());
                menuToSave.setHarga(price);
                menuToSave.setDeskripsi(descArea.getText().trim());
                menuToSave.setKetersediaan((String) availabilityCombo.getSelectedItem());
                menuToSave.setGambar(currentImagePath[0]);
                
                boolean success;
                if (menu == null) {
                    success = menuDAO.create(menuToSave);
                } else {
                    success = menuDAO.update(menuToSave);
                }
                
                if (success) {
                    showModernNotification("Menu saved successfully!", SUCCESS_GREEN);
                    loadMenuData();
                    loadStats();
                    dialog.dispose();
                } else {
                    showModernNotification("Failed to save menu!", ERROR_RED);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showModernNotification("Error: " + ex.getMessage(), ERROR_RED);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(saveButton);
        
        // Handle image upload
        uploadImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Menu Image");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image files", "jpg", "jpeg", "png", "gif", "bmp"));
            
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    
                    java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(selectedFile);
                    if (image == null) {
                        showModernNotification("Selected file is not a valid image!", ERROR_RED);
                        return;
                    }
                    
                    java.io.File imagesDir = new java.io.File("images/menu/");
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }
                    
                    String extension = getFileExtension(selectedFile.getName());
                    String newFileName = "menu_" + System.currentTimeMillis() + "." + extension;
                    String newFilePath = "images/menu/" + newFileName;
                    
                    java.nio.file.Path sourcePath = selectedFile.toPath();
                    java.nio.file.Path targetPath = java.nio.file.Paths.get(newFilePath);
                    java.nio.file.Files.copy(sourcePath, targetPath, 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    currentImagePath[0] = newFilePath;
                    displayImagePreview(imagePreviewLabel, newFilePath);
                    removeImageButton.setEnabled(true);
                    
                    showModernNotification("Image uploaded successfully!", SUCCESS_GREEN);
                        
                } catch (Exception ex) {
                    showModernNotification("Error uploading image: " + ex.getMessage(), ERROR_RED);
                }
            }
        });
        
        removeImageButton.addActionListener(e -> {
            currentImagePath[0] = null;
            clearImagePreview(imagePreviewLabel);
            removeImageButton.setEnabled(false);
            showModernNotification("Image removed", WARNING_ORANGE);
        });
        
        // Layout
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(imagePanel, BorderLayout.EAST);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(0, 35));
        return field;
    }
    
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
    // Helper methods
    private void displayImagePreview(JLabel imageLabel, String imagePath) {
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                java.io.File imageFile = new java.io.File(imagePath);
                if (imageFile.exists()) {
                    java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(imageFile);
                    if (originalImage != null) {
                        java.awt.Image scaledImage = originalImage.getScaledInstance(
                            150, 100, java.awt.Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaledImage));
                        imageLabel.setText("");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image preview: " + e.getMessage());
        }
        clearImagePreview(imageLabel);
    }
    
    private void clearImagePreview(JLabel imageLabel) {
        imageLabel.setIcon(null);
        imageLabel.setText("No Image");
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg";
    }
    
    private void editSelectedMenu() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int menuId = (Integer) menuTableModel.getValueAt(selectedRow, 0);
            Menu menu = menuDAO.findById(menuId);
            if (menu != null) {
                openMenuDialog(menu);
            }
        } else {
            showModernNotification("Please select a menu to edit!", WARNING_ORANGE);
        }
    }
    
    private void deleteSelectedMenu() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int menuId = (Integer) menuTableModel.getValueAt(selectedRow, 0);
            String menuName = (String) menuTableModel.getValueAt(selectedRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete menu: " + menuName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (menuDAO.delete(menuId)) {
                    showModernNotification("Menu deleted successfully!", SUCCESS_GREEN);
                    loadMenuData();
                } else {
                    showModernNotification("Failed to delete menu!", ERROR_RED);
                }
            }
        } else {
            showModernNotification("Please select a menu to delete!", WARNING_ORANGE);
        }
    }
    
    private void viewOrderDetails() {
        int selectedRow = orderTrackingTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (Integer) orderTableModel.getValueAt(selectedRow, 0);
            showOrderDetailsDialog(orderId);
        } else {
            showModernNotification("Please select an order to view details!", WARNING_ORANGE);
        }
    }
    
    private void showOrderDetailsDialog(int orderId) {
        CustomerOrder order = customerOrderDAO.findById(orderId);
        if (order == null) {
            showModernNotification("Order not found!", ERROR_RED);
            return;
        }
        
        JDialog detailsDialog = new JDialog(this, "Order Details", true);
        detailsDialog.setSize(700, 900);
        detailsDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_LIGHT);
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_LIGHT);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Order #" + orderId);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Order info panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 4, 15, 10));
        infoPanel.setBackground(BACKGROUND_WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 12),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        String customerName = "Unknown";
        try {
            User customer = userDAO.findById(order.getCustomerId());
            if (customer != null) {
                customerName = customer.getNama();
            }
        } catch (Exception e) {
            // Handle silently
        }
        
        infoPanel.add(createInfoLabel("Customer:", customerName));
        infoPanel.add(createInfoLabel("Date:", dateFormat.format(order.getTanggalPesanan())));
        infoPanel.add(createInfoLabel("Status:", order.getStatusPesanan()));
        infoPanel.add(createInfoLabel("Total:", currencyFormat.format(order.getTotalPesanan())));
        
        // Items table
        JPanel itemsPanel = createModernTablePanel();
        String[] itemColumns = {"Menu Item", "Quantity", "Unit Price", "Subtotal"};
        DefaultTableModel itemsModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable itemsTable = new JTable(itemsModel);
        styleTable(itemsTable);
        
        List<OrderDetail> orderDetails = orderDetailDAO.findByOrderId(orderId);
        for (OrderDetail detail : orderDetails) {
            String menuName = "Unknown Menu";
            try {
                Menu menu = menuDAO.findById(detail.getIdMenu());
                if (menu != null) {
                    menuName = menu.getNamaMenu();
                }
            } catch (Exception e) {
                // Handle silently
            }
            
            Object[] row = {
                menuName,
                detail.getJumlah(),
                currencyFormat.format(detail.getHargaSatuan()),
                currencyFormat.format(detail.getJumlah() * detail.getHargaSatuan())
            };
            itemsModel.addRow(row);
        }
        
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        itemsPanel.add(itemsScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BACKGROUND_LIGHT);
        
        JButton closeButton = createModernButton("Close", TEXT_SECONDARY);
        closeButton.addActionListener(e -> detailsDialog.dispose());
        buttonPanel.add(closeButton);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(infoPanel, BorderLayout.CENTER);
        mainPanel.add(itemsPanel, BorderLayout.SOUTH);
        
        detailsDialog.add(mainPanel);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailsDialog.setVisible(true);
    }
    
    private JPanel createInfoLabel(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_WHITE);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComp.setForeground(TEXT_SECONDARY);
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueComp.setForeground(TEXT_PRIMARY);
        
        panel.add(labelComp, BorderLayout.NORTH);
        panel.add(valueComp, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void analyzeCancellationReasons() {
        try {
            List<PesananDibatalkan> cancelledOrders = pesananDibatalkanDAO.findAll();
            
            if (cancelledOrders.isEmpty()) {
                showModernNotification("No cancelled orders found!", WARNING_ORANGE);
                return;
            }
            
            Map<String, Integer> reasonCounts = new HashMap<>();
            Map<String, Double> reasonAmounts = new HashMap<>();
            
            for (PesananDibatalkan cancelled : cancelledOrders) {
                String reason = cancelled.getAlasanBatal();
                reasonCounts.put(reason, reasonCounts.getOrDefault(reason, 0) + 1);
                
                CustomerOrder originalOrder = customerOrderDAO.findById(cancelled.getIdPesanan());
                if (originalOrder != null) {
                    reasonAmounts.put(reason, 
                        reasonAmounts.getOrDefault(reason, 0.0) + originalOrder.getTotalPesanan());
                }
            }
            
            JDialog analysisDialog = new JDialog(this, "Cancellation Analysis", true);
            analysisDialog.setSize(600, 450);
            analysisDialog.setLocationRelativeTo(this);
            
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(BACKGROUND_LIGHT);
            mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
            
            // Header
            JLabel titleLabel = new JLabel("Cancellation Reasons Analysis");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titleLabel.setForeground(TEXT_PRIMARY);
            titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
            
            // Analysis table
            JPanel tablePanel = createModernTablePanel();
            String[] columns = {"Reason", "Count", "Percentage", "Lost Revenue"};
            DefaultTableModel analysisModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable analysisTable = new JTable(analysisModel);
            styleTable(analysisTable);
            
            int totalCancellations = cancelledOrders.size();
            double totalLostRevenue = reasonAmounts.values().stream().mapToDouble(Double::doubleValue).sum();
            
            for (Map.Entry<String, Integer> entry : reasonCounts.entrySet()) {
                String reason = entry.getKey();
                int count = entry.getValue();
                double percentage = (count * 100.0) / totalCancellations;
                double lostRevenue = reasonAmounts.getOrDefault(reason, 0.0);
                
                Object[] row = {
                    reason,
                    count,
                    String.format("%.1f%%", percentage),
                    currencyFormat.format(lostRevenue)
                };
                analysisModel.addRow(row);
            }
            
            JScrollPane scrollPane = new JScrollPane(analysisTable);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            
            // Summary panel
            JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 15, 0));
            summaryPanel.setBackground(BACKGROUND_LIGHT);
            summaryPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
            
            JPanel totalCard = createSummaryCard("Total Pembatalan", String.valueOf(totalCancellations), ERROR_RED);
            JPanel revenueCard = createSummaryCard("Total Kerugian", currencyFormat.format(totalLostRevenue), WARNING_ORANGE);
            JPanel avgCard = createSummaryCard("Rata-Rata Kerugian", 
                currencyFormat.format(totalLostRevenue / totalCancellations), PRIMARY_BLUE);
            
            summaryPanel.add(totalCard);
            summaryPanel.add(revenueCard);
            summaryPanel.add(avgCard);
            
            mainPanel.add(titleLabel, BorderLayout.NORTH);
            mainPanel.add(tablePanel, BorderLayout.CENTER);
            mainPanel.add(summaryPanel, BorderLayout.SOUTH);
            
            analysisDialog.add(mainPanel);
            analysisDialog.setVisible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            showModernNotification("Error analyzing cancellation reasons", ERROR_RED);
        }
    }
    
    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BACKGROUND_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(color, 8),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(TEXT_SECONDARY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void exportSalesData() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Sales Data");
            fileChooser.setSelectedFile(new java.io.File("sales_report_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Date,Order ID,Customer,Items,Total,Payment Method,Status\n");
                
                for (int i = 0; i < salesTableModel.getRowCount(); i++) {
                    for (int j = 0; j < salesTableModel.getColumnCount(); j++) {
                        if (j > 0) csvContent.append(",");
                        Object value = salesTableModel.getValueAt(i, j);
                        csvContent.append("\"").append(value != null ? value.toString() : "").append("\"");
                    }
                    csvContent.append("\n");
                }
                
                try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                    writer.write(csvContent.toString());
                }
                
                showModernNotification("Sales data exported successfully!", SUCCESS_GREEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showModernNotification("Error exporting data", ERROR_RED);
        }
    }
    
    // Custom JDateChooser implementation
    private static class JDateChooser extends JPanel {
        private JTextField dateField;
        private Date selectedDate;
        
        public JDateChooser() {
            setLayout(new BorderLayout());
            setBackground(BACKGROUND_WHITE);
            
            dateField = new JTextField(12);
            dateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)
            ));
            dateField.setEditable(false);
            
            JButton calendarButton = new JButton("📅");
            calendarButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            calendarButton.setBackground(PRIMARY_BLUE);
            calendarButton.setForeground(Color.WHITE);
            calendarButton.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            calendarButton.setFocusPainted(false);
            calendarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            calendarButton.addActionListener(e -> showDatePicker());
            
            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
            
            setDate(new Date());
        }
        
        private void showDatePicker() {
            String dateStr = JOptionPane.showInputDialog(this, 
                "Enter date (dd/MM/yyyy):", 
                dateField.getText());
            
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse(dateStr);
                    setDate(date);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use dd/MM/yyyy", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        public void setDate(Date date) {
            this.selectedDate = date;
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateField.setText(sdf.format(date));
            } else {
                dateField.setText("");
            }
        }
        
        public Date getDate() {
            return selectedDate;
        }
        
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            dateField.setEnabled(enabled);
        }
    }
    
    // Custom rounded border class
    private static class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;
        
        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
        
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = 1;
            return insets;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new ManagerFrame().setVisible(true);
        });
    }
}