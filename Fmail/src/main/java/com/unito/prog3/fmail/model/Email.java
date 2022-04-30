package com.unito.prog3.fmail.model;

import java.io.Serializable;
import java.util.Date;

/*
 * The class email represents the prototype of the email that will be sent and received.
 * It has the following instance variables:
 *  - id: the unique id for the email.
 *  - from: the sender of the email.
 *  - to: the recipient of the email
 *  - object: the object of the email.
 *  - text: the message contained in the email.
 *  - date: the date when the email was sent.
 */
public class Email implements Serializable {
    private int id;
    private String from;
    private String to;
    private String object;
    private String text;
    private Date date;

    /**
     *   This constructor is used by Client because it doesn't know the current id counter.
     */
    public Email(String from, String to, String object, String text) {
        this.from = from;
        this.to = to;
        this.object = object;
        this.text = text;
        this.date = new Date();
    }

    /**
     *  This constructor is used by Server because it knows the current id counter.
     */
    public Email(int id, String from, String to, String object, String text, Date date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.object = object;
        this.text = text;
        this.date = date;
    }

    /**
     * It is used when the user click on the reply_all button.
     * It deletes its name from the recipients list.
     * @param account_to_remove the account to remove from the list.
     * @return the string with the new recipients list.
     */
    public String get_to_except(String account_to_remove){
        String string_return = to.replace(account_to_remove +" ","");
        if(string_return.contains(account_to_remove)) {
            string_return = to.replace(account_to_remove, "");
        }
        return  string_return;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {this.id = id;}

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getObject() {
        return object;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public void setTo(String to) {this.to = to;}

    @Override
    public String toString() {
        return "\n              Email{" +
                "id=" + id +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", object='" + object + '\'' +
                ", text='" + text + '\'' +
                ", date=" + date +
                '}';
    }
}
