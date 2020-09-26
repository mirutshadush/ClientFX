package client;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import models.ChatMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class Client extends Thread {
    String host;
    int port;
    boolean isConnected = false;
    public BufferedReader reader;
    public PrintWriter writer;
    String username;
    //    public ObservableList<ChatMessage> messages = FXCollections.<ChatMessage>observableArrayList();
    public HashMap<String, ObservableList<ChatMessage>> messages = new HashMap<>();
    public ObservableList<String> friends = FXCollections.<String>observableArrayList();

    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            byte[] ipaddr = new byte[4];
            if (this.host.toLowerCase().equals("localhost")) {
                ipaddr[0] = (byte) 127;
                ipaddr[1] = (byte) 0;
                ipaddr[2] = (byte) 0;
                ipaddr[3] = (byte) 1;
            } else {
                String[] hostList = this.host.split("\\.");
                if (hostList.length != 4) {
                    throw new Exception("Invalid IP Address");
                }
                ipaddr[0] = (byte) Integer.parseInt(hostList[0]);
                ipaddr[1] = (byte) Integer.parseInt(hostList[1]);
                ipaddr[2] = (byte) Integer.parseInt(hostList[2]);
                ipaddr[3] = (byte) Integer.parseInt(hostList[3]);
            }

            Socket socket = new Socket(InetAddress.getByAddress(ipaddr), port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            if (response.equals("Enter your name please")) {
                writer.println(username);
                this.isConnected = true;
            }
            String message;
            while ((message = reader.readLine()) != null) {

                List<String> messageArray = Arrays.asList(message.split(" "));
                if (messageArray.get(0).equals("newuser")) {
                    friends.add(messageArray.get(1));
                    continue;
                } else if (messageArray.get(0).equals("users")) {
                    friends.addAll(messageArray.subList(1, messageArray.size()));
                    continue;
                }
                String from = messageArray.get(0);
                StringBuilder realMessage = new StringBuilder();

                for (int i = 1; i < messageArray.size(); i++) {
                    realMessage.append(messageArray.get(i)).append(" ");
                }
                if (!messages.containsKey(from)) {
                    Platform.runLater(() -> messages.put(from, FXCollections.observableArrayList(new ChatMessage(from, username, realMessage.toString()))));
                } else {
                    Platform.runLater(() -> messages.get(from).add(new ChatMessage(from, username, realMessage.toString())));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}