package io.edge.games.explorer.service;

import java.util.List;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface LobbyService {
	
	static String ADDRESS = "edge.games.lobbies.service";

	void register(String gameName, String host, int port, JsonObject information, Handler<AsyncResult<Boolean>> resultHandler);

	void unregister(String gameName, String host, int port, Handler<AsyncResult<Boolean>> resultHandler);

	void getAll(String gameName, Handler<AsyncResult<List<JsonObject>>> resultHandler);

	void findGame(String gameName, String host, int port, Handler<AsyncResult<JsonObject>> resultHandler);

}
