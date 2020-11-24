package com.spring.controller;

import com.spring.constants.ResponseCode;
import com.spring.constants.WebConstants;
import com.spring.model.Product;
import com.spring.repository.OrderRepository;
import com.spring.repository.ProductRepository;
import com.spring.response.prodResp;
import com.spring.response.serverResp;
import com.spring.util.Validator;
import com.spring.util.jwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/products")
public class ProductsController {
    @Autowired
    private ProductRepository prodRepo;
    @Autowired
    private jwtUtil jwtutil;

    @GetMapping("")
    public ResponseEntity<prodResp> getProducts(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN)
            throws IOException {

        prodResp resp = new prodResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.LIST_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(prodRepo.findAll());
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<prodResp>(resp, HttpStatus.ACCEPTED);
    }

    @PostMapping("/")
    public ResponseEntity<prodResp> addProduct(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                               @RequestParam(name = WebConstants.PROD_FILE) MultipartFile prodImage,
                                               @RequestParam(name = WebConstants.PROD_DESC) String description,
                                               @RequestParam(name = WebConstants.PROD_PRICE) String price,
                                               @RequestParam(name = WebConstants.PROD_NAME) String productname,
                                               @RequestParam(name = WebConstants.PROD_QUANITY) String quantity) throws IOException {
        prodResp resp = new prodResp();
        if (Validator.isStringEmpty(productname) || Validator.isStringEmpty(description)
                || Validator.isStringEmpty(price) || Validator.isStringEmpty(quantity)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                Product prod = new Product();
                prod.setDescription(description);
                prod.setPrice(Double.parseDouble(price));
                prod.setProductname(productname);
                prod.setQuantity(Integer.parseInt(quantity));
                prod.setProductimage(prodImage.getBytes());
                prodRepo.save(prod);

                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.ADD_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(prodRepo.findAll());
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        }
        return new ResponseEntity<prodResp>(resp, HttpStatus.ACCEPTED);
    }


    @PutMapping("/{productId}")
    public ResponseEntity<serverResp> updateProducts(
            @RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
            @RequestParam(name = WebConstants.PROD_FILE, required = false) MultipartFile prodImage,
            @RequestParam(name = WebConstants.PROD_DESC) String description,
            @RequestParam(name = WebConstants.PROD_PRICE) String price,
            @RequestParam(name = WebConstants.PROD_NAME) String productname,
            @RequestParam(name = WebConstants.PROD_QUANITY) String quantity,
            @PathVariable(name = WebConstants.PROD_ID) String productid) throws IOException {
        serverResp resp = new serverResp();
        if (Validator.isStringEmpty(productname) || Validator.isStringEmpty(description)
                || Validator.isStringEmpty(price) || Validator.isStringEmpty(quantity)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                Product prodOrg;
                Product prod;
                if (prodImage != null) {
                    prod = new Product(Integer.parseInt(productid), description, productname, Double.parseDouble(price),
                            Integer.parseInt(quantity), prodImage.getBytes());
                } else {
                    prodOrg = prodRepo.findByProductid(Integer.parseInt(productid));
                    prod = new Product(Integer.parseInt(productid), description, productname, Double.parseDouble(price),
                            Integer.parseInt(quantity), prodOrg.getProductimage());
                }
                prodRepo.save(prod);
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.UPD_SUCCESS_MESSAGE);
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

    @DeleteMapping("/{productId}")
    public ResponseEntity<prodResp> delProduct(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                               @PathVariable(name = WebConstants.PROD_ID) String productId) throws IOException {
        prodResp resp = new prodResp();
        if (Validator.isStringEmpty(productId)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                prodRepo.deleteByProductid(Integer.parseInt(productId));
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.DEL_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(prodRepo.findAll());
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.toString());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<prodResp>(resp, HttpStatus.ACCEPTED);
    }
}
