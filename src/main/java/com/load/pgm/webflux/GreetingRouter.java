package com.load.pgm.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@EnableWebFlux
public class GreetingRouter {

	@Bean
	public RouterFunction<ServerResponse> route(GreetingHandler greetingHandler) {

		return RouterFunctions
			.route(RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), greetingHandler::hello)
			.andRoute(RequestPredicates.GET("/hello/long").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), greetingHandler::helloLong)
			.andRoute(RequestPredicates.GET("/hello/exception").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), greetingHandler::helloException);
	}
}
