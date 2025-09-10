package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.time.Duration;

class NativeImageTest {
	DisposableServer disposableServer;

	@AfterEach
	void tearDown() {
		if (disposableServer != null) {
			disposableServer.disposeNow();
		}
	}

	@Test
	void test() {
		disposableServer =
				HttpServer.create()
						.wiretap(true)
						.handle((req, res) -> res.sendString(Mono.just("Hello World!")))
						.bindNow();

		HttpClient.create()
				.port(disposableServer.port())
				.wiretap(true)
				.get()
				.uri("/")
				.responseContent()
				.aggregate()
				.asString()
				.as(StepVerifier::create)
				.expectNext("Hello World!")
				.expectComplete()
				.verify(Duration.ofSeconds(5));
	}
}
