package DAO;

import Model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtworkDAO {

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS artworks (" +
                "id INT PRIMARY KEY, " +
                "title VARCHAR(100), " +
                "artist VARCHAR(100), " +
                "price DOUBLE, " +
                "art_type VARCHAR(20), " +
                "file_format VARCHAR(20), " +
                "material VARCHAR(50))";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void seedData() {
        if (!getAll().isEmpty()) return;
        add(new DigitalArt(1, "Neon City", "Aida", 120.0, "PNG"));
        add(new DigitalArt(2, "Pixel Dream", "Bek", 80.5, "JPG"));
        add(new PhysicalArt(3, "Mountain Oil", "Aida", 450.0, "Oil on canvas"));
        add(new PhysicalArt(4, "Clay Horse", "Nur", 200.0, "Clay"));
    }


    public void add(Artwork a) {
        String sql = "INSERT INTO artworks (id, title, artist, price, art_type, file_format, material) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, a.getId());
            ps.setString(2, a.getTitle());
            ps.setString(3, a.getArtist());
            ps.setDouble(4, a.getPrice());
            ps.setString(5, a.getType());
            if (a instanceof DigitalArt d) {
                ps.setString(6, d.getFileFormat());
                ps.setNull(7, Types.VARCHAR);
            } else if (a instanceof PhysicalArt p) {
                ps.setNull(6, Types.VARCHAR);
                ps.setString(7, p.getMaterial());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Artwork> getAll() {
        List<Artwork> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM artworks");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public Artwork getById(int id) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM artworks WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void update(int id, String title, String artist, double price) {
        String sql = "UPDATE artworks SET title = ?, artist = ?, price = ? WHERE id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, artist);
            ps.setDouble(3, price);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void delete(int id) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM artworks WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void printStatistics() {
        String sql = "SELECT COUNT(*), SUM(price), AVG(price), MIN(price), MAX(price) FROM artworks";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Total artworks: " + rs.getInt(1));
                System.out.println("Total price:    " + rs.getDouble(2));
                System.out.println("Average price:  " + rs.getDouble(3));
                System.out.println("Min price:      " + rs.getDouble(4));
                System.out.println("Max price:      " + rs.getDouble(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String byType = "SELECT art_type, COUNT(*), AVG(price) FROM artworks GROUP BY art_type";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(byType);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getString(1) + ": count = " + rs.getInt(2)
                        + ", avg price = " + rs.getDouble(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private Artwork mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String artist = rs.getString("artist");
        double price = rs.getDouble("price");
        String type = rs.getString("art_type");

        if (type.equals("Digital")) {
            return new DigitalArt(id, title, artist, price, rs.getString("file_format"));
        } else {
            return new PhysicalArt(id, title, artist, price, rs.getString("material"));
        }
    }
}