package com.unito.prog3.fmail.model;

import com.unito.prog3.fmail.support.Support;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/*
 *The class MailServer represents our server to which the clients can create the connection.
 *It has the following variables:
 *  - EMAIL_ID_COUNT: the unique value that every email has.
 *  - mailboxes: the list of mailbox that the server have registered to itself.
 *  - logs: the list property on which we do the binding for the log show file.
 *  - logs_content: the observable list that we fill with the log action.
 *  - NUM_CLIENT: the value that indicates how mani clients are connected actually to the server.
 */
public class MailServer{
    private static AtomicInteger EMAIL_ID_COUNT;
    private final List<Mailbox> mailboxes;
    private final ListProperty<String> logs;
    private final ObservableList<String> logs_content;
    /**
     *Constructor of the class;
     **/
    public MailServer() {
        this.mailboxes = new ArrayList<>();
        EMAIL_ID_COUNT = new AtomicInteger();

        this.logs_content = FXCollections.observableList(new LinkedList<>());
        this.logs = new SimpleListProperty<>();
        this.logs.set(logs_content);

    }

    /**
     * It's called when the server is launched; it creates the directories for the mailboxes
     * of all the mail clients that are in the MailBoxes list.
     * @throws IOException;
     */
    public void create_dirs() throws IOException {
        for (int i = 0; i < this.getMailboxes().size(); i++) {
            String dir_name = this.getNameByIndex(i);

            File f = new File(Support.PATH_NAME_DIR + "\\" + dir_name + "\\" + "deleted");
            f.mkdirs();

            f = new File(Support.PATH_NAME_DIR + "\\" + dir_name + "\\" + "received");
            f.mkdirs();

            f = new File(Support.PATH_NAME_DIR + "\\" + dir_name + "\\" + "sent");
            f.mkdirs();
        }

        File f = new File(Support.PATH_NAME_DIR + "\\id_count.txt");
        if(f.createNewFile()){
            String s = "0";
            BufferedWriter buffer = new BufferedWriter(new FileWriter(f));
            buffer.write(s);
            buffer.flush();
            buffer.close();
        }
    }

    /**
     * The function extracts the sender and the recipient information
     * and save the email in the right directory of each one. It also updates the value of the EMAIL_ID_COUNT.
     * @param email email to save
     * @param single_recipient recipient of the email (only for the email sent to only one person).
     * @throws IOException;
     * @return true if the call to the method ended correctly; false otherwise.
     */
    public boolean saveEmail(Email email, String single_recipient) throws IOException {
        boolean saved = false;
        email.setId(EMAIL_ID_COUNT.getAndIncrement());

        File id = new File(Support.PATH_NAME_DIR+"\\id_count.txt");
        FileOutputStream fos = new FileOutputStream(id,false);
        fos.write(EMAIL_ID_COUNT.toString().getBytes());
        fos.close();

        String path_sender = Support.PATH_NAME_DIR + "\\" + email.getFrom() +"\\sent";
        String path_recipient = Support.PATH_NAME_DIR + "\\" + single_recipient + "\\received";

        try{
            File rcvd = new File(path_recipient + "\\" +  email.getId() + ".txt");
            File sent = new File(path_sender + "\\" +  email.getId() + ".txt");

            if(rcvd.createNewFile() && sent.createNewFile()){
                SimpleDateFormat DateFor = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String content =  email.getId()+"\n"+email.getFrom()+"\n"+email.getTo()+"\n"+email.getObject()+"\n"+email.getText().replaceAll("\n","@#%")+"\n"+ DateFor.format(email.getDate());

                BufferedWriter buffer = new BufferedWriter(new FileWriter(rcvd));
                buffer.write(content);
                buffer.flush();
                buffer.close();

                buffer = new BufferedWriter(new FileWriter(sent));
                buffer.write(content);
                buffer.flush();
                buffer.close();

                this.mailboxes.get(getIndexByName(email.getFrom())).setMailSent(email);
                this.mailboxes.get(getIndexByName(single_recipient)).setMailRcvd(email);
                saved = true;
            }
        } catch (IOException e) {e.printStackTrace();}
        return saved;
    }

    /**
     * It scrolls through all the folders of each account saved locally and for each account
     * and for each folder of the emails received, sent and deleted, loads the emails in the mailbox corresponding
     * to each account. Forward also loads the current value of the id_count.
     * @throws IOException;
     * @throws ParseException;
     */
    public void loadEmailFromLocal() throws IOException, ParseException {
        File file = new File(Support.PATH_NAME_DIR);
        for (File main_dir : Objects.requireNonNull(file.listFiles())){
            if(main_dir.getName().equals("id_count.txt")){
                BufferedReader reader = new BufferedReader(new FileReader(main_dir.getAbsolutePath()));
                String id_value = reader.readLine();
                EMAIL_ID_COUNT.set(Integer.parseInt(id_value));
                reader.close();
                break;
            }
            for(File list : Objects.requireNonNull(main_dir.listFiles())){
                switch (list.getName()){
                case "deleted":
                    for (File email: Objects.requireNonNull(list.listFiles())){
                        if(!email.isDirectory()){
                            BufferedReader reader = new BufferedReader(new FileReader(email.getAbsolutePath()));
                            String line = reader.readLine();
                            ArrayList<String> email_string_array = new ArrayList<>();
                            while(line != null){
                                email_string_array.add(line);
                                line = reader.readLine();
                            }
                            Email email_to_load = new Email(Integer.parseInt(email_string_array.get(0)), email_string_array.get(1), email_string_array.get(2), email_string_array.get(3), email_string_array.get(4).replaceAll("@#%","\n"), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(email_string_array.get(5)));
                            this.mailboxes.get(this.getIndexByName(main_dir.getName())).setMail_del(email_to_load);
                            reader.close();
                        }
                    }
                    break;
                case "received":
                    for (File email: Objects.requireNonNull(list.listFiles())){
                        if(!email.isDirectory()){
                            BufferedReader reader = new BufferedReader(new FileReader(email.getAbsolutePath()));
                            String line = reader.readLine();
                            ArrayList<String> email_string_array = new ArrayList<>();
                            while(line != null){
                                email_string_array.add(line);
                                line = reader.readLine();
                            }
                            Email email_to_load = new Email(Integer.parseInt(email_string_array.get(0)), email_string_array.get(1), email_string_array.get(2), email_string_array.get(3), email_string_array.get(4).replaceAll("@#%","\n"), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(email_string_array.get(5)));
                            this.mailboxes.get(this.getIndexByName(main_dir.getName())).setMailRcvd(email_to_load);
                            reader.close();
                        }
                    }
                    break;
                case "sent":
                    for (File email: Objects.requireNonNull(list.listFiles())){
                        if(!email.isDirectory()){
                            BufferedReader reader = new BufferedReader(new FileReader(email.getAbsolutePath()));
                            String line = reader.readLine();
                            ArrayList<String> email_string_array = new ArrayList<>();
                            while(line != null) {
                                email_string_array.add(line);
                                line = reader.readLine();
                            }
                            Email email_to_load = new Email(Integer.parseInt(email_string_array.get(0)), email_string_array.get(1), email_string_array.get(2), email_string_array.get(3), email_string_array.get(4).replaceAll("@#%", "\n"), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(email_string_array.get(5)));
                            this.mailboxes.get(this.getIndexByName(main_dir.getName())).setMailSent(email_to_load);
                            reader.close();
                        }
                    }
                    break;
                }
            }
        }
        System.out.println("All the mailboxes were loaded successfully from local directory");
    }

    /**
     * It's called when to delete all the email in the deleted mails list of the account.
     * @param account_name the account name of the mail client
     */
    public void clearDelEmail(String account_name){
        String path = Support.PATH_NAME_DIR + "\\" + account_name +"\\deleted";
        File file = new File(path);
        for(File email : Objects.requireNonNull(file.listFiles())){
            email.delete();
        }
    }

    /**
     * Moves an Email from rcvd to deleted list.
     * @param account_name the account name of the mailbox
     * @param id the id of the email handled
     */
    public void deleteEmailRcvd(String account_name, int id) throws IOException {
        String path_rcvd = Support.PATH_NAME_DIR + "\\" + account_name +"\\received\\" + id + ".txt";
        String path_del = Support.PATH_NAME_DIR + "\\" + account_name +"\\deleted\\" + id + ".txt";
        Path rcvd = Paths.get(path_rcvd);
        Path del = Paths.get(path_del);
        Files.move(rcvd,del);
    }

    /**
     * Moves an Email from sent to deleted list.
     * @param account_name the account name of the mailbox
     * @param id the id of the email handled
     */
    public void deleteEmailSent(String account_name, int id) throws IOException {
        String path_sent = Support.PATH_NAME_DIR +"\\"+ account_name +"\\sent\\" + id + ".txt";
        String path_del = Support.PATH_NAME_DIR + "\\" + account_name +"\\deleted\\" + id + ".txt";
        Path sent = Paths.get(path_sent);
        Path del = Paths.get(path_del);
        Files.move(sent, del);
    }

    /**
     * It is called to find the account name in a certain position.
     * @param i the position of the account name in the mailboxes list.
     * @return the account if found in the list of the mailboxes list.
     */
    private String getNameByIndex(Integer i){
        return mailboxes.get(i).getAccount_name();
    }

    /**
     * It is called to find the position a certain account name.
     * @param account the account name to look for
     * @return the position of the account name in the mailboxes list if found; -1 otherwise
     */
    public int getIndexByName(String account){
        for (int i = 0; i < mailboxes.size(); i++){
            if(account.equals(mailboxes.get(i).getAccount_name())){
                return i;
            }
        }
        return -1;
    }

    /**
     * It is called to check if a certain account exist.
     * @param account_name the account name to which check the existence
     * @return true if the account exist in the mailboxes list; false otherwise.
     */
    public Boolean existAccount(String account_name){
        boolean exist = false;
        for (int i = 0; i < mailboxes.size() && !exist; i++) {
            if (mailboxes.get(i).getAccount_name().equals(account_name)) {
                exist = true;
            }
        }
        return exist;
    }

    public void addMailBox(Mailbox mailbox){this.mailboxes.add(mailbox);}

    public void addLog(String log){this.logs_content.add(log);}

    public List<Mailbox> getMailboxes() {return mailboxes;}

    public ListProperty<String> logsProperty(){return logs;}

    @Override
    public String toString() {
        return "MailServer{`\n" +
                "   mailboxes= " + mailboxes +
                '}';
    }
}
