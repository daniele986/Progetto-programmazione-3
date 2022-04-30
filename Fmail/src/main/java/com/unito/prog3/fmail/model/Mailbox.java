package com.unito.prog3.fmail.model;

import com.unito.prog3.fmail.support.Support;
import java.io.Serializable;
import java.util.LinkedList;

/*
 * The class Mailbox represents the actual unique mailbox that every mail account has.
 * It contains the following variables:
 *  - mail_rcvd: it is the list that contains the received mails.
 *  - mail_sent: it is the list that contains the sent mails.
 *  - mail_del: it is the list that contains the deleted mails.
 *  - account_name: this string indicates the owner account of the mailbox.
 */
public class Mailbox implements Serializable{
    private String account_name;
    private final LinkedList<Email> mail_rcvd;
    private final LinkedList<Email> mail_sent;
    private final LinkedList<Email> mail_del;
    private boolean connected;

    /**
     * Constructor of the class; takes the account name of the mailbox.
     * It calls the method match_account() int the Support class,
     * that check if the account name passed follows the right pattern.
     * @param account_name of the mailbox
     */
    public Mailbox(String account_name) {
        if(Support.match_account(account_name)){
            this.account_name = account_name;
        }else{
            System.out.println("The account name does not follow the right pattern. Example: example@gmail.com");
        }
        mail_rcvd = new LinkedList<>();
        mail_sent = new LinkedList<>();
        mail_del = new LinkedList<>();
        connected = false;
    }

    /**
     * Constructor of the class; only instantiate the list of the mailbox.
     * It is used for the call of the MailClient() constructor in HomeController.java
     */
    public Mailbox(){
        mail_rcvd = new LinkedList<>();
        mail_sent = new LinkedList<>();
        mail_del = new LinkedList<>();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * It is called when client send a request of definitive deleting.
     */
    public void clearMailDel(){this.mail_del.clear();}

    /**
     * Takes the id of the email to delete
     * and first adds it to the mail_del list of the mailbox
     * then removes it from the mail_rcvd list.
     * @param id of the email;
     */
    public synchronized void delete_email_rcvd(int id){
        this.mail_del.add(this.mail_rcvd.get(getIndexByID_rcvd(id)));
        this.mail_rcvd.remove(getIndexByID_rcvd(id));
    }

    /**
     * Takes the id of the email to delete
     * and first adds it to the mail_del list of the mailbox
     * then removes it from the mail_sent list
     * @param id of the email.
     */
    public synchronized void delete_email_sent(int id){
        this.mail_del.add(this.mail_sent.get(getIndexByID_sent(id)));
        this.mail_sent.remove(getIndexByID_sent(id));
    }

    /**
     * Takes the ID of the email to delete and check on the mail list (rcvd in this case)
     * if there is such element. If it is, it returns the element position in the list.
     * @param ID of the email
     * @return i position of the email in the list
     */
    public int getIndexByID_rcvd(int ID){
        for(int i = 0; i < mail_rcvd.size(); i++){
            if(mail_rcvd.get(i).getId() == ID){
                return i;
            }
        }
        return -1;
    }

    /**
     * Takes the ID of the email to delete and check on the mail list (sent in this case)
     * if there is such element. If it is, it returns the element position in the list.
     * @param ID of the email
     * @return i position of the email in the list
     */
    public int getIndexByID_sent(int ID){
        for(int i = 0; i < mail_sent.size(); i++){
            if(mail_sent.get(i).getId() == ID){
                return i;
            }
        }
        return -1;
    }

    /**
     * Setter of MailBox
     */
    public void setMailRcvd(Email mail_rcvd) {this.mail_rcvd.add(mail_rcvd);}

    public void setMailSent(Email mail_sent) {this.mail_sent.add(mail_sent);}

    public void setMail_del(Email mail_del) {this.mail_del.add(mail_del);}

    /**
     * Getter of MailBox
     */
    public LinkedList<Email> getAllMailRcvd(){return this.mail_rcvd;}

    public LinkedList<Email> getAllMailSent(){return this.mail_sent;}

    public LinkedList<Email> getAllMailDel(){return this.mail_del;}

    public String getAccount_name() {return account_name;}

    @Override
    public String toString() {
        return  "\n       account_name: " + account_name +
                "\n         mail_rcvd = " + mail_rcvd +
                "\n         mail_sent = " + mail_sent +
                "\n         mail_del = " + mail_del +
                '}';
    }

}
