package com.example.rpicargui2022;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import java.net.URI;
import java.net.URISyntaxException;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {

    private static MainActivity instance;
    private WebSocketClient webSocketClient;
    private boolean connected = false;
    private final Vehicle v = new Vehicle();
    private MessageHandler messageHandler = new MessageHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        //createWebSocketClient("10.0.2.2");
        connected = false;
        ImageView cross;

        {
            cross = (ImageView) findViewById(R.id.cross);
        }
        cross.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
                public boolean onTouch(View view, MotionEvent event) {
                final int actionPerformed = event.getAction();
                switch (actionPerformed) {
                    case MotionEvent.ACTION_DOWN:
                        //touch down (merged with ACTION_Move
                    case MotionEvent.ACTION_MOVE: {
                        //moving around
                        doMoveAction(event.getX(), event.getY());
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        //touched up
                        //setTextTerminal("ACTION UP");
                        doStopMove();
                        return true;                    }
                }
                return true;
            }
        });
    }

    public static MainActivity getInstance() {
        return instance;
    }
    public void sendCommand(String _command){
        webSocketClient.send(_command);
    }
    public void sendMessage(View view){

        EditText commandText = (EditText) findViewById(R.id.commandTxt);
        String message = commandText.getText().toString();
        if (!connected){
            createWebSocketClient(message);
            connected = true;
        }else
        webSocketClient.send(message);

    }
    public void doMoveAction(float _x, float _y) {
        String status = "";
        ImageView cross = (ImageView) findViewById(R.id.cross);
        float height = cross.getBottom() - cross.getTop();
        float width = cross.getRight() - cross.getLeft();
        float heightThird = height / 3;
        float widthThird = width / 3;

        //steering
        //left third = left = 1, right third = right = 2, center third = straight = 0
        if (_x < widthThird) {
            if (connected) {
                sendCommand(v.getSteerCommand (1));
            }
            status += "Left, ";
        } else if (_x > (widthThird * 2)) {
            if (connected) {
                sendCommand(v.getSteerCommand(2));
            }
            status += "Right, ";
        } else {
            if (connected) {
                sendCommand(v.getSteerCommand(0));
            }
            status += "Straight, ";
        }
        //moving
        //lower third = reward = speed < 0 , upper third = forward = speed > 0 , center third = no moving = speed 0

        int speed = 0;
        if (_y < heightThird) {
            if (_y < 1) {
                speed = 100;
            } else {
                speed = 100 - Math.round((_y / (heightThird) * 100));
                if (connected) {
                    sendCommand(v.getMoveCommand(speed));                }
            }
            status += "FWD speed: " + speed;
        } else if (_y > (heightThird * 2)) {
            if (_y > height) {
                speed = -100;
            } else {
                speed = Math.round((_y - 2 * height / 3) / (heightThird) * 100)*-1;
            }
            if (connected) {
                sendCommand(v.getMoveCommand(speed));            }
            status += "RWD speed: " + speed;
        } else {
            if (connected) {
                sendCommand(v.getMoveCommand(speed));
            }
            status += "Stop speed: " + speed;
        }
        setTextTerminal(status);
    }
    public void setTextTerminal(String _message){
        TextView textView = findViewById(R.id.Feedkack);
        textView.setText(_message);
    }
    public void setTexttxtSonarF(String _message){
        TextView textView = findViewById(R.id.txtSonarF);
        textView.setText(_message);
    }
    public void setTexttxtSonarL(String _message){
        TextView textView = findViewById(R.id.txtSonarL);
        textView.setText(_message);
    }
    public void setTexttxtSonarR(String _message){
        TextView textView = findViewById(R.id.txtSonarR);
        textView.setText(_message);
    }
    public void doStopMove() {
        String status="Straight, Stop speed: 0";
        if (connected) {
            sendCommand(v.getMoveCommand(0));
            sendCommand(v.getSteerCommand(0));
        }
        setTextTerminal(status);
    }
    private void createWebSocketClient(String _ip) {
        URI uri;
        try {
            // Connect to local host 10.0.2.2 (developing machine)
            uri = new URI("ws://"+_ip+":8080/events");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                webSocketClient.send("Android_Client");
            }

            @Override
            public void onTextReceived(String s) {
                Log.i("WebSocket", "Message received");
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TextView textView = findViewById(R.id.Feedkack);
                            textView.setText(messageHandler.processMessage(message));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBinaryReceived(byte[] data) {
            }

            @Override
            public void onPingReceived(byte[] data) {
            }

            @Override
            public void onPongReceived(byte[] data) {
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }
}
