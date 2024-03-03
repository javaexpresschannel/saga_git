package com.je.CommonService.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelProductReservationCommand {

	@TargetAggregateIdentifier
	private String productId;
	private int quantity;
	private String ordreId;
	private String userId;
	private String reason;
}
