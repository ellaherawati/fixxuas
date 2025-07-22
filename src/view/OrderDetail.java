package view;

public class OrderDetail {
    private int idDetail;
    private int idPesanan;
    private int idMenu;
    private int jumlah;
    private double hargaSatuan;
    private double subtotal;
    private String namaMenu; // Added for display purposes

    // Constructors
    public OrderDetail() {}

    public OrderDetail(int idPesanan, int idMenu, int jumlah, double hargaSatuan) {
        this.idPesanan = idPesanan;
        this.idMenu = idMenu;
        this.jumlah = jumlah;
        this.hargaSatuan = hargaSatuan;
        this.subtotal = jumlah * hargaSatuan;
    }

    // Getters and Setters
    public int getIdDetail() { return idDetail; }
    public void setIdDetail(int idDetail) { this.idDetail = idDetail; }
    
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }
    
    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    
    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { 
        this.jumlah = jumlah; 
        this.subtotal = this.jumlah * this.hargaSatuan; // Auto-calculate subtotal
    }
    
    public double getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(double hargaSatuan) { 
        this.hargaSatuan = hargaSatuan; 
        this.subtotal = this.jumlah * this.hargaSatuan; // Auto-calculate subtotal
    }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }
}