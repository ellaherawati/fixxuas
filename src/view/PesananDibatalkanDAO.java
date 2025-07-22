package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PesananDibatalkanDAO {
    private Connection connection;

    public PesananDibatalkanDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean create(PesananDibatalkan pesananBatal) {
        String sql = "INSERT INTO PesananDibatalkan (id_pesanan, tanggal_batal, alasan_batal) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pesananBatal.getIdPesanan());
            stmt.setTimestamp(2, pesananBatal.getTanggalBatal());
            stmt.setString(3, pesananBatal.getAlasanBatal());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public PesananDibatalkan findByOrderId(int idPesanan) {
        String sql = "SELECT * FROM PesananDibatalkan WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PesananDibatalkan pesananBatal = new PesananDibatalkan();
                pesananBatal.setIdBatal(rs.getInt("id_batal"));
                pesananBatal.setIdPesanan(rs.getInt("id_pesanan"));
                pesananBatal.setTanggalBatal(rs.getTimestamp("tanggal_batal"));
                pesananBatal.setAlasanBatal(rs.getString("alasan_batal"));
                return pesananBatal;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PesananDibatalkan> findAll() {
        List<PesananDibatalkan> pesananBatals = new ArrayList<>();
        String sql = "SELECT * FROM PesananDibatalkan ORDER BY tanggal_batal DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PesananDibatalkan pesananBatal = new PesananDibatalkan();
                pesananBatal.setIdBatal(rs.getInt("id_batal"));
                pesananBatal.setIdPesanan(rs.getInt("id_pesanan"));
                pesananBatal.setTanggalBatal(rs.getTimestamp("tanggal_batal"));
                pesananBatal.setAlasanBatal(rs.getString("alasan_batal"));
                pesananBatals.add(pesananBatal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pesananBatals;
    }
}