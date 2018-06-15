package com.mj.myplayerbaseuse;

import android.app.Application;

import com.kk.taurus.exoplayer.ExoMediaPlayer;
import com.kk.taurus.ijkplayer.IjkPlayer;
import com.kk.taurus.playerbase.config.PlayerConfig;
import com.kk.taurus.playerbase.config.PlayerLibrary;
import com.kk.taurus.playerbase.entity.DecoderPlan;
import com.kk.taurus.playerbase.log.PLog;
import com.squareup.leakcanary.LeakCanary;

public class App extends Application {

    public static final int PLAN_ID_IJK = 1;
    public static final int PLAN_ID_EXO = 2;

    private static App instance;

    public static boolean ignoreMobile;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        PLog.LOG_OPEN = true;
        /*如果当前的进程是用来给LeakCanary 进行堆分析的则return，否则会执行LeakCanary的install方法*/
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        PlayerConfig.addDecoderPlan(new DecoderPlan(PLAN_ID_IJK, IjkPlayer.class.getName(), "IjkPlayer"));
        PlayerConfig.addDecoderPlan(new DecoderPlan(PLAN_ID_EXO, ExoMediaPlayer.class.getName(), "ExoPlayer"));
        PlayerConfig.setDefaultPlanId(PLAN_ID_IJK);

        //use default NetworkEventProducer.
        PlayerConfig.setUseDefaultNetworkEventProducer(true);

        PlayerLibrary.init(this);
    }
    public static App get(){
        return instance;
    }
}
