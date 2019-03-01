package io.edge.games.explorer.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public final class HolePunchingPacket {

	private final int packetID;

	private final JsonObject payload;

	private HolePunchingPacket(int packetID, JsonObject payload) {
		super();
		this.packetID = packetID;
		this.payload = payload;
	}

	public static Buffer encode(int packetID, JsonObject data) {

		Buffer buffer = Buffer.buffer();

		String payload = data.encode();

		// TODO : calculate the CRC

		int crc = 0;

		return buffer.appendByte((byte)packetID).appendIntLE(payload.length()).appendString(payload).appendIntLE(crc);

	}

	public static HolePunchingPacket decode(Buffer buffer) {

		int packetID = buffer.getByte(0);

		int length = buffer.getIntLE(1);

		String payload = buffer.getString(5, 5 + length);

		// TODO : check the CRC

		// int crc = buffer.getIntLE(4 + length);

		return new HolePunchingPacket(packetID, new JsonObject(payload));

	}

	public int getPacketID() {
		return packetID;
	}

	public JsonObject getPayload() {
		return payload;
	}

}
