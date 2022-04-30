package com.unito.prog3.fmail.server;

import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.MailServer;
import com.unito.prog3.fmail.model.Mailbox;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/*
 *The ThreadConnectionHandle is a runnable that is used to handle the requests of the client.
 *It has to sort three different requests:
 *  - new account asked for the connection for the first time. -> clientConnectionCase()
 *  - a client wants to send an email. -> sendMailCase()
 *  - a client wants to delete/close connection/update. -> requestCase()
 */
public record ThreadConnectionHandle(MailServer server, Socket socket) implements Runnable {
    @Override
    public void run() {
        synchronized (server) {
            ObjectInputStream input;
            ObjectOutputStream output;
            Object in;
            try {
                input = new ObjectInputStream(socket.getInputStream());
                output = new ObjectOutputStream(socket.getOutputStream());
                try {
                    in = input.readObject();
                    //new client asked for connection
                    if (in instanceof String name) {
                        clientConnectionCase(output, name);
                    //new email sent
                    } else if (in instanceof Email email) {
                        sendMailCase(output, email);
                    //a client sent a request (update, delete_all/delete_single, close connection)
                    } else if (in instanceof ArrayList request) {
                        requestCase(output, request);
                    }
                } finally {output.flush();input.close();output.close();input.close();socket.close();}
            } catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
        }
    }

    /**
     * It has to handle the request of the client that depends on the buttons clicked or the events occurred.
     * It can have an update request, a delete_all request, a delete_single request, a connection close request.
     * @param output the output stream built from the socket
     * @param request the array tha contains the string requests + some additional data.
     * @throws IOException;
     */
    private void requestCase(ObjectOutputStream output, ArrayList<String> request) throws IOException {
        //update case
        String client_name = request.get(0);
        switch (request.get(1)) {
            case "update":
                if (server.existAccount(client_name)) {
                    Objects.requireNonNull(output).writeObject("true");
                    Objects.requireNonNull(output).writeObject(server.getMailboxes().get(server.getIndexByName(client_name)));
                } else {
                    output.writeObject("false");
                }
                break;

        //delete_all case
            case "delete_all":
                if (server.existAccount(client_name)) {
                    server.getMailboxes().get(server.getIndexByName(client_name)).clearMailDel();
                    server.clearDelEmail(client_name);
                    Objects.requireNonNull(output).writeObject("true");
                    Platform.runLater(() -> server.addLog(new Date() + ": Deleted mails of client " + client_name + " successfully cleared"));
                } else {
                    output.writeObject("false");
                }
                break;
        //delete_single case
            case "delete_single":
                if (server.existAccount(client_name)) {
                    int id = Integer.parseInt(request.get(2));

                    if (request.get(3).equals("rcvd")) {
                        Mailbox mailbox = server.getMailboxes().get(server.getIndexByName(client_name));
                        Objects.requireNonNull(output).writeObject("true");

                        mailbox.delete_email_rcvd(id);
                        server.deleteEmailRcvd(client_name, id);
                        Objects.requireNonNull(output).writeObject(server.getMailboxes().get(server.getIndexByName(client_name)));

                        Platform.runLater(() -> server.addLog(new Date() + ": Email " + id + " from client " + client_name + " was successfully deleted"));

                    }else if (request.get(3).equals("sent")){
                        Mailbox mailbox = server.getMailboxes().get(server.getIndexByName(client_name));
                        Objects.requireNonNull(output).writeObject("true");

                        mailbox.delete_email_sent(id);
                        server.deleteEmailSent(client_name, id);
                        Objects.requireNonNull(output).writeObject(server.getMailboxes().get(server.getIndexByName(client_name)));

                        Platform.runLater(() -> server.addLog(new Date() + ": Email " + id + " from client " + client_name + " was successfully deleted"));
                    }
                }else{
                    output.writeObject("false");
                }
                break;
        //close_connection case
            case "close_connection":
                if (server.existAccount(client_name)) {
                    Objects.requireNonNull(output).writeObject("true");
                    Platform.runLater(() -> server.addLog(new Date() + ": Client " + client_name + " closed the connection with the server"));
                    server.getMailboxes().get(server.getIndexByName(client_name)).setConnected(false);
                } else {
                    output.writeObject("false");
                }
                break;
            default:
                output.writeObject("false");
                break;
        }
    }

    /**
     * It is called when the user clicked on the send button for an email.
     * It has to look find all the recipients and check for their existence.
     * Finally, it has to save locally the new mail sent.
     * @param output the output stream built from the socket.
     * @param email the email the clients wants to send.
     * @throws IOException;
     */
    private void sendMailCase(ObjectOutputStream output, Email email) throws IOException {
        String recipients_text = email.getTo();
        String[] recipients = recipients_text.split(" ");
        ArrayList<String> NaNAccounts = new ArrayList<>();
        for (String recipient : recipients) {
            if (!server.existAccount(recipient)) {
                NaNAccounts.add(recipient);
                recipients_text = recipients_text.replace(recipient, "");
            }
        }
        email.setTo(recipients_text);
        for (String s2 : recipients) {
            if (!server.saveEmail(email, s2)) {
                Platform.runLater(() -> server.addLog(new Date() + ": Error occurred on saving email" + email.getId() + " from " + email.getFrom() + " to " + s2 + " because the recipient doesn't exists"));
            }else {
                Platform.runLater(() -> server.addLog(new Date() + ": Email from " + email.getFrom() + " to " + s2 + " sent successfully"));
            }
        }if(NaNAccounts.isEmpty()) {
            Objects.requireNonNull(output).writeObject("true");
        }else{
            Objects.requireNonNull(output).writeObject("false");
            Objects.requireNonNull(output).writeObject(NaNAccounts);
        }
    }

    /**
     * It is called when the user wants to close the GUI;
     * The server writes on the log show file this event.
     * @param output the output stream built from the socket.
     * @param name the account name of the client that sent the closing request.
     * @throws IOException;
     */
    private void clientConnectionCase(ObjectOutputStream output, String name) throws IOException {
        if (server.existAccount(name)) {
            if(!server.getMailboxes().get(server.getIndexByName(name)).isConnected()){
                Objects.requireNonNull(output).writeObject("true");
                server.getMailboxes().get(server.getIndexByName(name)).setConnected(true);
                Platform.runLater(() -> server.addLog(new Date() + ": Client " + name + " is now connected"));
                Objects.requireNonNull(output).writeObject(server.getMailboxes().get(server.getIndexByName(name)));
            }
        } else {
            output.writeObject("false");
            Platform.runLater(() -> server.addLog(new Date() + ":Unknown client " + name + " asked for a closing request"));
        }
    }

}



