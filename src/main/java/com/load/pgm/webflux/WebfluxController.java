package com.load.pgm.webflux;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value="/webflux")
public class WebfluxController {

	// webflux 테스트는 boot 환경에서는 어렵다.. 내가 잘 모른다 ㅠ
	// 추후에 시간 될 때, 다른 소스들 좀 보면서 수정해야 할듯
	// java.lang.IllegalStateException: No suitable default ClientHttpConnector found
	
	@GetMapping(value="/hello")
	public void helloMethod() {
		GreetingWebClient gwc = new GreetingWebClient();
		System.out.println(gwc.getResult());
	}
}
