package com.example.novamarket.Class;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public  class LoggerManager {
    public static void setAdapter()
    {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(true)// (Optional) Whether to show thread info or not. Default true
                .methodCount(2)// (Optional) How many method line to show. Default 2
                .methodOffset(0)// (Optional) Hides internal method calls up to offset. Default 5
                .tag("PRETTY_LOGGER")// (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }
}
