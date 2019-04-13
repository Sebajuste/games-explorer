package io.edge.games.explorer.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.edge.games.explorer.service.LobbyService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class LobbyServiceClusterImpl implements LobbyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LobbyServiceClusterImpl.class);

	private final SharedData sd;

	public LobbyServiceClusterImpl(Vertx vertx) {
		this.sd = vertx.sharedData();
	}

	@Override
	public void register(String gameName, String host, int port, JsonObject gameServer, Handler<AsyncResult<Boolean>> resultHandler) {

		if (!gameServer.containsKey("host") || !gameServer.containsKey("port")) {
			resultHandler.handle(Future.succeededFuture(false));
			return;
		}

		try {

			String gameServerID = host + ":" + String.valueOf(port);

			LocalMap<String, JsonObject> gameMap = sd.getLocalMap("edge.game.explorer." + gameName);

			LOGGER.info(gameMap);

			gameMap.merge(gameServerID, gameServer, (oldGameServer, newGameServer) -> {
				return JsonObject.mapFrom(oldGameServer).mergeIn(newGameServer);
			});

			resultHandler.handle(Future.succeededFuture(true));

		} catch (Exception e) {

			LOGGER.error("Cannot register server. Cause : " + e.getMessage(), e);

			resultHandler.handle(Future.failedFuture(e));

		}

	}

	@Override
	public void unregister(String gameName, String host, int port, Handler<AsyncResult<Boolean>> resultHandler) {

		Map<String, JsonObject> gameMap = sd.getLocalMap("edge.game.explorer." + gameName);

		try {

			String gameServerID = host + ":" + String.valueOf(port);

			JsonObject oldGameServer = gameMap.remove(gameServerID);

			resultHandler.handle(Future.succeededFuture(oldGameServer != null));

		} catch (Exception e) {

			resultHandler.handle(Future.failedFuture(e));

		}

	}

	@Override
	public void getAll(String gameName, Handler<AsyncResult<List<JsonObject>>> resultHandler) {

		LocalMap<String, JsonObject> gameMap = sd.getLocalMap("edge.game.explorer." + gameName);

		List<JsonObject> gameServerList = new ArrayList<>(gameMap.values());

		resultHandler.handle(Future.succeededFuture(gameServerList));

	}

	@Override
	public void findGame(String gameName, String host, int port, Handler<AsyncResult<JsonObject>> resultHandler) {

		Map<String, JsonObject> gameMap = sd.getLocalMap("edge.game.explorer." + gameName);

		String gameServerID = host + ":" + String.valueOf(port);

		JsonObject gameServer = gameMap.get(gameServerID);

		resultHandler.handle(Future.succeededFuture(gameServer));

	}

}
