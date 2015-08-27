package jp.hanasake.hackathon.demo;

import android.app.Application;

import jp.hanasake.hackathon.demo.character_animation.CharacterAnimationDownloader;

/**
 * Created by Ryohei Komiya on 15/08/26.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CharacterAnimationDownloader.getInstance(getApplicationContext()).preloadAnimations();
    }
}
