package io.edge.games.explorer.dao.impl;

import java.util.List;
import java.util.Objects;

import io.edge.games.explorer.dao.GameScoreDao;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

public class GameScoreDaoMongo implements GameScoreDao {

	private static final String COLLECTION = "game_scores";

	private final MongoClient client;

	private final CircuitBreaker getBestScoreCB;

	private final CircuitBreaker addScoreCB;

	public GameScoreDaoMongo(Vertx vertx, MongoClient client) {
		super();
		Objects.requireNonNull(vertx);
		this.client = Objects.requireNonNull(client);
		this.getBestScoreCB = CircuitBreaker.create("io.edge.games.score-dao.getBestScore", vertx);
		this.addScoreCB = CircuitBreaker.create("io.edge.games.score-dao.addScore", vertx);
	}

	@Override
	public void getBestScore(String game, int level, Handler<AsyncResult<List<JsonObject>>> resultHandler) {

		this.getBestScoreCB.<List<JsonObject>> execute(future -> {

			try {

				JsonObject query = new JsonObject()//
						.put("game", game)//
						.put("level", level);
				
				FindOptions options = new FindOptions()//
						.setSort(new JsonObject().put("score", -1).put("time", 1));
				
				this.client.findWithOptions(GameScoreDaoMongo.COLLECTION, query, options, ar -> {

					if (ar.succeeded()) {

						List<JsonObject> scoreList = ar.result();

						future.complete(scoreList);

					} else {
						future.fail(ar.cause());
					}

				});

			} catch (Exception e) {
				future.fail(e);
			}

		}).setHandler(resultHandler);

	}

	@Override
	public void addScore(String game, int level, String username, JsonObject scoreData, Handler<AsyncResult<Boolean>> resultHandler) {

		this.addScoreCB.<Boolean> execute(future -> {

			try {

				int score = scoreData.getInteger("score");
				int time = scoreData.getInteger("time");
				
				JsonObject query = new JsonObject()//
						.put("game", game)//
						.put("level", level)//
						.put("username", username)
						.put("$or", new JsonArray()//
								.add(new JsonObject().put("score", new JsonObject().put("$gt", score)))//
								.add(new JsonObject().put("$and", new JsonArray()//
										.add(new JsonObject().put("score", new JsonObject().put("$eq", score)))//
										.add(new JsonObject().put("time", new JsonObject().put("$lt", time)))))
								);//
						
				JsonObject update = new JsonObject()//
						.put("score", score)//
						.put("time", time);

				UpdateOptions options = new UpdateOptions()//
						.setUpsert(true);

				this.client.updateCollectionWithOptions(GameScoreDaoMongo.COLLECTION, query, new JsonObject().put("$set", update), options, ar -> {

					if (ar.succeeded()) {
						
						future.complete(ar.result().getDocMatched() > 0 || ar.result().getDocModified() > 0 );
					} else {
						future.fail(ar.cause());
					}

				});

			} catch (Exception e) {
				future.fail(e);
			}

		}).setHandler(resultHandler);
	}

}
