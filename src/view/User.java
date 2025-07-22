package view;



public class User {
    private int userId;
    private String username;
    private String password;
    private String nama;
    private String role;

    // Constructors
    public User() {}

    public User(String username, String password, String nama, String role) {
        this.username = username;
        this.password = password;
        this.nama = nama;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
