package com.example.rpicargui2022;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.net.URI;
import java.net.URISyntaxException;

import tech.gusavila92.websocketclient.WebSocketClient;

public class MainActivity extends AppCompatActivity {

    private WebSocketClient webSocketClient;
    private boolean connected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
                final int actionPeformed = event.getAction();
                switch (actionPeformed) {
                    case MotionEvent.ACTION_DOWN:
                        //touch down (merged with ACTION_Move
                    case MotionEvent.ACTION_MOVE: {
                        //moving arround
                        doMoveAction(event.getX(), event.getY());
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        //touched up
                        doStopMove();
                        break;
                    }
                }
                return true;
            }
        });
    };

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

        if (_x < widthThird) {
            if (connected) {
                sendCommand("vehicle.steer 1");
            }
            status += "Left, ";
        } else if (_x > (widthThird * 2)) {
            if (connected) {
                sendCommand("vehicle.steer 2");
            }
            status += "Right, ";
        } else {
            if (connected) {
                sendCommand("vehicle.steer 0");
            }

            status += "Straight, ";
        }
        int speed = 0;

        if (_y < heightThird) {
            if (_y < 1) {
                speed = 100;

            } else {
                speed = 100 - Math.round((_y / (heightThird) * 100));
                if (connected) {
                    sendCommand("vehicle.move " + speed);
                }
            }
            status += "FWDspeed: " + speed;
        } else if (_y > (heightThird * 2)) {
            if (_y > height) {
                speed = 100;
                // status += "RWDspeed: " + speed;
            } else {
                speed = Math.round((_y - 2 * height / 3) / (heightThird) * 100);
            }
            status += "RWDspeed: " + speed;
            if (connected) {
                sendCommand("vehicle.move " + speed);
            }
        } else {
            if (connected) {
                sendCommand("vehicle.move 0");
            }
            status += "Stop: " + speed;
        }
        setTextTerminal(status);
    }
    public void setTextTerminal(String _message){
        TextView textView = findViewById(R.id.Feedkack);
        textView.setText(_message);

    }

    public void doStopMove() {
        if (connected) {
            String status="Stop: 0";
            sendCommand("vehicle.move 0");
            setTextTerminal(status);

        }
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
                            textView.setText(message);
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
