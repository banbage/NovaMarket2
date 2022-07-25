package com.example.novamarket.Class;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Date;

// 방송되고 있는 내용중에 원하는 항목을 받았을때 어떻게 처리할지 작성하는 클래스
public class SMSReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // 수신 되었을때 콜백 되는 메서드
        // 매개변수 intent 의 action 에 방송의 '종류' 가 들어있고, 필드에는 '추가정보' 가 들어 있다.
        Log.e(TAG, "내용 : 문자 메세지 BroadCast 시작");
        // SMS 메세지를 파싱합니다.
        Bundle bundle = intent.getExtras();
        Log.e(TAG, "내용 : bundle : " + bundle);
        SmsMessage[] messages = parseSmsMessage(bundle);
        Log.e(TAG, "내용 : messages : " + messages);

        // PDU 로 포멧되어 있는 메세지를 복원

        if (messages.length > 0) {
            String sender = messages[0].getOriginatingAddress();
            String content = messages[0].getMessageBody().toString();
            Date date = new Date(messages[0].getTimestampMillis());

            // 발신자 정보 가져오기
            Log.e(TAG, "내용 : smsSender : " + sender);
            // 매세지 내용 가져오기
            Log.e(TAG, "내용 : smsContent : " + content);
            // 메세지 작성 날자 정보 가져오기
            Log.e(TAG, "내용 : smsDate : " + date);

            // 문제 내용 0~9까지 숫자 제외하고, 전부 "" 로 대체 => 인증번호만 남음
            String smsNum = content.replaceAll("[^0-9]", "");
            Log.e(TAG, "내용 : 메세지 내용 파싱  smsNum : " + smsNum);

            PreferenceManager.setString(context, "smsNum", smsNum);
            Log.e(TAG, "내용 : 인증번호 PreferenceManager 에 등록 : " + PreferenceManager.getString(context, "smsNum"));


//          문자 발송된 화면
//          회원가입 : 0 // 아이디 찾기 : 1 // 비밀번호 찾기 : 2
            int submitAct = PreferenceManager.getInt(context, "submitAct");

//            switch (submitAct) {
//                case 0:
//                    Intent appIntent = new Intent(context, JoinPhone.class);
//                    appIntent.putExtra("smsNum",smsNum);
//                    appIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    context.startActivity(appIntent);
//                    break;
//
//                case 1:
//                    Intent intent1 = new Intent(context, findId.class);
//                    intent1.putExtra("smsNum",smsNum);
//                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    context.startActivity(intent1);
//                    break;
//
//                case 2:
//                    Intent intent2 = new Intent(context, findPw.class);
//                    intent2.putExtra("smsNum",smsNum);
//                    intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                    context.startActivity(intent2);
//                    break;
//            }

//            Intent AppIntent = new Intent(context, JoinPhone.class);
//            AppIntent.putExtra("smsNum", smsNum);
//            AppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            context.startActivity(AppIntent);
        }
    }

    private SmsMessage[] parseSmsMessage(Bundle bundle) {
        Object[] messages = (Object[]) bundle.get("pdus");
        SmsMessage[] smsMessages = new SmsMessage[messages.length];

        // 실제 메세지는 Object 타입의 배열에 PDU 형식으로 저장된다.
        Log.e(TAG, "내용 : messages : " + messages);
        // PDU
        Log.e(TAG, "내용 : smsMessages : " + smsMessages);

        for (int i = 0; i < messages.length; i++) {
            smsMessages[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
            Log.e(TAG, "내용 : smsMessage[i] :" + smsMessages[i]);
        }

        return smsMessages;
    }
}