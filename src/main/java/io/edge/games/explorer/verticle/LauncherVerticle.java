package io.edge.games.explorer.verticle;

import java.util.ArrayList;
import java.util.List;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class LauncherVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(LauncherVerticle.class);

	@Override
	public void start(Future<Void> startFuture) {

		ConfigRetriever.create(vertx).getConfig(configResult -> {

			if (configResult.succeeded()) {

				JsonObject config = configResult.result();

				DeploymentOptions options = new DeploymentOptions().setConfig(config);

				@SuppressWarnings("rawtypes")
				List<Future> futureList = new ArrayList<>();

				Future<String> holePunchingDeployFuture = Future.future();
				this.vertx.deployVerticle(HolePunchingVerticle.class.getName(), options, holePunchingDeployFuture);
				futureList.add(holePunchingDeployFuture);

				Future<String> gameExplorerDeployFuture = Future.future();
				this.vertx.deployVerticle(GameExplorerVerticle.class.getName(), options, gameExplorerDeployFuture);
				futureList.add(gameExplorerDeployFuture);

				if( config.getBoolean("score", true)) {
					Future<String> deployFuture = Future.future();
					this.vertx.deployVerticle(GameScoreVerticle.class.getName(), options, deployFuture);
					futureList.add(deployFuture);
				}
				
				if ( config.getBoolean("webapi", true)) {
					Future<String> deployFuture = Future.future();
					this.vertx.deployVerticle(ProxyVerticle.class.getName(), options, deployFuture);
					futureList.add(deployFuture);
				}
				
				if( config.getBoolean("matchmaking", true)) {
					Future<String> deployFuture = Future.future();
					this.vertx.deployVerticle(MatchmakingVerticle.class.getName(), options, deployFuture);
					futureList.add(deployFuture);
				}

				CompositeFuture.all(futureList).setHandler(deployResult -> {

					if (deployResult.succeeded()) {
						LOGGER.info("Games Explorer service started");
						startFuture.complete();
					} else {
						LOGGER.error(deployResult.cause());
						startFuture.fail(deployResult.cause());
					}

				});

			} else {

				startFuture.fail(configResult.cause());
			}

		});

	}

}
