package com.unito.prog3.fmail.support;

import com.unito.prog3.fmail.model.Email;
import com.unito.prog3.fmail.model.Mailbox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *The Support class is a class that contains static variables and methods shared by the various class.
 */
public class Support {
    public final static String PATH_NAME_DIR = "C:\\Users\\Daniele\\Desktop\\DirProjProg3";
    public final static Integer port = 8189;

    public final static Mailbox daniele = new Mailbox("daniele@gmail.com");
    public final static Mailbox gabriele = new Mailbox("gabriele@gmail.com");
    public final static Mailbox danieleSer = new Mailbox("danieleSer@gmail.com");

    /**
     * Creates a new Alert pop-up.
     * @param alert_string string to show
     */
    public static void alertMethod(String alert_string){
        Alert alert = new Alert(Alert.AlertType.NONE,alert_string, ButtonType.OK);
        alert.show();
    }

    /**
     * It is called by the ConnectionController to check if the account written is legit.
     * @param account_name the account name syntax that it has to check.
     * @return true if the account respects the pattern; false otherwise.
     */
    public static boolean match_account(String account_name){
        Pattern p = Pattern.compile("[a-zA-Z0-9]++@[a-zA-Z]++.com");
        Matcher m = p.matcher(account_name);
        return m.matches();
    }

    public static class cellVisual extends ListCell<Email> {
        @Override
        protected void updateItem(Email item, boolean empty) {
            super.updateItem(item, empty);
            if(item != null) {
                setText(item.getFrom()+"  |  " + item.getObject() + "  |  " + item.getDate());
            }else{
                setText("");
            }
        }
    }


}
