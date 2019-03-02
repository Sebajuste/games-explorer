package io.edge.games.explorer.verticle;

import java.util.HashMap;
import java.util.Map;

import io.edge.games.explorer.bo.Client;
import io.edge.games.explorer.service.GameRegistry;
import io.edge.games.explorer.service.impl.GameRegistryImpl;
import io.edge.games.explorer.util.UDPReliability;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class HolePunchingVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(HolePunchingVerticle.class);

	private final Map<Client, UDPReliability> reliabilityMap = new HashMap<>();
	
	private GameRegistry gameRegistry;

	private DatagramSocket socket;

	private UDPReliability getOrCreateUDPReliability(Client client) {
		UDPReliability reliability;
		
		if( this.reliabilityMap.containsKey(client)) {
		
			reliability = this.reliabilityMap.get(client);
		} else {
			reliability = new UDPReliability(vertx, socket, client);
			this.reliabilityMap.put(client, reliability);
		}
		return reliability;
	}

	private void requestProcess(DatagramPacket packet, UDPReliability reliability, JsonObject request) {
		
		if ("register".equalsIgnoreCase(request.getString("action"))) {

			JsonObject gameServer = request.getJsonObject("server");

			gameServer//
					.put("host", packet.sender().host())//
					.put("port", packet.sender().port());
			
			LOGGER.info("gameServer configured : " + gameServer);

			gameRegistry.register(request.getString("game"), gameServer, ar -> {

				if (ar.succeeded()) {

					if (ar.result()) {

						JsonObject response = new JsonObject()//
								.put("action", "response")//
								.put("status", "OK")//
								.put("server", new JsonObject()//
										.put("host", packet.sender().host())//
										.put("port", packet.sender().port())//
						);

						// this.sendResponse(packet.sender().host(), packet.sender().port(), response);
						
						reliability.send(response);
						
					} else {

						JsonObject response = new JsonObject()//
								.put("status", "ERROR");

						reliability.send(response);

					}

				} else {

					JsonObject response = new JsonObject()//
							.put("status", "ERROR");

					reliability.send(response);

				}

			});

		} else if ("unregister".equalsIgnoreCase(request.getString("action"))) {

			JsonObject gameServer = request.getJsonObject("server");

			gameRegistry.unregister(request.getString("game"), gameServer, ar -> {

				if (ar.succeeded()) {

					JsonObject response = new JsonObject()//
							.put("action", "response")//
							.put("status", "OK");

					reliability.send(response);

				} else {

					JsonObject response = new JsonObject()//
							.put("status", "ERROR");

					reliability.send(response);

				}

			});

		} else if ("request".equalsIgnoreCase(request.getString("action"))) {

			gameRegistry.getAll(request.getString("game"), ar -> {

				if (ar.succeeded()) {

					JsonObject response = new JsonObject()//
							.put("action", "request_response")//
							.put("status", "OK")//
							.put("game_list", new JsonArray(ar.result()));

					reliability.send(response);

				} else {

					JsonObject response = new JsonObject()//
							.put("status", "ERROR");

					reliability.send( response);

				}

			});

		} else if ("join".equalsIgnoreCase(request.getString("action"))) {

			JsonObject serverInfo = request.getJsonObject("server");

			gameRegistry.findGame(request.getString("game"), serverInfo.getString("host"), serverInfo.getInteger("port"), ar -> {

				if (ar.succeeded()) {

					JsonObject gameServer = ar.result();

					// TODO : check count players

					JsonObject joinRequest = new JsonObject()//
							.put("action", "join_client").put("client", new JsonObject()//
									.put("host", packet.sender().host())//
									.put("port", packet.sender().port()));

					// this.sendResponse(gameServer.getString("host"), gameServer.getInteger("port"), joinRequest);
					
					Client server = new Client(gameServer.getString("host"), gameServer.getInteger("port"));
					
					UDPReliability serverReliabilty = this.reliabilityMap.get(server);
					
					serverReliabilty.send(joinRequest, joinResult -> {
						
						if( joinResult.succeeded()) {
							
							JsonObject response = new JsonObject()//
									.put("status", "OK")//
									.put("action", "join_server")//
									.put("server", new JsonObject()//
											.put("host", server.getHost())//
											.put("port", server.getPort())
											);

							reliability.send(response);
							
						} else {
							
							JsonObject response = new JsonObject()//
									.put("status", "ERROR");

							reliability.send(response);
							
						}
						
					});
					

				} else {
					JsonObject response = new JsonObject()//
							.put("status", "ERROR");

					reliability.send(response);
				}

			});

		} else {

			// TODO : send error

			JsonObject response = new JsonObject()//
					.put("status", "ERROR")//
					.put("cause", "Action not found");

			reliability.send(response);

		}
		
	}
	
	private void onRequest(DatagramPacket packet) {

		Buffer requestPacket = packet.data();
		
		Client client = new Client(packet.sender().host(), packet.sender().port());

		UDPReliability reliability = this.getOrCreateUDPReliability(client);
		
		reliability.decode(requestPacket, ar -> {
			
			// Callback called only if the packet is not a response !
			
			if( ar.succeeded()) {
				
				JsonObject request = ar.result();
				
				this.requestProcess(packet, reliability, request);
				
			} else {
				LOGGER.error(ar.cause());
			}
			
		});

	}

	private void checkServerAlive(long timerID) {
		
		// TODO : send Ping for all server registered
		
		
		// this.gameRegistry.get
		
		
	}
	
	@Override
	public void start(Future<Void> startFuture) {

		this.gameRegistry = new GameRegistryImpl(vertx);

		DatagramSocketOptions options = new DatagramSocketOptions();

		// options.setReuseAddress(true);
		// options.setReusePort(true);

		this.socket = vertx.createDatagramSocket(options);

		this.socket.listen(config().getInteger("port", 34000), config().getString("interface", "0.0.0.0"), ar -> {

			if (ar.succeeded()) {
				
				this.socket.handler(this::onRequest);
				
				this.vertx.setPeriodic(5000L, this::checkServerAlive);
				
				LOGGER.info("Hole Punching server started");
				startFuture.complete();
			} else {
				LOGGER.error(ar.cause().getCause());
				startFuture.fail(ar.cause());
			}

		});

	}

}
