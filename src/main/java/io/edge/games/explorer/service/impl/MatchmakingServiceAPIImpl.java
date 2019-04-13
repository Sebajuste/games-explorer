package io.edge.games.explorer.service.impl;

import java.util.UUID;

import io.edge.games.explorer.service.MatchmakingServiceAPI;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class MatchmakingServiceAPIImpl implements MatchmakingServiceAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(MatchmakingServiceAPIImpl.class);

	private final EventBus eventBus;

	public MatchmakingServiceAPIImpl(Vertx vertx) {
		super();
		this.eventBus = vertx.eventBus();
	}

	@Override
	public void joinRequest(String gameName, JsonObject player, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		String requestToken = UUID.randomUUID().toString();

		DeliveryOptions options = new DeliveryOptions()//
				.addHeader("game", gameName)//
				.addHeader("requestToken", requestToken);

		this.eventBus.<JsonObject> send("io.games.matchmaking.join-request", player, options, ar -> {

			if (ar.succeeded()) {

				JsonObject response = new JsonObject()//
						.put("request_token", requestToken);

				resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(response)));

			} else {

				LOGGER.error(ar.cause());

				OperationResponse response = new OperationResponse() //
						.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

				resultHandler.handle(Future.succeededFuture(response));

			}

		});

	}

	@Override
	public void getStatus(String gameName, String requestToken, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		DeliveryOptions options = new DeliveryOptions()//
				.addHeader("game", gameName);

		this.eventBus.<JsonObject>send("io.games.matchmaking.status-request", requestToken, options, ar -> {

			if (ar.succeeded()) {
				
				JsonObject response = ar.result().body();
				
				if( response != null) {
					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(response)));
				} else {
					OperationResponse opeResponse = new OperationResponse() //
							.setStatusCode(HttpResponseStatus.NO_CONTENT.code());

					resultHandler.handle(Future.succeededFuture(opeResponse));
				}
				
			} else {

				LOGGER.error(ar.cause());

				OperationResponse response = new OperationResponse() //
						.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

				resultHandler.handle(Future.succeededFuture(response));

			}
		});

	}

	@Override
	public void cancelJoin(String gameName, String requestToken, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {
		
		LOGGER.info("cancelJoin for game "+ gameName + ", token: "+requestToken);
		
		DeliveryOptions options = new DeliveryOptions()//
				.addHeader("game", gameName);
		
		this.eventBus.<JsonObject>send("io.games.matchmaking.cancel-request", requestToken, options, ar -> {
			
			if (ar.succeeded()) {
				
				resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(ar.result().body())));
				
			}  else {

				LOGGER.error(ar.cause());

				OperationResponse response = new OperationResponse() //
						.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

				resultHandler.handle(Future.succeededFuture(response));

			}
			
		});
		
	}

}
