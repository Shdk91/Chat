package client;

import server.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    @Override
    protected SocketThread getSocketThread() {
        BotSocketThread botSocketThread = new BotSocketThread();
        return botSocketThread;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        int x = (int) (Math.random() * 100);
        return "date_bot_"+x;
    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String userNameDelimiter = ": ";
            String[] split = message.split(userNameDelimiter);
            String userName = split[0];
            if (split.length != 2) return;

            Date date = new GregorianCalendar().getTime();
            switch (split[1]){
                case "дата":
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("d.MM.YYYY").format(date));
                    break;
                case "день" :
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("d").format(date));
                    break;
                case "месяц" :
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("MMMM").format(date));
                    break;
                case "год" :
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("YYYY").format(date));
                    break;
                case "время" :
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("H:mm:ss").format(date));
                    break;
                case "час" :
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("H").format(date));
                    break;
                case "минуты":
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("m").format(date));
                    break;
                case "секунды":
                    sendTextMessage("Информация для " + userName + ": " + new SimpleDateFormat("s").format(date));
                    break;
            }

        }
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }
}
