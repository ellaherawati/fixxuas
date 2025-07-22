package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderDAO {
    private Connection connection;

    public CustomerOrderDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public int create(CustomerOrder order) {
        String sql = "INSERT INTO CustomerOrder (tanggal_pesanan, total_pesanan, catatan, customer_id, status_pesanan) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, order.getTanggalPesanan());
            stmt.setDouble(2, order.getTotalPesanan());
            stmt.setString(3, order.getCatatan());
            stmt.setInt(4, order.getCustomerId());
            stmt.setString(5, order.getStatusPesanan());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean updateStatus(int idPesanan, String newStatus) {
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

    public CustomerOrder findById(int idPesanan) {
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

    public List<CustomerOrder> findAll() {
        List<CustomerOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM CustomerOrder ORDER BY tanggal_pesanan DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CustomerOrder order = new CustomerOrder();
                order.setIdPesanan(rs.getInt("id_pesanan"));
                order.setTanggalPesanan(rs.getTimestamp("tanggal_pesanan"));
                order.setTotalPesanan(rs.getDouble("total_pesanan"));
                order.setCatatan(rs.getString("catatan"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStatusPesanan(rs.getString("status_pesanan"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
}
