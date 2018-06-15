package com.mj.myplayerbaseuse.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.kk.taurus.playerbase.event.OnPlayerEventListener;
import com.kk.taurus.playerbase.receiver.OnReceiverEventListener;
import com.kk.taurus.playerbase.receiver.ReceiverGroup;
import com.mj.myplayerbaseuse.R;
import com.mj.myplayerbaseuse.adapter.VideoListAdapter;
import com.mj.myplayerbaseuse.bean.VideoBean;
import com.mj.myplayerbaseuse.cover.GestureCover;
import com.mj.myplayerbaseuse.play.AssistPlayer;
import com.mj.myplayerbaseuse.play.DataInter;
import com.mj.myplayerbaseuse.play.ReceiverGroupManager;
import com.mj.myplayerbaseuse.untils.DataUtils;

import java.util.ArrayList;
import java.util.List;

public class OnlineVideoListActivity extends AppCompatActivity implements
        VideoListAdapter.OnListListener,
        OnReceiverEventListener, OnPlayerEventListener {

    private List<VideoBean> mItems = new ArrayList<>();
    private VideoListAdapter mAdapter;

    private RecyclerView mRecycler;
    private FrameLayout mContainer;

    private boolean toDetail;
    private boolean isLandScape;

    private ReceiverGroup mReceiverGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setTitle("在线视频");

        mRecycler = findViewById(R.id.recycler);
        mContainer = findViewById(R.id.listPlayContainer);
        mRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        AssistPlayer.get().addOnReceiverEventListener(this);
        AssistPlayer.get().addOnPlayerEventListener(this);

        mReceiverGroup = ReceiverGroupManager.get().getLiteReceiverGroup(this);
        mReceiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_NETWORK_RESOURCE, true);

        mItems.addAll(DataUtils.getVideoList());
        mAdapter = new VideoListAdapter(getApplicationContext(), mRecycler, mItems);
        mAdapter.setOnListListener(OnlineVideoListActivity.this);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onPlayerEvent(int eventCode, Bundle bundle) {


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isLandScape = newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE;
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
            attachFullScreen();
        }else if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
            attachList();
        }
        mReceiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_IS_LANDSCAPE, isLandScape);
    }

    @Override
    public void onBackPressed() {
        if(isLandScape){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        super.onBackPressed();
    }

    private void attachFullScreen(){
        mReceiverGroup.addReceiver(DataInter.ReceiverKey.KEY_GESTURE_COVER, new GestureCover(this));
        mReceiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_CONTROLLER_TOP_ENABLE, true);
        if(AssistPlayer.get().isPlaying())
            AssistPlayer.get().play(mContainer,null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toDetail = false;
        AssistPlayer.get().setReceiverGroup(mReceiverGroup);
        if(isLandScape){
            attachFullScreen();
        }else{
            attachList();
        }
        AssistPlayer.get().resume();
        mReceiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_CONTROLLER_TOP_ENABLE, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!toDetail){
            AssistPlayer.get().pause();
        }
    }

    private void attachList() {
        if(mAdapter!=null){
            mReceiverGroup.removeReceiver(DataInter.ReceiverKey.KEY_GESTURE_COVER);
            mReceiverGroup.getGroupValue().putBoolean(DataInter.Key.KEY_CONTROLLER_TOP_ENABLE, false);
            mAdapter.getListPlayLogic().attachPlay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AssistPlayer.get().removeReceiverEventListener(this);
        AssistPlayer.get().removePlayerEventListener(this);
        AssistPlayer.get().destroy();
    }

    @Override
    public void onReceiverEvent(int eventCode, Bundle bundle) {
        switch (eventCode){
            case DataInter.Event.EVENT_CODE_REQUEST_BACK:
                onBackPressed();
                break;
            case DataInter.Event.EVENT_CODE_REQUEST_TOGGLE_SCREEN:
                setRequestedOrientation(isLandScape?
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    @Override
    public void onTitleClick(VideoBean item, int position) {
        toDetail = true;
        Intent intent = new Intent(this, DetailPageActivity.class);
        intent.putExtra(DetailPageActivity.KEY_ITEM, item);
        startActivity(intent);
    }
}
