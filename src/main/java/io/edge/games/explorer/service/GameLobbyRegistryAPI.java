package io.edge.games.explorer.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;

@WebApiServiceGen
public interface GameLobbyRegistryAPI {

	static String ADDRESS = "edge.games.registries";

	void getAll(String gameName, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler);

}
