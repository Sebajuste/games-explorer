package io.edge.games.explorer.dao;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface LobbyDao {

	void addOrUpdateLobby(String gameName, String host, int port, JsonObject metadata, Handler<AsyncResult<Boolean>> resultHandler);

	void removeLobby(String gameName, String host, int port, Handler<AsyncResult<Boolean>> resultHandler);

	void getLobbies(String gameName, Handler<AsyncResult<List<JsonObject>>> resultHandler);

	void findGame(String gameName, String host, int port, Handler<AsyncResult<JsonObject>> resultHandler);

}
