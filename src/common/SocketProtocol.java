package common;

public interface SocketProtocol {

	public void setSocket(AsyncSocket sock);

	public byte[] textPacket(String str);

	public String parsePacket(byte[] bytes);
}
