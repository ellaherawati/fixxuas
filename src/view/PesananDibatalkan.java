package view;

import java.sql.Timestamp;

public class PesananDibatalkan {
    private int idBatal;
    private int idPesanan;
    private Timestamp tanggalBatal;
    private String alasanBatal;

    // Constructors
    public PesananDibatalkan() {}

    public PesananDibatalkan(int idPesanan, Timestamp tanggalBatal, String alasanBatal) {
        this.idPesanan = idPesanan;
        this.tanggalBatal = tanggalBatal;
        this.alasanBatal = alasanBatal;
    }

    // Getters and Setters
    public int getIdBatal() { return idBatal; }
    public void setIdBatal(int idBatal) { this.idBatal = idBatal; }
    
    public int getIdPesanan() { return idPesanan; }
    public void setIdPesanan(int idPesanan) { this.idPesanan = idPesanan; }
    
    public Timestamp getTanggalBatal() { return tanggalBatal; }
    public void setTanggalBatal(Timestamp tanggalBatal) { this.tanggalBatal = tanggalBatal; }
    
    public String getAlasanBatal() { return alasanBatal; }
    public void setAlasanBatal(String alasanBatal) { this.alasanBatal = alasanBatal; }
}