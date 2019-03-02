package io.edge.games.explorer.service.impl;

import java.util.Objects;

import io.edge.games.explorer.dao.GameScoreDao;
import io.edge.games.explorer.service.GameScoreAPI;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class GameScoreAPIImpl implements GameScoreAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(GameScoreAPIImpl.class);

	private GameScoreDao gameScoreDao;

	public GameScoreAPIImpl(GameScoreDao gameScoreDao) {
		super();
		this.gameScoreDao = Objects.requireNonNull(gameScoreDao);
	}

	@Override
	public void getBestGameLevel(String gameName, String level, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		try {
			
			this.gameScoreDao.getBestScore(gameName, Integer.parseInt(level), ar -> {

				if (ar.succeeded()) {

					resultHandler.handle(Future.succeededFuture(OperationResponse.completedWithJson(new JsonArray(ar.result()))));

				} else {

					LOGGER.error(ar.cause());

					OperationResponse response = new OperationResponse() //
							.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

					resultHandler.handle(Future.succeededFuture(response));
				}

			});

		} catch (Exception e) {
			LOGGER.error(e);

			OperationResponse response = new OperationResponse() //
					.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

			resultHandler.handle(Future.succeededFuture(response));
		}

	}

	@Override
	public void pushScore(String gameName, String level, String username, JsonObject body, OperationRequest context, Handler<AsyncResult<OperationResponse>> resultHandler) {

		try {
						
			this.gameScoreDao.addScore(gameName, Integer.parseInt(level), username, body, ar -> {

				OperationResponse response = new OperationResponse();

				if (ar.succeeded()) {

					boolean updated = ar.result();

					if (updated) {
						response.setStatusCode(HttpResponseStatus.CREATED.code());
					} else {
						response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
					}

				} else {
					LOGGER.error(ar.cause());
					response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
				}

				resultHandler.handle(Future.succeededFuture(response));

			});

		} catch (Exception e) {
			LOGGER.error(e);

			OperationResponse response = new OperationResponse() //
					.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

			resultHandler.handle(Future.succeededFuture(response));
		}

	}

}
