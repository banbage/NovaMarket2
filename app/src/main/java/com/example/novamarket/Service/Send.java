package com.example.novamarket.Service;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Send extends Thread {
    private Socket sc = null;
    private String message = null;
    private PrintWriter pw = null;

    // Service 에서 socket 을 받아서 문자를 전송하면됨
    public Send(Socket socket, String message) {
        try {
            this.sc = socket;
            this.pw = new PrintWriter(new OutputStreamWriter(sc.getOutputStream(), StandardCharsets.UTF_8));
            this.message = message;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        /* 받은 intent 데이터를 서버로 전송    */
        if (message != null) {
            pw.println(message);
            pw.flush();
            Logger.json("서버로 보내는 메세지 : " + message);
        }
    }
}
