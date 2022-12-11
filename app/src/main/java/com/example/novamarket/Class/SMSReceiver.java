package com.example.novamarket.Class;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.novamarket.Activity.Join;
import com.example.novamarket.Fragment.Frag_Phone;
import com.orhanobut.logger.Logger;

import java.util.Date;


public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 수신 되었을때 콜백 되는 메서드
        // 매개변수 intent 의 action 에 방송의 '종류' 가 들어있고, 필드에는 '추가정보' 가 들어 있다.
        // SMS 메세지를 파싱합니다.
        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = parseSmsMessage(bundle);
        Logger.d("문자 메세지 받음");

        // PDU 로 포멧되어 있는 메세지를 복원
        if (messages.length > 0) {
            String sender = messages[0].getOriginatingAddress();
            String content = messages[0].getMessageBody().toString();
            Date date = new Date(messages[0].getTimestampMillis());

            // 문제 내용 0~9까지 숫자 제외하고, 전부 "" 로 대체 => 인증번호만 남음
            String smsNum = content.replaceAll("[^0-9]", "");
            Logger.d(smsNum);

//          문자 발송된 화면
//          회원가입 : 0 // 아이디 찾기 : 1 // 비밀번호 찾기 : 2
            int submitAct = PreferenceManager.getInt(context, "sns_receive");

            switch (submitAct) {
                case 0:
                    Intent intent1 = new Intent(context, Join.class);
                    intent1.putExtra("sms_number",smsNum);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent1);
                    Logger.d("sms_number : " + smsNum);
                    break;

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
            }
        }
    }

    private SmsMessage[] parseSmsMessage(Bundle bundle) {
        Object[] messages = (Object[]) bundle.get("pdus");
        SmsMessage[] smsMessages = new SmsMessage[messages.length];
        for (int i = 0; i < messages.length; i++) {
            smsMessages[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
        }

        return smsMessages;
    }
}