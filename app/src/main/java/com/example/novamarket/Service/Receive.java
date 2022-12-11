package com.example.novamarket.Service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.novamarket.Activity.Home_Chat;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Receive extends Thread implements Runnable {

    private BufferedReader br = null;
    private PrintWriter pw = null;
    private Socket sc = null;
    private String request = null;
    private String user_id;
    private Context context;
    private int temp = 0;
    private NotificationCompat.Builder builder = null;

    public Receive(Socket socket, Context context) {
        this.sc = socket;
        this.context = context;
        try {
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            String NOTIFICATION_CHANNEL_ID = "NovaMarket";

            /* TODO : 노티 채널  */
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "channel_test1", NotificationManager.IMPORTANCE_HIGH);

                /*  채널 설정 */
                channel.setDescription("Channel description");
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.setVibrationPattern(new long[]{0, 1000});
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
            // 채팅 받은거 어떻게 처리할지 정리
            while (true) {
                request = br.readLine();
                Logger.d(request);
                DTO_Chat chat = makeDTO(request);
                String user_id = chat.getUser_id(); // 작성자 값
                String name = chat.getMsg_name(); // 작성자 이름(채팅방 이름)
                String image = chat.getMsg_profile(); // 프로필 이미지
                String message = chat.getMsg(); // 채팅 내용
                String room_id = chat.getRoom_id(); // 채팅방 이름 (고유값)
                String home_id = chat.getHome_id();
                String code = chat.getMsg_code(); //  채팅방 코드 어떤상태인지
                String date = chat.getMsg_date(); // 채팅 작성일자
                String type = chat.getMsg_type(); // 메세지인지, 이미지인지, 출입 메세지인지 구분
                int noti_id = Integer.parseInt(room_id); // 숫자로 변환 한 채팅방 고유값

                /* 어디를 보고있는지 확인 */
                String actName = getNowUseActivity(context);

                /* 현재 위치별 노티 설정 */
                switch (actName) {
                    case "com.example.novamarket.Activity.Home_Chat": // 채팅룸
                        Logger.d("채팅방 일때 메세지 전송");
                        Intent intent = new Intent("Act_Chat");
                        intent.putExtra("msg", request);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        break;
                    case "com.example.novamarket.Activity.Home_Chat_list":
                        Logger.d("매물 목록일때 메세지 전송");
                        Intent intent2 = new Intent("Act_List");
                        intent2.putExtra("msg", request);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                    default:
                        Logger.d("채팅방 밖일 경우 알림 전송");
                        Intent intent1 = new Intent("Act_Home");
                        intent1.putExtra("msg", request);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                }

                /* 알림 */
                /* 알림 전체 끄고 켜기 */
                if (PreferenceManager.getBoolean(context, "alarm")) {
                    Logger.d("전체 알림 끄기");
                    continue;
                }
                /* 일부 방만 끄고 켜기 */
                if (PreferenceManager.getBoolean(context, "alarm" + room_id)) {
                    Logger.d(room_id + "방 알림 끄기");
                    continue;
                }
                if (code.equals("in")) {
                    Logger.d("채팅방 입장 알림 끄기");
                    continue;
                }
                if (code.equals("out")) {
                    Logger.d("채팅방 나가기 알림 끄기");
                    continue;
                }
                if (code.equals("quit")){
                    Logger.d("채팅방 나가기 알림");
                    continue;
                }

                /* 채팅방에서 send, join, quit 알림 안받음 */
                if (actName.equals("com.example.novamarket.Activity.Home_Chat") && code.equals("send") && room_id.equals(PreferenceManager.getString(context, "nowRoom"))) {
                    continue;
                }

                /* 노티피케이션 기본 상단 알림 구현 */
                NotificationCompat.Builder builder = null;
                /* 안드로이드 오레오 이상부터는 Channel 을 꼭 생성해야댐 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

                    /* 알림 클릭시 동작 추가하기 */
                    Intent intent1 = new Intent(context, Home_Chat.class);
                    intent1.putExtra("home_id", home_id);
                    intent1.putExtra("read_id", user_id);
                    intent1.putExtra("room_id", room_id);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

                    /* builder 에 알림 내용과 아이콘을 설정 */
                    builder.setContentTitle(name) // 제목텍스트 *생략가능
                            .setContentText(message) // 본문 텍스트 *생략가능
                            .setSmallIcon(R.drawable.sparkler) // 알림시 보여지는 아이콘, 필수
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
//                            .setTimeoutAfter(5000) // 지정한 시간 이후 알림이 취소된다.
                            .setPriority(NotificationCompat.PRIORITY_HIGH) // 헤드업 알림을 위한 중요도 설정 High 이상으로 해야됨 7.1 이상버전에선 Max까지
                            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE);

                    /* 기존에 방과 같은 방인지 다른 방인지지 */
                    if (noti_id == temp) {
                                            /*
                                            같은 방이면 그대로
                                            builder 의 build() 를 통해 Notification 객체를 생성하고,
                                               알림을 표시 하기 위한 NotificationManagerCompat.notify() 를
                                               호출하여 알림의 고유 ID와 함께 전달 */
                        Notification notification = builder.build();
                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                        notificationManagerCompat.notify(noti_id, notification);
                        temp = noti_id;
                    } else {
                        notificationManager.cancel(temp);
                        temp = noti_id;
                                             /* builder 의 build() 를 통해 Notification 객체를 생성하고,
                                                알림을 표시 하기 위한 NotificationManagerCompat.notify() 를
                                                호출하여 알림의 고유 ID와 함께 전달 */
                        Notification notification = builder.build();
                        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                        notificationManagerCompat.notify(noti_id, notification);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Logger.d("Receive Thread 종료");
        }


    }

    // json String => DTO 로 변경해줌
    private static DTO_Chat makeDTO(String json) {
        Gson gson = new Gson();
        DTO_Chat message = gson.fromJson(json, DTO_Chat.class);
        return message;
    }

    // TODO [현재 사용중인 최상위 액티비티 명 확인]
    public static String getNowUseActivity(Context mContext) {

        /**
         * 참고 : [특정 클래스에서 본인 클래스명 확인 방법]
         * getClass().getName()
         * */

        // [초기 리턴 결과 반환 변수 선언 실시]
        String returnActivityName = "";
        try {
            ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

            String className = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                className = String.valueOf(manager.getAppTasks().get(0).getTaskInfo().topActivity.getClassName());
            } else {
                List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);
                ComponentName componentName = info.get(0).topActivity;
                className = componentName.getClassName();
            }


            // [리턴 반환 데이터 삽입 실시]
            returnActivityName = className;
        } catch (Exception e) {
            //e.printStackTrace();

            Log.e("//===========//", "================================================");
            Log.i("", "\n" + "[C_Util >> getNowUseActivity() :: 현재 사용 중인 최상위 액티비티 확인]");
            Log.i("", "\n" + "[catch [에러] :: " + String.valueOf(e.getMessage()) + "]");
            Log.e("//===========//", "================================================");
        }

        // [리턴 결과 반환 수행 실시]
        return returnActivityName;
    }

}


