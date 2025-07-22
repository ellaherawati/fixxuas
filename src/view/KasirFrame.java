package view;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class KasirFrame extends JFrame {
    private RestaurantDAO restaurantDAO;
    private PembayaranDAO pembayaranDAO;
    private NotaDAO notaDAO;
    private CustomerOrderDAO customerOrderDAO;
    
    // Components
    private JTextField idNotaField;
    private JButton cariButton, clearButton, printButton, exitButton, confirmPaymentButton;
    private JLabel statusLabel;
    
    // Info Nota Panel
    private JLabel noNotaLabel, waktuCetakLabel, idPesananLabel;
    private JLabel metodePembayaranNotaLabel, statusPembayaranNotaLabel, totalPembayaranNotaLabel;
    
    // Info Pesanan Panel
    private JLabel customerNameLabel, tanggalPesananLabel, statusPesananLabel;
    private JLabel totalPesananLabel, catatanLabel;
    
    // Info Pembayaran Panel
    private JLabel idPembayaranLabel, kasirLabel, tanggalPembayaranLabel;
    private JLabel metodePembayaranLabel, jumlahPembayaranLabel, statusPembayaranLabel;
    
    // Detail Pesanan Table
    private JTable detailTable;
    private DefaultTableModel tableModel;
    
    // Current data
    private Nota currentNota;
    private CustomerOrder currentOrder;
    private Pembayaran currentPembayaran;
    private List<OrderDetail> currentDetails;
    
    // Formatters
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public KasirFrame() {
        restaurantDAO = new RestaurantDAO();
        pembayaranDAO = new PembayaranDAO();
        notaDAO = new NotaDAO();
        customerOrderDAO = new CustomerOrderDAO();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        clearAllData();
        
        setTitle("Sistem Kasir - Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initializeComponents() {
        // Search Panel Components
        idNotaField = new JTextField(15);
        idNotaField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        cariButton = new JButton("Cari Nota");
        clearButton = new JButton("Clear");
        printButton = new JButton("Print Nota");
        confirmPaymentButton = new JButton("Konfirmasi Pembayaran Cash"); // New button
        exitButton = new JButton("Keluar");
        statusLabel = new JLabel("Siap untuk mencari nota...");
        statusLabel.setForeground(Color.BLUE);
        
        // Info Nota Labels
        noNotaLabel = new JLabel("-");
        waktuCetakLabel = new JLabel("-");
        idPesananLabel = new JLabel("-");
        metodePembayaranNotaLabel = new JLabel("-");
        statusPembayaranNotaLabel = new JLabel("-");
        totalPembayaranNotaLabel = new JLabel("-");
        
        // Info Pesanan Labels
        customerNameLabel = new JLabel("-");
        tanggalPesananLabel = new JLabel("-");
        statusPesananLabel = new JLabel("-");
        totalPesananLabel = new JLabel("-");
        catatanLabel = new JLabel("-");
        
        // Info Pembayaran Labels
        idPembayaranLabel = new JLabel("-");
        kasirLabel = new JLabel("-");
        tanggalPembayaranLabel = new JLabel("-");
        metodePembayaranLabel = new JLabel("-");
        jumlahPembayaranLabel = new JLabel("-");
        statusPembayaranLabel = new JLabel("-");
        
        // Detail Table
        String[] columnNames = {"No", "Nama Menu", "Harga Satuan", "Jumlah", "Subtotal"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: case 3: return Integer.class;
                    case 2: case 4: return Double.class;
                    default: return String.class;
                }
            }
        };
        
        detailTable = new JTable(tableModel);
        detailTable.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        detailTable.setRowHeight(25);
        detailTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Set column widths
        detailTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // No
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Nama Menu
        detailTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Harga
        detailTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Jumlah
        detailTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Subtotal
        
        // Center alignment for numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        detailTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        detailTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        // Currency renderer for price columns
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Double) {
                    setText("Rp " + currencyFormat.format((Double) value));
                } else {
                    setText(value != null ? value.toString() : "");
                }
                setHorizontalAlignment(JLabel.RIGHT);
            }
        };
        detailTable.getColumnModel().getColumn(2).setCellRenderer(currencyRenderer);
        detailTable.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        
        // Button states
        printButton.setEnabled(false);
        confirmPaymentButton.setEnabled(false);
        
        // Style confirm payment button
        confirmPaymentButton.setBackground(new Color(40, 167, 69));
        confirmPaymentButton.setForeground(Color.WHITE);
        confirmPaymentButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        confirmPaymentButton.setFocusPainted(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Top Panel - Search
        JPanel searchPanel = createSearchPanel();
        
        // Center Panel - Info and Details
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Info Panel (3 columns: Nota, Pesanan, Pembayaran)
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        infoPanel.add(createNotaInfoPanel());
        infoPanel.add(createPesananInfoPanel());
        infoPanel.add(createPembayaranInfoPanel());
        
        // Detail Panel
        JPanel detailPanel = createDetailPanel();
        
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(detailPanel, BorderLayout.CENTER);
        
        // Bottom Panel - Status and Actions
        JPanel bottomPanel = createBottomPanel();
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Pencarian Nota", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        panel.add(new JLabel("ID Nota:"));
        panel.add(idNotaField);
        panel.add(cariButton);
        panel.add(clearButton);
        
        return panel;
    }

    private JPanel createNotaInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.BLUE), 
            "Informasi Nota", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        addInfoRow(panel, gbc, 0, "No. Nota:", noNotaLabel);
        addInfoRow(panel, gbc, 1, "Waktu Cetak:", waktuCetakLabel);
        addInfoRow(panel, gbc, 2, "ID Pesanan:", idPesananLabel);
        addInfoRow(panel, gbc, 3, "Metode Bayar:", metodePembayaranNotaLabel);
        addInfoRow(panel, gbc, 4, "Status Bayar:", statusPembayaranNotaLabel);
        addInfoRow(panel, gbc, 5, "Total Bayar:", totalPembayaranNotaLabel);
        
        return panel;
    }

    private JPanel createPesananInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GREEN), 
            "Informasi Pesanan", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        addInfoRow(panel, gbc, 0, "Customer:", customerNameLabel);
        addInfoRow(panel, gbc, 1, "Tgl Pesan:", tanggalPesananLabel);
        addInfoRow(panel, gbc, 2, "Status Pesan:", statusPesananLabel);
        addInfoRow(panel, gbc, 3, "Total Pesan:", totalPesananLabel);
        addInfoRow(panel, gbc, 4, "Catatan:", catatanLabel);
        
        return panel;
    }

    private JPanel createPembayaranInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.ORANGE), 
            "Informasi Pembayaran", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        addInfoRow(panel, gbc, 0, "ID Bayar:", idPembayaranLabel);
        addInfoRow(panel, gbc, 1, "Kasir:", kasirLabel);
        addInfoRow(panel, gbc, 2, "Tgl Bayar:", tanggalPembayaranLabel);
        addInfoRow(panel, gbc, 3, "Metode:", metodePembayaranLabel);
        addInfoRow(panel, gbc, 4, "Jumlah:", jumlahPembayaranLabel);
        addInfoRow(panel, gbc, 5, "Status:", statusPembayaranLabel);
        
        return panel;
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        panel.add(label, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        panel.add(valueLabel, gbc);
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.MAGENTA), 
            "Detail Pesanan", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 12)
        ));
        
        JScrollPane scrollPane = new JScrollPane(detailTable);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(confirmPaymentButton); // Add new button
        buttonPanel.add(printButton);
        buttonPanel.add(exitButton);
        
        panel.add(statusPanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }

    private void setupEventHandlers() {
        // Enter key on ID field
        idNotaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    cariNota();
                }
            }
        });
        
        // Button actions
        cariButton.addActionListener(e -> cariNota());
        clearButton.addActionListener(e -> clearAllData());
        printButton.addActionListener(e -> printNota());
        confirmPaymentButton.addActionListener(e -> confirmCashPayment()); // New action
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Keluar",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
    }

    /**
     * New method to confirm cash payment
     */
    private void confirmCashPayment() {
        if (currentNota == null) {
            JOptionPane.showMessageDialog(this, 
                "Tidak ada nota yang dimuat!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!"cash".equalsIgnoreCase(currentNota.getMetodePembayaran())) {
            JOptionPane.showMessageDialog(this, 
                "Konfirmasi hanya tersedia untuk pembayaran cash!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!"menunggu".equalsIgnoreCase(currentNota.getStatusPembayaran())) {
            JOptionPane.showMessageDialog(this, 
                "Pembayaran sudah dikonfirmasi atau dibatalkan!", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Show confirmation dialog
        String customerName = restaurantDAO.getCustomerName(currentOrder.getCustomerId());
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><body style='width: 300px;'>" +
            "<h3>Konfirmasi Pembayaran Cash</h3>" +
            "<b>ID Nota:</b> " + currentNota.getIdNota() + "<br>" +
            "<b>Customer:</b> " + customerName + "<br>" +
            "<b>Total:</b> Rp " + currencyFormat.format(currentNota.getTotalPembayaran()) + "<br><br>" +
            "Apakah customer sudah memberikan uang pembayaran?" +
            "</body></html>",
            "Konfirmasi Pembayaran Cash",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Get kasir information
            String kasirName = JOptionPane.showInputDialog(this,
                "Masukkan nama kasir yang mengkonfirmasi:",
                "Info Kasir",
                JOptionPane.PLAIN_MESSAGE);
            
            if (kasirName == null || kasirName.trim().isEmpty()) {
                kasirName = "Kasir Default";
            }
            
            try {
                // Update payment status in database
                if (updatePaymentStatusToSuccess()) {
                    // Update display
                    loadNotaData(currentNota.getIdNota());
                    
                    // Show success message
                    JOptionPane.showMessageDialog(this,
                        "<html><body style='width: 250px;'>" +
                        "<h3>✅ Pembayaran Berhasil Dikonfirmasi!</h3>" +
                        "<b>ID Nota:</b> " + currentNota.getIdNota() + "<br>" +
                        "<b>Customer:</b> " + customerName + "<br>" +
                        "<b>Total:</b> Rp " + currencyFormat.format(currentNota.getTotalPembayaran()) + "<br>" +
                        "<b>Dikonfirmasi oleh:</b> " + kasirName + "<br>" +
                        "</body></html>",
                        "Pembayaran Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    statusLabel.setText("Pembayaran berhasil dikonfirmasi!");
                    statusLabel.setForeground(new Color(40, 167, 69));
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Gagal mengkonfirmasi pembayaran! Silakan coba lagi.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error saat mengkonfirmasi pembayaran: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Update payment status to 'berhasil' in database
     */
    private boolean updatePaymentStatusToSuccess() {
        try {
            boolean success = true;
            
            // 1. Update Nota status
            currentNota.setStatusPembayaran("berhasil");
            // Note: You'll need to add an update method to NotaDAO
            
            // 2. Update Pembayaran status
            if (currentPembayaran != null) {
                currentPembayaran.setStatusPembayaran("berhasil");
                currentPembayaran.setTanggalPembayaran(new Timestamp(System.currentTimeMillis()));
                success &= pembayaranDAO.updateStatus(currentPembayaran.getIdPembayaran(), "berhasil");
            }
            
            // 3. Update Customer Order status
            if (currentOrder != null) {
                success &= customerOrderDAO.updateStatus(currentOrder.getIdPesanan(), "selesai");
            }
            
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void cariNota() {
        String idNotaText = idNotaField.getText().trim();
        
        if (idNotaText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan ID Nota terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            idNotaField.requestFocus();
            return;
        }
        
        try {
            int idNota = Integer.parseInt(idNotaText);
            loadNotaData(idNota);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID Nota harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            idNotaField.requestFocus();
        }
    }

    private void loadNotaData(int idNota) {
        // Reset current data
        currentNota = null;
        currentOrder = null;
        currentPembayaran = null;
        currentDetails = null;
        
        statusLabel.setText("Mencari nota...");
        statusLabel.setForeground(Color.ORANGE);
        
        try {
            // Find nota by ID
            currentNota = restaurantDAO.findNotaById(idNota);
            
            if (currentNota == null) {
                statusLabel.setText("Nota dengan ID " + idNota + " tidak ditemukan!");
                statusLabel.setForeground(Color.RED);
                clearDisplayData();
                return;
            }
            
            // Load customer order data
            currentOrder = restaurantDAO.findCustomerOrderById(currentNota.getIdPesanan());
            if (currentOrder == null) {
                statusLabel.setText("Data pesanan tidak ditemukan!");
                statusLabel.setForeground(Color.RED);
                clearDisplayData();
                return;
            }
            
            // Load pembayaran data
            currentPembayaran = restaurantDAO.findPembayaranByPesananId(currentNota.getIdPesanan());
            
            // Load order details
            currentDetails = restaurantDAO.findOrderDetailsByPesananId(currentNota.getIdPesanan());
            
            // Display all data
            displayNotaData();
            displayPesananData();
            displayPembayaranData();
            displayDetailData();
            
            statusLabel.setText("Data nota berhasil dimuat!");
            statusLabel.setForeground(Color.GREEN);
            printButton.setEnabled(true);
            
            // Enable confirm payment button only for cash payments with 'menunggu' status
            if ("cash".equalsIgnoreCase(currentNota.getMetodePembayaran()) && 
                "menunggu".equalsIgnoreCase(currentNota.getStatusPembayaran())) {
                confirmPaymentButton.setEnabled(true);
                statusLabel.setText("Data nota berhasil dimuat! Pembayaran cash menunggu konfirmasi.");
                statusLabel.setForeground(new Color(255, 193, 7)); // Orange for waiting
            } else {
                confirmPaymentButton.setEnabled(false);
            }
            
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            clearDisplayData();
            e.printStackTrace();
        }
    }

    private void displayNotaData() {
        if (currentNota != null) {
            noNotaLabel.setText(String.valueOf(currentNota.getIdNota()));
            waktuCetakLabel.setText(dateFormat.format(currentNota.getWaktuCetak()));
            idPesananLabel.setText(String.valueOf(currentNota.getIdPesanan()));
            metodePembayaranNotaLabel.setText(currentNota.getMetodePembayaran().toUpperCase());
            statusPembayaranNotaLabel.setText(currentNota.getStatusPembayaran().toUpperCase());
            totalPembayaranNotaLabel.setText("Rp " + currencyFormat.format(currentNota.getTotalPembayaran()));
            
            // Color coding for status
            if ("berhasil".equalsIgnoreCase(currentNota.getStatusPembayaran()) || 
                "lunas".equalsIgnoreCase(currentNota.getStatusPembayaran())) {
                statusPembayaranNotaLabel.setForeground(new Color(40, 167, 69)); // Green
            } else if ("gagal".equalsIgnoreCase(currentNota.getStatusPembayaran())) {
                statusPembayaranNotaLabel.setForeground(new Color(220, 53, 69)); // Red
            } else if ("menunggu".equalsIgnoreCase(currentNota.getStatusPembayaran())) {
                statusPembayaranNotaLabel.setForeground(new Color(255, 193, 7)); // Orange
            } else {
                statusPembayaranNotaLabel.setForeground(Color.BLACK);
            }
        }
    }

    private void displayPesananData() {
        if (currentOrder != null) {
            // Get customer name
            String customerName = restaurantDAO.getCustomerName(currentOrder.getCustomerId());
            customerNameLabel.setText(customerName);
            tanggalPesananLabel.setText(dateFormat.format(currentOrder.getTanggalPesanan()));
            statusPesananLabel.setText(currentOrder.getStatusPesanan().toUpperCase());
            totalPesananLabel.setText("Rp " + currencyFormat.format(currentOrder.getTotalPesanan()));
            
            // Handle catatan (notes) - could be null
            String catatan = currentOrder.getCatatan();
            catatanLabel.setText(catatan != null && !catatan.trim().isEmpty() ? catatan : "Tidak ada catatan");
            
            // Color coding for order status
            if ("selesai".equalsIgnoreCase(currentOrder.getStatusPesanan())) {
                statusPesananLabel.setForeground(new Color(40, 167, 69)); // Green
            } else if ("menunggu_pembayaran".equalsIgnoreCase(currentOrder.getStatusPesanan())) {
                statusPesananLabel.setForeground(new Color(255, 193, 7)); // Orange
            } else if ("dibatalkan".equalsIgnoreCase(currentOrder.getStatusPesanan())) {
                statusPesananLabel.setForeground(new Color(220, 53, 69)); // Red
            } else {
                statusPesananLabel.setForeground(Color.BLACK);
            }
        }
    }

    private void displayPembayaranData() {
        if (currentPembayaran != null) {
            idPembayaranLabel.setText(String.valueOf(currentPembayaran.getIdPembayaran()));
            
            // Get kasir name
            String kasirName = restaurantDAO.getKasirName(currentPembayaran.getIdKasir());
            kasirLabel.setText(kasirName);
            
            tanggalPembayaranLabel.setText(dateFormat.format(currentPembayaran.getTanggalPembayaran()));
            metodePembayaranLabel.setText(currentPembayaran.getMetodePembayaran().toUpperCase());
            jumlahPembayaranLabel.setText("Rp " + currencyFormat.format(currentPembayaran.getJumlahPembayaran()));
            statusPembayaranLabel.setText(currentPembayaran.getStatusPembayaran().toUpperCase());
            
            // Color coding for payment status
            if ("berhasil".equalsIgnoreCase(currentPembayaran.getStatusPembayaran()) || 
                "lunas".equalsIgnoreCase(currentPembayaran.getStatusPembayaran())) {
                statusPembayaranLabel.setForeground(new Color(40, 167, 69)); // Green
            } else if ("gagal".equalsIgnoreCase(currentPembayaran.getStatusPembayaran())) {
                statusPembayaranLabel.setForeground(new Color(220, 53, 69)); // Red
            } else if ("menunggu".equalsIgnoreCase(currentPembayaran.getStatusPembayaran())) {
                statusPembayaranLabel.setForeground(new Color(255, 193, 7)); // Orange
            } else {
                statusPembayaranLabel.setForeground(Color.BLACK);
            }
        } else {
            // No payment record found
            idPembayaranLabel.setText("Belum ada");
            kasirLabel.setText("-");
            tanggalPembayaranLabel.setText("-");
            metodePembayaranLabel.setText("-");
            jumlahPembayaranLabel.setText("-");
            statusPembayaranLabel.setText("BELUM DIBAYAR");
            statusPembayaranLabel.setForeground(new Color(220, 53, 69)); // Red
        }
    }

    private void displayDetailData() {
        tableModel.setRowCount(0);
        
        if (currentDetails != null && !currentDetails.isEmpty()) {
            int no = 1;
            for (OrderDetail detail : currentDetails) {
                Object[] rowData = {
                    no++,
                    detail.getNamaMenu(),
                    detail.getHargaSatuan(),
                    detail.getJumlah(),
                    detail.getSubtotal()
                };
                tableModel.addRow(rowData);
            }
        }
    }

    private void clearAllData() {
        idNotaField.setText("");
        clearDisplayData();
        statusLabel.setText("Siap untuk mencari nota...");
        statusLabel.setForeground(Color.BLUE);
        printButton.setEnabled(false);
        confirmPaymentButton.setEnabled(false);
        idNotaField.requestFocus();
    }

    private void clearDisplayData() {
        // Clear nota info
        noNotaLabel.setText("-");
        waktuCetakLabel.setText("-");
        idPesananLabel.setText("-");
        metodePembayaranNotaLabel.setText("-");
        statusPembayaranNotaLabel.setText("-");
        statusPembayaranNotaLabel.setForeground(Color.BLACK);
        totalPembayaranNotaLabel.setText("-");
        
        // Clear pesanan info
        customerNameLabel.setText("-");
        tanggalPesananLabel.setText("-");
        statusPesananLabel.setText("-");
        statusPesananLabel.setForeground(Color.BLACK);
        totalPesananLabel.setText("-");
        catatanLabel.setText("-");
        
        // Clear pembayaran info
        idPembayaranLabel.setText("-");
        kasirLabel.setText("-");
        tanggalPembayaranLabel.setText("-");
        metodePembayaranLabel.setText("-");
        jumlahPembayaranLabel.setText("-");
        statusPembayaranLabel.setText("-");
        statusPembayaranLabel.setForeground(Color.BLACK);
        
        // Clear table
        tableModel.setRowCount(0);
        
        // Reset current data
        currentNota = null;
        currentOrder = null;
        currentPembayaran = null;
        currentDetails = null;
    }

    private void printNota() {
        if (currentNota == null) {
            JOptionPane.showMessageDialog(this, "Tidak ada data nota untuk dicetak!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Build receipt
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== NOTA PEMBAYARAN ==========\n");
        receipt.append("No. Nota: ").append(currentNota.getIdNota()).append("\n");
        receipt.append("Tanggal: ").append(dateFormat.format(currentNota.getWaktuCetak())).append("\n");
        receipt.append("ID Pesanan: ").append(currentNota.getIdPesanan()).append("\n");
        receipt.append("=====================================\n");
        
        // Customer info
        if (currentOrder != null) {
            String customerName = restaurantDAO.getCustomerName(currentOrder.getCustomerId());
            receipt.append("Customer: ").append(customerName).append("\n");
            receipt.append("Tanggal Pesan: ").append(dateFormat.format(currentOrder.getTanggalPesanan())).append("\n");
            if (currentOrder.getCatatan() != null && !currentOrder.getCatatan().trim().isEmpty()) {
                receipt.append("Catatan: ").append(currentOrder.getCatatan()).append("\n");
            }
            receipt.append("=====================================\n");
        }
        
        // Order details
        if (currentDetails != null && !currentDetails.isEmpty()) {
            receipt.append("DETAIL PESANAN:\n");
            receipt.append("-------------------------------------\n");
            for (OrderDetail detail : currentDetails) {
                receipt.append(String.format("%-25s %2dx\n", 
                    detail.getNamaMenu(), detail.getJumlah()));
                receipt.append(String.format("  @Rp%8s = Rp%10s\n",
                    currencyFormat.format(detail.getHargaSatuan()),
                    currencyFormat.format(detail.getSubtotal())
                ));
            }
            receipt.append("-------------------------------------\n");
        }
        
        // Payment info
        receipt.append(String.format("TOTAL PESANAN: Rp %s\n", 
                      currentOrder != null ? currencyFormat.format(currentOrder.getTotalPesanan()) : "0.00"));
        receipt.append(String.format("TOTAL BAYAR: Rp %s\n", 
                      currencyFormat.format(currentNota.getTotalPembayaran())));
        receipt.append("Metode Bayar: ").append(currentNota.getMetodePembayaran().toUpperCase()).append("\n");
        receipt.append("Status: ").append(currentNota.getStatusPembayaran().toUpperCase()).append("\n");
        
        if (currentPembayaran != null) {
            receipt.append("-------------------------------------\n");
            String kasirName = restaurantDAO.getKasirName(currentPembayaran.getIdKasir());
            receipt.append("Kasir: ").append(kasirName).append("\n");
            receipt.append("Tanggal Bayar: ").append(dateFormat.format(currentPembayaran.getTanggalPembayaran())).append("\n");
        }
        
        receipt.append("=====================================\n");
        receipt.append("     Terima kasih atas kunjungan     \n");
        receipt.append("            Anda!                    \n");
        receipt.append("=====================================");
        
        // Show receipt in dialog
        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        textArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        
        int option = JOptionPane.showConfirmDialog(
            this,
            scrollPane,
            "Preview Nota - Apakah ingin mencetak?",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                boolean printed = textArea.print();
                if (printed) {
                    JOptionPane.showMessageDialog(this, "Nota berhasil dicetak!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Pencetakan dibatalkan!", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saat mencetak: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new KasirFrame().setVisible(true);
        });
    }
}