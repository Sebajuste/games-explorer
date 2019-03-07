package io.edge.games.explorer.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface GameService {

	void createGame(String name, Handler<AsyncResult<Boolean>> resultHandler);

	void createToken(String gameName, String client, Handler<AsyncResult<JsonObject>> resultHandler);

	void authenticateClient(JsonObject token, Handler<AsyncResult<Boolean>> resultHandler);

}
