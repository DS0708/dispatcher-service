package com.polarbookshop.dispatcherservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/*
* 통합 테스트
* 함수형 프로그래밍 패러다임을 사용하여 표준 자바로 비즈니스 로직을 구현(Function사용)하면
* 프레임워크의 영향 없이 JUnit으로 단위 테스트를 작성 가능 -> 비즈니스 로직 확인.
* 각 함수가 작동하는지 확인 후, Spring Cloud Function이 함수를 처리하고 설정한 대로
* 노출될 때 애플리케이션의 전반적인 작동을 확인하기 위해 통합 테스트 필요.
* */

/*
* Spring Cloud Function은 통합 테스트의 context를 설정하는데 사용할 수 있는 @FunctionalSpringBootTest 제공.
* 단위 테스트와 달리 함수를 직접 호출하지 않고 프레임워크로부터 함수를 제공받음 -> FunctionCatalog로부터 제공받음.
* 이때, 프레임워크는 작성한 구현만 제공되는 것이 아니라 유형 변환, 함수 합성과 같이 Spring Cloud Function이 제공하는 추가 기능도 가짐.
* */
@FunctionalSpringBootTest
public class DispatchingFunctionsIntegrationTests {

    @Autowired
    private FunctionCatalog catalog;

    @Test
    void packAndLabelOrder(){
        Function<OrderAcceptedMessage, Flux<OrderDispatchedMessage>>
                packAndLabel = catalog.lookup(  //FunctionCatalog로부터 합성 함수를 가져온다.
                Function.class,
                "pack|label"
        );
        long orderId = 121;

        StepVerifier.create(
                packAndLabel.apply(new OrderAcceptedMessage(orderId))   //함수에 대한 입력인 OrderAcceptedMessage를 정의
                )
                //함수의 출력이 OrderDispatchedMessage 객체인지 확인
                .expectNextMatches(dispatchedOrder -> dispatchedOrder.equals(new OrderDispatchedMessage(orderId)))
                .verifyComplete();
    }

    @Test
    void packOrder() {
        Function<OrderAcceptedMessage, Long> pack = catalog.lookup(Function.class, "pack");
        long orderId = 121;
        assertThat(pack.apply(new OrderAcceptedMessage(orderId))).isEqualTo(orderId);
    }

    @Test
    void labelOrder() {
        Function<Flux<Long>, Flux<OrderDispatchedMessage>> label = catalog.lookup(Function.class, "label");
        Flux<Long> orderId = Flux.just(121L);

        StepVerifier.create(label.apply(orderId))
                .expectNextMatches(dispatchedOrder ->
                        dispatchedOrder.equals(new OrderDispatchedMessage(121L)))
                .verifyComplete();
    }
}
