package com.example.novamarket.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.example.novamarket.Class.JsonMaker;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Service_Chat extends Service implements Runnable {
    private static Socket socket = null;
    private BufferedReader br = null;
    private PrintWriter pw = null;
    private String message;
    private static String user_id;
    private static final String HOST = "10.0.2.2"; // 내 로컬 주소를 가르킨다.
    private static final int PORT = 8888; // 해당 accept 할 port 번호
    private static Context context;


    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        user_id = PreferenceManager.getString(getApplicationContext(), "user_id");
        context = getApplicationContext();

        try{
            // 처음 서비스가 실행됬을때 채팅 서버에 소켓 연결하는 쓰레드 실행
            Runnable start = new Service_Chat();
            Thread t = new Thread(start);
            t.start();
        }catch (Exception e){
            Logger.e("에러메세지 :" + e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_STICKY; // 서비스가 종료되었을떄도 다시 자동으로 실행함;
        } else {
            return super.onStartCommand(intent, flags, startId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 클라이언트 서비스가 죽었을때 socket 연결 끊기
        try {
            if (socket != null) {
                socket.close();
            }
            if (br != null) {
                br.close();
            }
            if (pw != null) {
                pw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket(HOST, PORT);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            // 처음 연결 됬을때 connect 처리하기
            String msg = JsonMaker.makeJson(new DTO_Chat("","",user_id,"","connect","","","","","","","",""));
            pw.println(msg);
            pw.flush();

            // 연결이 되면 서버에서 계속 문자를 받을 Thread 생성
            Receive r = new Receive(socket,context); // 메세지 받는 Thread
            r.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Socket getSocket() {
        return socket;
    }
}