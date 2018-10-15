package com.bachecubano.nautabackgroundlibrary;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.bachecubano.nautabackgroundlibrary.util.Utils;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Quota;
import javax.mail.Session;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;

public class RetrieveImap {

    private final String username;
    private final String password;

    private final Context mContext;

    private final String protocol = "imap";
    private final String host = "imap.nauta.cu";
    private final String port = "143";

    public RetrieveImap(Context context, String username, String password) {
        this.mContext = context;
        this.username = username;
        this.password = password;
    }

    /**
     * Delete everyBody Here
     *
     * @return
     */
    public Boolean clearInbox() {

        Properties properties = getServerProperties(protocol, host, port);
        Session session = Session.getInstance(properties);

        int amount = 0;

        try {

            IMAPStore store = (IMAPStore) session.getStore(protocol);
            store.connect(username, password);

            if (store.isConnected())
                Log.d(Constants.TAG, "Successfully connected to IMAP");
            else
                Log.d(Constants.TAG, "Not connected to IMAP");

            if (!store.hasCapability("IDLE"))
                Log.d(Constants.TAG, "IDLE not supported");

            //Do a foreach folder
            IMAPFolder[] folders = (IMAPFolder[]) store.getDefaultFolder().list();
            for (IMAPFolder imapFolder : folders) {
                Log.d(Constants.TAG, "Cleaning " + imapFolder.getName());
                amount += clearFolder((IMAPFolder) store.getFolder(imapFolder.getName()));
                Log.d(Constants.TAG, "Cleared " + amount + " mails");
            }

            //Close connection
            store.close();

        } catch (NoSuchProviderException ex) {
            Log.d(Constants.TAG, "No provider for protocol: " + protocol);
            ex.printStackTrace();
        } catch (MessagingException ex) {
            Log.d(Constants.TAG, "Could not connect to the message store");
            ex.printStackTrace();
        }

        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("You didn't set a Nauta username");
        }
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("You didn't set a Nauta password");
        }
        if (!Utils.isNetworkAvailable(mContext)) {
            String TAG = "BackgroundMail";
            Log.d(TAG, "Necesitas una conexion a internet para poder recibir el correo");
        }

        return amount > 0;
    }

    public String[] retrieveData() {

        String[] data = new String[4];
        data[0] = "102400";
        data[1] = "0";
        data[2] = "15000";
        data[3] = "0";

        Properties properties = getServerProperties(protocol, host, port);
        Session session = Session.getInstance(properties);

        try {

            IMAPStore store = (IMAPStore) session.getStore(protocol);
            store.connect(username, password);

            if (store.isConnected())
                Log.d(Constants.TAG, "Successfully connected to IMAP");
            else
                Log.d(Constants.TAG, "Not connected to IMAP");

            if (!store.hasCapability("IDLE"))
                Log.d(Constants.TAG, "IDLE not supported");

            IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
            if (inbox.exists()) {
                inbox.open(IMAPFolder.READ_WRITE);

                int count = inbox.getMessageCount();
                Log.d(Constants.TAG, "Count>" + count);

                Quota[] quotas = store.getQuota("INBOX");
                for (Quota quota : quotas) {
                    Log.d(Constants.TAG, String.format("quotaRoot:'%s'", quota.quotaRoot));
                    for (Quota.Resource resource : quota.resources) {
                        if (resource.name.equals("STORAGE"))
                            data[0] = String.valueOf(resource.limit);
                        if (resource.name.equals("STORAGE"))
                            data[1] = String.valueOf(resource.usage);
                        if (resource.name.equals("MESSAGE"))
                            data[2] = String.valueOf(resource.limit);
                        if (resource.name.equals("MESSAGE"))
                            data[3] = String.valueOf(resource.usage);
                        Log.d(Constants.TAG, String.format("name:'%s', limit:'%s', usage:'%s'", resource.name, resource.limit, resource.usage));
                    }
                }
            }

            //Close conection
            store.close();

        } catch (NoSuchProviderException ex) {
            Log.d(Constants.TAG, "No provider for protocol: " + protocol);
            ex.printStackTrace();
        } catch (MessagingException ex) {
            Log.d(Constants.TAG, "Could not connect to the message store");
            ex.printStackTrace();
        }

        if (TextUtils.isEmpty(username)) {
            throw new IllegalArgumentException("You didn't set a Nauta username");
        }
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("You didn't set a Nauta password");
        }
        if (!Utils.isNetworkAvailable(mContext)) {
            String TAG = "BackgroundMail";
            Log.d(TAG, "Necesitas una conexion a internet para poder recibir el correo");
        }

        return data;
    }

    /**
     * Conecction Properties
     *
     * @param protocol
     * @param host
     * @param port
     * @return
     */
    private Properties getServerProperties(String protocol, String host, String port) {
        Properties properties = new Properties();
        properties.put(String.format("mail.%s.host", protocol), host);
        properties.put(String.format("mail.%s.port", protocol), port);
        properties.put(String.format("mail.%s.connectionpooltimeout", protocol), 30);
        properties.put(String.format("mail.%s.starttls.enable", protocol), false);
        return properties;
    }

    /**
     * Clear specified Folder
     *
     * @param folder
     * @return
     * @throws MessagingException
     */
    private int clearFolder(IMAPFolder folder) throws MessagingException {

        int deleted = 0;

        if (folder.exists()) {
            folder.open(IMAPFolder.READ_WRITE);
            Message[] arrayMessages = folder.getMessages();
            for (Message message : arrayMessages) {
                message.setFlag(Flags.Flag.DELETED, true);
                deleted++;
            }
            folder.close(true);
        }

        return deleted;
    }
}
