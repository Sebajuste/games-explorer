package io.edge.games.explorer.dao.impl;

import java.util.List;
import java.util.Objects;

import io.edge.games.explorer.dao.LobbyDao;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;

public class LobbyDaoMongo implements LobbyDao {

	private static final String COLLECTION = "game_lobbies";

	private final MongoClient client;

	private final CircuitBreaker addOrUpdateLobbyCB;

	private final CircuitBreaker removeLobbyCB;

	private final CircuitBreaker getLobbiesCB;

	private final CircuitBreaker findGameCB;

	public LobbyDaoMongo(Vertx vertx, MongoClient client) {
		super();
		Objects.requireNonNull(vertx);
		this.client = Objects.requireNonNull(client);
		this.addOrUpdateLobbyCB = CircuitBreaker.create("io.edge.games.lobby-dao.addOrUpdateLobby", vertx);
		this.removeLobbyCB = CircuitBreaker.create("io.edge.games.lobby-dao.removeLobby", vertx);
		this.getLobbiesCB = CircuitBreaker.create("io.edge.games.lobby-dao.getLobbies", vertx);
		this.findGameCB = CircuitBreaker.create("io.edge.games.lobby-dao.findGameCB", vertx);
	}

	@Override
	public void addOrUpdateLobby(String gameName, String host, int port, JsonObject metadata, Handler<AsyncResult<Boolean>> resultHandler) {

		JsonObject query = new JsonObject()//
				.put("game", gameName)//
				.put("host", host)//
				.put("port", port);

		JsonObject update = new JsonObject()//
				.put("metadata", metadata);

		UpdateOptions options = new UpdateOptions()//
				.setUpsert(true);

		this.addOrUpdateLobbyCB.<Boolean> execute(future -> {

			try {

				this.client.updateCollectionWithOptions(COLLECTION, query, new JsonObject().put("$set", update), options, ar -> {

					if (ar.succeeded()) {

						future.complete(ar.result().getDocMatched() > 0 || ar.result().getDocModified() > 0);

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
	public void removeLobby(String gameName, String host, int port, Handler<AsyncResult<Boolean>> resultHandler) {

		JsonObject query = new JsonObject()//
				.put("game", gameName)//
				.put("host", host)//
				.put("port", port);

		this.removeLobbyCB.<Boolean> execute(future -> {

			this.client.removeDocuments(COLLECTION, query, ar -> {

				if (ar.succeeded()) {

					future.complete(ar.result().getRemovedCount() > 0);

				} else {
					future.fail(ar.cause());
				}

			});

		}).setHandler(resultHandler);

	}

	@Override
	public void getLobbies(String gameName, Handler<AsyncResult<List<JsonObject>>> resultHandler) {

		this.getLobbiesCB.<List<JsonObject>> execute(future -> {

			JsonObject query = new JsonObject()//
					.put("gameName", gameName);

			FindOptions options = new FindOptions();

			this.client.findWithOptions(LobbyDaoMongo.COLLECTION, query, options, ar -> {

				if (ar.succeeded()) {

					List<JsonObject> scoreList = ar.result();

					future.complete(scoreList);

				} else {
					future.fail(ar.cause());
				}

			});

		}).setHandler(resultHandler);

	}

	@Override
	public void findGame(String gameName, String host, int port, Handler<AsyncResult<JsonObject>> resultHandler) {

		this.getLobbiesCB.<JsonObject> execute(future -> {

			JsonObject query = new JsonObject()//
					.put("gameName", gameName)//
					.put("host", host)//
					.put("port", port);

			JsonObject fields = new JsonObject().put("metadata", 1);

			this.client.findOne(LobbyDaoMongo.COLLECTION, query, fields, ar -> {

				if (ar.succeeded()) {
					future.complete(ar.result());

				} else {
					future.fail(ar.cause());
				}

			});

		}).setHandler(resultHandler);

	}

}
