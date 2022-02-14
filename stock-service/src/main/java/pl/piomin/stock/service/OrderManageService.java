package pl.piomin.stock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ioevent.starter.annotations.IOEvent;
import com.ioevent.starter.annotations.IOFlow;
import com.ioevent.starter.annotations.SourceEvent;
import com.ioevent.starter.annotations.TargetEvent;

import pl.piomin.base.domain.Order;
import pl.piomin.stock.domain.Product;
import pl.piomin.stock.repository.ProductRepository;

@IOFlow(name = "Distributed transaction SAGA")
@Service
public class OrderManageService {

    private static final String SOURCE = "stock";
    private static final Logger LOG = LoggerFactory.getLogger(OrderManageService.class);
    private ProductRepository repository;

    public OrderManageService(ProductRepository repository) {
        this.repository = repository;
    }
	@IOEvent(key = "STOCK  CHECK", source = @SourceEvent(key="ORDER CREATED",topic = "orders"),//
			target = @TargetEvent(key = "STOCK CHECKED", topic = "stock-orders"))
    public Order reserve(Order order) {
		 Product product = repository.findById(order.getProductId()).orElseThrow();
	        LOG.info("Found: {}", product);
	            if (order.getProductCount() < product.getAvailableItems()) {
	                product.setReservedItems(product.getReservedItems() + order.getProductCount());
	                product.setAvailableItems(product.getAvailableItems() - order.getProductCount());
	                order.setStatus("ACCEPT");
	                repository.save(product);
	            } else {
	                order.setStatus("REJECT");
	            }
	            order.setSource(SOURCE);
	            LOG.info("Sent: {}", order);
	            return order;
	        }
            
      
    
	

	@IOEvent(key = "UPDATE STOCK",topic = "orders", source = @SourceEvent(key="STATE UPDATED"))
    public void confirm(Order order) {
        Product product = repository.findById(order.getProductId()).orElseThrow();
        LOG.info("Found: {}", product);
        if (order.getStatus().equals("CONFIRMED")) {
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            repository.save(product);
        } else if (order.getStatus().equals("ROLLBACK") && !order.getSource().equals(SOURCE)) {
            product.setReservedItems(product.getReservedItems() - order.getProductCount());
            product.setAvailableItems(product.getAvailableItems() + order.getProductCount());
            repository.save(product);
        }
    }

}
