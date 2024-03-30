package com.example.springboot.mock_test;

import static org.mockito.ArgumentMatchers.any;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HelloServiceMockTest {

    @Mock
    HelloClient helloClient;

    @Mock
    HelloRepository helloRepository;

    @InjectMocks
    HelloService helloService;

    @Test
    void mockTest() {
        // given
        String clientMockData = "HelloClient Mock 데이터";
        String repositoryMockData = "HelloRepository Mock 데이터";
        BDDMockito.given(helloClient.getData())
            .willReturn(clientMockData);
        BDDMockito.given(helloRepository.save(any()))
            .willReturn(repositoryMockData);

        // when
        String data = helloService.save();

        // then
        Assertions.assertThat(data).isEqualTo(repositoryMockData);
    }
}
