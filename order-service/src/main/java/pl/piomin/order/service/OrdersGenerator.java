package pl.piomin.order.service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import pl.piomin.base.domain.Order;

@Service
public class OrdersGenerator {

	@Autowired
	OrderGeneratorService orderGeneratorService;
	private static final Logger log = LoggerFactory.getLogger(OrdersGenerator.class);

	private static Random RAND = new Random();
	private AtomicLong id = new AtomicLong();
	
	@Async
	public void generate() {
		for (int i = 0; i < 100; i++) {
			int x = RAND.nextInt(5) + 1;
			Order o = new Order(id.incrementAndGet(), Long.valueOf(RAND.nextInt(100) + 1),
					Long.valueOf(RAND.nextInt(100) + 1), "NEW");
			o.setPrice(100 * x);
			o.setProductCount(x);
			orderGeneratorService.createOrder(o);
		}
	}
}
