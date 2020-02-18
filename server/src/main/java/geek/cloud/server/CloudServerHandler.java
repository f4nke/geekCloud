package geek.cloud.server;

import com.geek.cloud.common.FileMessage;
import com.geek.cloud.common.MyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CloudServerHandler extends ChannelInboundHandlerAdapter {
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
                if (Files.exists(Paths.get("server_storage/" +  ((MyMessage) msg).getText()))) {
                    System.out.println("Клиент скачивает файл: " + ((MyMessage) msg).getText());
                    FileMessage fr = new FileMessage(Paths.get("server_storage/" + ((MyMessage) msg).getText()));
                    ctx.writeAndFlush(fr);
                }




            }
            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                System.out.println("Получен файл от клиента");
                Files.write(Paths.get("server_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
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
