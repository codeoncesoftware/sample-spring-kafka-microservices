package pl.piomin.order.service;

import org.springframework.stereotype.Service;

import com.ioevent.starter.annotations.GatewaySourceEvent;
import com.ioevent.starter.annotations.IOEvent;
import com.ioevent.starter.annotations.IOPayload;
import com.ioevent.starter.annotations.SourceEvent;
import com.ioevent.starter.annotations.TargetEvent;

import pl.piomin.base.domain.Order;

@Service
public class OrderManageService {

	@IOEvent(key = "JOIN STATE", //
			gatewaySource = @GatewaySourceEvent(parallel = true, source = { //
					@SourceEvent(key = "PAYMENT CHECKED", topic = "payment-orders"), //
					@SourceEvent(key = "STOCK CHECKED", topic = "stock-orders") }), //
			target = @TargetEvent(key = "STATE UPDATED", topic = "orders"))
	   public Order confirm(@IOPayload(index = 0) Order orderPayment,@IOPayload(index = 1) Order orderStock) {
        Order o = new Order(orderPayment.getId(),
                orderPayment.getCustomerId(),
                orderPayment.getProductId(),
                orderPayment.getProductCount(),
                orderPayment.getPrice());
        if (orderPayment.getStatus().equals("ACCEPT") &&
                orderStock.getStatus().equals("ACCEPT")) {
            o.setStatus("CONFIRMED");
        } else if (orderPayment.getStatus().equals("REJECT") &&
                orderStock.getStatus().equals("REJECT")) {
            o.setStatus("REJECTED");
        } else if (orderPayment.getStatus().equals("REJECT") ||
                orderStock.getStatus().equals("REJECT")) {
            String source = orderPayment.getStatus().equals("REJECT")
                    ? "PAYMENT" : "STOCK";
            o.setStatus("ROLLBACK");
            o.setSource(source);
        }
        return o;
    }
}
