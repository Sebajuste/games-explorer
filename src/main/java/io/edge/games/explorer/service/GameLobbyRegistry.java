package io.edge.games.explorer.service;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface GameLobbyRegistry {

	void register(String gameName, JsonObject gameServer, Handler<AsyncResult<Boolean>> resultHandler);

	void unregister(String gameName, JsonObject gameServer, Handler<AsyncResult<Boolean>> resultHandler);

	void getAll(String gameName, Handler<AsyncResult<List<JsonObject>>> resultHandler);

	void findGame(String gameName, String host, int port, Handler<AsyncResult<JsonObject>> resultHandler);

}
