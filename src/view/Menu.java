package view;

public class Menu {
    private int idMenu;
    private String namaMenu;
    private String jenisMenu;
    private double harga;
    private String deskripsi; // New field for description
    private String ketersediaan;
    private String gambar; // Field for image path

    // Default constructor
    public Menu() {}

    // Constructor without description (for backward compatibility)
    public Menu(String namaMenu, String jenisMenu, double harga, String ketersediaan) {
        this.namaMenu = namaMenu;
        this.jenisMenu = jenisMenu;
        this.harga = harga;
        this.ketersediaan = ketersediaan;
    }


    // Full constructor with all fields
    public Menu(String namaMenu, String jenisMenu, double harga, String deskripsi, String ketersediaan, String gambar) {
        this.namaMenu = namaMenu;
        this.jenisMenu = jenisMenu;
        this.harga = harga;
        this.deskripsi = deskripsi;
        this.ketersediaan = ketersediaan;
        this.gambar = gambar;
    }

    // Constructor with ID (for updates)
    public Menu(int idMenu, String namaMenu, String jenisMenu, double harga, String deskripsi, String ketersediaan, String gambar) {
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.jenisMenu = jenisMenu;
        this.harga = harga;
        this.deskripsi = deskripsi;
        this.ketersediaan = ketersediaan;
        this.gambar = gambar;
    }

    // Getters and Setters
    public int getIdMenu() { 
        return idMenu; 
    }
    
    public void setIdMenu(int idMenu) { 
        this.idMenu = idMenu; 
    }
    
    public String getNamaMenu() { 
        return namaMenu; 
    }
    
    public void setNamaMenu(String namaMenu) { 
        this.namaMenu = namaMenu; 
    }
    
    public String getJenisMenu() { 
        return jenisMenu; 
    }
    
    public void setJenisMenu(String jenisMenu) { 
        this.jenisMenu = jenisMenu; 
    }
    
    public double getHarga() { 
        return harga; 
    }
    
    public void setHarga(double harga) { 
        this.harga = harga; 
    }
    
    public String getDeskripsi() { 
        return deskripsi; 
    }
    
    public void setDeskripsi(String deskripsi) { 
        this.deskripsi = deskripsi; 
    }
    
    public String getKetersediaan() { 
        return ketersediaan; 
    }
    
    public void setKetersediaan(String ketersediaan) { 
        this.ketersediaan = ketersediaan; 
    }
    
    public String getGambar() { 
        return gambar; 
    }
    
    public void setGambar(String gambar) { 
        this.gambar = gambar; 
    }

    // Utility methods
    public boolean isAvailable() {
        return "1".equals(ketersediaan);
    }

    public void setAvailable(boolean available) {
        this.ketersediaan = available ? "1" : "0";
    }

    public boolean hasImage() {
        return gambar != null && !gambar.trim().isEmpty();
    }

    public boolean hasDescription() {
        return deskripsi != null && !deskripsi.trim().isEmpty();
    }

    public String getShortDescription(int maxLength) {
        if (!hasDescription()) {
            return "";
        }
        if (deskripsi.length() <= maxLength) {
            return deskripsi;
        }
        return deskripsi.substring(0, maxLength) + "...";
    }

    @Override
    public String toString() {
        return "Menu{" +
                "idMenu=" + idMenu +
                ", namaMenu='" + namaMenu + '\'' +
                ", jenisMenu='" + jenisMenu + '\'' +
                ", harga=" + harga +
                ", deskripsi='" + deskripsi + '\'' +
                ", ketersediaan='" + ketersediaan + '\'' +
                ", gambar='" + gambar + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Menu menu = (Menu) obj;
        return idMenu == menu.idMenu;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idMenu);
    }
}