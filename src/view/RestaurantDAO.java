package view;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {
    private Connection connection;

    public RestaurantDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    /**
     * Update nota status by nota ID
     */
    public boolean updateNotaStatus(int idNota, String newStatus) {
        String sql = "UPDATE Nota SET status_pembayaran = ? WHERE id_nota = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, idNota);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update pembayaran status by pembayaran ID
     */
    public boolean updatePembayaranStatus(int idPembayaran, String newStatus) {
        String sql = "UPDATE Pembayaran SET status_pembayaran = ? WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, idPembayaran);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update customer order status by order ID
     */
    public boolean updateOrderStatus(int idPesanan, String newStatus) {
        String sql = "UPDATE CustomerOrder SET status_pesanan = ? WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, idPesanan);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Find nota by ID
     */
    public Nota findNotaById(int idNota) {
        String sql = "SELECT * FROM Nota WHERE id_nota = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Nota nota = new Nota();
                nota.setIdNota(rs.getInt("id_nota"));
                nota.setIdPesanan(rs.getInt("id_pesanan"));
                nota.setWaktuCetak(rs.getTimestamp("waktu_cetak"));
                nota.setTotalPembayaran(rs.getDouble("total_pembayaran"));
                nota.setMetodePembayaran(rs.getString("metode_pembayaran"));
                nota.setStatusPembayaran(rs.getString("status_pembayaran"));
                return nota;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find customer order by ID
     */
    public CustomerOrder findCustomerOrderById(int idPesanan) {
        String sql = "SELECT * FROM CustomerOrder WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CustomerOrder order = new CustomerOrder();
                order.setIdPesanan(rs.getInt("id_pesanan"));
                order.setTanggalPesanan(rs.getTimestamp("tanggal_pesanan"));
                order.setTotalPesanan(rs.getDouble("total_pesanan"));
                order.setCatatan(rs.getString("catatan"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStatusPesanan(rs.getString("status_pesanan"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find pembayaran by pesanan ID
     */
    public Pembayaran findPembayaranByPesananId(int idPesanan) {
        String sql = "SELECT * FROM Pembayaran WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Pembayaran pembayaran = new Pembayaran();
                pembayaran.setIdPembayaran(rs.getInt("id_pembayaran"));
                pembayaran.setIdPesanan(rs.getInt("id_pesanan"));
                pembayaran.setIdKasir(rs.getInt("id_kasir"));
                pembayaran.setTanggalPembayaran(rs.getTimestamp("tanggal_pembayaran"));
                pembayaran.setMetodePembayaran(rs.getString("metode_pembayaran"));
                pembayaran.setJumlahPembayaran(rs.getDouble("jumlah_pembayaran"));
                pembayaran.setStatusPembayaran(rs.getString("status_pembayaran"));
                return pembayaran;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find order details by pesanan ID with menu names
     */
    public List<OrderDetail> findOrderDetailsByPesananId(int idPesanan) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT od.*, m.nama_menu FROM OrderDetail od " +
                     "JOIN Menu m ON od.id_menu = m.id_menu " +
                     "WHERE od.id_pesanan = ? ORDER BY od.id_detail";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setIdDetail(rs.getInt("id_detail"));
                detail.setIdPesanan(rs.getInt("id_pesanan"));
                detail.setIdMenu(rs.getInt("id_menu"));
                detail.setJumlah(rs.getInt("jumlah"));
                detail.setHargaSatuan(rs.getDouble("harga_satuan"));
                detail.setSubtotal(rs.getDouble("subtotal"));
                detail.setNamaMenu(rs.getString("nama_menu"));
                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    /**
     * Get customer name by customer ID
     */
    public String getCustomerName(int customerId) {
        String sql = "SELECT nama FROM User WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nama");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Customer";
    }

    /**
     * Get kasir name by kasir ID
     */
    public String getKasirName(int kasirId) {
        String sql = "SELECT nama FROM User WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, kasirId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nama");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Kasir";
    }

    /**
     * Get all pending cash payments for kasir dashboard
     */
    public List<Nota> getPendingCashPayments() {
        List<Nota> pendingNotes = new ArrayList<>();
        String sql = "SELECT * FROM Nota WHERE metode_pembayaran = 'cash' " +
                     "AND (status_pembayaran = 'menunggu pembayaran' OR status_pembayaran = 'menunggu') " +
                     "ORDER BY waktu_cetak DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Nota nota = new Nota();
                nota.setIdNota(rs.getInt("id_nota"));
                nota.setIdPesanan(rs.getInt("id_pesanan"));
                nota.setWaktuCetak(rs.getTimestamp("waktu_cetak"));
                nota.setTotalPembayaran(rs.getDouble("total_pembayaran"));
                nota.setMetodePembayaran(rs.getString("metode_pembayaran"));
                nota.setStatusPembayaran(rs.getString("status_pembayaran"));
                pendingNotes.add(nota);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pendingNotes;
    }

    /**
     * Get payment statistics for dashboard
     */
    public PaymentStats getPaymentStats() {
        PaymentStats stats = new PaymentStats();
        
        // Count pending cash payments
        String pendingSql = "SELECT COUNT(*) FROM Nota WHERE metode_pembayaran = 'cash' " +
                           "AND (status_pembayaran = 'menunggu pembayaran' OR status_pembayaran = 'menunggu')";
        try (PreparedStatement stmt = connection.prepareStatement(pendingSql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setPendingCashPayments(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Count successful payments today
        String successfulSql = "SELECT COUNT(*) FROM Nota WHERE status_pembayaran = 'berhasil' " +
                              "AND DATE(waktu_cetak) = CURDATE()";
        try (PreparedStatement stmt = connection.prepareStatement(successfulSql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setSuccessfulPaymentsToday(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Calculate total revenue today
        String revenueSql = "SELECT COALESCE(SUM(total_pembayaran), 0) FROM Nota " +
                           "WHERE status_pembayaran = 'berhasil' AND DATE(waktu_cetak) = CURDATE()";
        try (PreparedStatement stmt = connection.prepareStatement(revenueSql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                stats.setTotalRevenueToday(rs.getDouble(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }
    
    // Inner class for payment statistics
    public static class PaymentStats {
        private int pendingCashPayments;
        private int successfulPaymentsToday;
        private double totalRevenueToday;
        
        // Getters and setters
        public int getPendingCashPayments() { return pendingCashPayments; }
        public void setPendingCashPayments(int pendingCashPayments) { this.pendingCashPayments = pendingCashPayments; }
        
        public int getSuccessfulPaymentsToday() { return successfulPaymentsToday; }
        public void setSuccessfulPaymentsToday(int successfulPaymentsToday) { this.successfulPaymentsToday = successfulPaymentsToday; }
        
        public double getTotalRevenueToday() { return totalRevenueToday; }
        public void setTotalRevenueToday(double totalRevenueToday) { this.totalRevenueToday = totalRevenueToday; }
    }
}