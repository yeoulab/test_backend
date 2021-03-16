package com.load.pgm.webflux;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component
public class GreetingHandler {

	static int callCnt;
	
	public Mono<ServerResponse> hello(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
			.body(BodyInserters.fromValue("Hello, Spring!"));
	}
	
	public Mono<ServerResponse> helloLong(ServerRequest request) {
		int randomValue = (int)(Math.random()*1000 + 500);
		System.out.println(String.format("Hello, Spring sleep time is %d", randomValue));
		
		try {
			Thread.sleep(randomValue);
		}
		catch(Exception e){
		}
		
		return ServerResponse.ok().contentType(MediaType.TEXT_PLAIN)
			.body(BodyInserters.fromValue("Hello, Spring!"));
	}
	
	public Mono<ServerResponse> helloException(ServerRequest request){
		System.out.println(String.format("Hello, Exception Call Count : %d", callCnt));
		callCnt++;
		return ServerResponse.ok()
				.body(sayHello(request)
				.onErrorResume(e -> Mono.error(new IllegalArgumentException("username is required")))
				, String.class);
	}
	
	public Mono<String> sayHello(ServerRequest request) throws IllegalArgumentException{
		try {
			return Mono.just("Hello, " + request.queryParam("name").get());
		}catch(Exception e) {
			System.out.println(e.getMessage());
			throw new IllegalArgumentException(e.getMessage());
		}
	}
}
