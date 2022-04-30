package com.unito.prog3.fmail.server;

import com.unito.prog3.fmail.model.MailServer;
import com.unito.prog3.fmail.support.Support;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 *StartConnectionHandle is runnable called in the method initialize of MailServerController.
 *It is used to handle with different threads the connection of the clients.
 *We decided to use a PoolThread with a fixed number (10).
 *In the run method when a new connection is called, it accepts it on a new socket and gives
 *to a thread the runnable ThreadConnectionHandle that has as parameters the server and the socket.
 */
public record StartConnectionHandle(MailServer server) implements Runnable {
    @Override
    public void run() {
        ExecutorService exc = Executors.newFixedThreadPool(10);
        try {
            ServerSocket server_socket = new ServerSocket(Support.port);
            server.addLog(new Date() + " : Server connected.");
            while(true){
                Socket incoming = server_socket.accept();
                Runnable connectionHandle = new ThreadConnectionHandle(server,incoming);
                exc.execute(connectionHandle);
            }
        }catch (IOException e) {e.printStackTrace();}
    }
}
