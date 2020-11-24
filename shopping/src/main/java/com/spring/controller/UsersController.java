package com.spring.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spring.constants.ResponseCode;
import com.spring.constants.WebConstants;
import com.spring.model.Address;
import com.spring.model.Bufcart;
import com.spring.model.Product;
import com.spring.model.User;
import com.spring.repository.AddressRepository;
import com.spring.repository.CartRepository;
import com.spring.repository.OrderRepository;
import com.spring.repository.ProductRepository;
import com.spring.repository.UserRepository;
import com.spring.response.cartResp;
import com.spring.response.response;
import com.spring.response.serverResp;
import com.spring.response.userResp;
import com.spring.util.Validator;
import com.spring.util.jwtUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UsersController {

    private static Logger logger = Logger.getLogger(UsersController.class.getName());

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AddressRepository addrRepo;

    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private jwtUtil jwtutil;

    @PostMapping("/")
    public ResponseEntity<serverResp> addUser(@Valid @RequestBody User user) {

        serverResp resp = new serverResp();
        try {
            if (Validator.isUserEmpty(user)) {
                resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
                resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
            } else if (!Validator.isValidEmail(user.getEmail())) {
                resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
                resp.setMessage(ResponseCode.INVALID_EMAIL_FAIL_MSG);
            } else {
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.CUST_REG);
                User reg = userRepo.save(user);
                resp.setObject(reg);
            }
        } catch (Exception e) {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(e.getMessage());
        }
        return new ResponseEntity<serverResp>(resp, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{username}/verify")
    public ResponseEntity<serverResp> verifyUser(@Valid @RequestBody String password, @PathVariable String username) {

        User user = userRepo.findByUsername(username);
        String email = user.getEmail();

        User loggedUser = userRepo.findByEmailAndPassword(email, password);
        serverResp resp = new serverResp();
        if (loggedUser != null) {
            String jwtToken = jwtutil.createToken(email, password, WebConstants.USER_CUST_ROLE);
            resp.setStatus(ResponseCode.SUCCESS_CODE);
            resp.setMessage(ResponseCode.SUCCESS_MESSAGE);
            resp.setAUTH_TOKEN(jwtToken);
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<serverResp>(resp, HttpStatus.OK);
    }

    @PutMapping("/{username}/address")
    public ResponseEntity<userResp> addAddress(@Valid @RequestBody Address address,
                                               @RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                               @PathVariable String username) {
        userResp resp = new userResp();
        if (Validator.isAddressEmpty(address)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                User user = jwtutil.checkToken(AUTH_TOKEN);
                user.setAddress(address);
                address.setUser(user);
                Address adr = addrRepo.saveAndFlush(address);
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.CUST_ADR_ADD);
                resp.setUser(user);
                resp.setAddress(adr);
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
        return new ResponseEntity<userResp>(resp, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{username}/address")
    public ResponseEntity<response> getAddress(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN) {

        response resp = new response();
        if (jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                User user = jwtutil.checkToken(AUTH_TOKEN);
                Address adr = addrRepo.findByUser(user);

                HashMap<String, String> map = new HashMap<>();
                map.put(WebConstants.ADR_NAME, adr.getAddress());
                map.put(WebConstants.ADR_CITY, adr.getCity());
                map.put(WebConstants.ADR_STATE, adr.getState());
                map.put(WebConstants.ADR_COUNTRY, adr.getCountry());
                map.put(WebConstants.ADR_ZP, String.valueOf(adr.getZipcode()));
                map.put(WebConstants.PHONE, adr.getPhonenumber());

                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.CUST_ADR_ADD);
                resp.setMap(map);
                // resp.setAddress(adr);
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
        return new ResponseEntity<response>(resp, HttpStatus.ACCEPTED);
    }

    @PostMapping("/{username}/cart")
    public ResponseEntity<serverResp> addToCart(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                                @RequestParam(WebConstants.PROD_ID) String productId) throws IOException {

        serverResp resp = new serverResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                User loggedUser = jwtutil.checkToken(AUTH_TOKEN);
                Product cartItem = prodRepo.findByProductid(Integer.parseInt(productId));

                Bufcart buf = new Bufcart();
                buf.setEmail(loggedUser.getEmail());
                buf.setQuantity(1);
                buf.setPrice(cartItem.getPrice());
                buf.setProductId(Integer.parseInt(productId));
                buf.setProductname(cartItem.getProductname());
                Date date = new Date();
                buf.setDateAdded(date);

                cartRepo.save(buf);
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.CART_UPD_MESSAGE_CODE);
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

    @GetMapping("/{username}/cart")
    public ResponseEntity<cartResp> viewCart(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN)
            throws IOException {
        logger.info("Inside View cart request method");
        cartResp resp = new cartResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                logger.info("Inside View cart request method 2");
                User loggedUser = jwtutil.checkToken(AUTH_TOKEN);
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.VW_CART_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(cartRepo.findByEmail(loggedUser.getEmail()));
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<cartResp>(resp, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{username}/cart/{bufcartId}")
    public ResponseEntity<cartResp> updateCart(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                               @PathVariable(name = WebConstants.BUF_ID) String bufcartId,
                                               @RequestParam(name = WebConstants.BUF_QUANTITY) String quantity) throws IOException {

        cartResp resp = new cartResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                User loggedUser = jwtutil.checkToken(AUTH_TOKEN);
                Bufcart userCart = cartRepo.findByBufcartIdAndEmail(Integer.parseInt(bufcartId), loggedUser.getEmail());
                userCart.setQuantity(Integer.parseInt(quantity));
                cartRepo.save(userCart);
                List<Bufcart> bufcarts = cartRepo.findByEmail(loggedUser.getEmail());
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.UPD_CART_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(bufcarts);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<cartResp>(resp, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{username}/cart")
    public ResponseEntity<cartResp> delCart(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                            @RequestParam(name = WebConstants.BUF_ID) String bufcartid) throws IOException {

        cartResp resp = new cartResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                User loggedUser = jwtutil.checkToken(AUTH_TOKEN);
                cartRepo.deleteByBufcartIdAndEmail(Integer.parseInt(bufcartid), loggedUser.getEmail());
                List<Bufcart> bufcartlist = cartRepo.findByEmail(loggedUser.getEmail());
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.DEL_CART_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(bufcartlist);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<cartResp>(resp, HttpStatus.ACCEPTED);
    }
}
