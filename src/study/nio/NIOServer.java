package study.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {

	private int flag = 0;
	private int BLOCK = 4096;
	private ByteBuffer sendbuffer = ByteBuffer.allocate(BLOCK);
	private ByteBuffer receivebuffer = ByteBuffer.allocate(BLOCK);
	private Selector selector;

	public NIOServer(int port) throws IOException {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);

		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));

		selector = Selector.open();
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		System.out.println("nio server start,port:" + port);
	}

	private void listen() throws IOException {
		while (true) {
			selector.select();

			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectionKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				iterator.remove();
				handleKey(selectionKey);
			}
		}
	}

	private void handleKey(SelectionKey selectionKey) throws IOException {

		ServerSocketChannel server = null;
		SocketChannel client = null;
		String receiveText;
		String sendText;
		int count = 0;

		if (selectionKey.isAcceptable()) {
			server = (ServerSocketChannel) selectionKey.channel();
			client = server.accept();

			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
		} else if (selectionKey.isReadable()) {
			client = (SocketChannel) selectionKey.channel();
			
			receivebuffer.clear();
			count = client.read(receivebuffer);
			if (count > 0) {
				receiveText = new String(receivebuffer.array(), 0, count);
				System.out.println("server receive data:" + receiveText);
				client.register(selector, SelectionKey.OP_WRITE);
			}
		} else if (selectionKey.isWritable()) {

			sendbuffer.clear();

			client = (SocketChannel) selectionKey.channel();
			sendText = "message from server--" + flag++;

			sendbuffer.put(sendText.getBytes());

			sendbuffer.flip();

			client.write(sendbuffer);
			client.write(sendbuffer);
			System.out.println("server write data to client:" + sendText);
			client.register(selector, SelectionKey.OP_READ);
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		int port = 8888;
		NIOServer server = new NIOServer(port);
		server.listen();
	}
}