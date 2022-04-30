package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.MailClient;
import com.unito.prog3.fmail.support.Support;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.unito.prog3.fmail.support.Support.alertMethod;

public class SendPageController implements Initializable {
    private MailClient client;

    @FXML
    private TextArea area_sendpage;
    @FXML
    private TextField recipient_sendpage;
    @FXML
    private TextField object_sendpage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    /**
     * The default initModel that is called for when it is clicked the SendPageButton.
     * @param client MODEL
     */
    public void initModel(MailClient client) {
        this.client = client;
    }

    public void initModel_Email_replyall(MailClient client, Email email){
        this.client = client;
        area_sendpage.setText("\nOriginal message:\n" + email.getText());
        object_sendpage.setText("RE:"+email.getObject());
        recipient_sendpage.setText(email.getFrom()+ " " + email.get_to_except(client.getMailbox().getAccount_name()));
        recipient_sendpage.setEditable(false);
    }

    public void initModel_Email_forward(MailClient client, Email email){
        this.client = client;
        area_sendpage.setText(email.getText());
        object_sendpage.setText("FW:"+email.getObject());
        area_sendpage.setEditable(false);
    }

    public void initModel_Email(MailClient client, Email email){
        this.client = client;
        area_sendpage.setText("\nOriginal message:\n" + email.getText());
        object_sendpage.setText("RE:"+email.getObject());
        recipient_sendpage.setText(email.getFrom());
        recipient_sendpage.setEditable(false);
    }

    /**
     *If the server is offline, a popup is sent. Otherwise the inserted fields are analyzed, it is checked if there are more than one mail and the sendEmail () function is called.
     */
    public void SendmailButton(ActionEvent event) {
        if(client.isConnect()) {
            String recipient = recipient_sendpage.getText();
            String text = area_sendpage.getText();
            String object = object_sendpage.getText();

            //Split recipient if there are more than one
            String[] recipients = recipient.split(" ");
            boolean recipients_corrects = true;
            for (String s : recipients) {
                System.out.println(client.getMailbox().getAccount_name());
                if (!Support.match_account(s) || Objects.equals(client.getMailbox().getAccount_name(), s)) {
                    recipients_corrects = false;
                }
            }
            //Sends the email to all recipients
            if (recipients_corrects){
                ArrayList<String> fails = client.sendEmail(new Email(client.getMailbox().getAccount_name(), recipient, object, text));
                if(!fails.isEmpty()){
                    StringBuilder recipients_failed_string = new StringBuilder();
                    for (String s : fails) {
                        recipients_failed_string.append(s).append("\n");
                    }
                    alertMethod("Mail to the following recipients: " + recipients_failed_string + " have not been sent");
                    recipient_sendpage.clear();
                }else{
                    alertMethod("Mail sent successfully to all the recipients");
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                }
            }else {
                alertMethod("Check the mail account inserted");
            }
        }else{
            alertMethod("The server is momentarily offline, please try again in a while");
        }
    }
}
