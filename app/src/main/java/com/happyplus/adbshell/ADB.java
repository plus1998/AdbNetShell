package com.happyplus.adbshell;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

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
            AdbStream stream = connection.open("shell:" + command);
            while (!stream.isClosed()) try {
                String res = new String(stream.read(), "US-ASCII");
                Log.e("HAPPYPLUS", res);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        } catch (Exception e) {
            String res = e.toString();
            Log.e("HAPPYPULS", "SHELL FAILED: " + e.toString());
            return  res;
        }
        return "成功";
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
