package io.edge.games.explorer.bo;

import io.vertx.core.json.JsonObject;

public class MatchmakingRequest {

	private final String game;

	private final String requestToken;

	private final JsonObject player;

	public MatchmakingRequest(String game, String requestToken, JsonObject player) {
		super();
		this.game = game;
		this.requestToken = requestToken;
		this.player = player;
	}

	public String getGame() {
		return game;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public JsonObject getPlayer() {
		return player;
	}

}
