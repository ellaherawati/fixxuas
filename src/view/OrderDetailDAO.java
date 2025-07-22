package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO {
    private Connection connection;

    public OrderDetailDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean create(OrderDetail detail) {
        String sql = "INSERT INTO OrderDetail (id_pesanan, id_menu, jumlah, harga_satuan, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, detail.getIdPesanan());
            stmt.setInt(2, detail.getIdMenu());
            stmt.setInt(3, detail.getJumlah());
            stmt.setDouble(4, detail.getHargaSatuan());
            stmt.setDouble(5, detail.getSubtotal());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderDetail> findByOrderId(int idPesanan) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM OrderDetail WHERE id_pesanan = ?";
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
                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }

    public List<OrderDetail> findAll() {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM OrderDetail ORDER BY id_detail";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setIdDetail(rs.getInt("id_detail"));
                detail.setIdPesanan(rs.getInt("id_pesanan"));
                detail.setIdMenu(rs.getInt("id_menu"));
                detail.setJumlah(rs.getInt("jumlah"));
                detail.setHargaSatuan(rs.getDouble("harga_satuan"));
                detail.setSubtotal(rs.getDouble("subtotal"));
                details.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
}