package com.example.webshopapp.service;

import com.example.webshopapp.entity.ProductInOrder;
import com.example.webshopapp.entity.User;

public interface ProductInOrderService {
    void update(String itemId, Integer quantity, User user);
    ProductInOrder findOne(String itemId, User user);
}
