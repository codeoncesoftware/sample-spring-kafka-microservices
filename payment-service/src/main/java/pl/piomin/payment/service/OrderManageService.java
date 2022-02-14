package pl.piomin.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ioevent.starter.annotations.IOEvent;
import com.ioevent.starter.annotations.IOFlow;
import com.ioevent.starter.annotations.SourceEvent;
import com.ioevent.starter.annotations.TargetEvent;

import pl.piomin.base.domain.Order;
import pl.piomin.payment.domain.Customer;
import pl.piomin.payment.repository.CustomerRepository;

@IOFlow(name = "Distributed transaction SAGA")
@Service
public class OrderManageService {
 
	private static final String SOURCE = "payment";
	private static final Logger LOG = LoggerFactory.getLogger(OrderManageService.class);
	private CustomerRepository repository;

	public OrderManageService(CustomerRepository repository) {
		this.repository = repository;
	}

	@IOEvent(key = "PAYMENT  CHECK", source = @SourceEvent(key = "ORDER CREATED", topic = "orders"),//
			target = @TargetEvent(key = "PAYMENT CHECKED", topic = "payment-orders")//
	)
	public Order reserve(Order order) {
		Customer customer = repository.findById(order.getCustomerId()).orElseThrow();
		LOG.info("Found: {}", customer);
		if (order.getPrice() < customer.getAmountAvailable()) {
			order.setStatus("ACCEPT");
			customer.setAmountReserved(customer.getAmountReserved() + order.getPrice());
			customer.setAmountAvailable(customer.getAmountAvailable() - order.getPrice());
		} else {
			order.setStatus("REJECT");
		}
		order.setSource(SOURCE);
		repository.save(customer);
		LOG.info("Sent: {}", order);
		return order;
	}

	@IOEvent(key = "UPDATE PAYEMENT", topic = "orders", source = @SourceEvent(key = "STATE UPDATED"))
	public void confirm(Order order) {
		Customer customer = repository.findById(order.getCustomerId()).orElseThrow();
		LOG.info("Found: {}", customer);
		if (order.getStatus().equals("CONFIRMED")) {
			customer.setAmountReserved(customer.getAmountReserved() - order.getPrice());
			repository.save(customer);
		} else if (order.getStatus().equals("ROLLBACK") && !order.getSource().equals(SOURCE)) {
			customer.setAmountReserved(customer.getAmountReserved() - order.getPrice());
			customer.setAmountAvailable(customer.getAmountAvailable() + order.getPrice());
			repository.save(customer);
		}

	}
}
