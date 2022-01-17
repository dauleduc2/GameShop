package admin.controller;

import constants.Message;
import constants.Router;
import constants.StatusCode;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import order.daos.OrderDao;
import utils.GetParam;
import utils.Helper;
import order.models.Order;
import orderitem.daos.OrderItemDao;
import product.daos.ProductDao;
import orderitem.models.OrderItem;
import product.models.Product;

@WebServlet(name = "UpdateStatusController", urlPatterns = {"/" + Router.ADMIN_UPDATE_ORDER_CONTROLLER})
public class UpdateStatusController extends HttpServlet {

    protected int processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {
        response.setContentType("text/html;charset=UTF-8");
        OrderDao orderDao = new OrderDao();
        HttpSession session = request.getSession();

        String status = GetParam.getStringParam(request, "status", "status", 3, 255, null);
        int statusValue = -1;
        switch (status) {
            case "Waiting":
                statusValue = 0;
                break;
            case "Confirm":
                statusValue = 1;
                break;
            case "Done":
                statusValue = 2;
                break;
            case "Cancel":
                statusValue = 3;
                break;
            default:
        }
        String address = GetParam.getStringParam(request, "address", "address", 3, 255, null);

        if (address == null) {
            address = "";
        }
        String id = GetParam.getStringParam(request, "id", "user id", 3, 255, null);
        String consigneeName = GetParam.getStringParam(request, "consigneeName", "Consignee name", 3, 255, null);

        if (consigneeName == null) {
            consigneeName = "";
        }

        String phone = GetParam.getStringParam(request, "phone", "phone number", 3, 255, null);

        if (phone == null) {
            consigneeName = "";
        }

        Order order = orderDao.getOrderByOrderId(id);

        // check quantity
        if (order.getStatus() == 0 && statusValue != 3) {
            OrderItemDao orderItemDao = new OrderItemDao();
            ProductDao productDao = new ProductDao();
            ArrayList<OrderItem> orderItems = orderItemDao.getOrderItemByOrderId(order.getId());
            for (OrderItem orderItem : orderItems) {
                Product product = productDao.getProductById(orderItem.getProductId());
                if (product.getQuantity() < orderItem.getQuantity()) {
                    session.setAttribute("quantityError", Message.NOT_ENOUGH_QUANTITY_MESSAGE.getContent());
                    return 2;
                }
            }
        }

        orderDao.updateOrderStatus(id, statusValue, address, phone, consigneeName);
        return 1;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
            //get prev query string
            HttpSession session = request.getSession();
            String prevUrl = (String) session.getAttribute("prevUrl");
            response.sendRedirect(Router.ADMIN_ORDERS_CONTROLLER + "?" + prevUrl);
        } catch (Exception ex) {
            System.out.println(ex);
            // forward on 500
            Helper.setAttribute(request, StatusCode.INTERNAL_SERVER_ERROR.getValue(),
                    Message.INTERNAL_ERROR_TITLE.getContent(),
                    Message.INTERNAL_ERROR_MESSAGE.getContent());
            request.getRequestDispatcher(Router.ERROR).forward(request, response);
        }
    }

}
