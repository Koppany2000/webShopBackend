package com.example.webshopapp.service.impl;

import com.example.webshopapp.entity.Cart;
import com.example.webshopapp.entity.OrderMain;
import com.example.webshopapp.entity.ProductInOrder;
import com.example.webshopapp.entity.User;
import com.example.webshopapp.repository.CartRepository;
import com.example.webshopapp.repository.OrderRepository;
import com.example.webshopapp.repository.ProductInOrderRepository;
import com.example.webshopapp.repository.UserRepository;
import com.example.webshopapp.service.CartService;
import com.example.webshopapp.service.ProductService;
import com.example.webshopapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {

    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductInOrderRepository productInOrderRepository;
    private final CartRepository cartRepository;
    private final UserService userService;

    @Override
    public Cart getCart(User user) {
        return user.getCart();
    }

    @Override
    @Transactional
    public void mergeLocalCart(Collection<ProductInOrder> productInOrders, User user) {
        Cart finalCart = user.getCart();
        productInOrders.forEach(productInOrder -> {
            Set<ProductInOrder> set = finalCart.getProducts();
            Optional<ProductInOrder> old = set.stream().filter(e -> e.getProductId().equals(productInOrder.getProductId())).findFirst();
            ProductInOrder prod;
            if (old.isPresent()) {
                prod = old.get();
                prod.setCount(productInOrder.getCount() + prod.getCount());
            } else {
                prod = productInOrder;
                prod.setCart(finalCart);
                finalCart.getProducts().add(prod);
            }
            productInOrderRepository.save(prod);
        });
        cartRepository.save(finalCart);

    }

    @Override
    @Transactional
    public void delete(String itemId, User user) {
        var op = user.getCart().getProducts().stream().filter(e -> itemId.equals(e.getProductId())).findFirst();
        op.ifPresent(productInOrder -> {
            productInOrder.setCart(null);
            productInOrderRepository.deleteById(productInOrder.getId());
        });
    }



    @Override
    @Transactional
    public void checkout(User user) {
        // Creat an order
        OrderMain order = new OrderMain(user);
        orderRepository.save(order);

        // clear cart's foreign key & set order's foreign key& decrease stock
        user.getCart().getProducts().forEach(productInOrder -> {
            productInOrder.setCart(null);
            productInOrder.setOrderMain(order);
            productService.decreaseStock(productInOrder.getProductId(), productInOrder.getCount());
            productInOrderRepository.save(productInOrder);
        });

    }
}
