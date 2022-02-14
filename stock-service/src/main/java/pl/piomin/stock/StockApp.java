package pl.piomin.stock;

import java.util.Random;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ioevent.starter.annotations.EnableIOEvent;

import pl.piomin.stock.domain.Product;
import pl.piomin.stock.repository.ProductRepository;

@SpringBootApplication
@EnableIOEvent
public class StockApp {

    private static final Logger LOG = LoggerFactory.getLogger(StockApp.class);

    public static void main(String[] args) {
        SpringApplication.run(StockApp.class, args);
    }



    @Autowired
    private ProductRepository repository;

    @PostConstruct
    public void generateData() {
        Random r = new Random();
        for (int i = 0; i < 1000; i++) {
            int count = r.nextInt(1000);
            Product p = new Product(null, "Product" + i, count, 0);
            repository.save(p);
        }
    }
}
