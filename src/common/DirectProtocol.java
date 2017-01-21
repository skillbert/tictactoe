package common;

public class DirectProtocol implements SocketProtocol {
	@Override
	public void setSocket(AsyncSocket sock) {
	}

	@Override
	public byte[] textPacket(String str) {
		return str.getBytes(Protocol.charset);
	}

	@Override
	public String parsePacket(byte[] bytes) {
		return new String(bytes, Protocol.charset);
	}
}
