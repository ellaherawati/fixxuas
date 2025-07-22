package view;

import java.sql.Timestamp;

public class Pembayaran {
    private int idPembayaran;
    private int idPesanan;
    private int idKasir;
    private Timestamp tanggalPembayaran;
    private String metodePembayaran;
    private double jumlahPembayaran;
    private String statusPembayaran;

    // Constructors
    public Pembayaran() {}

    public Pembayaran(int idPesanan, int idKasir, Timestamp tanggalPembayaran,
                     String metodePembayaran, double jumlahPembayaran, String statusPembayaran) {
        this.idPesanan = idPesanan;
        this.idKasir = idKasir;
        this.tanggalPembayaran = tanggalPembayaran;
        this.metodePembayaran = metodePembayaran;
        this.jumlahPembayaran = jumlahPembayaran;
        this.statusPembayaran = statusPembayaran;
    }

    // Getters and Setters
    public int getIdPembayaran() { return idPembayaran; }
    public void setIdPembayaran(int idPembayaran) { this.idPembayaran = idPembayaran; }
    
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }
    
    public int getIdKasir() { return idKasir; }
    public void setIdKasir(int idKasir) { this.idKasir = idKasir; }
    
    public Timestamp getTanggalPembayaran() { return tanggalPembayaran; }
    public void setTanggalPembayaran(Timestamp tanggalPembayaran) { this.tanggalPembayaran = tanggalPembayaran; }
    
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    
    public double getJumlahPembayaran() { return jumlahPembayaran; }
    public void setJumlahPembayaran(double jumlahPembayaran) { this.jumlahPembayaran = jumlahPembayaran; }
    
    public String getStatusPembayaran() { return statusPembayaran; }
    public void setStatusPembayaran(String statusPembayaran) { this.statusPembayaran = statusPembayaran; }
}
