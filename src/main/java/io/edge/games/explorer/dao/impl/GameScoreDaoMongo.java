package io.edge.games.explorer.dao.impl;

import java.util.List;
import java.util.Objects;

import io.edge.games.explorer.dao.GameScoreDao;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
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

				client.find(GameScoreDaoMongo.COLLECTION, query, ar -> {

					if (ar.succeeded()) {

						List<JsonObject> scoreList = ar.result();

						scoreList.sort((score1, score2) -> {
							int scoreDiff = score1.getInteger("score") - score2.getInteger("score");
							if (scoreDiff != 0) {
								return scoreDiff;
							}
							return score1.getInteger("time") - score2.getInteger("time");
						});

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
	public void addScore(String game, int level, String username, JsonObject score, Handler<AsyncResult<Boolean>> resultHandler) {

		this.addScoreCB.<Boolean> execute(future -> {

			try {

				JsonObject query = new JsonObject()//
						.put("game", game)//
						.put("level", level)//
						.put("username", username);

				JsonObject update = new JsonObject()//
						.put("score", score.getInteger("score"))//
						.put("time", score.getInteger("time"));

				UpdateOptions options = new UpdateOptions()//
						.setUpsert(true);

				client.updateCollectionWithOptions(GameScoreDaoMongo.COLLECTION, query, new JsonObject().put("$set", update), options, ar -> {

					if (ar.succeeded()) {
						
						future.complete(ar.result().getDocMatched() > 0);
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
