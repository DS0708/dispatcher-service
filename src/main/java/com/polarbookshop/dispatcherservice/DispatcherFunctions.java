package com.polarbookshop.dispatcherservice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
public class DispatcherFunctions {
    private static final Logger log = LoggerFactory.getLogger(DispatcherFunctions.class);

    //Imperative Function
    @Bean //Spring Cloud Function은 함수를 빈으로 등록하면 인식할 수 있다.
    public Function<OrderAcceptedMessage, Long> pack(){ //주문을 포장하는 비즈니스 로직을 구현하는 함수, 자바 표준 인터페이스로 구현되었다는 것에 주의(Spring 필요없음)
        return orderAcceptedMessage -> { //OrderAcceptedMessage를 입력으로 받음.
            log.info("The order with id {} is packed.", orderAcceptedMessage.orderId());
            return orderAcceptedMessage.orderId();  //주문의 식별자(Long 타입)를 출력으로 반환.
        };
    }

    //Reactive Function
    @Bean
    public Function<Flux<Long>, Flux<OrderDispatchedMessage>> label() { //Flux를 사용함으로써 리액티브하게 동작(Spring Cloud Function은 Imperative와 Reactive를 모두 지원하며 함수의 시그니처에 따라 다르게 사용)
        return orderFlux -> orderFlux.map(orderId -> { //주문의 아이디(Long 타입)을 입력으로 받는다.
            log.info("The order with id {} is labeled.", orderId);
            return new OrderDispatchedMessage(orderId); //출력으로 OrderDispatchedMessage를 반환.
        });
    }
}
