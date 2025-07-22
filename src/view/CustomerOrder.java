package view;

import java.sql.Timestamp;

public class CustomerOrder {
    private int idPesanan;
    private Timestamp tanggalPesanan;
    private double totalPesanan;
    private String catatan;
    private int customerId;
    private String statusPesanan;

    // Constructors
    public CustomerOrder() {}

    public CustomerOrder(Timestamp tanggalPesanan, double totalPesanan, String catatan, 
                        int customerId, String statusPesanan) {
        this.tanggalPesanan = tanggalPesanan;
        this.totalPesanan = totalPesanan;
        this.catatan = catatan;
        this.customerId = customerId;
        this.statusPesanan = statusPesanan;
    }

    // Getters and Setters
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }
    
    public Timestamp getTanggalPesanan() { return tanggalPesanan; }
    public void setTanggalPesanan(Timestamp tanggalPesanan) { this.tanggalPesanan = tanggalPesanan; }
    
    public double getTotalPesanan() { return totalPesanan; }
    public void setTotalPesanan(double totalPesanan) { this.totalPesanan = totalPesanan; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public String getStatusPesanan() { return statusPesanan; }
    public void setStatusPesanan(String statusPesanan) { this.statusPesanan = statusPesanan; }
}