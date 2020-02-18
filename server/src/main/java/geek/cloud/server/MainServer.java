package geek.cloud.server;

import com.geek.cloud.common.MyMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    public void run() throws Exception {
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024 * 100, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new CloudServerHandler()
                            );
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = b.bind(8189).sync();
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new MainServer().run();
    }
}


//    serializableServerExample();
//}
//
//
//
//private static void serializableServerExample() {
//        try (ServerSocket serverSocket = new ServerSocket(8189)) {
//        System.out.println("Сервер запущен. Ожидаем подключение клиента");
//        try (Socket socket = serverSocket.accept();
//        ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
//        System.out.println("Клиент подключился");
//        MyMessage tf = (MyMessage)in.readObject();
//        System.out.println("Получен пакет от клиента: " + tf);
//        } catch (ClassNotFoundException e) {
//        e.printStackTrace();
//        }
//        } catch (IOException e) {
//        e.printStackTrace();
//        }