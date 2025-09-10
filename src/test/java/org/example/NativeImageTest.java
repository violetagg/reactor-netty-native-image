package org.example;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.time.Duration;

class NativeImageTest {
	DisposableServer disposableServer;
	SelfSignedCertificate cert;

	@BeforeEach
	void setUp() throws Exception {
		cert = new SelfSignedCertificate();
	}

	@AfterEach
	void tearDown() {
		if (disposableServer != null) {
			disposableServer.disposeNow();
		}
	}

	@Test
	void test1() {
		disposableServer =
				HttpServer.create()
						.wiretap(true)
						.secure(spec -> spec.sslContext(Http11SslContextSpec.forServer(cert.certificate(), cert.privateKey())))
						.handle((req, res) -> res.sendString(Mono.just("Hello World!")))
						.bindNow();

		HttpClient.create()
				.port(disposableServer.port())
				.secure(spec -> spec.sslContext(Http11SslContextSpec.forClient()
						.configure(builder -> builder.trustManager(InsecureTrustManagerFactory.INSTANCE))))
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

	@Test
	void test2() {
		HttpClient.create()
				.wiretap(true)
				.get()
				.uri("https://projectreactor.io/")
				.responseContent()
				.aggregate()
				.asString()
				.as(StepVerifier::create)
				.expectNextMatches(s -> s.contains("Project Reactor"))
				.expectComplete()
				.verify(Duration.ofSeconds(5));
	}
}
