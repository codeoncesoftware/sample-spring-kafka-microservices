# Distributed Transactions in Microservices with IOEvent ;)

## Description
This repository is used as the IOEvent Version of the example for the following articles:

1. [Distributed Transactions in Microservices with Kafka Streams and Spring Boot](https://piotrminkowski.com/2022/01/24/distributed-transactions-in-microservices-with-kafka-streams-and-spring-boot/)

In this project we are modifying the original code using our framework (IOEvent) to get simpler code that serves the same purpose as the initial project
## Basic changes

In this project we adapted the code using our framework by making these basic changes on each microservice: 

1.Remove kafka and kafka-streams dependency and add IOEvent starter dependency to Microservices ( order-service , payment-service and stock-service)   : 

		<dependency>
			<groupId>io.ioevent</groupId>
			<artifactId>ioevent-spring-boot-starter</artifactId>
			<version>1.0.0-beta</version>
		</dependency>
		 
		
2.Remove `@EnableKafkaStreams` or `@EnableKafka` from the main class of each microservice and add the `@EnableIOEvent` for each main class 

3.Set our properties for each microservice :
	exemple (order-service):
	

		spring.application.name: orders
		spring.kafka:
			bootstrap-servers: localhost:29092
		ioevent:
 		  	prefix: DistributedTransactions
   			group_id: orders
 		    api_key: 7e3f052e-1bcc-4077-a713-aab44d5b1117
    		eureka:
    		  enabled: true
		eureka:
 			 instance:
 			   preferIpAddress: true
 			 client:
  			   service-url:
  				  defaultZone: http://localhost:8761/eureka
		server:
 			 port: 8087



## Specific changes

After making th basics changes in microservices , now we will list the specific changes for each microservice : 

**Order-Service :**

1.First of all, in OrderApp.java there is no need to create kafka topics or create stream to join events, all this is handled by **IOEvent**.  <br />
2.We remove all kafkatemplate calls and injections in the controller or the service  
2.Passing to services : <br /> - For OrderGeneratorService we used `@IOFlow` on the class  with specifying the name of our flow  :  <br />


>`@IOFlow(name = "Distributed transaction SAGA")`

then we create the method responsible for the creation of orders and sending them in kafka `createOrder` and we use `@IOEvent` to specify the (task name,topic name,target event )   <br />

>`@IOEvent(key = "CREATE ORDER", topic = "orders",target = @TargetEvent(key = "ORDER CREATED"))` <br />
>`	public Order createOrder(Order order) {` <br />
>`	log.info("Sent: {}", order);` <br />
>`return order;` <br />
>`}`

-For OrderManageService , we used the same confirm method we just add `@IOEvent` on it , in the annotation we specified that we are waiting for two parallel event from two topics (payment-orders and stock-orders) and the output will be sent to the topic (orders).


>`@IOEvent(key = "JOIN STATE",gatewaySource = @GatewaySourceEvent(parallel = true, source = {@SourceEvent(key = "PAYMENT CHECKED", topic = "payment-orders"),` 
				`@SourceEvent(key = "STOCK CHECKED", topic = "stock-orders") }), `
			`target = @TargetEvent(key = "STATE UPDATED", topic = "orders"))` <br />
	   `public Order confirm(@IOPayload(index = 0) Order orderPayment,@IOPayload(index = 1) Order orderStock) {.....}`
	   
in the method parameters we used `@IOPayload(index = ? )` to consume each Order by the source index declared in the  `@IOEvent` above.
   
	   
**Payment-Service and Stock-Service :**


1.As the Order service in this microservices there is no need for creating listeners or calling kafkatemplate  ,  all this is handled by **IOEvent**.

2.Services :
-We used the same reserve method and we add `@IOEvent` on it , in the annotation we specified that we are waiting for an "ORDER CREATED" event from the topic (orders) and the output of the method will be sent to the topic (payment-orders).

>`@IOEvent(key = "PAYMENT  CHECK", source = @SourceEvent(key = "ORDER CREATED", topic = "orders"),` <br />
`target = @TargetEvent(key = "PAYMENT CHECKED", topic = "payment-orders"))` <br />
	`public Order reserve(Order order) {...}`
	

-We used the same confirm method and we add `@IOEvent` on it , in the annotation we specified that we are waiting for an "STATE UPDATED" event from two topics (orders) and without a target as an end event .

>`@IOEvent(key = "UPDATE STOCK",topic = "orders", source = @SourceEvent(key="STATE UPDATED"))`<br />
   ` public void confirm(Order order) {...}` 


we make the same change on both microservices Service with changing just the names of sources and the targets events (PAYMENT / STOCK ) and topics ( payment-orders / stock-orders )


## Conclusion

In this project we adapted the Distributed Transactions artical code with **IOEvent** , we reached the same purpose with less code and less configuration , thanks to **IOEvent** we can connect microservices with a fast and simple way using annotations also we can supervise our microservices using **IOEvent-Admin** to display the IOFLOW diagram created by annotation and supervise instances executed.

![image](https://raw.githubusercontent.com/codeoncesoftware/sample-spring-kafka-microservices/IOEvent-version/img/run%202.PNG)
