package com.happyplus.adbshell;

import android.util.Base64;
import android.util.Log;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ADB {
    private static String TAG = "HAPPYPLUS";

    private Socket socket;
    private String HOST = "localhost";
    private int POST = 5555;
    private AdbConnection connection;
    private AdbCrypto crypto;

    private static final String PUBLIC_KEY_NAME = "public.key";
    private static final String PRIVATE_KEY_NAME = "private.key";

    public ADB(File dataDir) {
        // adb加密
        AdbBase64 adbBase64 = data -> Base64.encodeToString(data, 2);
        try {
            // 读取本地库
            File pubKey = new File(dataDir, PUBLIC_KEY_NAME);
            File privKey = new File(dataDir, PRIVATE_KEY_NAME);
            Log.d(TAG, "pubKey: " + pubKey + ", exist:" + pubKey.exists());
            Log.d(TAG, "privKey: " + privKey + ", exist:" + privKey.exists());
            if (pubKey.exists() && privKey.exists()) {
                Log.d(TAG, "ADB: 用本地密钥对");
                crypto = AdbCrypto.loadAdbKeyPair(adbBase64, privKey, pubKey);
            } else {
                Log.d(TAG, "ADB: 新建密钥对");
                crypto = AdbCrypto.generateAdbKeyPair(adbBase64);
                crypto.saveAdbKeyPair(privKey, pubKey);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public String ExecCommand(String command) {
        try {
            final AdbStream adbStream;
            try {
                adbStream = connection.open("shell:");
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
            // Start the receiving thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!adbStream.isClosed())
                        try {
                            // Print each thing we read from the shell stream
                            Log.d("ADB OUTPUT", new String(adbStream.read(), "US-ASCII"));
                        } catch (InterruptedException | IOException e) {
                            e.printStackTrace();
                            return;
                        }
                }
            }).start();
            try {
                adbStream.write(command + '\n');
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "ExecCommand Error " + e.getMessage());
                return e.getMessage();
            }
        } catch (Exception e) {
            String res = e.toString();
            Log.e("HAPPYPULS", "SHELL FAILED: " + e);
            return res;
        }
        return "ok";
    }

    public Boolean adbConnect(String host, int port) {
        try {
            HOST = host;
            POST = port;
            socket = new Socket(HOST, POST); // put phone IP address here
            connection = AdbConnection.create(socket, crypto);
            connection.connect();
            return true;
        } catch (IOException | InterruptedException e) {
            String res = e.toString();
            Log.e("HAPPYPLUS", res);
            return false;
        }
    }

    public void disconnectAdb() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
