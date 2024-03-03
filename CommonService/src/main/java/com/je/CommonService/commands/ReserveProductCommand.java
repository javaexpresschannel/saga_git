package com.je.CommonService.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReserveProductCommand {

	@TargetAggregateIdentifier
	private String productId;
	private int quantity;
	private String orderId;
	private String userId;
	
}
