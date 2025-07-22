package view;

import java.sql.Timestamp;

public class Nota {
    private int idNota;
    private int idPesanan;
    private Timestamp waktuCetak;
    private double totalPembayaran;
    private String metodePembayaran;
    private String statusPembayaran;

    // Constructors
    public Nota() {}

    public Nota(int idPesanan, Timestamp waktuCetak, double totalPembayaran, 
                      String metodePembayaran, String statusPembayaran) {
        this.idPesanan = idPesanan;
        this.waktuCetak = waktuCetak;
        this.totalPembayaran = totalPembayaran;
        this.metodePembayaran = metodePembayaran;
        this.statusPembayaran = statusPembayaran;
    }

    // Getters and Setters
    public int getIdNota() { return idNota; }
    public void setIdNota(int idNota) { this.idNota = idNota; }
    
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }
    
    public Timestamp getWaktuCetak() { return waktuCetak; }
    public void setWaktuCetak(Timestamp waktuCetak) { this.waktuCetak = waktuCetak; }
    
    public double getTotalPembayaran() { return totalPembayaran; }
    public void setTotalPembayaran(double totalPembayaran) { this.totalPembayaran = totalPembayaran; }
    
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    
    public String getStatusPembayaran() { return statusPembayaran; }
    public void setStatusPembayaran(String statusPembayaran) { this.statusPembayaran = statusPembayaran; }
}