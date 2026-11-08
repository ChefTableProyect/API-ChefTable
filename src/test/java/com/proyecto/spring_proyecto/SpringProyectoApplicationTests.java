package com.proyecto.spring_proyecto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.proyecto.spring_proyecto.core.entity.CustomerOrder;
import com.proyecto.spring_proyecto.order.OrderIdempotencyService;
import com.proyecto.spring_proyecto.order.dto.CreateOrderItemRequest;
import com.proyecto.spring_proyecto.order.dto.CreateOrderRequest;
import com.proyecto.spring_proyecto.shared.ApiException;

class SpringProyectoApplicationTests {

	private final OrderIdempotencyService idempotencyService = new OrderIdempotencyService();

	@Test
	void equivalentRequestsHaveTheSameFingerprint() {
		var first = new CreateOrderRequest("mesa-01", List.of(
				new CreateOrderItemRequest(2L, 1, " sin cebolla "),
				new CreateOrderItemRequest(1L, 2, null)));
		var second = new CreateOrderRequest(" MESA-01 ", List.of(
				new CreateOrderItemRequest(1L, 2, ""),
				new CreateOrderItemRequest(2L, 1, "sin cebolla")));

		assertThat(idempotencyService.fingerprint(first))
				.isEqualTo(idempotencyService.fingerprint(second));
	}

	@Test
	void reusingAKeyForAnotherRequestIsRejected() {
		var order = new CustomerOrder();
		order.setRequestFingerprint("original");

		assertThatThrownBy(() -> idempotencyService.verifySameRequest(order, "different"))
				.isInstanceOfSatisfying(ApiException.class, exception -> {
					assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
					assertThat(exception.getType()).isEqualTo("IDEMPOTENCY_KEY_REUSED");
				});
	}
}
