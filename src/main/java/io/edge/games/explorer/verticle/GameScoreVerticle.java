package io.edge.games.explorer.verticle;

import io.edge.games.explorer.dao.GameScoreDao;
import io.edge.games.explorer.dao.impl.GameScoreDaoMongo;
import io.edge.games.explorer.service.GameScoreAPI;
import io.edge.games.explorer.service.impl.GameScoreAPIImpl;
import io.edge.games.explorer.util.WebApiService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;

public class GameScoreVerticle extends AbstractVerticle {

	@Override
	public void start() {

		JsonObject mongoConfig = new JsonObject();

		mongoConfig.put("host", config().getString("MONGODB_ADDON_HOST", "127.0.0.1"));
		mongoConfig.put("port", config().getInteger("MONGODB_ADDON_PORT", 27017));

		mongoConfig.put("db_name", config().getString("MONGODB_ADDON_DB", "edge_games_scores"));

		if (config().containsKey("MONGODB_ADDON_USER")) {
			mongoConfig.put("username", config().getString("MONGODB_ADDON_USER"));
		}

		if (config().containsKey("MONGODB_ADDON_PASSWORD")) {
			mongoConfig.put("password", config().getString("MONGODB_ADDON_PASSWORD"));
		}

		MongoClient mongoClient = MongoClient.createShared(vertx, mongoConfig);

		GameScoreDao gameScore = new GameScoreDaoMongo(vertx, mongoClient);

		/**
		 * Bind API
		 */

		ServiceBinder serviceBinder = new ServiceBinder(vertx);

		GameScoreAPI gameScoreAPI = new GameScoreAPIImpl(gameScore);
		serviceBinder.setAddress(GameScoreAPI.ADDRESS).register(GameScoreAPI.class, gameScoreAPI);

		/**
		 * Publish Web API
		 */

		JsonObject config = new JsonObject()//
				.put("name", "Games-Score")//
				.put("endpoint", "io.edge.games-score.yaml")//
				.put("file", "src/main/resources/games-score.yaml")//
				.put("subpath", "/games-score");

		WebApiService.create(vertx).bind(config);

	}

}
