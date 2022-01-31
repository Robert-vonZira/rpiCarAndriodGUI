package com.example.rpicargui2022;

import java.util.ArrayList;


public class MessageHandler {


    private ArrayList<String> splitMessag(String _message)
    {

        String identifyer = _message.substring(0, _message.indexOf('.'));
        String command = _message.substring(_message.indexOf('.')+1, _message.indexOf(' '));
        String[] values = _message.substring(_message.indexOf(' ')+1,_message.length()).split(" ");

        ArrayList<String> message =new ArrayList<String>();
        message.add(identifyer);
        message.add(command);
        for (String s : values) {
            message.add(s);
        }
        return message;
    }
    public String processMessage(String _message)
    {
        if (_message.contains(".")&_message.contains(" "))
        {
            try {
                this.setAction(this.splitMessag(_message));
            } catch (Exception e) {
                System.err.println("\n MessageHanlder: could not process Message!");
                //System.err.println(e.getLocalizedMessage());
            }
        }
            return _message;

    }
    private void setAction(ArrayList<String> _message)
    {
        switch (_message.get(0)) {
            case "HC_SR04_SonicSensorF":

                if (_message.get(1).equals("distance"))
                {
                    MainActivity.getInstance().setTexttxtSonarF(_message.get(2));
                }
                break;
            case "HC_SR04_SonicSensorL":
                if (_message.get(1).equals("distance"))
                {
                    MainActivity.getInstance().setTexttxtSonarL(_message.get(2));
                }
                break;
            case "HC_SR04_SonicSensorR":
                if (_message.get(1).equals("distance"))
                {
                    MainActivity.getInstance().setTexttxtSonarR(_message.get(2));
                }
                break;

            default:
                break;
        }
    }
}
