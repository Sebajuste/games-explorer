package io.edge.games.explorer.service.impl;

import java.util.List;

import io.edge.games.explorer.dao.LobbyDao;
import io.edge.games.explorer.service.LobbyService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class LobbyServiceImpl implements LobbyService {

	private final LobbyDao lobbyDao;

	public LobbyServiceImpl(LobbyDao lobbyDao) {
		super();
		this.lobbyDao = lobbyDao;
	}

	@Override
	public void register(String gameName, String host, int port, JsonObject information, Handler<AsyncResult<Boolean>> resultHandler) {

		this.lobbyDao.addOrUpdateLobby(gameName, host, port, information, resultHandler);

	}

	@Override
	public void unregister(String gameName, String host, int port, Handler<AsyncResult<Boolean>> resultHandler) {

		this.lobbyDao.removeLobby(gameName, host, port, resultHandler);

	}

	@Override
	public void getAll(String gameName, Handler<AsyncResult<List<JsonObject>>> resultHandler) {

		this.lobbyDao.getLobbies(gameName, resultHandler);

	}

	@Override
	public void findGame(String gameName, String host, int port, Handler<AsyncResult<JsonObject>> resultHandler) {

		this.lobbyDao.findGame(gameName, host, port, resultHandler);

	}

}
