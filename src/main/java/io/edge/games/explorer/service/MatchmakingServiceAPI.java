package io.edge.games.explorer.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface MatchmakingServiceAPI {

	static String ADDRESS = "edge.games.matchmaking.wabapi";

	void joinRequest(String gameName, JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

	void getStatus(String gameName, String requestToken, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

	void cancelJoin(String gameName, String requestToken, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);
	
}
