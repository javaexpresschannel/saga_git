package com.je.OrderService.command.api.saga;

import java.util.UUID;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import com.je.CommonService.commands.CancelOrderCommand;
import com.je.CommonService.commands.CancelPaymentCommand;
import com.je.CommonService.commands.CancelProductReservationCommand;
import com.je.CommonService.commands.CompleteOrderCommand;
import com.je.CommonService.commands.ReserveProductCommand;
import com.je.CommonService.commands.ShipOrderCommand;
import com.je.CommonService.commands.ValidatePaymentCommand;
import com.je.CommonService.events.OrderCancelledEvent;
import com.je.CommonService.events.OrderCompletedEvent;
import com.je.CommonService.events.OrderShippedEvent;
import com.je.CommonService.events.PaymentCancelledEvent;
import com.je.CommonService.events.PaymentProcessedEvent;
import com.je.CommonService.events.ProductReservedEvent;
import com.je.CommonService.model.User;
import com.je.CommonService.queries.GetUserPaymentDetailsQuery;
import com.je.OrderService.command.api.events.OrderCreatedEvent;

import lombok.extern.slf4j.Slf4j;

@Saga
@Slf4j
public class OrderProcessingSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;


    public OrderProcessingSaga() {
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent in Saga for Order Id : {}",
                event.getOrderId());
        
        reserveProductCommand(event);
        
        GetUserPaymentDetailsQuery getUserPaymentDetailsQuery
                = new GetUserPaymentDetailsQuery(event.getUserId());

        User user = null;

        try {
//        	if(true)
//        		throw new Exception();
            user = queryGateway.query(
                    getUserPaymentDetailsQuery,
                    ResponseTypes.instanceOf(User.class)
            ).join();

        } catch (Exception e) {
            log.error(e.getMessage());
            //Start the Compensating transaction
            cancelOrderCommand(event.getOrderId());
            //cancelProductReservation(event,"Could not process user payment with provided payment details");
        }

        ValidatePaymentCommand validatePaymentCommand
                = ValidatePaymentCommand
                .builder()
                .cardDetails(user.getCardDetails())
                .orderId(event.getOrderId())
                .paymentId(UUID.randomUUID().toString())
                .build();

        commandGateway.sendAndWait(validatePaymentCommand);
    }

	private void reserveProductCommand(OrderCreatedEvent event) {
		ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
				.orderId(event.getOrderId()).productId(event.getProductId())
				.quantity(event.getQuantity()).userId(event.getUserId()).build();

		log.info("OrderCreated Event handled for orderId {} and productId {}", reserveProductCommand.getOrderId(),
				reserveProductCommand.getProductId());

		commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

			@Override
			public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
					CommandResultMessage<? extends Object> commandResultMessage) {
				if (commandResultMessage.isExceptional()) {
					// start a compenstating transaction
				}
			}

		});
	}
	
	private void cancelProductReservation(OrderCreatedEvent event,String reason) {
		var cancelProductReservationCommand = CancelProductReservationCommand.builder()
				.ordreId(event.getOrderId())
				.productId(event.getProductId())
				.quantity(event.getQuantity())
				.userId(event.getUserId())
				.reason(reason)
				.build();
		commandGateway.send(cancelProductReservationCommand);
	}

    private void cancelOrderCommand(String orderId) {
        CancelOrderCommand cancelOrderCommand
                = new CancelOrderCommand(orderId);
        commandGateway.send(cancelOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    private void handle(PaymentProcessedEvent event) {
        log.info("PaymentProcessedEvent in Saga for Order Id : {}",
                event.getOrderId());
        try {

//            if(true)
//                throw new Exception();

            ShipOrderCommand shipOrderCommand
                    = ShipOrderCommand
                    .builder()
                    .shipmentId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .build();
            commandGateway.send(shipOrderCommand);
        } catch (Exception e) {
            log.error(e.getMessage());
            // Start the compensating transaction
            cancelPaymentCommand(event);
        }
    }

    private void cancelPaymentCommand(PaymentProcessedEvent event) {
        CancelPaymentCommand cancelPaymentCommand
                = new CancelPaymentCommand(
                event.getPaymentId(), event.getOrderId()
        );

        commandGateway.send(cancelPaymentCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderShippedEvent event) {

        log.info("OrderShippedEvent in Saga for Order Id : {}",
                event.getOrderId());

        CompleteOrderCommand completeOrderCommand
                = CompleteOrderCommand.builder()
                .orderId(event.getOrderId())
                .orderStatus("APPROVED")
                .build();

        commandGateway.send(completeOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handle(OrderCompletedEvent event) {
        log.info("OrderCompletedEvent in Saga for Order Id : {}",
                event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handle(OrderCancelledEvent event) {
        log.info("OrderCancelledEvent in Saga for Order Id : {}",
                event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentCancelledEvent event) {
        log.info("PaymentCancelledEvent in Saga for Order Id : {}",
                event.getOrderId());
        cancelOrderCommand(event.getOrderId());
    }
}
