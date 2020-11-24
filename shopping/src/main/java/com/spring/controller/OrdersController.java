package com.spring.controller;

import com.spring.constants.ResponseCode;
import com.spring.constants.WebConstants;
import com.spring.model.Bufcart;
import com.spring.model.PlaceOrder;
import com.spring.model.User;
import com.spring.repository.CartRepository;
import com.spring.repository.OrderRepository;
import com.spring.response.order;
import com.spring.response.serverResp;
import com.spring.response.viewOrdResp;
import com.spring.util.Validator;
import com.spring.util.jwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private OrderRepository ordRepo;
    @Autowired
    private jwtUtil jwtutil;


    @PostMapping("")
    public ResponseEntity<serverResp> placeOrder(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN)
            throws IOException {

        serverResp resp = new serverResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                User loggedUser = jwtutil.checkToken(AUTH_TOKEN);
                PlaceOrder po = new PlaceOrder();
                po.setEmail(loggedUser.getEmail());
                Date date = new Date();
                po.setOrderDate(date);
                po.setOrderStatus(ResponseCode.ORD_STATUS_CODE);
                double total = 0;
                List<Bufcart> buflist = cartRepo.findAllByEmail(loggedUser.getEmail());
                for (Bufcart buf : buflist) {
                    total = +(buf.getQuantity() * buf.getPrice());
                }
                po.setTotalCost(total);
                PlaceOrder res = ordRepo.save(po);
                buflist.forEach(bufcart -> {
                    bufcart.setOrderId(res.getOrderId());
                    cartRepo.save(bufcart);

                });
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.ORD_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<serverResp>(resp, HttpStatus.ACCEPTED);
    }

    @GetMapping("/")
    public ResponseEntity<viewOrdResp> viewOrders(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN)
            throws IOException {

        viewOrdResp resp = new viewOrdResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.VIEW_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                List<order> orderList = new ArrayList<>();
                List<PlaceOrder> poList = ordRepo.findAll();
                poList.forEach((po) -> {
                    order ord = new order();
                    ord.setOrderBy(po.getEmail());
                    ord.setOrderId(po.getOrderId());
                    ord.setOrderStatus(po.getOrderStatus());
                    ord.setProducts(cartRepo.findAllByOrderId(po.getOrderId()));
                    orderList.add(ord);
                });
                resp.setOrderlist(orderList);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<viewOrdResp>(resp, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<serverResp> updateOrders(
            @RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
            @PathVariable(name = WebConstants.ORD_ID) String orderId,
            @RequestParam(name = WebConstants.ORD_STATUS) String orderStatus) throws IOException {

        serverResp resp = new serverResp();
        if (Validator.isStringEmpty(orderId) || Validator.isStringEmpty(orderStatus)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                PlaceOrder pc = ordRepo.findByOrderId(Integer.parseInt(orderId));
                pc.setOrderStatus(orderStatus);
                ordRepo.save(pc);
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.UPD_ORD_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.toString());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<serverResp>(resp, HttpStatus.ACCEPTED);
    }

}
