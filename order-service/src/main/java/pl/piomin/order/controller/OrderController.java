package pl.piomin.order.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.piomin.base.domain.Order;
import pl.piomin.order.service.OrdersGenerator;
import pl.piomin.order.service.OrderGeneratorService;

@RestController
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private OrdersGenerator ordersGenerator;

	private static final Logger LOG = LoggerFactory.getLogger(OrderController.class);
	private AtomicLong id = new AtomicLong();
	private OrderGeneratorService orderGeneratorService;

	public OrderController(OrderGeneratorService orderGeneratorService) {

		this.orderGeneratorService = orderGeneratorService;
	}

	@PostMapping
	public Order create(@RequestBody Order order) {
		order.setId(id.incrementAndGet());
		return orderGeneratorService.createOrder(order);
	}

	@PostMapping("/generate")
	public boolean create() {
		ordersGenerator.generate();
		return true;
	}

}
