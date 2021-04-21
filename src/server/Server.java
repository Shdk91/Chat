package server;

import server.Connection;
import server.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static void main(String[] args)  {
        int x = ConsoleHelper.readInt();
        try(ServerSocket serverSocket = new ServerSocket(x)) {
            System.out.println("Сервер запущен");
            Socket socket;
            while (true){
                socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка");
        }
    }

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> a: connectionMap.entrySet()){
            try {
                a.getValue().send(message);
            } catch (IOException e) {
                System.out.println("Отправка сообщения не удалась");
            }
        }
    }



    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) {
            String name = null;
            Message request = new Message(MessageType.NAME_REQUEST, "Введите имя");
            try {
                connection.send(request);
                boolean accept = true;

                while (accept) {
                    try {
                        Message message = connection.receive();
                        name = message.getData();
                        if (message.getType() != MessageType.USER_NAME || name.isEmpty() || connectionMap.containsKey(name)) {
                            connection.send(request);
                        } else {
                            connectionMap.put(name, connection);
                            connection.send(new Message(MessageType.NAME_ACCEPTED, "Имя принято"));
                            accept = false;
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return name;

        }

        private void notifyUsers(Connection connection, String userName){
            for (Map.Entry<String, Connection> a : connectionMap.entrySet()){
                if (!a.getKey().equals(userName)){
                    try {
                        connection.send(new Message(MessageType.USER_ADDED, a.getKey()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String data = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, data));
                } else {
                    ConsoleHelper.writeMessage("Ошибка ввода");
                }
            }
        }
        public void run(){
            ConsoleHelper.writeMessage(socket.getRemoteSocketAddress().toString());
            try (Connection connection = new Connection(socket))
            {
                String name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с удаленным адресом.");
            }

        }
    }
}
