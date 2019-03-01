package io.edge.utils.webapiservice;

import java.util.Objects;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public class WebApiService {

	private final Vertx vertx;

	private final ServiceDiscovery discovery;

	public WebApiService(Vertx vertx) {
		super();
		this.vertx = Objects.requireNonNull(vertx);
		this.discovery = ServiceDiscovery.create(vertx);
	}

	public static WebApiService create(Vertx vertx) {
		return new WebApiService(vertx);
	}

	public void bind(JsonObject config) {

		vertx.eventBus().consumer(config.getString("endpoint"), message -> {

			String action = message.headers().get("action");

			if ("getOpenAPI".equals(action)) {

				vertx.fileSystem().readFile(config.getString("file"), readResult -> {

					if (readResult.succeeded()) {

						message.reply(readResult.result());

					} else {
						message.fail(0, readResult.cause().getMessage());
					}

				});

			} else {
				message.fail(0, "Invalid action");
			}

		});

		Record record = new Record()//
				.setName(config.getString("name"))//
				.setType("eventbus-webapi-service-proxy")//
				.setLocation(new JsonObject().put("endpoint", config.getString("endpoint")))//
				.setMetadata(new JsonObject().put("supath", config.getString("subpath")));

		discovery.publish(record, recordResult -> {

		});

	}

}
