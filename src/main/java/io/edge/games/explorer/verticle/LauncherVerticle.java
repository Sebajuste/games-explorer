package io.edge.games.explorer.verticle;

import java.util.ArrayList;
import java.util.List;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Load configuration and start other Verticles
 * 
 * @author Sebastien
 *
 */
public class LauncherVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(LauncherVerticle.class);

	@Override
	public void start(Promise<Void> startPromise) {

		ConfigRetriever.create(vertx).getConfig(configResult -> {

			if (configResult.succeeded()) {

				JsonObject config = configResult.result();

				DeploymentOptions options = new DeploymentOptions().setConfig(config);

				@SuppressWarnings("rawtypes")
				List<Future> futureList = new ArrayList<>();

				if (config.getBoolean("holepunching", false)) {
					Promise<String> holePunchingDeployPromise = Promise.promise();
					this.vertx.deployVerticle(HolePunchingVerticle.class.getName(), options, holePunchingDeployPromise);
					futureList.add(holePunchingDeployPromise.future());
				}

				if (config.getBoolean("lobbies", false)) {
					Promise<String> gameExplorerDeployPromise = Promise.promise();
					this.vertx.deployVerticle(GameExplorerVerticle.class.getName(), options, gameExplorerDeployPromise);
					futureList.add(gameExplorerDeployPromise.future());
				}

				if (config.getBoolean("score", false)) {
					Promise<String> deployPromise = Promise.promise();
					this.vertx.deployVerticle(GameScoreVerticle.class.getName(), options, deployPromise);
					futureList.add(deployPromise.future());
				}

				if (config.getBoolean("webapi", false)) {
					Promise<String> deployPromise = Promise.promise();
					this.vertx.deployVerticle(ProxyVerticle.class.getName(), options, deployPromise);
					futureList.add(deployPromise.future());
				}

				if (config.getBoolean("matchmaking", false)) {
					Promise<String> deployPromise = Promise.promise();
					this.vertx.deployVerticle(MatchmakingVerticle.class.getName(), options, deployPromise);
					futureList.add(deployPromise.future());
				}

				CompositeFuture.all(futureList).onComplete(deployResult -> {
					if (deployResult.succeeded()) {
						LOGGER.info("Games Explorer service started");
						startPromise.complete();
					} else {
						LOGGER.error(deployResult.cause());
						startPromise.fail(deployResult.cause());
					}
				});
				
				/*
				CompositeFuture.all(futureList).setHandler(deployResult -> {

					if (deployResult.succeeded()) {
						LOGGER.info("Games Explorer service started");
						startFuture.complete();
					} else {
						LOGGER.error(deployResult.cause());
						startFuture.fail(deployResult.cause());
					}

				});
				*/

			} else {

				startPromise.fail(configResult.cause());
			}

		});

	}

}
