package com.je.ProductService.command.api.events;

import com.je.CommonService.events.ProductReservationCancelledEvent;
import com.je.CommonService.events.ProductReservedEvent;
import com.je.ProductService.command.api.data.Product;
import com.je.ProductService.command.api.data.ProductRepository;

import lombok.extern.slf4j.Slf4j;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product")
@Slf4j
public class ProductEventsHandler {

    private ProductRepository productRepository;

    public ProductEventsHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {
        Product product =
                new Product();
        BeanUtils.copyProperties(event,product);
        productRepository.save(product);
        //throw new Exception("Exception Occurred");
    }

    @ExceptionHandler
    public void handle(Exception exception) throws Exception {
        throw exception;
    }
    
    @EventHandler
	public void on(ProductReservedEvent event) throws Exception {
		Product product = productRepository.findByProductId(event.getProductId());
		log.info("ProductReservedEvent: Current Produt Quantity {}",product.getQuantity());
		product.setQuantity(product.getQuantity() -  event.getQuantity());
		productRepository.save(product);
		log.info("ProductReservedEvent: New Produt Quantity {}",product.getQuantity());
		log.info("ProductReservedEvent is called for productId {} and orderId {} ",
				event.getProductId(),event.getOrderId());
	}
    
    @EventHandler
	public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {
		Product product = productRepository.findByProductId(productReservationCancelledEvent.getProductId());
		log.info("ProductReservationCancelledEvent: Current Produt Quantity {}",product.getQuantity());
		int newQuantity = product.getQuantity() + productReservationCancelledEvent.getQuantity();
		product.setQuantity(newQuantity);
		productRepository.save(product);
		log.info("ProductReservationCancelledEvent: New Produt Quantity {}",newQuantity);

	}
}
