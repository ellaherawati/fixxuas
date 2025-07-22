package view;


import java.sql.Timestamp;
import java.util.List;

public class OrderService {
    private CustomerOrderDAO orderDAO;
    private OrderDetailDAO orderDetailDAO;
    private PembayaranDAO pembayaranDAO;
    private NotaDAO notaDAO;
    private MenuDAO menuDAO;

    public OrderService() {
        this.orderDAO = new CustomerOrderDAO();
        this.orderDetailDAO = new OrderDetailDAO();
        this.pembayaranDAO = new PembayaranDAO();
        this.notaDAO = new NotaDAO();
        this.menuDAO = new MenuDAO();
    }

    public int createOrder(CustomerOrder order, List<OrderDetail> orderDetails) {
        try {
            // Create main order
            int orderId = orderDAO.create(order);
            if (orderId > 0) {
                // Create order details
                for (OrderDetail detail : orderDetails) {
                    detail.setIdPesanan(orderId);
                    orderDetailDAO.create(detail);
                }
                return orderId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean processPayment(int orderId, int kasirId, String metode, double jumlah) {
        try {
            CustomerOrder order = orderDAO.findById(orderId);
            if (order != null) {
                // Create payment record
                Pembayaran pembayaran = new Pembayaran(
                    orderId, kasirId, new Timestamp(System.currentTimeMillis()),
                    metode, jumlah, "berhasil"
                );
                
                if (pembayaranDAO.create(pembayaran)) {
                    // Update order status
                    orderDAO.updateStatus(orderId, "selesai");
                    
                    // Create nota
                    Nota nota = new Nota(
                        orderId, new Timestamp(System.currentTimeMillis()),
                        jumlah, metode, "berhasil"
                    );
                    notaDAO.create(nota);
                    
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<CustomerOrder> getOrdersByCustomer(int customerId) {
        return (List<CustomerOrder>) orderDAO.findById(customerId);
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        return orderDetailDAO.findByOrderId(orderId);
    }

    public boolean cancelOrder(int orderId, String alasan) {
        try {
            // Update order status
            if (orderDAO.updateStatus(orderId, "dibatalkan")) {
                // Record cancellation
                PesananDibatalkanDAO batalDAO = new PesananDibatalkanDAO();
                PesananDibatalkan batal = new PesananDibatalkan(
                    orderId, new Timestamp(System.currentTimeMillis()), alasan
                );
                return batalDAO.create(batal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
