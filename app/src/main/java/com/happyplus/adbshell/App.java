package com.happyplus.adbshell;

import android.util.Log;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class App extends NanoHTTPD {

    private static String TAG = "HAPPYPLUS";
    private static ADB adbService;

    public App() throws IOException {
        super(8999);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        Log.d(TAG, "\nRunning! Point your browsers to http://localhost:8999/ \n");
    }

    public static App main(ADB adb) {
        adbService = adb;
        try {
            return new App();
        } catch (IOException ioe) {
            Log.e(TAG, "Couldn't start server:\n" + ioe);
        }
        return null;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        if (method.equals(Method.POST)) {
            Map<String, String> files = new HashMap<String, String>();

            try {
                session.parseBody(files);
                Log.d(TAG, files.toString());
                String body = files.get("postData");

                String msg = "{\"success\":true}";
                if (body == null || body.isEmpty()) {
                    msg = "{\"success\":true,\"message\":\"参数错误\"}";
                } else {
                    // 执行CMD
                    msg = "{\"success\":true,\"message\":\"" + adbService.ExecCommand(body) + "\"}";
                }
                return newFixedLengthResponse(msg);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
