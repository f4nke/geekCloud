package geek.cloud.server;

import com.geek.cloud.common.FileMessage;
import com.geek.cloud.common.MyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class CloudServerHandler extends ChannelInboundHandlerAdapter {
    String nick;
    Path nickPath;
    String str;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {

                    if (msg == null) {
                        return;
                    }
                    System.out.println(msg.getClass());
                    if (msg instanceof MyMessage) {

                        System.out.println("Client text message: " + ((MyMessage) msg).getText());
                        if (Files.exists(Paths.get("server_storage/" + ((MyMessage) msg).getText()))) {
                            System.out.println("Клиент скачивает файл: " + ((MyMessage) msg).getText());
                            FileMessage fr = new FileMessage(Paths.get("server_storage/" + ((MyMessage) msg).getText()));
                            ctx.writeAndFlush(fr);
                        }
                        if (((MyMessage) msg).getText().equals("список")) {

                            ctx.writeAndFlush(new MyMessage(Files.list(Paths.get("server_storage")).toString()));
                        }
                        else
                            try {
                                str = ((MyMessage) msg).getText();
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[0], tokens[1]);
                                nick = newNick;
                                nickPath = Files.createDirectory(Paths.get("server_storage/" + nick));
                                MyMessage success = new MyMessage("Подключение успешно");
                                ctx.writeAndFlush(success);
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Неверный логин или пароль");
                                ctx.writeAndFlush(new MyMessage("Неверный логин или пароль"));
                            }

                    }
                    if (msg instanceof FileMessage) {
                        FileMessage fm = (FileMessage) msg;
                        System.out.println("Получен файл от клиента");
                        Files.write(Paths.get(nickPath + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        System.out.println("Файл сохранен");

                    }




        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
