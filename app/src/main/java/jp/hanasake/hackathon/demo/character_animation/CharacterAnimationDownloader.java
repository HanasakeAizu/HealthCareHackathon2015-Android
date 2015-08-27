package jp.hanasake.hackathon.demo.character_animation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by Ryohei Komiya on 15/08/26.
 */
public final class CharacterAnimationDownloader {

    //------------------------------------------------------------
    // インスタンスの生成
    //------------------------------------------------------------

    @NonNull
    public static CharacterAnimationDownloader getInstance(Context context) {
        if (instance == null) {
            instance = new CharacterAnimationDownloader(context);
        }
        return instance;
    }

    private static CharacterAnimationDownloader instance;

    @NonNull
    private final Context context;

    private CharacterAnimationDownloader(@NonNull Context context) {
        this.context = context;
        downloadRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }



    //------------------------------------------------------------
    // アニメーションデータのダウンロード
    //------------------------------------------------------------

    private final RequestQueue downloadRequestQueue;

    /**
     * アニメーションデータのプリロード
     */
    public void preloadAnimations() {
        downloadAnimation(CharacterAnimation.Emotion.Delight, cacheDownloadAnimationListener);
        downloadAnimation(CharacterAnimation.Emotion.Anger, cacheDownloadAnimationListener);
        downloadAnimation(CharacterAnimation.Emotion.Sorrow, cacheDownloadAnimationListener);
        downloadAnimation(CharacterAnimation.Emotion.Walk, cacheDownloadAnimationListener);
    }

    /**
     * アニメーションデータを読み込む
     *
     * @param emotion アニメーションデータの種類
     */
    public void downloadAnimation(@NonNull CharacterAnimation.Emotion emotion) {
        downloadAnimation(emotion, null);
    }

    /**
     * アニメーションデータを読み込む
     *
     * @param emotion アニメーションデータの種類
     * @param listener アニメーションデータの読み込み完了後に発生するイベント
     */
    public void downloadAnimation(@NonNull CharacterAnimation.Emotion emotion, @Nullable final OnDownloadAnimationListener listener) {
        final CharacterAnimation cachedAnim = animationCache.get(emotion.getName());
        if (cachedAnim != null) {
            if (listener != null) {
                listener.onDownload(cachedAnim);
            }
            return;
        }

        final String url = "http://54.65.195.96/api/v1/animations/" + emotion.getName();
        downloadRequestQueue.add(new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                final CharacterAnimation anim = CharacterAnimation.parseJson(response);
                if (anim != null) {
                    animationCache.put(anim.getEmotion().getName(), anim);
                }
                if (listener != null) {
                    listener.onDownload(anim);
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onDownload(null);
                }
            }
        }));
    }

    public interface OnDownloadAnimationListener {
        void onDownload(@Nullable CharacterAnimation animation);
    }



    //------------------------------------------------------------
    // キャッシュの管理
    //------------------------------------------------------------

    private final LruCache<String, CharacterAnimation> animationCache = new LruCache<>(CharacterAnimation.Emotion.values().length);

    private final OnDownloadAnimationListener cacheDownloadAnimationListener = new OnDownloadAnimationListener() {
        @Override
        public void onDownload(@Nullable CharacterAnimation animation) {
            if (animation != null) {
                for (String url : animation.getFrameURLs()) {
                    SimpleImageCache.getInstance(context).downloadImage(url);
                }
            }
        }
    };
}
