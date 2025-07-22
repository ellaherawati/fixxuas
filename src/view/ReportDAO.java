package view;


import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ReportDAO {
    private Connection connection;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public ReportDAO() {
        this.connection = DatabaseConnection.getConnection();
        if (this.connection == null) {
            System.err.println("Failed to establish database connection!");
        }
    }

    /**
     * Get daily summary report for today
     */
    public Map<String, Object> getDailySummary(Date date) {
        Map<String, Object> result = new HashMap<>();
        String dateStr = dateFormat.format(date);
        
        if (connection == null) {
            return result;
        }

        try {
            // Total orders today
            String orderSql = """
                SELECT 
                    COUNT(*) as total_orders,
                    SUM(CASE WHEN status_pesanan = 'selesai' THEN total_pesanan ELSE 0 END) as total_revenue,
                    SUM(CASE WHEN status_pesanan = 'pending' OR status_pesanan = 'menunggu_pembayaran' THEN 1 ELSE 0 END) as pending_count,
                    SUM(CASE WHEN status_pesanan = 'selesai' THEN 1 ELSE 0 END) as completed_count,
                    SUM(CASE WHEN status_pesanan = 'dibatalkan' THEN 1 ELSE 0 END) as cancelled_count,
                    AVG(CASE WHEN status_pesanan = 'selesai' THEN total_pesanan ELSE NULL END) as avg_order
                FROM Customer_Order 
                WHERE DATE(tanggal_pesanan) = ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(orderSql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    result.put("totalOrders", rs.getInt("total_orders"));
                    result.put("totalRevenue", rs.getDouble("total_revenue"));
                    result.put("pendingCount", rs.getInt("pending_count"));
                    result.put("completedCount", rs.getInt("completed_count"));
                    result.put("cancelledCount", rs.getInt("cancelled_count"));
                    result.put("avgOrder", rs.getDouble("avg_order"));
                }
            }

            // Payment method breakdown
            String paymentSql = """
                SELECT 
                    SUM(CASE WHEN p.metode_pembayaran = 'cash' AND p.status_pembayaran = 'berhasil' THEN p.jumlah_pembayaran ELSE 0 END) as cash_revenue,
                    SUM(CASE WHEN p.metode_pembayaran = 'qris' AND p.status_pembayaran = 'berhasil' THEN p.jumlah_pembayaran ELSE 0 END) as qris_revenue,
                    COUNT(CASE WHEN p.metode_pembayaran = 'cash' THEN 1 END) as cash_count,
                    COUNT(CASE WHEN p.metode_pembayaran = 'qris' THEN 1 END) as qris_count
                FROM Pembayaran p
                JOIN Customer_Order co ON p.id_pesanan = co.id_pesanan
                WHERE DATE(co.tanggal_pesanan) = ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(paymentSql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    result.put("cashRevenue", rs.getDouble("cash_revenue"));
                    result.put("qrisRevenue", rs.getDouble("qris_revenue"));
                    result.put("cashCount", rs.getInt("cash_count"));
                    result.put("qrisCount", rs.getInt("qris_count"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting daily summary: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get payment method summary
     */
    public Map<String, Object> getPaymentMethodSummary() {
        Map<String, Object> result = new HashMap<>();
        String dateStr = dateFormat.format(new Date());
        
        if (connection == null) {
            return result;
        }

        try {
            String sql = """
                SELECT 
                    p.metode_pembayaran,
                    p.status_pembayaran,
                    COUNT(*) as transaction_count,
                    SUM(p.jumlah_pembayaran) as total_amount,
                    AVG(p.jumlah_pembayaran) as avg_amount
                FROM Pembayaran p
                JOIN Customer_Order co ON p.id_pesanan = co.id_pesanan
                WHERE DATE(co.tanggal_pesanan) = ?
                GROUP BY p.metode_pembayaran, p.status_pembayaran
                ORDER BY p.metode_pembayaran, p.status_pembayaran
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                
                List<Map<String, Object>> payments = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> payment = new HashMap<>();
                    payment.put("method", rs.getString("metode_pembayaran"));
                    payment.put("status", rs.getString("status_pembayaran"));
                    payment.put("count", rs.getInt("transaction_count"));
                    payment.put("total", rs.getDouble("total_amount"));
                    payment.put("average", rs.getDouble("avg_amount"));
                    payments.add(payment);
                }
                result.put("payments", payments);
            }

        } catch (SQLException e) {
            System.err.println("Error getting payment method summary: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get pending cash payments
     */
    public List<Map<String, Object>> getPendingCashPayments() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (connection == null) {
            return result;
        }

        try {
            String sql = """
                SELECT 
                    n.id_nota,
                    n.waktu_cetak,
                    n.total_pembayaran,
                    u.nama as customer_name,
                    co.tanggal_pesanan,
                    TIMESTAMPDIFF(MINUTE, n.waktu_cetak, NOW()) as waiting_minutes
                FROM Nota n
                JOIN Customer_Order co ON n.id_pesanan = co.id_pesanan
                JOIN User u ON co.customer_id = u.user_id
                WHERE n.metode_pembayaran = 'cash' 
                AND n.status_pembayaran = 'menunggu'
                ORDER BY n.waktu_cetak ASC
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> pending = new HashMap<>();
                    pending.put("idNota", rs.getInt("id_nota"));
                    pending.put("waktuCetak", rs.getTimestamp("waktu_cetak"));
                    pending.put("totalPembayaran", rs.getDouble("total_pembayaran"));
                    pending.put("customerName", rs.getString("customer_name"));
                    pending.put("tanggalPesanan", rs.getTimestamp("tanggal_pesanan"));
                    pending.put("waitingMinutes", rs.getInt("waiting_minutes"));
                    result.add(pending);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting pending cash payments: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get menu performance for today
     */
    public Map<String, Object> getMenuPerformance() {
        Map<String, Object> result = new HashMap<>();
        String dateStr = dateFormat.format(new Date());
        
        if (connection == null) {
            return result;
        }

        try {
            String sql = """
                SELECT 
                    m.nama_menu,
                    m.jenis_menu,
                    SUM(od.jumlah) as total_sold,
                    SUM(od.jumlah * od.harga_satuan) as total_revenue,
                    AVG(od.harga_satuan) as avg_price,
                    COUNT(DISTINCT od.id_pesanan) as order_count
                FROM Order_Detail od
                JOIN Menu m ON od.id_menu = m.id_menu
                JOIN Customer_Order co ON od.id_pesanan = co.id_pesanan
                WHERE DATE(co.tanggal_pesanan) = ?
                AND co.status_pesanan = 'selesai'
                GROUP BY m.id_menu, m.nama_menu, m.jenis_menu
                ORDER BY total_sold DESC, total_revenue DESC
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                
                List<Map<String, Object>> menuItems = new ArrayList<>();
                int rank = 1;
                String bestSeller = "";
                String topCategory = "";
                int totalItemsSold = 0;
                double totalMenuRevenue = 0;
                
                while (rs.next()) {
                    Map<String, Object> menu = new HashMap<>();
                    menu.put("rank", rank);
                    menu.put("namaMenu", rs.getString("nama_menu"));
                    menu.put("jenisMenu", rs.getString("jenis_menu"));
                    menu.put("totalSold", rs.getInt("total_sold"));
                    menu.put("totalRevenue", rs.getDouble("total_revenue"));
                    menu.put("avgPrice", rs.getDouble("avg_price"));
                    menu.put("orderCount", rs.getInt("order_count"));
                    
                    if (rank == 1) {
                        bestSeller = rs.getString("nama_menu");
                        topCategory = rs.getString("jenis_menu");
                    }
                    
                    totalItemsSold += rs.getInt("total_sold");
                    totalMenuRevenue += rs.getDouble("total_revenue");
                    
                    menuItems.add(menu);
                    rank++;
                }
                
                result.put("menuItems", menuItems);
                result.put("bestSeller", bestSeller);
                result.put("topCategory", topCategory);
                result.put("totalItemsSold", totalItemsSold);
                result.put("totalMenuRevenue", totalMenuRevenue);
                result.put("avgMenuPrice", totalItemsSold > 0 ? totalMenuRevenue / totalItemsSold : 0);
            }

        } catch (SQLException e) {
            System.err.println("Error getting menu performance: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get hourly analysis for today
     */
    public Map<String, Object> getHourlyAnalysis() {
        Map<String, Object> result = new HashMap<>();
        String dateStr = dateFormat.format(new Date());
        
        if (connection == null) {
            return result;
        }

        try {
            String sql = """
                SELECT 
                    HOUR(co.tanggal_pesanan) as hour,
                    COUNT(*) as order_count,
                    SUM(CASE WHEN co.status_pesanan = 'selesai' THEN co.total_pesanan ELSE 0 END) as hour_revenue,
                    AVG(CASE WHEN co.status_pesanan = 'selesai' THEN co.total_pesanan ELSE NULL END) as avg_order_value,
                    SUM(CASE WHEN co.status_pesanan = 'selesai' THEN 1 ELSE 0 END) as completed_orders,
                    SUM(CASE WHEN co.status_pesanan = 'dibatalkan' THEN 1 ELSE 0 END) as cancelled_orders
                FROM Customer_Order co
                WHERE DATE(co.tanggal_pesanan) = ?
                GROUP BY HOUR(co.tanggal_pesanan)
                ORDER BY hour
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                
                List<Map<String, Object>> hourlyData = new ArrayList<>();
                int peakHour = 0;
                double peakRevenue = 0;
                double totalHourlyRevenue = 0;
                int totalHours = 0;
                
                while (rs.next()) {
                    Map<String, Object> hourData = new HashMap<>();
                    int hour = rs.getInt("hour");
                    int orderCount = rs.getInt("order_count");
                    double hourRevenue = rs.getDouble("hour_revenue");
                    double avgOrderValue = rs.getDouble("avg_order_value");
                    
                    hourData.put("hour", String.format("%02d:00", hour));
                    hourData.put("orderCount", orderCount);
                    hourData.put("revenue", hourRevenue);
                    hourData.put("avgOrderValue", avgOrderValue);
                    hourData.put("completedOrders", rs.getInt("completed_orders"));
                    hourData.put("cancelledOrders", rs.getInt("cancelled_orders"));
                    
                    String status = "Normal";
                    if (orderCount > 10) {
                        status = "Sibuk";
                    } else if (orderCount > 5) {
                        status = "Sedang";
                    } else if (orderCount > 0) {
                        status = "Sepi";
                    } else {
                        status = "Tutup";
                    }
                    hourData.put("status", status);
                    
                    if (hourRevenue > peakRevenue) {
                        peakRevenue = hourRevenue;
                        peakHour = hour;
                    }
                    
                    totalHourlyRevenue += hourRevenue;
                    totalHours++;
                    
                    hourlyData.add(hourData);
                }
                
                result.put("hourlyData", hourlyData);
                result.put("peakHour", String.format("%02d:00", peakHour));
                result.put("peakRevenue", peakRevenue);
                result.put("avgRevenuePerHour", totalHours > 0 ? totalHourlyRevenue / totalHours : 0);
            }

        } catch (SQLException e) {
            System.err.println("Error getting hourly analysis: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get recent transactions for today
     */
    public List<Map<String, Object>> getRecentTransactions(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String dateStr = dateFormat.format(new Date());
        
        if (connection == null) {
            return result;
        }

        try {
            String sql = """
                SELECT 
                    n.id_nota,
                    n.waktu_cetak,
                    u.nama as customer_name,
                    n.total_pembayaran,
                    n.metode_pembayaran,
                    n.status_pembayaran
                FROM Nota n
                JOIN Customer_Order co ON n.id_pesanan = co.id_pesanan
                JOIN User u ON co.customer_id = u.user_id
                WHERE DATE(co.tanggal_pesanan) = ?
                ORDER BY n.waktu_cetak DESC
                LIMIT ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, dateStr);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("idNota", rs.getInt("id_nota"));
                    transaction.put("waktuCetak", rs.getTimestamp("waktu_cetak"));
                    transaction.put("customerName", rs.getString("customer_name"));
                    transaction.put("totalPembayaran", rs.getDouble("total_pembayaran"));
                    transaction.put("metodePembayaran", rs.getString("metode_pembayaran"));
                    transaction.put("statusPembayaran", rs.getString("status_pembayaran"));
                    result.add(transaction);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting recent transactions: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get weekly summary report
     */
    public Map<String, Object> getWeeklySummary() {
        Map<String, Object> result = new HashMap<>();
        
        if (connection == null) {
            return result;
        }

        try {
            String sql = """
                SELECT 
                    DATE(co.tanggal_pesanan) as order_date,
                    COUNT(*) as daily_orders,
                    SUM(CASE WHEN co.status_pesanan = 'selesai' THEN co.total_pesanan ELSE 0 END) as daily_revenue,
                    COUNT(CASE WHEN p.metode_pembayaran = 'cash' THEN 1 END) as cash_transactions,
                    COUNT(CASE WHEN p.metode_pembayaran = 'qris' THEN 1 END) as qris_transactions
                FROM Customer_Order co
                LEFT JOIN Pembayaran p ON co.id_pesanan = p.id_pesanan
                WHERE co.tanggal_pesanan >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                GROUP BY DATE(co.tanggal_pesanan)
                ORDER BY order_date DESC
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                
                List<Map<String, Object>> weeklyData = new ArrayList<>();
                double totalWeeklyRevenue = 0;
                int totalWeeklyOrders = 0;
                
                while (rs.next()) {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", rs.getDate("order_date"));
                    dayData.put("orders", rs.getInt("daily_orders"));
                    dayData.put("revenue", rs.getDouble("daily_revenue"));
                    dayData.put("cashTransactions", rs.getInt("cash_transactions"));
                    dayData.put("qrisTransactions", rs.getInt("qris_transactions"));
                    
                    totalWeeklyRevenue += rs.getDouble("daily_revenue");
                    totalWeeklyOrders += rs.getInt("daily_orders");
                    
                    weeklyData.add(dayData);
                }
                
                result.put("weeklyData", weeklyData);
                result.put("totalWeeklyRevenue", totalWeeklyRevenue);
                result.put("totalWeeklyOrders", totalWeeklyOrders);
                result.put("avgDailyRevenue", weeklyData.size() > 0 ? totalWeeklyRevenue / weeklyData.size() : 0);
                result.put("avgDailyOrders", weeklyData.size() > 0 ? (double)totalWeeklyOrders / weeklyData.size() : 0);
            }

        } catch (SQLException e) {
            System.err.println("Error getting weekly summary: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get top customers (most orders or highest spending)
     */
    public List<Map<String, Object>> getTopCustomers(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (connection == null) {
            return result;
        }

        try {
            String sql = """
                SELECT 
                    u.nama as customer_name,
                    COUNT(co.id_pesanan) as total_orders,
                    SUM(CASE WHEN co.status_pesanan = 'selesai' THEN co.total_pesanan ELSE 0 END) as total_spent,
                    AVG(CASE WHEN co.status_pesanan = 'selesai' THEN co.total_pesanan ELSE NULL END) as avg_order_value,
                    MAX(co.tanggal_pesanan) as last_order_date
                FROM User u
                JOIN Customer_Order co ON u.user_id = co.customer_id
                WHERE u.role = 'customer'
                AND co.tanggal_pesanan >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
                GROUP BY u.user_id, u.nama
                HAVING total_orders > 0
                ORDER BY total_spent DESC, total_orders DESC
                LIMIT ?
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                int rank = 1;
                while (rs.next()) {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("rank", rank++);
                    customer.put("customerName", rs.getString("customer_name"));
                    customer.put("totalOrders", rs.getInt("total_orders"));
                    customer.put("totalSpent", rs.getDouble("total_spent"));
                    customer.put("avgOrderValue", rs.getDouble("avg_order_value"));
                    customer.put("lastOrderDate", rs.getTimestamp("last_order_date"));
                    result.add(customer);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting top customers: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Get cancelled orders analysis
     */
    public Map<String, Object> getCancelledOrdersAnalysis() {
        Map<String, Object> result = new HashMap<>();
        String dateStr = dateFormat.format(new Date());
        
        if (connection == null) {
            return result;
        }

        try {
            // Get cancellation reasons
            String reasonSql = """
                SELECT 
                    pd.alasan_batal,
                    COUNT(*) as count,
                    SUM(co.total_pesanan) as lost_revenue
                FROM Pesanan_Dibatalkan pd
                JOIN Customer_Order co ON pd.id_pesanan = co.id_pesanan
                WHERE DATE(pd.tanggal_batal) = ?
                GROUP BY pd.alasan_batal
                ORDER BY count DESC
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(reasonSql)) {
                stmt.setString(1, dateStr);
                ResultSet rs = stmt.executeQuery();
                
                List<Map<String, Object>> cancelReasons = new ArrayList<>();
                double totalLostRevenue = 0;
                int totalCancelled = 0;
                
                while (rs.next()) {
                    Map<String, Object> reason = new HashMap<>();
                    reason.put("reason", rs.getString("alasan_batal"));
                    reason.put("count", rs.getInt("count"));
                    reason.put("lostRevenue", rs.getDouble("lost_revenue"));
                    
                    totalLostRevenue += rs.getDouble("lost_revenue");
                    totalCancelled += rs.getInt("count");
                    
                    cancelReasons.add(reason);
                }
                
                result.put("cancelReasons", cancelReasons);
                result.put("totalCancelled", totalCancelled);
                result.put("totalLostRevenue", totalLostRevenue);
            }

        } catch (SQLException e) {
            System.err.println("Error getting cancelled orders analysis: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("ReportDAO database connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error closing ReportDAO database connection: " + e.getMessage());
            }
        }
    }
}
