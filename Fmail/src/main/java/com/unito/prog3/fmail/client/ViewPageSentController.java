package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.MailClient;
import com.unito.prog3.fmail.support.Support;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static com.unito.prog3.fmail.support.Support.alertMethod;

public class ViewPageSentController {
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

    public void initModel(MailClient client, Email email){
        this.client = client;
        this.email = email;
        this.from_text.setText(this.email.getFrom());
        this.email_text.setText(this.email.getText());
        this.to_text.setText(this.email.getTo());
        this.object_text.setText(this.email.getObject());
    }

    public void DeleteButtonSent(ActionEvent event) {
        if(client.isConnect()){
            if(client.deleteAction("delete_single", Integer.toString(email.getId()),"sent")){
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
