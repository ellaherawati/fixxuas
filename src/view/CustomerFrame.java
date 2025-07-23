package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CustomerFrame extends JFrame {
    // Database components
    private CustomerOrderDAO customerOrderDAO;
    private OrderDetailDAO orderDetailDAO;
    private PembayaranDAO pembayaranDAO;
    private NotaDAO notaDAO;
    private MenuDAO menuDAO;
    private UserDAO userDAO;
    private PesananDibatalkanDAO pesananDibatalkanDAO;
    
    // Current user info
    private int currentUserId = -1;
    private String currentCustomerName = null;
    
    // Data untuk menu items
    private List<MenuItem> menuItems;
    private List<OrderItem> orderItems;
    private JLabel totalLabel;
    private JPanel cartPanel;
    private JScrollPane cartScrollPane;
    private NumberFormat currencyFormat;
    private JLabel welcomeLabel;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229); // Indigo
    private static final Color SECONDARY_COLOR = new Color(99, 102, 241); // Light indigo
    private static final Color ACCENT_COLOR = new Color(16, 185, 129); // Emerald
    private static final Color DANGER_COLOR = new Color(239, 68, 68); // Red
    private static final Color WARNING_COLOR = new Color(245, 158, 11); // Amber
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Gray-50
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39); // Gray-900
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128); // Gray-500
    
    // Menu item class
    static class MenuItem {
        String name;
        int price;
        String description;
        String category;
        String imagePath;
        ImageIcon imageIcon;
        int menuId;
        
        MenuItem(String name, int price, String description, String category, String imagePath) {
            this.name = name;
            this.price = price;
            this.description = description;
            this.category = category;
            this.imagePath = imagePath;
            this.imageIcon = loadImage(imagePath);
        }
        
        private ImageIcon loadImage(String path) {
            try {
                if (path != null && !path.isEmpty()) {
                    File imageFile = new File(path);
                    if (imageFile.exists()) {
                        BufferedImage img = ImageIO.read(imageFile);
                        Image scaledImg = img.getScaledInstance(180, 120, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading image: " + path + " - " + e.getMessage());
            }
            return createPlaceholderImage();
        }
        
        private ImageIcon createPlaceholderImage() {
            BufferedImage placeholder = new BufferedImage(180, 120, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = placeholder.createGraphics();
            
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Modern gradient background
            GradientPaint gradient = new GradientPaint(0, 0, BACKGROUND_COLOR, 180, 120, new Color(229, 231, 235));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, 180, 120);
            
            // Modern food icon
            g2d.setColor(TEXT_SECONDARY);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            String icon = "🍽️";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (180 - fm.stringWidth(icon)) / 2;
            int y = 60;
            g2d.drawString(icon, x, y);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            String text = "No Image";
            fm = g2d.getFontMetrics();
            x = (180 - fm.stringWidth(text)) / 2;
            y = 90;
            g2d.drawString(text, x, y);
            
            g2d.dispose();
            return new ImageIcon(placeholder);
        }
    }
    
    // Order item class
    static class OrderItem {
        MenuItem menuItem;
        int quantity;
        
        OrderItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }
    }

    public CustomerFrame() {
        initializeDAOs();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        initializeMenuData();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeDAOs() {
        try {
            customerOrderDAO = new CustomerOrderDAO();
            orderDetailDAO = new OrderDetailDAO();
            pembayaranDAO = new PembayaranDAO();
            notaDAO = new NotaDAO();
            menuDAO = new MenuDAO();
            userDAO = new UserDAO();
            pesananDibatalkanDAO = new PesananDibatalkanDAO();
            System.out.println("All DAOs initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing DAOs: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error menginisialisasi koneksi database: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initializeMenuData() {
        menuItems = new ArrayList<>();
        orderItems = new ArrayList<>();
        loadMenuFromDatabase();
    }
    
    private void loadMenuFromDatabase() {
        try {
            List<Menu> dbMenus = menuDAO.findAll();
            
            if (dbMenus.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Tidak ada menu yang ditemukan di database.\nSilakan hubungi administrator.", 
                    "Menu Kosong", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            for (Menu menu : dbMenus) {
                String imagePath = null;
                String description = menu.getDeskripsi();
                
                if (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) {
                    imagePath = menu.getGambar();
                } else {
                    imagePath = findImageForMenu(menu.getNamaMenu());
                }
                
                if (description == null || description.trim().isEmpty()) {
                    description = generateFallbackDescription(menu.getNamaMenu(), menu.getJenisMenu());
                }
                
                MenuItem menuItem = new MenuItem(
                    menu.getNamaMenu(),
                    (int) menu.getHarga(),
                    description,
                    menu.getJenisMenu().toUpperCase(),
                    imagePath
                );
                menuItem.menuId = menu.getIdMenu();
                menuItems.add(menuItem);
            }
            
            System.out.println("Loaded " + menuItems.size() + " menu items from database");
        } catch (Exception e) {
            System.err.println("Error loading menu from database: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error memuat menu dari database: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String findImageForMenu(String menuName) {
        String[] possiblePaths = {
            "images/menu/" + menuName.toLowerCase().replace(" ", "_") + ".jpg",
            "images/menu/" + menuName.toLowerCase().replace(" ", "_") + ".jpeg",
            "images/menu/" + menuName.toLowerCase().replace(" ", "_") + ".png",
            "images/menu/" + menuName.toLowerCase().replace(" ", "-") + ".jpg",
            "images/" + menuName.toLowerCase().replace(" ", "_") + ".jpg"
        };
        
        for (String path : possiblePaths) {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                return path;
            }
        }
        return null;
    }
    
    private String generateFallbackDescription(String menuName, String jenisMenu) {
        if (jenisMenu.equalsIgnoreCase("makanan")) {
            return "Hidangan lezat " + menuName + " dengan cita rasa yang menggugah selera.";
        } else if (jenisMenu.equalsIgnoreCase("minuman")) {
            return "Minuman segar " + menuName + " yang menyegarkan dahaga Anda.";
        } else {
            return "Menu spesial " + menuName + " dari dapur kami.";
        }
    }
    
    private void initializeComponents() {
        setTitle("Dapur Arunika - Ordering System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        
        getContentPane().setBackground(BACKGROUND_COLOR);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Left Panel - Menu
        JPanel menuPanel = createMenuPanel();
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setPreferredSize(new Dimension(900, 600));
        menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScrollPane.setBorder(BorderFactory.createEmptyBorder());
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Right Panel - Order Summary
        JPanel orderPanel = createOrderPanel();
        
        mainPanel.add(menuScrollPane, BorderLayout.CENTER);
        mainPanel.add(orderPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // Add subtle shadow effect
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        String welcomeText = currentCustomerName != null && !currentCustomerName.isEmpty()
            ? "Welcome back, " + currentCustomerName 
            : "Welcome to Dapur Arunika";
        
        welcomeLabel = new JLabel(welcomeText);
        welcomeLabel.setForeground(TEXT_SECONDARY);
        welcomeLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel restaurantLabel = new JLabel("Dapur Arunika");
        restaurantLabel.setForeground(PRIMARY_COLOR);
        restaurantLabel.setFont(new Font("Inter", Font.BOLD, 32));
        restaurantLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setBackground(CARD_COLOR);
        headerContent.add(welcomeLabel, BorderLayout.NORTH);
        headerContent.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(CARD_COLOR);
        titlePanel.add(restaurantLabel, BorderLayout.CENTER);
        
        headerContent.add(titlePanel, BorderLayout.SOUTH);
        
        // Modern logout button
        JButton logoutButton = createModernButton("Logout", DANGER_COLOR, Color.WHITE);
        logoutButton.setPreferredSize(new Dimension(100, 40));
        logoutButton.addActionListener(e -> logout());
        
        headerPanel.add(headerContent, BorderLayout.CENTER);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JButton createModernButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(BACKGROUND_COLOR);
        
        if (menuItems.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(CARD_COLOR);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
            
            JLabel emptyLabel = new JLabel("Menu tidak tersedia", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Inter", Font.BOLD, 18));
            emptyLabel.setForeground(TEXT_SECONDARY);
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            menuPanel.add(emptyPanel);
            return menuPanel;
        }
        
        createMenuSection(menuPanel, "MAKANAN");
        menuPanel.add(Box.createVerticalStrut(30));
        createMenuSection(menuPanel, "MINUMAN");
        
        return menuPanel;
    }
    
    private void createMenuSection(JPanel parent, String category) {
        // Modern section header
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setBackground(BACKGROUND_COLOR);
        sectionHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Inter", Font.BOLD, 24));
        categoryLabel.setForeground(TEXT_PRIMARY);
        
        // Count items in category
        long itemCount = menuItems.stream()
            .filter(item -> item.category.equals(category))
            .count();
        
        JLabel countLabel = new JLabel(itemCount + " items");
        countLabel.setFont(new Font("Inter", Font.BOLD, 14));
        countLabel.setForeground(TEXT_SECONDARY);
        
        sectionHeader.add(categoryLabel, BorderLayout.WEST);
        sectionHeader.add(countLabel, BorderLayout.EAST);
        
        parent.add(sectionHeader);
        
        if (itemCount == 0) {
            JPanel noItemsPanel = new JPanel(new BorderLayout());
            noItemsPanel.setBackground(CARD_COLOR);
            noItemsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
            ));
            
            JLabel noItemsLabel = new JLabel("Tidak ada menu dalam kategori ini", SwingConstants.CENTER);
            noItemsLabel.setFont(new Font("Inter", Font.BOLD, 16));
            noItemsLabel.setForeground(TEXT_SECONDARY);
            
            noItemsPanel.add(noItemsLabel, BorderLayout.CENTER);
            parent.add(noItemsPanel);
            return;
        }
        
        // Create responsive grid
        int columns = 3;
        int rows = (int) Math.ceil(itemCount / (double) columns);
        JPanel itemGrid = new JPanel(new GridLayout(rows, columns, 20, 20));
        itemGrid.setBackground(BACKGROUND_COLOR);
        
        menuItems.stream()
            .filter(item -> item.category.equals(category))
            .forEach(item -> itemGrid.add(createMenuItemPanel(item)));
        
        parent.add(itemGrid);
    }
    
    private JPanel createMenuItemPanel(MenuItem item) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(CARD_COLOR);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        itemPanel.setPreferredSize(new Dimension(280, 350));
        
        // Add subtle shadow effect
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 10)),
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1)
            ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Image container
        JPanel imageContainer = new JPanel(new BorderLayout());
        imageContainer.setBackground(BACKGROUND_COLOR);
        imageContainer.setPreferredSize(new Dimension(240, 120));
        
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        
        if (item.imageIcon != null) {
            imageLabel.setIcon(item.imageIcon);
        } else {
            imageLabel.setIcon(item.createPlaceholderImage());
        }
        
        imageContainer.add(imageLabel, BorderLayout.CENTER);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Name
        JLabel nameLabel = new JLabel(item.name);
        nameLabel.setFont(new Font("Inter", Font.BOLD, 16));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Price
        JLabel priceLabel = new JLabel(currencyFormat.format(item.price));
        priceLabel.setFont(new Font("Inter", Font.BOLD, 18));
        priceLabel.setForeground(ACCENT_COLOR);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Description
        JTextArea descArea = new JTextArea(item.description);
        descArea.setFont(new Font("Inter", Font.PLAIN, 12));
        descArea.setForeground(TEXT_SECONDARY);
        descArea.setBackground(CARD_COLOR);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        descArea.setRows(2);
        
        // Add button
        JButton addButton = createModernButton("Add to Cart", PRIMARY_COLOR, Color.WHITE);
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addButton.addActionListener(e -> addToOrder(item));
        
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(priceLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(descArea);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(addButton);
        
        itemPanel.add(imageContainer, BorderLayout.NORTH);
        itemPanel.add(contentPanel, BorderLayout.CENTER);
        
        return itemPanel;
    }
    
    private JPanel createOrderPanel() {
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBackground(CARD_COLOR);
        orderPanel.setPreferredSize(new Dimension(400, 600));
        orderPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // Order title
        JLabel orderTitle = new JLabel("Your Order");
        orderTitle.setFont(new Font("Inter", Font.BOLD, 24));
        orderTitle.setForeground(TEXT_PRIMARY);
        
        // Cart panel
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(CARD_COLOR);
        
        cartScrollPane = new JScrollPane(cartPanel);
        cartScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cartScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cartScrollPane.setBorder(BorderFactory.createEmptyBorder());
        cartScrollPane.setBackground(CARD_COLOR);
        cartScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Total panel
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(CARD_COLOR);
        totalPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel totalTextLabel = new JLabel("Total");
        totalTextLabel.setFont(new Font("Inter", Font.BOLD, 18));
        totalTextLabel.setForeground(TEXT_PRIMARY);
        
        totalLabel = new JLabel(currencyFormat.format(0));
        totalLabel.setFont(new Font("Inter", Font.BOLD, 24));
        totalLabel.setForeground(ACCENT_COLOR);
        
        totalPanel.add(totalTextLabel, BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        buttonPanel.setBackground(CARD_COLOR);
        
        JButton checkoutButton = createModernButton("Checkout", ACCENT_COLOR, Color.WHITE);
        checkoutButton.setPreferredSize(new Dimension(350, 50));
        checkoutButton.addActionListener(e -> checkout());
        
        JButton clearCartButton = createModernButton("Clear Cart", DANGER_COLOR, Color.WHITE);
        clearCartButton.setPreferredSize(new Dimension(350, 45));
        clearCartButton.addActionListener(e -> clearCart());
        
        buttonPanel.add(checkoutButton);
        buttonPanel.add(clearCartButton);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.add(orderTitle, BorderLayout.WEST);
        
        orderPanel.add(headerPanel, BorderLayout.NORTH);
        orderPanel.add(cartScrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CARD_COLOR);
        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        orderPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        return orderPanel;
    }
    
    private void addToOrder(MenuItem item) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.menuItem.name.equals(item.name)) {
                orderItem.quantity++;
                updateOrderDisplay();
                return;
            }
        }
        
        orderItems.add(new OrderItem(item, 1));
        updateOrderDisplay();
    }
    
    private void clearCart() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Cart is already empty!", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear the cart?",
            "Clear Cart",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            orderItems.clear();
            updateOrderDisplay();
        }
    }
    
    private void updateOrderDisplay() {
        cartPanel.removeAll();
        
        if (orderItems.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout());
            emptyPanel.setBackground(CARD_COLOR);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
            
            JLabel emptyLabel = new JLabel("Your cart is empty", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Inter", Font.BOLD, 16));
            emptyLabel.setForeground(TEXT_SECONDARY);
            
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
            cartPanel.add(emptyPanel);
        } else {
            for (int i = 0; i < orderItems.size(); i++) {
                cartPanel.add(createOrderItemPanel(orderItems.get(i)));
                if (i < orderItems.size() - 1) {
                    cartPanel.add(Box.createVerticalStrut(10));
                }
            }
        }
        
        updateTotal();
        cartPanel.revalidate();
        cartPanel.repaint();
    }
    
    private JPanel createOrderItemPanel(OrderItem orderItem) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(BACKGROUND_COLOR);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        itemPanel.setMaximumSize(new Dimension(350, 80));
        
        // Item info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel nameLabel = new JLabel(orderItem.menuItem.name);
        nameLabel.setFont(new Font("Inter", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel(currencyFormat.format(orderItem.menuItem.price));
        priceLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        priceLabel.setForeground(TEXT_SECONDARY);
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        
        // Quantity controls
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        quantityPanel.setBackground(BACKGROUND_COLOR);
        
        JButton minusButton = new JButton("−");
        minusButton.setFont(new Font("Inter", Font.BOLD, 14));
        minusButton.setPreferredSize(new Dimension(30, 30));
        minusButton.setBackground(DANGER_COLOR);
        minusButton.setForeground(Color.WHITE);
        minusButton.setFocusPainted(false);
        minusButton.setBorderPainted(false);
        minusButton.addActionListener(e -> decreaseQuantity(orderItem));
        
        JLabel quantityLabel = new JLabel(String.valueOf(orderItem.quantity));
        quantityLabel.setFont(new Font("Inter", Font.BOLD, 14));
        quantityLabel.setForeground(TEXT_PRIMARY);
        quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        quantityLabel.setPreferredSize(new Dimension(30, 30));
        
        JButton plusButton = new JButton("+");
        plusButton.setFont(new Font("Inter", Font.BOLD, 14));
        plusButton.setPreferredSize(new Dimension(30, 30));
        plusButton.setBackground(ACCENT_COLOR);
        plusButton.setForeground(Color.WHITE);
        plusButton.setFocusPainted(false);
        plusButton.setBorderPainted(false);
        plusButton.addActionListener(e -> increaseQuantity(orderItem));
        
        quantityPanel.add(minusButton);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(plusButton);
        
        itemPanel.add(infoPanel, BorderLayout.WEST);
        itemPanel.add(quantityPanel, BorderLayout.EAST);
        
        return itemPanel;
    }
    
    private void increaseQuantity(OrderItem orderItem) {
        orderItem.quantity++;
        updateOrderDisplay();
    }
    
    private void decreaseQuantity(OrderItem orderItem) {
        if (orderItem.quantity > 1) {
            orderItem.quantity--;
        } else {
            orderItems.remove(orderItem);
        }
        updateOrderDisplay();
    }
    
    private void updateTotal() {
        double total = 0;
        for (OrderItem orderItem : orderItems) {
            total += orderItem.menuItem.price * orderItem.quantity;
        }
        totalLabel.setText(currencyFormat.format(total));
    }

    private int findMenuIdByName(String menuName) {
        try {
            for (MenuItem item : menuItems) {
                if (item.name.equals(menuName) && item.menuId > 0) {
                    return item.menuId;
                }
            }
            
            List<Menu> allMenus = menuDAO.findAll();
            for (Menu menu : allMenus) {
                if (menu.getNamaMenu().equals(menuName)) {
                    return menu.getIdMenu();
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding menu ID: " + e.getMessage());
        }
        return -1;
    }

    private void proceedToPayment(int orderId, double total) {
        List<OrderItem> savedOrderItems = new ArrayList<>(orderItems);
        
        PaymentMethodDialog paymentDialog = new PaymentMethodDialog(this, orderId, total, savedOrderItems);
        paymentDialog.setVisible(true);

        String metodeBayar = paymentDialog.getSelectedPaymentMethod();

        if (metodeBayar != null) {
            orderItems.clear();
            updateOrderDisplay();
        }
    }
    
    private void cancelOrder(int orderId, String cancelReason) {
        try {
            if (customerOrderDAO.updateStatus(orderId, "dibatalkan")) {
                PesananDibatalkan pesananBatal = new PesananDibatalkan(
                    orderId,
                    new Timestamp(System.currentTimeMillis()),
                    cancelReason
                );
                
                if (pesananDibatalkanDAO.create(pesananBatal)) {
                    JOptionPane.showMessageDialog(this,
                        "Order cancelled successfully!\n" +
                        "Order ID: " + orderId + "\n" +
                        "Reason: " + cancelReason,
                        "Order Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Order cancelled, but failed to log cancellation.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to cancel order!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error cancelling order: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void checkout() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = orderItems.stream()
            .mapToDouble(item -> item.menuItem.price * item.quantity)
            .sum();

        // Create modern checkout dialog
        JDialog checkoutDialog = new JDialog(this, "Checkout", true);
        checkoutDialog.setSize(500, 600);
        checkoutDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(CARD_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header
        JLabel headerLabel = new JLabel("Complete Your Order");
        headerLabel.setFont(new Font("Inter", Font.BOLD, 24));
        headerLabel.setForeground(TEXT_PRIMARY);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Order summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(BACKGROUND_COLOR);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel summaryTitle = new JLabel("Order Summary");
        summaryTitle.setFont(new Font("Inter", Font.BOLD, 16));
        summaryTitle.setForeground(TEXT_PRIMARY);
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.add(summaryTitle);
        summaryPanel.add(Box.createVerticalStrut(15));
        
        for (OrderItem item : orderItems) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setBackground(BACKGROUND_COLOR);
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            
            JLabel itemLabel = new JLabel(item.menuItem.name + " x" + item.quantity);
            itemLabel.setFont(new Font("Inter", Font.BOLD, 14));
            itemLabel.setForeground(TEXT_PRIMARY);
            
            JLabel itemPrice = new JLabel(currencyFormat.format(item.menuItem.price * item.quantity));
            itemPrice.setFont(new Font("Inter", Font.BOLD, 14));
            itemPrice.setForeground(TEXT_SECONDARY);
            
            itemPanel.add(itemLabel, BorderLayout.WEST);
            itemPanel.add(itemPrice, BorderLayout.EAST);
            summaryPanel.add(itemPanel);
            summaryPanel.add(Box.createVerticalStrut(8));
        }
        
        // Total
        JSeparator separator = new JSeparator();
        summaryPanel.add(separator);
        summaryPanel.add(Box.createVerticalStrut(10));
        
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel totalTextLabel = new JLabel("Total");
        totalTextLabel.setFont(new Font("Inter", Font.BOLD, 18));
        totalTextLabel.setForeground(TEXT_PRIMARY);
        
        JLabel totalValueLabel = new JLabel(currencyFormat.format(total));
        totalValueLabel.setFont(new Font("Inter", Font.BOLD, 18));
        totalValueLabel.setForeground(ACCENT_COLOR);
        
        totalPanel.add(totalTextLabel, BorderLayout.WEST);
        totalPanel.add(totalValueLabel, BorderLayout.EAST);
        summaryPanel.add(totalPanel);
        
        // Customer input
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(CARD_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Customer Name *");
        nameLabel.setFont(new Font("Inter", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);
        inputPanel.add(nameLabel, gbc);
        
        gbc.gridy = 1;
        JTextField customerNameField = new JTextField(20);
        customerNameField.setFont(new Font("Inter", Font.PLAIN, 14));
        customerNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        inputPanel.add(customerNameField, gbc);
        
        gbc.gridy = 2;
        JLabel noteLabel = new JLabel("Notes (Optional)");
        noteLabel.setFont(new Font("Inter", Font.BOLD, 14));
        noteLabel.setForeground(TEXT_PRIMARY);
        inputPanel.add(noteLabel, gbc);
        
        gbc.gridy = 3;
        JTextArea messageArea = new JTextArea(3, 20);
        messageArea.setFont(new Font("Inter", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        JScrollPane noteScrollPane = new JScrollPane(messageArea);
        noteScrollPane.setBorder(messageArea.getBorder());
        inputPanel.add(noteScrollPane, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(CARD_COLOR);
        
        JButton continueButton = createModernButton("Continue to Payment", ACCENT_COLOR, Color.WHITE);
        continueButton.setPreferredSize(new Dimension(180, 45));
        
        JButton cancelButton = createModernButton("Cancel Order", DANGER_COLOR, Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(130, 45));
        
        JButton backButton = createModernButton("Back", new Color(107, 114, 128), Color.WHITE);
        backButton.setPreferredSize(new Dimension(80, 45));
        
        buttonPanel.add(backButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(continueButton);
        
        final boolean[] dialogResult = {false};
        final boolean[] shouldCancel = {false};
        
        continueButton.addActionListener(e -> {
            String customerName = customerNameField.getText().trim();
            if (customerName.isEmpty()) {
                JOptionPane.showMessageDialog(checkoutDialog,
                    "Customer name is required!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                customerNameField.requestFocus();
                return;
            }
            dialogResult[0] = true;
            checkoutDialog.dispose();
        });
        
        cancelButton.addActionListener(e -> {
            String[] cancelOptions = {
                "Changed mind",
                "Too expensive", 
                "Wrong order",
                "Taking too long",
                "Other..."
            };
            
            String selectedReason = (String) JOptionPane.showInputDialog(
                checkoutDialog,
                "Select cancellation reason:",
                "Cancellation Reason",
                JOptionPane.QUESTION_MESSAGE,
                null,
                cancelOptions,
                cancelOptions[0]
            );
            
            if (selectedReason != null) {
                if ("Other...".equals(selectedReason)) {
                    selectedReason = JOptionPane.showInputDialog(
                        checkoutDialog,
                        "Enter cancellation reason:",
                        "Cancellation Reason",
                        JOptionPane.PLAIN_MESSAGE
                    );
                    
                    if (selectedReason == null || selectedReason.trim().isEmpty()) {
                        selectedReason = "No reason provided";
                    }
                }
                
                shouldCancel[0] = true;
                orderItems.clear();
                updateOrderDisplay();
                
                JOptionPane.showMessageDialog(checkoutDialog,
                    "Order cancelled.\nReason: " + selectedReason,
                    "Order Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
                
                checkoutDialog.dispose();
            }
        });
        
        backButton.addActionListener(e -> {
            dialogResult[0] = false;
            checkoutDialog.dispose();
        });
        
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.add(summaryPanel, BorderLayout.NORTH);
        contentPanel.add(inputPanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        checkoutDialog.setContentPane(mainPanel);
        checkoutDialog.setVisible(true);
        
        if (shouldCancel[0]) {
            return;
        }
        
        if (!dialogResult[0]) {
            return;
        }

        String customerName = customerNameField.getText().trim();
        String messageOptional = messageArea.getText().trim();

        // Save customer to database
        int customerId = saveOrUpdateCustomer(customerName);
        if (customerId <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Failed to save customer data!", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentUserId = customerId;
        currentCustomerName = customerName;
        updateHeaderWithCustomerName();

        // Save order to database
        try {
            CustomerOrder customerOrder = new CustomerOrder(
                new Timestamp(System.currentTimeMillis()),
                total,
                messageOptional.isEmpty() ? null : messageOptional,
                customerId,
                "pending"
            );
            
            int orderId = customerOrderDAO.create(customerOrder);
            if (orderId <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to save order!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Save order details
            boolean allDetailsSaved = true;
            for (OrderItem orderItem : orderItems) {
                int menuId = findMenuIdByName(orderItem.menuItem.name);
                if (menuId <= 0) {
                    System.err.println("Menu ID not found for: " + orderItem.menuItem.name);
                    continue;
                }
                
                OrderDetail orderDetail = new OrderDetail(
                    orderId,
                    menuId,
                    orderItem.quantity,
                    (double) orderItem.menuItem.price
                );
                
                if (!orderDetailDAO.create(orderDetail)) {
                    allDetailsSaved = false;
                    System.err.println("Failed to save order detail for: " + orderItem.menuItem.name);
                }
            }
            
            if (!allDetailsSaved) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Some order details failed to save.\n" +
                    "Do you want to cancel this order?",
                    "Error Saving Details",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    cancelOrder(orderId, "Failed to save order details");
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Order saved successfully!\nOrder ID: " + orderId + 
                    "\nCustomer: " + customerName, 
                    "Order Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // Proceed to payment
            proceedToPayment(orderId, total);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error saving order: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int saveOrUpdateCustomer(String customerName) {
        try {
            List<User> allUsers = userDAO.findAll();
            for (User user : allUsers) {
                if (user.getNama().equalsIgnoreCase(customerName) && 
                    "customer".equalsIgnoreCase(user.getRole())) {
                    return user.getUserId();
                }
            }
            
            String username = generateCustomerUsername(customerName);
            
            User newCustomer = new User(
                username,
                "default123",
                customerName,
                "customer"
            );
            
            if (userDAO.create(newCustomer)) {
                User createdUser = userDAO.findByUsername(username);
                if (createdUser != null) {
                    return createdUser.getUserId();
                }
            }
            
            return -1;
            
        } catch (Exception e) {
            System.err.println("Error saving customer: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    private String generateCustomerUsername(String customerName) {
        String baseUsername = customerName.toLowerCase().replaceAll("\\s+", "");
        long timestamp = System.currentTimeMillis();
        String timestampSuffix = String.valueOf(timestamp).substring(8);
        return "cust_" + baseUsername + "_" + timestampSuffix;
    }

    private void updateHeaderWithCustomerName() {
        if (welcomeLabel != null && currentCustomerName != null && !currentCustomerName.isEmpty()) {
            welcomeLabel.setText("Welcome back, " + currentCustomerName);
            welcomeLabel.revalidate();
            welcomeLabel.repaint();
        }
    }

    // PaymentMethodDialog class with improved error handling
    class PaymentMethodDialog extends JDialog {
        private int orderId;
        private double totalAmount;
        private String selectedPaymentMethod;
        private CustomerFrame parentFrame;
        private List<OrderItem> savedOrderItems;

        public PaymentMethodDialog(Frame parent, int orderId, double totalAmount, List<OrderItem> savedOrderItems) {
            super(parent, "Payment Method", true);
            this.orderId = orderId;
            this.totalAmount = totalAmount;
            this.parentFrame = (CustomerFrame) parent;
            this.savedOrderItems = new ArrayList<>(savedOrderItems);

            setSize(450, 350);
            setLocationRelativeTo(parent);
            
            setupPaymentDialog();
        }
        
        private void setupPaymentDialog() {
            JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
            mainPanel.setBackground(CARD_COLOR);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            
            // Header
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(CARD_COLOR);
            
            JLabel titleLabel = new JLabel("Choose Payment Method");
            titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
            titleLabel.setForeground(TEXT_PRIMARY);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JLabel infoLabel = new JLabel(
                "<html><center>Order ID: " + orderId + 
                "<br>Total: " + currencyFormat.format(totalAmount) + "</center></html>");
            infoLabel.setFont(new Font("Inter", Font.BOLD, 14));
            infoLabel.setForeground(TEXT_SECONDARY);
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            headerPanel.add(titleLabel, BorderLayout.NORTH);
            headerPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
            headerPanel.add(infoLabel, BorderLayout.SOUTH);
            
            // Payment buttons
            JPanel paymentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            paymentPanel.setBackground(CARD_COLOR);
            
            JButton cashButton = createPaymentButton("💵 Cash", "Pay with cash at counter");
            cashButton.addActionListener(e -> {
                selectedPaymentMethod = "cash";
                processCashPayment();
                dispose();
            });
            
            JButton qrisButton = createPaymentButton("📱 QRIS", "Pay with digital wallet");
            qrisButton.addActionListener(e -> {
                selectedPaymentMethod = "qris";
                showQRISPopup();
            });
            
            paymentPanel.add(cashButton);
            paymentPanel.add(qrisButton);
            
            // Bottom buttons
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            bottomPanel.setBackground(CARD_COLOR);
            
            JButton cancelOrderButton = createModernButton("Cancel Order", DANGER_COLOR, Color.WHITE);
            cancelOrderButton.addActionListener(e -> showCancelOrderDialog());
            
            JButton backButton = createModernButton("Back", new Color(107, 114, 128), Color.WHITE);
            backButton.addActionListener(e -> {
                selectedPaymentMethod = null;
                dispose();
            });
            
            bottomPanel.add(backButton);
            bottomPanel.add(cancelOrderButton);
            
            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(paymentPanel, BorderLayout.CENTER);
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
            
            setContentPane(mainPanel);
        }
        
        private JButton createPaymentButton(String title, String description) {
            JPanel buttonContent = new JPanel();
            buttonContent.setLayout(new BoxLayout(buttonContent, BoxLayout.Y_AXIS));
            buttonContent.setBackground(BACKGROUND_COLOR);
            buttonContent.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Inter", Font.BOLD, 18));
            titleLabel.setForeground(TEXT_PRIMARY);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel descLabel = new JLabel(description);
            descLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            descLabel.setForeground(TEXT_SECONDARY);
            descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            buttonContent.add(titleLabel);
            buttonContent.add(Box.createVerticalStrut(8));
            buttonContent.add(descLabel);
            
            JButton button = new JButton();
            button.setLayout(new BorderLayout());
            button.add(buttonContent, BorderLayout.CENTER);
            button.setBackground(BACKGROUND_COLOR);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            // Hover effect
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                }
            });
            
            return button;
        }

        private void processCashPayment() {
            try {
                // Validate database connection
                if (pembayaranDAO == null) {
                    throw new Exception("Payment DAO is not initialized");
                }
                
                System.out.println("Processing cash payment for order ID: " + orderId);
                
                // Update order status
                boolean statusUpdated = customerOrderDAO.updateStatus(orderId, "menunggu_pembayaran");
                if (!statusUpdated) {
                    throw new Exception("Failed to update order status");
                }
                
                // Create payment record
                Pembayaran pembayaran = new Pembayaran(
                    orderId,
                    1, // Default cashier ID
                    new Timestamp(System.currentTimeMillis()),
                    selectedPaymentMethod,
                    totalAmount,
                    "menunggu"
                );
                
                boolean paymentCreated = pembayaranDAO.create(pembayaran);
                if (!paymentCreated) {
                    throw new Exception("Failed to create payment record");
                }
                
                // Create nota
                if (notaDAO != null) {
                    Nota nota = new Nota(
                        orderId,
                        new Timestamp(System.currentTimeMillis()),
                        totalAmount,
                        selectedPaymentMethod,
                        "menunggu"
                    );
                    
                    boolean notaCreated = notaDAO.create(nota);
                    if (notaCreated) {
                        Nota createdNota = notaDAO.findByOrderId(orderId);
                        if (createdNota != null) {
                            showCashPaymentPending(createdNota.getIdNota());
                        } else {
                            showCashPaymentPending(orderId);
                        }
                    } else {
                        showCashPaymentPending(orderId);
                        JOptionPane.showMessageDialog(this, 
                            "Payment processed but receipt creation failed.", 
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    showCashPaymentPending(orderId);
                }
                
            } catch (Exception e) {
                System.err.println("Error processing cash payment: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error processing payment: " + e.getMessage(), 
                    "Payment Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showCashPaymentPending(int notaId) {
            JDialog pendingDialog = new JDialog(this, "Cash Payment - Awaiting Confirmation", true);
            pendingDialog.setSize(500, 450);
            pendingDialog.setLocationRelativeTo(this);

            JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
            contentPanel.setBackground(CARD_COLOR);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            // Status header
            JPanel statusPanel = new JPanel(new BorderLayout());
            statusPanel.setBackground(WARNING_COLOR.brighter());
            statusPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel statusLabel = new JLabel("⏳ AWAITING CASHIER CONFIRMATION", SwingConstants.CENTER);
            statusLabel.setFont(new Font("Inter", Font.BOLD, 18));
            statusLabel.setForeground(new Color(146, 64, 14));
            
            JLabel subtitleLabel = new JLabel("Please show this Receipt ID to the cashier", SwingConstants.CENTER);
            subtitleLabel.setFont(new Font("Inter", Font.BOLD, 14));
            subtitleLabel.setForeground(new Color(120, 53, 15));
            
            statusPanel.add(statusLabel, BorderLayout.NORTH);
            statusPanel.add(subtitleLabel, BorderLayout.SOUTH);

            // Receipt ID display
            JPanel receiptPanel = new JPanel(new BorderLayout());
            receiptPanel.setBackground(new Color(254, 243, 199));
            receiptPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNING_COLOR, 3),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
            ));
            
            JLabel receiptLabel = new JLabel("RECEIPT ID", SwingConstants.CENTER);
            receiptLabel.setFont(new Font("Inter", Font.BOLD, 16));
            receiptLabel.setForeground(new Color(146, 64, 14));
            
            JLabel receiptIdLabel = new JLabel(String.valueOf(notaId), SwingConstants.CENTER);
            receiptIdLabel.setFont(new Font("Inter", Font.BOLD, 48));
            receiptIdLabel.setForeground(WARNING_COLOR);
            
            receiptPanel.add(receiptLabel, BorderLayout.NORTH);
            receiptPanel.add(receiptIdLabel, BorderLayout.CENTER);

            // Order details
            JPanel detailPanel = new JPanel();
            detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
            detailPanel.setBackground(BACKGROUND_COLOR);
            detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Order Details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            String[] details = {
                "Customer: " + currentCustomerName,
                "Order ID: " + orderId,
                "Total: " + currencyFormat.format(totalAmount),
                "Method: Cash"
            };
            
            for (String detail : details) {
                JLabel detailLabel = new JLabel(detail);
                detailLabel.setFont(new Font("Inter", Font.BOLD, 14));
                detailLabel.setForeground(TEXT_PRIMARY);
                detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailPanel.add(detailLabel);
                detailPanel.add(Box.createVerticalStrut(5));
            }

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonPanel.setBackground(CARD_COLOR);
            
            JButton printButton = createModernButton("Print Receipt", SECONDARY_COLOR, Color.WHITE);
            printButton.addActionListener(e -> printReceiptId(notaId));
            
            JButton closeButton = createModernButton("Close", new Color(107, 114, 128), Color.WHITE);
            closeButton.addActionListener(e -> {
                pendingDialog.dispose();
                parentFrame.updateHeaderWithCustomerName();
            });
            
            buttonPanel.add(printButton);
            buttonPanel.add(closeButton);

            contentPanel.add(statusPanel, BorderLayout.NORTH);
            contentPanel.add(receiptPanel, BorderLayout.CENTER);
            
            JPanel bottomPanel = new JPanel(new BorderLayout(0, 15));
            bottomPanel.setBackground(CARD_COLOR);
            bottomPanel.add(detailPanel, BorderLayout.NORTH);
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            contentPanel.add(bottomPanel, BorderLayout.SOUTH);

            pendingDialog.setContentPane(contentPanel);
            pendingDialog.setVisible(true);
        }

        private void printReceiptId(int notaId) {
            try {
                // Create receipt content
                StringBuilder receipt = new StringBuilder();
                receipt.append("========================================\n");
                receipt.append("         DAPUR ARUNIKA\n");
                receipt.append("     CASH PAYMENT RECEIPT\n");
                receipt.append("========================================\n\n");
                receipt.append("RECEIPT ID: ").append(notaId).append("\n");
                receipt.append("ORDER ID: ").append(orderId).append("\n");
                receipt.append("CUSTOMER: ").append(currentCustomerName).append("\n");
                receipt.append("DATE: ").append(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
                receipt.append("----------------------------------------\n");
                receipt.append("TOTAL AMOUNT: ").append(currencyFormat.format(totalAmount)).append("\n");
                receipt.append("METHOD: CASH\n");
                receipt.append("STATUS: AWAITING CASHIER CONFIRMATION\n");
                receipt.append("----------------------------------------\n\n");
                receipt.append("INSTRUCTIONS:\n");
                receipt.append("1. Show this receipt to cashier\n");
                receipt.append("2. Make cash payment\n");
                receipt.append("3. Wait for cashier confirmation\n\n");
                receipt.append("========================================\n");
                receipt.append("       Thank you!\n");
                receipt.append("========================================");

                // Show print dialog
                JDialog printDialog = new JDialog(this, "Print Receipt", true);
                printDialog.setSize(400, 500);
                printDialog.setLocationRelativeTo(this);

                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBackground(CARD_COLOR);
                contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JTextArea textArea = new JTextArea(receipt.toString());
                textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
                textArea.setEditable(false);
                textArea.setBackground(BACKGROUND_COLOR);
                textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(360, 400));

                JPanel buttonPanel = new JPanel(new FlowLayout());
                buttonPanel.setBackground(CARD_COLOR);
                
                JButton printBtn = createModernButton("Print", ACCENT_COLOR, Color.WHITE);
                printBtn.addActionListener(e -> {
                    try {
                        boolean printed = textArea.print();
                        if (printed) {
                            JOptionPane.showMessageDialog(printDialog, 
                                "Receipt printed successfully!", 
                                "Print Success", JOptionPane.INFORMATION_MESSAGE);
                            printDialog.dispose();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(printDialog, 
                            "Print error: " + ex.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                
                JButton cancelBtn = createModernButton("Cancel", new Color(107, 114, 128), Color.WHITE);
                cancelBtn.addActionListener(e -> printDialog.dispose());
                
                buttonPanel.add(printBtn);
                buttonPanel.add(cancelBtn);

                contentPanel.add(new JLabel("Receipt Preview:", SwingConstants.CENTER), BorderLayout.NORTH);
                contentPanel.add(scrollPane, BorderLayout.CENTER);
                contentPanel.add(buttonPanel, BorderLayout.SOUTH);

                printDialog.setContentPane(contentPanel);
                printDialog.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error creating receipt: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showQRISPopup() {
            JDialog qrisDialog = new JDialog(this, "QRIS Payment", true);
            qrisDialog.setSize(400, 550);
            qrisDialog.setLocationRelativeTo(this);

            JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
            contentPanel.setBackground(CARD_COLOR);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            // Header
            JLabel headerLabel = new JLabel("Scan QR Code to Pay");
            headerLabel.setFont(new Font("Inter", Font.BOLD, 20));
            headerLabel.setForeground(TEXT_PRIMARY);
            headerLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // QR Code placeholder
            JPanel qrPanel = new JPanel(new BorderLayout());
            qrPanel.setBackground(BACKGROUND_COLOR);
            qrPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 2),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
            ));
            qrPanel.setPreferredSize(new Dimension(250, 250));
            
            JLabel qrLabel = new JLabel("📱", SwingConstants.CENTER);
            qrLabel.setFont(new Font("Arial", Font.PLAIN, 80));
            qrLabel.setForeground(TEXT_SECONDARY);
            
            JLabel qrText = new JLabel("QR CODE", SwingConstants.CENTER);
            qrText.setFont(new Font("Inter", Font.BOLD, 16));
            qrText.setForeground(TEXT_SECONDARY);
            
            qrPanel.add(qrLabel, BorderLayout.CENTER);
            qrPanel.add(qrText, BorderLayout.SOUTH);

            // Payment details
            JPanel detailPanel = new JPanel();
            detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
            detailPanel.setBackground(CARD_COLOR);
            
            JLabel orderLabel = new JLabel("Order ID: " + orderId, SwingConstants.CENTER);
            orderLabel.setFont(new Font("Inter", Font.BOLD, 14));
            orderLabel.setForeground(TEXT_SECONDARY);
            
            JLabel totalLabel = new JLabel("Total: " + currencyFormat.format(totalAmount), SwingConstants.CENTER);
            totalLabel.setFont(new Font("Inter", Font.BOLD, 18));
            totalLabel.setForeground(ACCENT_COLOR);
            
            JLabel instructionLabel = new JLabel(
                "<html><center>Scan the QR code above using your<br>QRIS-enabled mobile banking app</center></html>", 
                SwingConstants.CENTER);
            instructionLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            instructionLabel.setForeground(TEXT_SECONDARY);
            
            detailPanel.add(orderLabel);
            detailPanel.add(Box.createVerticalStrut(5));
            detailPanel.add(totalLabel);
            detailPanel.add(Box.createVerticalStrut(15));
            detailPanel.add(instructionLabel);

            // Buttons
            JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
            buttonPanel.setBackground(CARD_COLOR);
            
            JButton completeButton = createModernButton("Payment Complete", ACCENT_COLOR, Color.WHITE);
            completeButton.setPreferredSize(new Dimension(200, 45));
            completeButton.addActionListener(e -> {
                qrisDialog.dispose();
                processQRISPayment();
                dispose();
            });
            
            JButton cancelQrisButton = createModernButton("Cancel Payment", DANGER_COLOR, Color.WHITE);
            cancelQrisButton.setPreferredSize(new Dimension(200, 40));
            cancelQrisButton.addActionListener(e -> {
                qrisDialog.dispose();
                showCancelOrderDialog();
            });

            buttonPanel.add(completeButton);
            buttonPanel.add(cancelQrisButton);

            contentPanel.add(headerLabel, BorderLayout.NORTH);
            contentPanel.add(qrPanel, BorderLayout.CENTER);
            
            JPanel bottomPanel = new JPanel(new BorderLayout(0, 20));
            bottomPanel.setBackground(CARD_COLOR);
            bottomPanel.add(detailPanel, BorderLayout.NORTH);
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            contentPanel.add(bottomPanel, BorderLayout.SOUTH);

            qrisDialog.setContentPane(contentPanel);
            qrisDialog.setVisible(true);
        }

        private void processQRISPayment() {
            try {
                // Update order status
                customerOrderDAO.updateStatus(orderId, "selesai");
                
                // Create payment record
                Pembayaran pembayaran = new Pembayaran(
                    orderId,
                    1, // Default cashier ID for QRIS
                    new Timestamp(System.currentTimeMillis()),
                    selectedPaymentMethod,
                    totalAmount,
                    "berhasil"
                );
                
                if (pembayaranDAO.create(pembayaran)) {
                    // Create nota
                    Nota nota = new Nota(
                        orderId,
                        new Timestamp(System.currentTimeMillis()),
                        totalAmount,
                        selectedPaymentMethod,
                        "berhasil"
                    );
                    
                    if (notaDAO.create(nota)) {
                        showSuccessReceipt();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Payment successful but receipt creation failed.", 
                            "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to process payment!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error processing QRIS payment: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showSuccessReceipt() {
            JDialog successDialog = new JDialog(this, "Payment Successful", true);
            successDialog.setSize(450, 600);
            successDialog.setLocationRelativeTo(this);
            
            JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
            mainPanel.setBackground(CARD_COLOR);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            
            // Success header
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(ACCENT_COLOR.brighter());
            headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel successLabel = new JLabel("✓ PAYMENT SUCCESSFUL", SwingConstants.CENTER);
            successLabel.setFont(new Font("Inter", Font.BOLD, 20));
            successLabel.setForeground(new Color(6, 78, 59));
            
            JLabel thankLabel = new JLabel("Thank you for your order!", SwingConstants.CENTER);
            thankLabel.setFont(new Font("Inter", Font.BOLD, 14));
            thankLabel.setForeground(new Color(6, 78, 59));
            
            headerPanel.add(successLabel, BorderLayout.NORTH);
            headerPanel.add(thankLabel, BorderLayout.SOUTH);
            
            // Order summary
            JPanel summaryPanel = new JPanel();
            summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
            summaryPanel.setBackground(BACKGROUND_COLOR);
            summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Order Summary"),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            
            String[] orderInfo = {
                "Customer: " + currentCustomerName,
                "Order ID: " + orderId,
                "Payment Method: " + selectedPaymentMethod.toUpperCase(),
                "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            };
            
            for (String info : orderInfo) {
                JLabel infoLabel = new JLabel(info);
                infoLabel.setFont(new Font("Inter", Font.BOLD, 14));
                infoLabel.setForeground(TEXT_PRIMARY);
                infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                summaryPanel.add(infoLabel);
                summaryPanel.add(Box.createVerticalStrut(8));
            }
            
            // Order items
            for (OrderItem item : savedOrderItems) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBackground(BACKGROUND_COLOR);
                itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                
                JLabel itemLabel = new JLabel(item.menuItem.name + " x" + item.quantity);
                itemLabel.setFont(new Font("Inter", Font.PLAIN, 12));
                itemLabel.setForeground(TEXT_SECONDARY);
                
                JLabel itemPrice = new JLabel(currencyFormat.format(item.menuItem.price * item.quantity));
                itemPrice.setFont(new Font("Inter", Font.PLAIN, 12));
                itemPrice.setForeground(TEXT_SECONDARY);
                
                itemPanel.add(itemLabel, BorderLayout.WEST);
                itemPanel.add(itemPrice, BorderLayout.EAST);
                summaryPanel.add(itemPanel);
                summaryPanel.add(Box.createVerticalStrut(5));
            }
            
            // Total
            JSeparator separator = new JSeparator();
            summaryPanel.add(separator);
            summaryPanel.add(Box.createVerticalStrut(10));
            
            JPanel totalPanel = new JPanel(new BorderLayout());
            totalPanel.setBackground(BACKGROUND_COLOR);
            
            JLabel totalTextLabel = new JLabel("TOTAL PAID");
            totalTextLabel.setFont(new Font("Inter", Font.BOLD, 16));
            totalTextLabel.setForeground(TEXT_PRIMARY);
            
            JLabel totalValueLabel = new JLabel(currencyFormat.format(totalAmount));
            totalValueLabel.setFont(new Font("Inter", Font.BOLD, 16));
            totalValueLabel.setForeground(ACCENT_COLOR);
            
            totalPanel.add(totalTextLabel, BorderLayout.WEST);
            totalPanel.add(totalValueLabel, BorderLayout.EAST);
            summaryPanel.add(totalPanel);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            buttonPanel.setBackground(CARD_COLOR);
            
            JButton printButton = createModernButton("Print Receipt", SECONDARY_COLOR, Color.WHITE);
            printButton.addActionListener(e -> {
                successDialog.dispose();
                printFinalReceipt();
            });
            
            JButton closeButton = createModernButton("Close", ACCENT_COLOR, Color.WHITE);
            closeButton.addActionListener(e -> {
                successDialog.dispose();
                parentFrame.updateHeaderWithCustomerName();
            });
            
            buttonPanel.add(printButton);
            buttonPanel.add(closeButton);

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(summaryPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            successDialog.setContentPane(mainPanel);
            successDialog.setVisible(true);
        }

        private void printFinalReceipt() {
            // Implementation for printing final receipt
            JOptionPane.showMessageDialog(this, 
                "Receipt printing feature will be implemented.", 
                "Print Receipt", JOptionPane.INFORMATION_MESSAGE);
        }

        private void showCancelOrderDialog() {
            String[] cancelOptions = {
                "Changed mind about payment",
                "Payment method not available", 
                "Taking too long",
                "Want to modify order",
                "Other..."
            };
            
            String selectedReason = (String) JOptionPane.showInputDialog(
                this,
                "Select cancellation reason:",
                "Cancel Order",
                JOptionPane.QUESTION_MESSAGE,
                null,
                cancelOptions,
                cancelOptions[0]
            );
            
            if (selectedReason != null) {
                if ("Other...".equals(selectedReason)) {
                    selectedReason = JOptionPane.showInputDialog(
                        this,
                        "Enter cancellation reason:",
                        "Cancellation Reason",
                        JOptionPane.PLAIN_MESSAGE
                    );
                    
                    if (selectedReason == null || selectedReason.trim().isEmpty()) {
                        selectedReason = "Cancelled during payment";
                    }
                }
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel this order?\n" +
                    "Order ID: " + orderId + "\n" +
                    "Total: " + currencyFormat.format(totalAmount) + "\n" +
                    "Reason: " + selectedReason,
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    parentFrame.cancelOrder(orderId, selectedReason);
                    dispose();
                }
            }
        }

        public String getSelectedPaymentMethod() {
            return selectedPaymentMethod;
        }
    }

    private void setupEventHandlers() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
            }
        });
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", 
            "Confirm Logout", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new CustomerFrame().setVisible(true);
        });
    }
}