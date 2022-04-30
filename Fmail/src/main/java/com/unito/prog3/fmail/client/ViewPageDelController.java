package com.unito.prog3.fmail.client;

import com.unito.prog3.fmail.model.Email;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class ViewPageDelController {
    @FXML
    private TextArea email_text;
    @FXML
    private Text from_text;
    @FXML
    private Text to_text;
    @FXML
    private Text object_text;

    public void initModel(Email email){
        this.from_text.setText(email.getFrom());
        this.email_text.setText(email.getText());
        this.to_text.setText(email.getTo());
        this.object_text.setText(email.getObject());
    }
}
