package io.edge.games.explorer.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface GameScoreAPI {

	static String ADDRESS = "edge.games.scores";

	void getAll(String gameName, String level, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

	void pushScore(String gameName, String level, String username, JsonObject score, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}
