package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaDAO {
    private Connection connection;

    public NotaDAO() {
        this.connection = DatabaseConnection.getConnection();
        if (this.connection == null) {
            System.err.println("Failed to establish database connection!");
        }
    }

    public boolean create(Nota nota) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }

        String sql = "INSERT INTO Nota (id_pesanan, waktu_cetak, total_pembayaran, metode_pembayaran, status_pembayaran) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, nota.getIdPesanan());
            stmt.setTimestamp(2, nota.getWaktuCetak());
            stmt.setDouble(3, nota.getTotalPembayaran());
            stmt.setString(4, nota.getMetodePembayaran());
            stmt.setString(5, nota.getStatusPembayaran());
            
            int rowsAffected = stmt.executeUpdate();
            
            // Get generated ID and set it to the nota object
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        nota.setIdNota(generatedKeys.getInt(1));
                    }
                }
            }
            
            System.out.println("Nota created successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in create(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Nota findById(int idNota) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return null;
        }
        
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
            System.err.println("SQL Error in findById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Nota findByOrderId(int idPesanan) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return null;
        }
        
        String sql = "SELECT * FROM Nota WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
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
            System.err.println("SQL Error in findByOrderId(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Nota> findAll() {
        List<Nota> notas = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return notas;
        }
        
        String sql = "SELECT * FROM Nota ORDER BY waktu_cetak DESC";
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
                notas.add(nota);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }
        return notas;
    }

    /**
     * Update nota status
     */
    public boolean updateStatus(int idNota, String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Nota SET status_pembayaran = ? WHERE id_nota = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, idNota);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Nota status updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in updateStatus(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update nota payment status by order ID
     */
    public boolean updateStatusByOrderId(int idPesanan, String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Nota SET status_pembayaran = ? WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, idPesanan);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Nota status updated by order ID successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in updateStatusByOrderId(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update complete nota information
     */
    public boolean update(Nota nota) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Nota SET id_pesanan = ?, waktu_cetak = ?, total_pembayaran = ?, metode_pembayaran = ?, status_pembayaran = ? WHERE id_nota = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, nota.getIdPesanan());
            stmt.setTimestamp(2, nota.getWaktuCetak());
            stmt.setDouble(3, nota.getTotalPembayaran());
            stmt.setString(4, nota.getMetodePembayaran());
            stmt.setString(5, nota.getStatusPembayaran());
            stmt.setInt(6, nota.getIdNota());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Nota updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in update(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get notas by status
     */
    public List<Nota> findByStatus(String status) {
        List<Nota> notas = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return notas;
        }
        
        String sql = "SELECT * FROM Nota WHERE status_pembayaran = ? ORDER BY waktu_cetak DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Nota nota = new Nota();
                nota.setIdNota(rs.getInt("id_nota"));
                nota.setIdPesanan(rs.getInt("id_pesanan"));
                nota.setWaktuCetak(rs.getTimestamp("waktu_cetak"));
                nota.setTotalPembayaran(rs.getDouble("total_pembayaran"));
                nota.setMetodePembayaran(rs.getString("metode_pembayaran"));
                nota.setStatusPembayaran(rs.getString("status_pembayaran"));
                notas.add(nota);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findByStatus(): " + e.getMessage());
            e.printStackTrace();
        }
        return notas;
    }

    /**
     * Get pending cash payments
     */
    public List<Nota> findPendingCashPayments() {
        List<Nota> notas = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return notas;
        }
        
        String sql = "SELECT * FROM Nota WHERE metode_pembayaran = 'cash' AND status_pembayaran = 'menunggu' ORDER BY waktu_cetak DESC";
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
                notas.add(nota);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findPendingCashPayments(): " + e.getMessage());
            e.printStackTrace();
        }
        return notas;
    }

    /**
     * Delete nota by ID
     */
    public boolean delete(int idNota) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "DELETE FROM Nota WHERE id_nota = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Nota deleted successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in delete(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get count of notas by status
     */
    public int getCountByStatus(String status) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return 0;
        }
        
        String sql = "SELECT COUNT(*) as count FROM Nota WHERE status_pembayaran = ?";
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