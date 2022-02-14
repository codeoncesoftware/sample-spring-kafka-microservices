package pl.piomin.payment;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ioevent.starter.annotations.EnableIOEvent;

import pl.piomin.payment.domain.Customer;
import pl.piomin.payment.repository.CustomerRepository;

@SpringBootApplication
@EnableIOEvent
public class PaymentApp {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentApp.class);

    public static void main(String[] args) {
        SpringApplication.run(PaymentApp.class, args);
    }

    @Autowired
    private CustomerRepository repository;

    @PostConstruct
    public void generateData() {
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            int count = r.nextInt(1000);
            Customer c = new Customer(null, "Customer" + i, count, 0);
            repository.save(c);
        }
    }
}
