package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.ClientMain;
import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.MailClient;
import com.unito.prog3.fmail.support.Support;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;

import static com.unito.prog3.fmail.support.Support.alertMethod;

public class ViewPageController {
    private MailClient client;
    private Email email;

    @FXML
    private TextArea email_text;
    @FXML
    private Text from_text;
    @FXML
    private Text to_text;
    @FXML
    private Text object_text;

    public void initModel(MailClient client, Email email) {
        if(email != null) {
            this.email = email;
            this.client = client;
            this.from_text.setText(this.email.getFrom());
            this.email_text.setText(this.email.getText());
            this.object_text.setText(this.email.getObject());
            this.to_text.setText(this.email.getTo());
        }
    }

    @FXML
    public void ReplyButton(ActionEvent event) throws IOException {
        if(client.isConnect()) {
            FXMLLoader sendloader = new FXMLLoader(ClientMain.class.getResource("SendmailPage.fxml"));
            Parent root = sendloader.load();
            SendPageController sendPageController = sendloader.getController();
            sendPageController.initModel_Email(client, email);
            Stage window = (Stage) email_text.getScene().getWindow();
            window.setScene(new Scene(root));
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    @FXML
    public void ReplyAllButton(ActionEvent event) throws IOException {
        if(client.isConnect()) {
            FXMLLoader sendloader = new FXMLLoader(ClientMain.class.getResource("SendmailPage.fxml"));
            Parent root = sendloader.load();
            SendPageController sendPageController = sendloader.getController();
            sendPageController.initModel_Email_replyall(client, email);
            Stage window = (Stage) email_text.getScene().getWindow();
            window.setScene(new Scene(root));
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    @FXML
    public void ForwardButton(ActionEvent event) throws IOException {
        if(client.isConnect()) {
            FXMLLoader sendloader = new FXMLLoader(ClientMain.class.getResource("SendmailPage.fxml"));
            Parent root = sendloader.load();
            SendPageController sendPageController = sendloader.getController();
            sendPageController.initModel_Email_forward(client, email);
            Stage window = (Stage) email_text.getScene().getWindow();
            window.setScene(new Scene(root));
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

    @FXML
    public void DeleteButtonRcvd(ActionEvent event) {
        if(client.isConnect()){
            if(client.deleteAction("delete_single", Integer.toString(email.getId()),"rcvd")){
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
                Support.alertMethod("Email moved to deleted mails");
            }else{
                Support.alertMethod("An error occurred, try later.");
            }
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }

}
