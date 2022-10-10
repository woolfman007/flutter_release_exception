package com.example.release_exception;

import android.util.Log;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {

    private static final String CHANNEL = "com.example.release_exception/test";
    private MethodChannel.MethodCallHandler _handler;
    private final ExecutorService _pool = Executors.newFixedThreadPool(1);
    private XMPPTCPConnection _connection;
    private Future _backgroundTaskFuture;


    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(
                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                        if (call.method.equals("connect")) {
                            connect();
                            result.success(null);
                        }

                        if (call.method.equals("disconnect")) {
                            disconnect();
                            result.success(null);
                        }
                    }
                });
    }

    private void connect() {
        waitForFuture();

        if(_connection == null) {
            try {
                Log.e("EXCEPTION_TEST", "trying to create connection...");
                _connection = new XMPPTCPConnection("sbtest1", "Abc123", "jabber.de");
                Log.e("EXCEPTION_TEST", "finished");
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
        }

        _backgroundTaskFuture = _pool.submit(new LoginTask(_connection, true));
    }

    private void disconnect() {
        waitForFuture();
        _backgroundTaskFuture = _pool.submit(new LoginTask(_connection, false));
    }

    private void waitForFuture() {
        if(_backgroundTaskFuture == null)
            return;

        try {
            _backgroundTaskFuture.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        _backgroundTaskFuture = null;
    }
}


class LoginTask implements Runnable {
    private final XMPPTCPConnection _connection;
    private final Boolean _login;

    LoginTask(XMPPTCPConnection connection, Boolean login) {
        _connection = connection;
        _login = login;
    }

    public void run() { // run the service
        try {

            if(_login) {
                Log.e("EXCEPTION_TEST", "trying to log in...");
                _connection.connect().login();
                Log.e("EXCEPTION_TEST", "finished.");
            }
            else {
                Log.e("EXCEPTION_TEST", "trying to log out...");
                _connection.disconnect();
                Log.e("EXCEPTION_TEST", "finished.");
            }

        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

