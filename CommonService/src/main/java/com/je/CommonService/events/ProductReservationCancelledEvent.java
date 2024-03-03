package com.je.CommonService.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductReservationCancelledEvent {

	private String productId;
	private int quantity;
	private String orderId;
	private String userId;
	private String reason;
}
