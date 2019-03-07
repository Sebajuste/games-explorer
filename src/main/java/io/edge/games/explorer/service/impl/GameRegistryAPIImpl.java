package io.edge.games.explorer.service.impl;

import java.util.List;

import io.edge.games.explorer.service.GameLobbyRegistry;
import io.edge.games.explorer.service.GameLobbyRegistryAPI;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class GameRegistryAPIImpl implements GameLobbyRegistryAPI {

	private final GameLobbyRegistry gameRegistry;

	public GameRegistryAPIImpl(GameLobbyRegistry gameRegistry) {
		super();
		this.gameRegistry = gameRegistry;
	}

	@Override
	public void getAll(String gameName, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		gameRegistry.getAll(gameName, ar -> {

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

}
