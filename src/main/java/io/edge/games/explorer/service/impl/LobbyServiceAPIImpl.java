package io.edge.games.explorer.service.impl;

import java.util.List;

import io.edge.games.explorer.service.LobbyService;
import io.edge.games.explorer.service.LobbyServiceAPI;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class LobbyServiceAPIImpl implements LobbyServiceAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(LobbyServiceAPIImpl.class);

	private final LobbyService gameRegistry;

	public LobbyServiceAPIImpl(LobbyService gameRegistry) {
		super();
		this.gameRegistry = gameRegistry;
	}

	@Override
	public void getAll(String gameName, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		this.gameRegistry.getAll(gameName, ar -> {

			if (ar.succeeded()) {

				List<JsonObject> gameServerList = ar.result();

				resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonArray(gameServerList))));

			} else {

				OperationResponse response = new OperationResponse() //
						.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

				resultHandler.handle(Future.succeededFuture(response));

			}

		});

	}

	@Override
	public void find(String gameName, String server, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		String[] serverSplit = server.split(":");
		String host = serverSplit[0];
		int port = Integer.parseInt(serverSplit[1]);

		this.gameRegistry.findGame(gameName, host, port, ar -> {

			if (ar.succeeded()) {

				resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(ar.result())));

			} else {
				OperationResponse response = new OperationResponse() //
						.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

				resultHandler.handle(Future.succeededFuture(response));
			}

		});

	}

	@Override
	public void register(String gameName, String server, JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		String host = body.getString("host");
		int port = body.getInteger("port");

		body.put("name", server);
		body.put("timestamp", System.currentTimeMillis());
		

		this.gameRegistry.register(gameName, host, port, body, ar -> {

			OperationResponse response = new OperationResponse();

			if (ar.succeeded()) {
				response.setStatusCode(ar.result() ? HttpResponseStatus.CREATED.code() : HttpResponseStatus.NO_CONTENT.code());
			} else {
				LOGGER.error(ar.cause());
				response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
			}

			resultHandler.handle(Future.succeededFuture(response));

		});

	}

	@Override
	public void unregister(String gameName, String server, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		String[] serverSplit = server.split(":");
		String host = serverSplit[0];
		int port = Integer.parseInt(serverSplit[1]);

		this.gameRegistry.unregister(gameName, host, port, ar -> {

			OperationResponse response = new OperationResponse();

			if (ar.succeeded()) {
				response.setStatusCode(ar.result() ? HttpResponseStatus.NO_CONTENT.code() : HttpResponseStatus.BAD_REQUEST.code());
			} else {
				response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
			}

			resultHandler.handle(Future.succeededFuture(response));

		});

	}

}
