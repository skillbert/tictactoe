package common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Provide an event based wrapper for async sockets.
 * 
 * @author Wilbert
 *
 */
public class AsyncSocket {
	private static final int BUFFERSIZE = 2048;

	private AsynchronousSocketChannel channel;
	private Queue<byte[]> writequeue = new LinkedList<>();
	private boolean iswriting = false;
	private boolean isreading = false;
	private ByteBuffer readbuffer = ByteBuffer.allocate(BUFFERSIZE);

	private Callback0 closeCb;
	private Callback0 connectedCb;
	private Callback0 connectFailedCb;
	private Callback1<byte[]> packetCb;

	/**
	 * Creates an idle AsyncSocket object. Call the connect function after
	 * setting the event functions to connect to a server
	 */
	public AsyncSocket() {
	}

	/**
	 * Creates an AsyncSocket object from an existing and connected
	 * AsynchronousSocketChannel
	 * 
	 * @param channel
	 *            an existing AsynchronousSocketChannel to wrap
	 */
	public AsyncSocket(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}

	/**
	 * Connect the socket to a server
	 * 
	 * @param host
	 *            hostname or ip of the server
	 * @param port
	 *            the port number to connect to
	 * @throws IOException
	 */
	public void connect(String host, int port) throws IOException {
		InetSocketAddress addr = new InetSocketAddress(host, port);
		try {
			channel = AsynchronousSocketChannel.open();
			channel.connect(addr, null, new Util.SimpleHandler<>(v -> connected(), ex -> connectFailed(ex)));
		} catch (UnresolvedAddressException | IOException ex) {
			close();
			connectFailed(ex);
		}
	}

	private void connected() {
		if (connectedCb != null) {
			connectedCb.run();
		}
		startReading();
	}

	private void connectFailed(Throwable ex) {
		if (connectFailedCb != null) {
			connectFailedCb.run();
		}
		close();
	}

	/**
	 * Send a series of bytes through the connection. The bytes are queued and
	 * flushed automatically
	 * 
	 * @param bytes
	 *            the bytes to send
	 */
	public synchronized void sendPacket(byte[] bytes) {
		writequeue.add(bytes);
		flush();
	}

	/**
	 * Attempts to flush any queued messages
	 */
	private synchronized void flush() {
		if (iswriting) {
			return;
		}
		byte[] bytes = writequeue.poll();
		if (bytes != null) {
			iswriting = true;
			channel.write(ByteBuffer.wrap(bytes), null, new Util.SimpleHandler<>(v -> {
				iswriting = false;
				flush();
			}, ex -> ex.printStackTrace()));
		}
	}

	/**
	 * Sets the callback function for when the connection closes
	 * 
	 * @param cb
	 *            The function to call when the socket connection closes
	 */
	public void onClose(Callback0 cb) {
		closeCb = cb;
	}

	/**
	 * Sets the callback function for when the socket receives a message
	 * 
	 * @param cb
	 *            This function is called with one string argument whenever the
	 *            socket receives a message
	 */
	public void onPacket(Callback1<byte[]> cb) {
		packetCb = cb;
		startReading();
	}

	/**
	 * Sets the callback function for when the socket is connected
	 * 
	 * @param cb
	 *            This function is called when the socket has successfully
	 *            connected
	 */
	public void onConnect(Callback0 cb) {
		connectedCb = cb;
	}

	/**
	 * Sets the callback function for if the socket failed to connect
	 * 
	 * @param cb
	 */
	public void onConnectFail(Callback0 cb) {
		connectFailedCb = cb;
	}

	/**
	 * closes the connection and the underlying socket
	 */
	public void close() {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException e) {
			}
			channel = null;
		}
	}

	/**
	 * Starts the async reading action on the socket
	 */
	private synchronized void startReading() {
		if (!isreading && isConnected()) {
			isreading = true;
			channel.read(readbuffer, null,
					new Util.SimpleHandler<>(length -> bufferReceived(length), ex -> connectionClosed()));
		}
	}

	/**
	 * Called when an error occurs while reading from the socket
	 */
	public void connectionClosed() {
		if (closeCb != null) {
			closeCb.run();
		}
		close();
	}

	/**
	 * Called when a new buffer is read from the socket
	 */
	private void bufferReceived(int length) {
		if (length < 0) {
			connectionClosed();
			return;
		}
		readbuffer.flip();
		byte[] bytes = new byte[length];
		readbuffer.get(bytes);
		if (packetCb != null) {
			packetCb.run(bytes);
		}
		readbuffer.clear();
		isreading = false;
		startReading();
	}

	/**
	 * gets whether this socket is connected.
	 */
	public boolean isConnected() {
		if (channel == null) {
			return false;
		}
		try {
			if (channel.getRemoteAddress() != null) {
				return true;
			}
		} catch (IOException ex) {
		}
		return false;
	}

	public AsynchronousSocketChannel getChannel() {
		return channel;
	}













	/**
	 * Generic callable interface without arguments or return value
	 * 
	 * @author Wilbert
	 *
	 */
	public static interface Callback0 {
		public abstract void run();
	}

	/**
	 * Generic callable interface with one argument and without return value
	 * 
	 * @author Wilbert
	 *
	 * @param <T>
	 */
	public static interface Callback1<T> {
		public abstract void run(T arg);
	}
}
