package sample;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Client extends JFrame {
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message="";
    private String serverIP;
    private Socket connection;

    //constructor
    public Client (String host){
        super("Client");
        serverIP=host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        sendMessage(event.getActionCommand());
                        userText.setText("");
                    }
                }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(400,200);
        setVisible(true);
    }

    //start running
    public void startRunning(){
        try{
            connectToServer();
            setupStreams();
            whileChatting();
        }catch(EOFException eofException){
            showMessage("\n Client terminated connection");
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally{
            closeAll();
        }
    }

    //connect to server
    private void connectToServer() throws IOException{
        showMessage("Attempting connection...");
        connection = new Socket (InetAddress.getByName(serverIP), 6789);
        showMessage("Connected to :"+ connection.getInetAddress().getHostName());
    }

    //get stream to send and receive data
    private void setupStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are now setup! \n");
    }

    //while chatting with server
    private void whileChatting() throws IOException{
        ableToType(true);
        do{
            try{
                message = (String)input.readObject();
                showMessage("\n"+ message);
            }catch(ClassNotFoundException classNotFoundException){
                showMessage("Unknown type of object");
            }

        }while(!message.equals("SERVER - END"));;
    }

    //close the streams and sockets
    private void closeAll(){
        showMessage("\n Exiting...");
        ableToType(false);
        try{
          output.close();
          input.close();
          connection.close();
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    //send messages to server
    private void sendMessage(String message){
        try{
            output.writeObject("CLIENT - "  + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        }catch (IOException ioException){
            chatWindow.append("\n Something went wrong!");
        }
    }

    //updates chatWindow
    private void showMessage(final String text){
        SwingUtilities.invokeLater(
                new Runnable(){
                    @Override
                    public void run() {
                        chatWindow.append(text);
                    }
                }
        );
    }

    //permits user to type
    private void ableToType(final boolean tof){
        SwingUtilities.invokeLater(
                new Runnable(){
                    @Override
                    public void run() {
                        userText.setEditable(tof);
                    }
                }
        );
    }
}
