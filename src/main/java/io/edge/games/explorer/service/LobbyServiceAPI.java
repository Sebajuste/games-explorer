package io.edge.games.explorer.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface LobbyServiceAPI {

	static String ADDRESS = "edge.games.lobbies.wabapi";

	void getAll(String gameName, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

	void find(String gameName, String server, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

	void register(String gameName, String server, JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

	void unregister(String gameName, String server, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}
