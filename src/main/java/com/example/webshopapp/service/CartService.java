package com.example.webshopapp.service;

import com.example.webshopapp.entity.Cart;
import com.example.webshopapp.entity.ProductInOrder;
import com.example.webshopapp.entity.User;

import java.util.Collection;

public interface CartService {
    Cart getCart(User user);

    void mergeLocalCart(Collection<ProductInOrder> productInOrders, User user);

    void delete(String itemId, User user);

    void checkout(User user);
}