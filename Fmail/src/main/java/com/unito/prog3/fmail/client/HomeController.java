package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.ClientMain;
import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.MailClient;
import com.unito.prog3.fmail.support.Support;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.unito.prog3.fmail.support.Support.alertMethod;

public class HomeController implements Initializable {
    private MailClient client;

    private Email selectedEmail;

    private ObservableList<Email> email_rcvd_content;
    private ObservableList<Email> email_sent_content;
    private ObservableList<Email> email_del_content;
    private ListProperty<Email> email_rcvd_prop;
    private ListProperty<Email> email_sent_prop;
    private ListProperty<Email> email_del_prop;

    @FXML
    private TextField account_name_text;
    @FXML
    private ListView<Email> ListView_rcvd;
    @FXML
    private ListView<Email> ListView_sent;
    @FXML
    private ListView<Email> ListView_del;

    /**
     * It sets all the listview properties.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MailClient client = new MailClient();

        this.email_rcvd_content = FXCollections.observableList(client.getMailbox().getAllMailRcvd());
        email_rcvd_prop = new SimpleListProperty<>();
        email_rcvd_prop.set(email_rcvd_content);
        ListView_rcvd.itemsProperty().bind(email_rcvd_prop);

        this.email_sent_content = FXCollections.observableList(client.getMailbox().getAllMailSent());
        email_sent_prop = new SimpleListProperty<>();
        email_sent_prop.set(email_sent_content);
        ListView_sent.itemsProperty().bind(email_sent_prop);

        this.email_del_content = FXCollections.observableList(client.getMailbox().getAllMailDel());
        email_del_prop = new SimpleListProperty<>();
        email_del_prop.set(email_del_content);
        ListView_del.itemsProperty().bind(email_del_prop);

        ListView_rcvd.setOnMouseClicked(this::showSelectedEmail_rcvd);
        ListView_sent.setOnMouseClicked(this::showSelectedEmail_sent);
        ListView_del.setOnMouseClicked(this::showSelectedEmail_del);

        ListView_rcvd.setCellFactory(emailListView -> new Support.cellVisual());
        ListView_sent.setCellFactory(emailListView -> new Support.cellVisual());
        ListView_del.setCellFactory(emailListView -> new Support.cellVisual());

    }

    private void showSelectedEmail_del(MouseEvent mouseEvent) {
        Email email = ListView_del.getSelectionModel().getSelectedItem();
        selectedEmail = email;
        if(email != null) {
            Parent root;
            FXMLLoader viewLoader = new FXMLLoader(ClientMain.class.getResource("ViewmailPageDel.fxml"));
            try {
                root = viewLoader.load();
                ViewPageDelController viewPageController = viewLoader.getController();
                viewPageController.initModel(email);
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        stage.close();
                        ListView_rcvd.getSelectionModel().clearSelection();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSelectedEmail_sent(MouseEvent mouseEvent) {
        Email email = ListView_sent.getSelectionModel().getSelectedItem();
        selectedEmail = email;
        if(email != null) {
            Parent root;
            FXMLLoader viewLoader = new FXMLLoader(ClientMain.class.getResource("ViewmailPageSent.fxml"));
            try {
                root = viewLoader.load();
                ViewPageSentController viewPageController = viewLoader.getController();
                viewPageController.initModel(client, email);
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        stage.close();
                        ListView_rcvd.getSelectionModel().clearSelection();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSelectedEmail_rcvd(MouseEvent mouseEvent){
        Email email = ListView_rcvd.getSelectionModel().getSelectedItem();
        selectedEmail = email;
        if(email != null) {
            Parent root;
            FXMLLoader viewLoader = new FXMLLoader(ClientMain.class.getResource("ViewmailPage.fxml"));
            try {
                root = viewLoader.load();
                ViewPageController viewPageController = viewLoader.getController();
                viewPageController.initModel(client, email);
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent windowEvent) {
                        stage.close();
                        ListView_rcvd.getSelectionModel().clearSelection();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Initialize the model within the controller and start the automatic update for the emails.
     * @param client MODEL
     */
    public void initModel(MailClient client) {
        this.client = client;
        account_name_text.setText(client.getMailbox().getAccount_name());
        automaticUpdate();
    }

    /**
     *If the server is offline, a popup is sent. Otherwise, the SendPage is opened.
     * @throws IOException;
     */
    @FXML
    private void SendPageButton() throws IOException {
        if(client.isConnect()) {
            FXMLLoader sendloader = new FXMLLoader(ClientMain.class.getResource("SendmailPage.fxml"));
            Parent root = sendloader.load();
            SendPageController sendPageController = sendloader.getController();
            sendPageController.initModel(client);
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    /**
     *If the server is offline, a popup is sent. Otherwise, the updateMailbox() function is called.
     */
    @FXML
    private void updateButton() {
        if (client.isConnect()) {
            if (client.updateAction()) {
                alertMethod("Mailbox has been updated successfully");
                System.out.println(client.getMailbox().toString());
            } else {
                alertMethod("An error occurred updating the mailbox");
            }
        } else {
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    /**
     *If the server is offline, a popup is sent. Otherwise, the deleteMails() function is called.
     */
    @FXML
    private void deleteButton() {
        if(client.isConnect()) {
            if (client.deleteAction("delete_all","","")) {
                email_del_content.clear();
                alertMethod("Mails deleted have been completely erased");
            } else {
                alertMethod("An error occurred while deleting the emails");
            }
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    /**
     * It triggers a timer that updates the mailing lists every 2000ms
     */
    private void automaticUpdate() {
        Timer timer_update = new Timer();
        timer_update.schedule(new TimerTask() {
            @Override
            public void run() {
            Platform.runLater(() -> {
                if(client.updateAction()){
                    changeView();
                }
            });
            }
        }, 0, 2000);
    }

    /**
     * Refresh all the views, to check if there have been changing in the lists.
     */
    private void changeView(){
        int new_size;

            new_size = client.checkChangeMail('r');

            if(new_size > email_rcvd_content.size()){
                for(int i = email_rcvd_content.size(); i < new_size; i++){
                    email_rcvd_content.add(client.getMailbox().getAllMailRcvd().get(i));
                }
            }else if(new_size < email_rcvd_content.size() && new_size > 0) {
                email_rcvd_content.remove(selectedEmail);
            }else if(new_size == 0){
                email_rcvd_content.clear();
            }

            new_size = client.checkChangeMail('s');

            if(new_size > email_sent_content.size()){
                for(int i = email_sent_content.size(); i < new_size; i++){
                    email_sent_content.add(client.getMailbox().getAllMailSent().get(i));
                }
            }else if(new_size < email_sent_content.size() && new_size > 0){
                email_sent_content.remove(selectedEmail);
            }else if(new_size == 0){
                email_sent_content.clear();
            }

            new_size = client.checkChangeMail( 'd');

            if(new_size > email_del_content.size()){
                for(int i = email_del_content.size(); i < new_size; i++){
                    email_del_content.add(client.getMailbox().getAllMailDel().get(i));
                }
            }
        }
}
