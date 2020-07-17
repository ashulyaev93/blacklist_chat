package Lesson_6.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {

    private MainServer server;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private List<String> blacklist;

    private String nick;
    private int id;

    public ClientHandler(MainServer server, Socket socket) {
        try {
            this.blacklist = new ArrayList<>();
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String str = in.readUTF();

                            if(str.startsWith("/auth")) {
                                String[] tokens = str.split(" ");
                                String newNick = AuthService.getNickByLoginAndPass(tokens[1], tokens[2]);
                                int newId = AuthService.getIdByNick(newNick);
                                if(newNick != null) {
                                    if(!server.isNickBusy(newNick)) {
                                        sendMsg("/authok");
                                        nick = newNick;
                                        id = newId;
                                        server.subscribe(ClientHandler.this);
                                        break;
                                    } else {
                                        sendMsg("Учетная запись уже используется!");
                                    }
                                } else {
                                    sendMsg("Неверный логин/пароль");
                                }
                            }
                        }

                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    out.writeUTF("/serverclosed");
                                    break;
                                }
                                if (str.startsWith("/w ")) {
                                    String[] tokens = str.split(" ",3);
                                    server.sendPersonalMsg(ClientHandler.this, tokens[1], tokens[2]);
                                }
                                if (str.startsWith("/blacklist ")) {
                                    String[] tokens = str.split(" ");
                                    int Id = AuthService.getIdByNick(tokens[1]);
                                    AuthService.addBlockedList(ClientHandler.this.getId(), Id); 
                                    sendMsg("Вы добавили пользователя " + tokens[1] + " в черный список");
                                }
                                if (str.startsWith("/delblack ")) {
                                    String[] tokens = str.split(" ");
                                    int Id = AuthService.getIdByNick(tokens[1]);
                                    AuthService.removeBlockedList(ClientHandler.this.getId(), Id); // добавить id для удаления;
                                    sendMsg("Вы удалили пользователя " + tokens[1] + " из черного списка");
                                }
                            } else {
                                server.broadcastMsg(ClientHandler.this, nick + " : " + str);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        server.unsubscribe(ClientHandler.this);
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
    public String getNick() {
        return nick;
    }

    public boolean checkBlackList(String nick, String nick2)  {

        Boolean result = false;

        try{
            result = AuthService.checkBlockedList(nick, nick2);
        }
        catch (SQLException e){
            e.printStackTrace();
        }

        return result;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
