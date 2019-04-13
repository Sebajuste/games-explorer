package io.edge.games.explorer.verticle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.edge.games.explorer.bo.MatchmakingRequest;
import io.edge.games.explorer.service.MatchmakingServiceAPI;
import io.edge.games.explorer.service.impl.MatchmakingServiceAPIImpl;
import io.edge.games.explorer.util.WebApiService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.GroupedFlowable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * Simple Matchmaking
 * 
 * @author smartinez
 *
 */
public class MatchmakingVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(MatchmakingVerticle.class);

	private int minPlayerPerSession;

	private int maxPlayerPerSession;
	

	private final Map<String, JsonObject> requestStatus = new HashMap<>();
	
	
	/**
	 * Listen on the bus the join session request from players
	 * @param emitter
	 */
	private void listenPlayerRequest(FlowableEmitter<MatchmakingRequest> emitter) {
		MessageConsumer<JsonObject> consumer = this.vertx.eventBus().consumer("io.games.matchmaking.join-request", message -> {
			
			String game = message.headers().get("game");
			String requestToken = message.headers().get("requestToken");
			
			this.requestStatus.put(requestToken, new JsonObject().put("status", "IN_QUEUE"));
			
			LOGGER.info("New player in matchmaking : " + message.body());
			emitter.onNext(new MatchmakingRequest(game, requestToken, message.body()));

			message.reply(null);
		});
		emitter.setCancellable(() -> consumer.unregister());
	}

	/**
	 * Retreive the options of game mode for the player, and create his unique
	 * hash
	 * 
	 * @param player
	 * @return
	 */
	private String getHashMode(MatchmakingRequest request) {
		return request.getGame() + request.getPlayer().getString("mode", "default");
	}

	
	private boolean checkValidRequest(List<MatchmakingRequest> requests) {
		
		Iterator<MatchmakingRequest> it =  requests.iterator();
		
		boolean valid = true;
		
		while( it.hasNext()) {
			MatchmakingRequest req = it.next();
			if( !this.requestStatus.containsKey(req.getRequestToken())) {
				it.remove();
				valid = false;
			}
		}
		
		return valid;
	}
	
	/**
	 * Create batch of players, minimum 2, maximum 4, each 5 seconds
	 * 
	 * @param modePlayers
	 * @return
	 */
	private Flowable<List<MatchmakingRequest>> groupPlayerForSession(GroupedFlowable<String, MatchmakingRequest> modePlayers) {
		
		Flowable<MatchmakingRequest> requests$ = modePlayers.share();
		
		return Flowable.<List<MatchmakingRequest>>create(emitter -> {
			
			List<MatchmakingRequest> gameSessionList = new ArrayList<>();
			
			Disposable gameTimeout = requests$.debounce(15L, TimeUnit.SECONDS).subscribe(event -> {
				LOGGER.info("wait player timeout. size: " + gameSessionList.size());
				if( gameSessionList.size() >= this.minPlayerPerSession && checkValidRequest(gameSessionList) ) {
					emitter.onNext(new ArrayList<>(gameSessionList));
					LOGGER.info("next session");
					gameSessionList.clear();
				}
			});
			
			Disposable fullSession = requests$.subscribe(req -> {
				gameSessionList.add(req);
				
				JsonObject status = this.requestStatus.get(req.getRequestToken());
				status.put("status", "IN_ROOM");
				
				LOGGER.info("New player in session, size: " + gameSessionList.size());
				if( gameSessionList.size() >= this.maxPlayerPerSession && checkValidRequest(gameSessionList) ) {
					emitter.onNext(new ArrayList<>(gameSessionList));
					LOGGER.info("next session");
					gameSessionList.clear();
				}
			});
			
			emitter.setCancellable(() -> {
				gameTimeout.dispose();
				fullSession.dispose();
			});
			
		}, BackpressureStrategy.BUFFER);
		
		/*
		return modePlayers//
				.buffer(minPlayerPerSession).flatMap(Flowable::fromIterable)// create minimum group, and reflat players
				.buffer(5L, TimeUnit.SECONDS, maxPlayerPerSession); // wait others player to timeout, or maximum players reached
		*/
	}

	/**
	 * Set a game master, and send information to all player to start game
	 * 
	 * @param gameSession
	 */
	private void startGameSession(List<MatchmakingRequest> requests) {
		
		JsonObject master = requests.get(0).getPlayer();
		
		String masterName = master.getString("name");
		String masterHost = master.getString("host");
		int masterPort = master.getInteger("port");
		
		JsonArray players = new JsonArray();
		
		for( MatchmakingRequest request : requests ) {
			players.add( request.getPlayer() );
		}
		
		JsonObject response = new JsonObject()//
				.put("status", "READY")//
				.put("master", masterName)//
				.put("host", masterHost)//
				.put("port", masterPort)//
				.put("players", players);
		
		for( MatchmakingRequest request : requests ) {
			this.requestStatus.put(request.getRequestToken(), response);
		}
		
		
		// TODO : send event
		
		LOGGER.info("Game Session started");
		
	}

	@Override
	public void start() {

		/**
		 * Config
		 */
		
		this.minPlayerPerSession = config().getInteger("MIN_PLAYERS_PER_SESSION", 2);
		this.maxPlayerPerSession = config().getInteger("MAX_PLAYERS_PER_SESSION", 4);

		/**
		 * Events
		 */
		
		this.vertx.eventBus().<String>consumer("io.games.matchmaking.status-request", message -> {
			
			String requestToken = message.body();
			
			JsonObject response = this.requestStatus.get(requestToken);
			
			message.reply(response);
			
		});

		this.vertx.eventBus().<String>consumer("io.games.matchmaking.cancel-request", message -> {
			
			String requestToken = message.body();
			
			this.requestStatus.remove(requestToken);
			
		});
		
		
		/**
		 * Matchmaking
		 */
		
		Flowable.<MatchmakingRequest> create(this::listenPlayerRequest, BackpressureStrategy.BUFFER) // Listen all player's join requests

				.groupBy(this::getHashMode)// Create separate matchmaking for each Mode

				.flatMap(this::groupPlayerForSession)// Regroup players for session
				
				.subscribe(this::startGameSession);// Start the game session

		/**
		 * Bind API
		 */

		ServiceBinder serviceBinder = new ServiceBinder(vertx);

		MatchmakingServiceAPI matchmakingAPI = new MatchmakingServiceAPIImpl(vertx);
		serviceBinder.setAddress(MatchmakingServiceAPI.ADDRESS).register(MatchmakingServiceAPI.class, matchmakingAPI);

		/**
		 * Publish Web API on Web Proxy
		 */

		JsonObject config = new JsonObject()//
				.put("name", "Games-Matchmaking")//
				.put("endpoint", "io.edge.games-matchmaking.yaml")//
				.put("file", "src/main/resources/games-matchmaking.yaml")//
				.put("subpath", "/games-matchmaking");

		WebApiService.create(vertx).bind(config);

		LOGGER.info("Game Matchmaking API started");

	}

}
