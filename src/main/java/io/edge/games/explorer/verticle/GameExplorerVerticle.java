package io.edge.games.explorer.verticle;

import io.edge.games.explorer.service.LobbyService;
import io.edge.games.explorer.service.LobbyServiceAPI;
import io.edge.games.explorer.service.impl.LobbyServiceAPIImpl;
import io.edge.games.explorer.service.impl.LobbyServiceClusterImpl;
import io.edge.games.explorer.util.WebApiService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceBinder;

public class GameExplorerVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameExplorerVerticle.class);

	@Override
	public void start() {

		LobbyService gameRegistry = new LobbyServiceClusterImpl(vertx);

		/**
		 * Bind API
		 */

		ServiceBinder serviceBinder = new ServiceBinder(vertx);

		LobbyServiceAPI lobbyServiceAPI = new LobbyServiceAPIImpl(gameRegistry);
		serviceBinder.setAddress(LobbyServiceAPI.ADDRESS).register(LobbyServiceAPI.class, lobbyServiceAPI);

		/**
		 * Publish Web API
		 */

		JsonObject config = new JsonObject()//
				.put("name", "Games-Lobbies")//
				.put("endpoint", "io.edge.games-lobbies.yaml")//
				.put("file", "src/main/resources/games-lobbies.yaml")//
				.put("subpath", "/games-lobbies");

		WebApiService.create(vertx).bind(config);

		LOGGER.info("Game Explorer API started");

	}

}
