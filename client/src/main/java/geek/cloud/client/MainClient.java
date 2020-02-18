package geek.cloud.client;



import com.geek.cloud.common.FileMessage;
import com.geek.cloud.common.MyMessage;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MainClient {
    public static void main(String[] args) {
        ObjectEncoderOutputStream oeos = null;
        ObjectDecoderInputStream odis = null;

        try (Socket socket = new Socket("localhost", 8189)) {
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            MyMessage downloadFile = new MyMessage("server.txt"); // запрашиваю имя файла на сервере
            FileMessage fmgToServer = new FileMessage(Paths.get("client_storage", "client.txt"));
            oeos.writeObject(downloadFile);
            oeos.writeObject(fmgToServer);
            oeos.flush();
            odis = new ObjectDecoderInputStream(socket.getInputStream(), 100 * 1024 * 1024);
            while (true) {
                MyMessage fromServer =  (MyMessage) odis.readObject();
                    if (fromServer instanceof FileMessage) {
                    FileMessage fileFromServer = (FileMessage)  fromServer;
                    Files.write(Paths.get("client_storage/" + fileFromServer.getFilename()), fileFromServer.getData(), StandardOpenOption.CREATE);
                        System.out.println("test");

                } else
                   System.out.println("Answer from serv: " + fromServer.getText());
            }

            } catch(Exception e){
                e.printStackTrace();
            } finally{
                try {
                    oeos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    odis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }


//    serializableClientExample();
//}
//
//private static void serializableClientExample() {
//        try (Socket socket = new Socket("localhost", 8189);
//        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
//        CloudPackage cp = new CloudPackage("Hello from client");
//        TempFileMsg tf = new TempFileMsg(Paths.get("GH1tyJbTcxI.jpg"));
//        out.writeObject(tf);
//        } catch (IOException e) {
//        e.printStackTrace();
//        }