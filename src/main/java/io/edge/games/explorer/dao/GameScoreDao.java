package io.edge.games.explorer.dao;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface GameScoreDao {

	void getBestScore(String game, int level, Handler<AsyncResult<List<JsonObject>>> resultHandler);

	void addScore(String game, int level, String username, JsonObject score, Handler<AsyncResult<Boolean>> resultHandler);

}
