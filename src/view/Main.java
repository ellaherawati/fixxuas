package view;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Example usage of the DAO classes
        
        // Create DAOs
        UserDAO userDAO = new UserDAO();
        MenuDAO menuDAO = new MenuDAO();
        OrderService orderService = new OrderService();
        
        // Example: Create a new user
        User newUser = new User("customer1", "password123", "John Doe", "customer");
        if (userDAO.create(newUser)) {
            System.out.println("User created successfully");
        }
        
        // Example: Find user by username
        User user = userDAO.findByUsername("customer1");
        if (user != null) {
            System.out.println("Found user: " + user.getNama());
        }
        
        // Example: Get all menus
        List<Menu> menus = menuDAO.findAll();
        System.out.println("Total menus: " + menus.size());
        
        // Example: Create an order
        CustomerOrder order = new CustomerOrder();
        order.setCustomerId(user.getUserId());
        order.setTanggalPesanan(new Timestamp(System.currentTimeMillis()));
        order.setTotalPesanan(50000.0);
        order.setCatatan("Extra spicy");
        order.setStatusPesanan("pending");
        
        List<OrderDetail> orderDetails = new ArrayList<>();
        OrderDetail detail = new OrderDetail();
        detail.setIdMenu(1);
        detail.setJumlah(2);
        detail.setHargaSatuan(25000.0);
        orderDetails.add(detail);
        
        int orderId = orderService.createOrder(order, orderDetails);
        if (orderId > 0) {
            System.out.println("Order created with ID: " + orderId);
            
            // Process payment
            if (orderService.processPayment(orderId, 1, "cash", 50000.0)) {
                System.out.println("Payment processed successfully");
            }
        }
        
        // Close database connection
        DatabaseConnection.closeConnection();
    }
}