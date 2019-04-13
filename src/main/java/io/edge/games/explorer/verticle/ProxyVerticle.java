package io.edge.games.explorer.verticle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import io.edge.games.explorer.util.OpenAPI3RouterBufferFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;

public class ProxyVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProxyVerticle.class);

	private void loadApiService(Router mainRouter, Record record) {

		String supath = record.getMetadata().getString("supath");

		String endpoint = record.getLocation().getString("endpoint");

		DeliveryOptions o = new DeliveryOptions()//
				.addHeader("action", "getOpenAPI");

		vertx.eventBus().<Buffer> send(endpoint, new JsonObject(), o, response -> {

			if (response.succeeded()) {

				// LOGGER.info("Response : " + response.result().body());

				Buffer buffer = response.result().body();

				// TODO : create routes

				Handler<RoutingContext> circuitBrakerHandler = routingContext -> {

					// LOGGER.info("Request called");

					routingContext.next();
				};

				OpenAPI3RouterBufferFactory.create(vertx, buffer, circuitBrakerHandler, result -> {

					if (result.succeeded()) {

						try {

							OpenAPI3RouterFactory routerFactory = result.result();

							routerFactory.mountServicesFromExtensions();

							Router router = routerFactory.getRouter();

							mainRouter.mountSubRouter(supath, router);

							LOGGER.info("API \"" + record.getName() + "\" mounted successfully");

						} catch (Exception e) {
							LOGGER.error("Cannot mount API", e);
						}

					} else {
						LOGGER.error("Cannot read file", result.cause());
					}

				});

			} else {
				LOGGER.error("Bad response", response.cause());
			}

		});

	}

	@Override
	public void start() {

		/**
		 * Start Http Server
		 */

		Router mainRouter = Router.router(vertx);

		mainRouter.route().handler(CorsHandler.create("*").allowedMethods(new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.OPTIONS))));

		HttpServerOptions options = new HttpServerOptions();
		options.setPort(8080);

		vertx.createHttpServer(options).requestHandler(mainRouter).listen(listenResult -> {

			if (listenResult.succeeded()) {
				LOGGER.info("HTTP Proxy started on port " + listenResult.result().actualPort());
			} else {
				LOGGER.error("Cannot start HTTP Proxy", listenResult.cause());
			}

		});

		/**
		 * Load all published Web API Services
		 */

		ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx);

		serviceDiscovery.getRecords(new JsonObject().put("type", "eventbus-webapi-service-proxy"), ar -> {

			if (ar.succeeded()) {

				List<Record> recordList = ar.result();

				for (Record record : recordList) {

					this.loadApiService(mainRouter, record);

				}

			} else {
				LOGGER.error("Cannot get records", ar.cause());
			}

		});

		/**
		 * Listen future Web API Services
		 */
		vertx.eventBus().<JsonObject> consumer("vertx.discovery.announce", message -> {

			JsonObject body = message.body();

			if ("UP".equals(body.getString("status")) && "eventbus-webapi-service-proxy".equals(body.getString("type"))) {
				Record record = new Record(body);
				this.loadApiService(mainRouter, record);
			}

			LOGGER.info("Discorvery annouce  event [headers=" + message.headers() + ", body=" + message.body() + "]");

		});

		/**
		 * Log usage events
		 */
		vertx.eventBus().consumer("vertx.discovery.usage", message -> {

			LOGGER.info("Discorvery usage event [headers=" + message.headers() + ", body=" + message.body() + "]");

		});

	}

}
