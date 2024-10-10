package com.polarbookshop.dispatcherservice;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class) //테스트 바인더에 대한 설정을 제공하는 클래스 import
public class FunctionsStreamIntegrationTests {

    //입력 바인딩 packlabel-in-0, 입력 바인딩은 하나여서 기본 설정상 이 바인딩을 나타냄
    @Autowired
    private InputDestination input;
    //출력 바인딩 packlabel-out-0, 출력 바인딩도 하나여서 기본 설정상 이 바인딩을 나타냄
    @Autowired
    private OutputDestination output;
    //JSON 메시지 페이로드를 자바 객체로 역직렬화하기 위해 잭슨을 사용
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenOrderAcceptedThenDispatched() throws IOException {
        long orderId = 121;
        //데이터 흐름은 Message 객체를 기반으로 하며, Message 객체를 명시적으로 제공해야함.
        Message<OrderAcceptedMessage> inputMessage = MessageBuilder
                .withPayload(new OrderAcceptedMessage(orderId)).build();
        Message<OrderDispatchedMessage> expectedOutputMessage = MessageBuilder
                .withPayload(new OrderDispatchedMessage(orderId)).build();

        this.input.send(inputMessage); //입력 채널로 메시지를 보냄
        /*
        * RabbitMQ 같은 Message Broker는 이진 데이터를 처리하므로 이를 통해 흐르는 모든 데이터는 자바의 byte[]에 매핑되며
        * byte[]와 DTO 간의 변환은 Spring Cloud Stream에서 내부적으로 알아서 처리.
        * 하지만 출력 채널에서 수신된 메시지의 내용을 확인하려면 명시적으로 처리가 필요하다.
        * */
        assertThat(objectMapper.readValue(output.receive().getPayload(), OrderDispatchedMessage.class)) //메시지를 읽고, OrderDispatchedMessage 타입의 자바 객체로 변환
                .isEqualTo(expectedOutputMessage.getPayload()); //출력 채널로부터 메시지를 받아서 확인
    }
}
