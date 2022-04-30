package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.ClientMain;
import com.unito.prog3.fmail.model.MailClient;
import com.unito.prog3.fmail.model.Mailbox;
import com.unito.prog3.fmail.support.Support;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import static com.unito.prog3.fmail.support.Support.alertMethod;

public class ConnectionController {
    @FXML
    private TextField account_name;

    /**
     * It checks if the account name inserted follows the correct format, then
     * calls the getConnection() method which returns a string that indicates the state of the connection.
     * If the result is "CC" then the client was able to connect successfully, so it calls startBeat() and finally changes the scene.
     * If the result is "CNR" it means that the account name inserted is not registered.
     * if the result is "SNC" it means that we have not been able to establish a connection with the server because is probably offline.
     */
    @FXML
    public void getConnectionButton() throws IOException {
        if(Support.match_account(account_name.getText())){
            MailClient client = new MailClient(new Mailbox(account_name.getText()));
            String result = client.getConnection();
            System.out.println(result);
            switch (result) {
                //Client Connected
                case "CC" -> {
                    client.startBeat();

                    FXMLLoader homeloader = new FXMLLoader(ClientMain.class.getResource("Home.fxml"));
                    Parent root = homeloader.load();
                    HomeController homeController = homeloader.getController();
                    homeController.initModel(client);
                    Stage window = (Stage) account_name.getScene().getWindow();
                    window.setScene(new Scene(root));
                    window.setOnCloseRequest(windowEvent -> {
                        client.closeAction();
                        window.close();
                        System.exit(0);
                    });
                }
                //Client Not Registered
                case "CNR" -> alertMethod("Email account inserted is not registered, try with another email account");
                //Server Not Connected
                case "SNC" -> alertMethod("There was an error connecting to the server, please try again");
            }
        }else{
            System.out.println("Email not correct");
            alertMethod("Email account inserted does not respect the format requested");
        }
    }

}
