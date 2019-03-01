package io.edge.games.explorer.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.edge.games.explorer.bo.Client;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.json.JsonObject;

public class UDPReliability {

	private static final class BufferResponseHandler {

		protected final Handler<AsyncResult<JsonObject>> responseHandler;

		protected final long resendTimerID;

		protected final long timeoutTimerID;

		public BufferResponseHandler(Handler<AsyncResult<JsonObject>> responseHandler, long resendTimerID, long timeoutTimerID) {
			super();
			this.responseHandler = responseHandler;
			this.resendTimerID = resendTimerID;
			this.timeoutTimerID = timeoutTimerID;
		}

	}

	private final Map<Integer, BufferResponseHandler> packetMap = new HashMap<>();

	private final Vertx vertx;

	private final DatagramSocket socket;

	private final Client client;

	private int packetID = 0;

	public UDPReliability(Vertx vertx, DatagramSocket socket, Client client) {
		super();
		this.vertx = Objects.requireNonNull(vertx);
		this.socket = Objects.requireNonNull(socket);
		this.client = Objects.requireNonNull(client);
	}

	private int nextPacketID() {
		return packetID++;
	}

	public void send(JsonObject payload) {

		this.send(payload, null);

	}

	public void send(JsonObject payload, Handler<AsyncResult<JsonObject>> responseHandler) {

		int packetID = nextPacketID();

		Buffer packet = HolePunchingPacket.encode(packetID, payload);

		long resendTimerID = this.vertx.setPeriodic(5000L, timerID -> {

			socket.send(packet, client.getPort(), client.getHost(), sendResult -> {
				if (sendResult.succeeded()) {

				} else {
					// LOGGER.error(sendResult.cause());
				}
			});

		});

		long timeoutID = this.vertx.setTimer(30000L, timerID -> {
			// TODO : raise error
			this.vertx.cancelTimer(resendTimerID);
			BufferResponseHandler bufferResponse = packetMap.remove(packetID);
			if (bufferResponse != null) {
				this.vertx.cancelTimer(bufferResponse.resendTimerID);

				if (bufferResponse.responseHandler != null) {
					bufferResponse.responseHandler.handle(Future.failedFuture("Timeout"));
				}
			}

		});

		BufferResponseHandler bufferresponse = new BufferResponseHandler(responseHandler, resendTimerID, timeoutID);

		packetMap.put(packetID, bufferresponse);

		socket.send(packet, client.getPort(), client.getHost(), sendResult -> {
			if (sendResult.succeeded()) {

			} else {
				// LOGGER.error(sendResult.cause());
			}
		});

	}

	public void decode(Buffer packet, Handler<AsyncResult<JsonObject>> resultHandler) {

		HolePunchingPacket hpp = HolePunchingPacket.decode(packet);

		int packetID = hpp.getPacketID();

		if (packetMap.containsKey(packetID)) {
			BufferResponseHandler brh = packetMap.get(packetID);

			this.vertx.cancelTimer(brh.resendTimerID);
			this.vertx.cancelTimer(brh.timeoutTimerID);

			packetMap.remove(packetID);

			if (brh.responseHandler != null) {
				brh.responseHandler.handle(Future.succeededFuture(hpp.getPayload()));
			}

		} else {
			resultHandler.handle(Future.succeededFuture(hpp.getPayload()));
		}

	}

}
