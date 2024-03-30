package com.example.springboot.mock_test;

import static org.mockito.ArgumentMatchers.any;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HelloServiceSpyTest {

    @Spy
    HelloRepository helloRepository;

    @Mock
    HelloClient helloClient;

    @InjectMocks
    HelloService helloService;

    @Test
    void spyTest() {
        // given
        String clientMockData = "HelloClient Mock 데이터";
        String methodData = "HelloRepository findById 데이터";
        BDDMockito.given(helloClient.getData())
            .willReturn(clientMockData);

        // when
        String saveData = helloService.save();

        String findData = helloRepository.findById(methodData);

        // then
        Assertions.assertThat(saveData).isEqualTo(clientMockData);
        Assertions.assertThat(findData).isEqualTo(methodData);
    }
}
