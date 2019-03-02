package io.edge.games.explorer.verticle;

import io.edge.games.explorer.service.GameRegistry;
import io.edge.games.explorer.service.GameRegistryAPI;
import io.edge.games.explorer.service.impl.GameRegistryAPIImpl;
import io.edge.games.explorer.service.impl.GameRegistryImpl;
import io.edge.games.explorer.util.WebApiService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;

public class GameExplorerVerticle extends AbstractVerticle {

	@Override
	public void start() {

		GameRegistry gameRegistry = new GameRegistryImpl(vertx);

		/**
		 * Bind API
		 */

		ServiceBinder serviceBinder = new ServiceBinder(vertx);

		GameRegistryAPI dashboardServiceAPI = new GameRegistryAPIImpl(gameRegistry);
		serviceBinder.setAddress(GameRegistryAPI.ADDRESS).register(GameRegistryAPI.class, dashboardServiceAPI);

		/**
		 * Publish Web API
		 */

		JsonObject config = new JsonObject()//
				.put("name", "Games-Explorer")//
				.put("endpoint", "io.edge.games-explorer.yaml")//
				.put("file", "src/main/resources/games-explorer.yaml")//
				.put("subpath", "/games-explorer");

		WebApiService.create(vertx).bind(config);

	}

}
