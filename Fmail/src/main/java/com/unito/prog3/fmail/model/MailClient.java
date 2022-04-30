package com.unito.prog3.fmail.model;

import com.unito.prog3.fmail.support.Support;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;

/*
 * The class MailClient represents the single client that can start a connection with the server.
 * It has the following variables:
 *  - mailbox: the mailbox linked to the account
 *  - local: the address of the client JVM (in our case is all on local).
 *  - connect: a flag that shows the actual status of the connection with the server.
 */

public class MailClient {
    private Mailbox mailbox;
    private InetAddress local;
    private boolean connect = false;

    /**
     * Constructor of the class;
     * @param mailbox the mailbox of the client.
     */
    public MailClient(Mailbox mailbox){
        try{local = InetAddress.getLocalHost();
        }catch (UnknownHostException e){e.printStackTrace();}

        this.mailbox = mailbox;
    }

    /**
     *  Constructor of the class; It has no param because is called
     *  in the initialize method of HomeController.java
     */
    public MailClient(){
        try{local = InetAddress.getLocalHost();
        }catch (UnknownHostException e){e.printStackTrace();}

        this.mailbox = new Mailbox();
    }

    /**
     * It tries to open a connection with the server; it sends to the server the name of the MailClient
     * and waits for a "true" string. Then sets return_result to "CC" (Client Connected) and waits for the mailbox.
     * Finally, set this.connect to true and ends.
     * @return CC: is the client can connect;
     * CNR: if the connection was accepted but the server did not recognise the client;
     * SNC: if the server did not accept the connection because it could be offline;
     */
    public String getConnection() {
        ObjectOutputStream output;
        ObjectInputStream input;
        String return_result;
        try {
            Socket client_socket = new Socket(this.local, Support.port);
            output = new ObjectOutputStream(client_socket.getOutputStream());
            input = new ObjectInputStream(client_socket.getInputStream());
            try {
                Objects.requireNonNull(output).writeObject(this.mailbox.getAccount_name());

                String in = (String) input.readObject();
                if (in.equals("true")) {
                    return_result = "CC"; //Client Connected
                    this.mailbox = (Mailbox) input.readObject();
                    connect = true;
                }else{
                    return_result = "CNR"; //Client Not Registered
                }
            }finally {output.flush();input.close();output.close();client_socket.close();}
        }catch (IOException | ClassNotFoundException e){
            return_result = "SNC"; //Server Not Connected
        }
        return return_result;
    }

    /**
     * A connection with the server is created and the email is sent
     * then waits for a string value from the server that indicates whether the email was sent successfully or not.
     * @param email email to send
     * @return true if the email was sent successfully; false otherwise.
     */
    public ArrayList<String> sendEmail(Email email) {
        ObjectOutputStream output;
        ObjectInputStream input;
        ArrayList<String> fails = new ArrayList<>();
        try{
            Socket client_socket = new Socket(this.local, Support.port);
            output = new ObjectOutputStream(client_socket.getOutputStream());
            input = new ObjectInputStream(client_socket.getInputStream());
            try {
                Objects.requireNonNull(output).writeObject(email);

                String in = (String) input.readObject();
                if(in.equals("true")){
                    System.out.println(mailbox.toString());
                }else{
                    fails = (ArrayList<String>) input.readObject();
                }
            } finally {output.flush();input.close();output.close();client_socket.close();}
        }catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
        return fails;
    }

    /**
     * Send a request to update the mailbox. If the server accepts the connection,
     * the client gets the new mailbox.
     * @return true if the update request went successfully; false otherwise.
     */
    public boolean updateAction(){
        boolean result = false;
        if(isConnect()){
            ObjectOutputStream output;
            ObjectInputStream input;
            try {
                Socket client_socket = new Socket(this.local, Support.port);
                output = new ObjectOutputStream(client_socket.getOutputStream());
                input = new ObjectInputStream(client_socket.getInputStream());
                try {
                    ArrayList<String> client_request = new ArrayList<>();
                    client_request.add(this.mailbox.getAccount_name());
                    client_request.add("update");

                    Objects.requireNonNull(output).writeObject(client_request);

                    String in = (String) input.readObject();
                    if (in.equals("true")) {
                        this.mailbox = (Mailbox) input.readObject();
                        result = true;
                    }
                } finally {output.flush();input.close();output.close();client_socket.close();}
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Server offline");
            }
        }
        return result;
    }

    /**
     * It sends to the server a request of deleting. The client can ask for the deleting of the single
     * or the deleting of all the mail_del list. It waits for the accepting of the connection and then
     * will get the new mailbox from the server.
     * @param request type of the deleting request.
     * @param id of the email to delete.
     * @param position in which list the mail is.
     * @return true if the delete request went successfully; false otherwise.
     */
    public boolean deleteAction(String request, String id, String position){
        boolean result = false;
        if(isConnect()){
            ObjectOutputStream output;
            ObjectInputStream input;
            try {
                Socket client_socket = new Socket(this.local, Support.port);
                output = new ObjectOutputStream(client_socket.getOutputStream());
                input = new ObjectInputStream(client_socket.getInputStream());
                try {
                    ArrayList<String> client_request = new ArrayList<>();
                    client_request.add(this.mailbox.getAccount_name());
                    client_request.add(request);
                    if(request.equals("delete_single")){
                        client_request.add(id);
                        client_request.add(position);
                    }
                    Objects.requireNonNull(output).writeObject(client_request);

                    String in = (String) input.readObject();
                    if (in.equals("true")) {
                        if (request.equals("delete_single")) {
                            this.mailbox = (Mailbox) input.readObject();
                        }
                        result = true;
                    }
                } finally {output.flush();input.close();output.close();client_socket.close();}
            } catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
        }
        return result;
    }

    /**
     * It is called by the client when the connection starts.
     * It is used to check every 2 seconds if the server is still online.
     * If the heartbeat sent catch an exception, it sets the connection flag to false
     * and the connection after 2 seconds
     */
    public void startBeat(){
        Thread heartbeatThread = new Thread(() -> {
            while (true) {
                try {
                    Socket client_socket = new Socket(this.local, Support.port);
                    ObjectOutputStream output = null;
                    try {
                        output = new ObjectOutputStream(client_socket.getOutputStream());
                        output.writeObject(this.mailbox.getAccount_name());
                        this.setConnect(true);
                        Thread.sleep(2000);
                    }finally {
                        assert output != null;
                        output.close();
                        client_socket.close();
                    }
                }catch(IOException | InterruptedException e){
                    System.out.println("Server offline");
                    this.setConnect(false);
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    /**
     * It notifies the server of a close request. It waits for the accepting of the server,
     * and then set the result flag to true. If the server does not accept the connection
     * the client closes the connection anyway
     * @return true if the close request went successfully; false otherwise.
     */
    public boolean closeAction(){
        boolean result = false;
        if(isConnect()) {
            ObjectOutputStream output;
            ObjectInputStream input;
            try {
                Socket client_socket = new Socket(this.local, Support.port);
                output = new ObjectOutputStream(client_socket.getOutputStream());
                input = new ObjectInputStream(client_socket.getInputStream());
                try {
                    ArrayList<String> client_request = new ArrayList<>();
                    client_request.add(this.mailbox.getAccount_name());
                    client_request.add("close_connection");
                    Objects.requireNonNull(output).writeObject(client_request);

                    String in = (String) input.readObject();
                    if (in.equals("true")) {
                        result = true;
                    }
                } finally {output.flush();input.close();output.close();client_socket.close();}
            } catch (IOException | ClassNotFoundException e){
                System.out.println("Server offline");
            }
        }
        return result;
    }


    /**
     * It is called by the client when an updating request is called.
     * It checks if the list of the mailbox had a changing in the size.
     * @param list a char that represents the list to check.
     * @return new_size of the list mail that client asked for.
     */
    public int checkChangeMail(char list) {
        int new_size = 0;
        switch (list) {
            case 'r' -> new_size = mailbox.getAllMailRcvd().size();
            case 's' -> new_size = mailbox.getAllMailSent().size();
            case 'd' -> new_size = mailbox.getAllMailDel().size();
        }
        return new_size;
    }

    public Mailbox getMailbox() {return mailbox;}

    public boolean isConnect() {return connect;}

    public void setConnect(boolean connect) {this.connect = connect;}

    @Override
    public String toString() {
        return "\n      MailClient{" +
                ", mailbox=" + mailbox +
                "}";
    }
}

