package com.je.PaymentService.command.api.events;

import com.je.CommonService.events.PaymentCancelledEvent;
import com.je.CommonService.events.PaymentProcessedEvent;
import com.je.PaymentService.command.api.data.Payment;
import com.je.PaymentService.command.api.data.PaymentRepository;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PaymentsEventHandler {

	@Autowired
    private PaymentRepository paymentRepository;

    @EventHandler
    public void on(PaymentProcessedEvent event) {
        Payment payment
                = Payment.builder()
                .paymentId(event.getPaymentId())
                .orderId(event.getOrderId())
                .paymentStatus("COMPLETED")
                .timeStamp(new Date())
                .build();
        paymentRepository.save(payment);
    }

    @EventHandler
    public void on(PaymentCancelledEvent event) {
        Payment payment
                = paymentRepository.findById(event.getPaymentId()).get();
        payment.setPaymentStatus(event.getPaymentStatus());
        paymentRepository.save(payment);
    }
}
