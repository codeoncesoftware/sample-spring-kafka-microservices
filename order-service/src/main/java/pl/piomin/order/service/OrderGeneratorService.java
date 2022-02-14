package pl.piomin.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ioevent.starter.annotations.IOEvent;
import com.ioevent.starter.annotations.IOFlow;
import com.ioevent.starter.annotations.TargetEvent;

import pl.piomin.base.domain.Order;

@IOFlow(name = "Distributed transaction SAGA")
@Service
public class OrderGeneratorService {

	private static final Logger log = LoggerFactory.getLogger(OrderGeneratorService.class);

	@IOEvent(key = "CREATE ORDER", topic = "orders", //
			target = @TargetEvent(key = "ORDER CREATED"))
	public Order createOrder(Order order) {
		log.info("Sent: {}", order);
		return order;
	}

}
