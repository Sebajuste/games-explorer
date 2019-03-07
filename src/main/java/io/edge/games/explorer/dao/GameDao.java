package io.edge.games.explorer.dao;

public interface GameDao {

	void create(String name);

	void createToken(String name, String key);

	void deleteToken(String name, String key);

}
