package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
    private Connection connection;

    public MenuDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean create(Menu menu) {
        String sql = "INSERT INTO Menu (nama_menu, jenis_menu, harga, deskripsi, gambar) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, menu.getNamaMenu());
            stmt.setString(2, menu.getJenisMenu());
            stmt.setDouble(3, menu.getHarga());
            stmt.setString(4, menu.getDeskripsi());
            stmt.setString(5, menu.getGambar());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Menu findById(int idMenu) {
        String sql = "SELECT * FROM Menu WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMenu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setDeskripsi(rs.getString("deskripsi"));
                menu.setGambar(rs.getString("gambar"));
                return menu;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Menu> findAll() {
        List<Menu> menus = new ArrayList<>();
        String sql = "SELECT * FROM Menu ORDER BY jenis_menu, nama_menu";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setDeskripsi(rs.getString("deskripsi"));
                menu.setGambar(rs.getString("gambar"));
                menus.add(menu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return menus;
    }

    public boolean update(Menu menu) {
        String sql = "UPDATE Menu SET nama_menu = ?, jenis_menu = ?, harga = ?, deskripsi = ?, gambar = ? WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, menu.getNamaMenu());
            stmt.setString(2, menu.getJenisMenu());
            stmt.setDouble(3, menu.getHarga());
            stmt.setString(4, menu.getDeskripsi());
            stmt.setString(5, menu.getGambar());
            stmt.setInt(6, menu.getIdMenu());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idMenu) {
        String sql = "DELETE FROM Menu WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMenu);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
