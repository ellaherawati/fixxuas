package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PembayaranDAO {
    private Connection connection;

    public PembayaranDAO() {
        this.connection = DatabaseConnection.getConnection();
        if (this.connection == null) {
            System.err.println("Failed to establish database connection!");
        }
    }

    public boolean create(Pembayaran pembayaran) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }

        String sql = "INSERT INTO Pembayaran (id_pesanan, id_kasir, tanggal_pembayaran, metode_pembayaran, jumlah_pembayaran, status_pembayaran) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pembayaran.getIdPesanan());
            stmt.setInt(2, pembayaran.getIdKasir());
            stmt.setTimestamp(3, pembayaran.getTanggalPembayaran());
            stmt.setString(4, pembayaran.getMetodePembayaran());
            stmt.setDouble(5, pembayaran.getJumlahPembayaran());
            stmt.setString(6, pembayaran.getStatusPembayaran());
            
            int rowsAffected = stmt.executeUpdate();
            
            // Get generated ID and set it to the pembayaran object
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        pembayaran.setIdPembayaran(generatedKeys.getInt(1));
                    }
                }
            }
            
            System.out.println("Pembayaran created successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in create(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Pembayaran findById(int idPembayaran) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return null;
        }
        
        String sql = "SELECT * FROM Pembayaran WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPembayaran);
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
            System.err.println("SQL Error in findById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Pembayaran findByOrderId(int idPesanan) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return null;
        }
        
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
            System.err.println("SQL Error in findByOrderId(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Pembayaran> findAll() {
        List<Pembayaran> pembayarans = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return pembayarans;
        }
        
        String sql = "SELECT * FROM Pembayaran ORDER BY tanggal_pembayaran DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Pembayaran pembayaran = new Pembayaran();
                pembayaran.setIdPembayaran(rs.getInt("id_pembayaran"));
                pembayaran.setIdPesanan(rs.getInt("id_pesanan"));
                pembayaran.setIdKasir(rs.getInt("id_kasir"));
                pembayaran.setTanggalPembayaran(rs.getTimestamp("tanggal_pembayaran"));
                pembayaran.setMetodePembayaran(rs.getString("metode_pembayaran"));
                pembayaran.setJumlahPembayaran(rs.getDouble("jumlah_pembayaran"));
                pembayaran.setStatusPembayaran(rs.getString("status_pembayaran"));
                pembayarans.add(pembayaran);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }
        return pembayarans;
    }

    /**
     * Update payment status
     */
    public boolean updateStatus(int idPembayaran, String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Pembayaran SET status_pembayaran = ?, tanggal_pembayaran = CURRENT_TIMESTAMP WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, idPembayaran);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Pembayaran status updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in updateStatus(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update payment status by order ID
     */
    public boolean updateStatusByOrderId(int idPesanan, String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Pembayaran SET status_pembayaran = ?, tanggal_pembayaran = CURRENT_TIMESTAMP WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, idPesanan);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Pembayaran status updated by order ID successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in updateStatusByOrderId(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update kasir and confirm payment
     */
    public boolean confirmPayment(int idPembayaran, int kasirId, String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Pembayaran SET id_kasir = ?, status_pembayaran = ?, tanggal_pembayaran = CURRENT_TIMESTAMP WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, kasirId);
            stmt.setString(2, status);
            stmt.setInt(3, idPembayaran);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Payment confirmed by kasir successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in confirmPayment(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update complete pembayaran information
     */
    public boolean update(Pembayaran pembayaran) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Pembayaran SET id_pesanan = ?, id_kasir = ?, tanggal_pembayaran = ?, metode_pembayaran = ?, jumlah_pembayaran = ?, status_pembayaran = ? WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pembayaran.getIdPesanan());
            stmt.setInt(2, pembayaran.getIdKasir());
            stmt.setTimestamp(3, pembayaran.getTanggalPembayaran());
            stmt.setString(4, pembayaran.getMetodePembayaran());
            stmt.setDouble(5, pembayaran.getJumlahPembayaran());
            stmt.setString(6, pembayaran.getStatusPembayaran());
            stmt.setInt(7, pembayaran.getIdPembayaran());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Pembayaran updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in update(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get payments by status
     */
    public List<Pembayaran> findByStatus(String status) {
        List<Pembayaran> pembayarans = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return pembayarans;
        }
        
        String sql = "SELECT * FROM Pembayaran WHERE status_pembayaran = ? ORDER BY tanggal_pembayaran DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Pembayaran pembayaran = new Pembayaran();
                pembayaran.setIdPembayaran(rs.getInt("id_pembayaran"));
                pembayaran.setIdPesanan(rs.getInt("id_pesanan"));
                pembayaran.setIdKasir(rs.getInt("id_kasir"));
                pembayaran.setTanggalPembayaran(rs.getTimestamp("tanggal_pembayaran"));
                pembayaran.setMetodePembayaran(rs.getString("metode_pembayaran"));
                pembayaran.setJumlahPembayaran(rs.getDouble("jumlah_pembayaran"));
                pembayaran.setStatusPembayaran(rs.getString("status_pembayaran"));
                pembayarans.add(pembayaran);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findByStatus(): " + e.getMessage());
            e.printStackTrace();
        }
        return pembayarans;
    }

    /**
     * Get payments by method
     */
    public List<Pembayaran> findByMethod(String method) {
        List<Pembayaran> pembayarans = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return pembayarans;
        }
        
        String sql = "SELECT * FROM Pembayaran WHERE metode_pembayaran = ? ORDER BY tanggal_pembayaran DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, method);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Pembayaran pembayaran = new Pembayaran();
                pembayaran.setIdPembayaran(rs.getInt("id_pembayaran"));
                pembayaran.setIdPesanan(rs.getInt("id_pesanan"));
                pembayaran.setIdKasir(rs.getInt("id_kasir"));
                pembayaran.setTanggalPembayaran(rs.getTimestamp("tanggal_pembayaran"));
                pembayaran.setMetodePembayaran(rs.getString("metode_pembayaran"));
                pembayaran.setJumlahPembayaran(rs.getDouble("jumlah_pembayaran"));
                pembayaran.setStatusPembayaran(rs.getString("status_pembayaran"));
                pembayarans.add(pembayaran);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findByMethod(): " + e.getMessage());
            e.printStackTrace();
        }
        return pembayarans;
    }

    /**
     * Get pending cash payments
     */
    public List<Pembayaran> findPendingCashPayments() {
        List<Pembayaran> pembayarans = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return pembayarans;
        }
        
        String sql = "SELECT * FROM Pembayaran WHERE metode_pembayaran = 'cash' AND status_pembayaran = 'menunggu' ORDER BY tanggal_pembayaran DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Pembayaran pembayaran = new Pembayaran();
                pembayaran.setIdPembayaran(rs.getInt("id_pembayaran"));
                pembayaran.setIdPesanan(rs.getInt("id_pesanan"));
                pembayaran.setIdKasir(rs.getInt("id_kasir"));
                pembayaran.setTanggalPembayaran(rs.getTimestamp("tanggal_pembayaran"));
                pembayaran.setMetodePembayaran(rs.getString("metode_pembayaran"));
                pembayaran.setJumlahPembayaran(rs.getDouble("jumlah_pembayaran"));
                pembayaran.setStatusPembayaran(rs.getString("status_pembayaran"));
                pembayarans.add(pembayaran);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findPendingCashPayments(): " + e.getMessage());
            e.printStackTrace();
        }
        return pembayarans;
    }

    /**
     * Get total payments by status
     */
    public double getTotalPaymentsByStatus(String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return 0.0;
        }
        
        String sql = "SELECT SUM(jumlah_pembayaran) as total FROM Pembayaran WHERE status_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getTotalPaymentsByStatus(): " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Get count of payments by status
     */
    public int getCountByStatus(String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return 0;
        }
        
        String sql = "SELECT COUNT(*) as count FROM Pembayaran WHERE status_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getCountByStatus(): " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Delete payment by ID
     */
    public boolean delete(int idPembayaran) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "DELETE FROM Pembayaran WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPembayaran);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Pembayaran deleted successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in delete(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}