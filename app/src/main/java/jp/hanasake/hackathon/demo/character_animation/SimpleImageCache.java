package jp.hanasake.hackathon.demo.character_animation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ryohei Komiya on 15/08/26.
 */
final class SimpleImageCache {

    //------------------------------------------------------------
    // インスタンスの生成
    //------------------------------------------------------------

    @NonNull
    static SimpleImageCache getInstance(Context context) {
        if (instance == null) {
            instance = new SimpleImageCache(context);
        }
        return instance;
    }

    private static SimpleImageCache instance;

    @NonNull
    private final Context context;

    private SimpleImageCache(@NonNull Context context) {
        this.context = context;
    }



    //------------------------------------------------------------
    // 画像のダウンロード
    //------------------------------------------------------------

    private final List<Target> pendingTargets = new LinkedList<>();

    void downloadImage(@NonNull String url) {
        downloadImage(url, null);
    }

    void downloadImage(@NonNull String url, @Nullable final OnDownloadImageListener listener) {
        final Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                pendingTargets.remove(this);
                if (listener != null) {
                    listener.onDownload(bitmap);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                pendingTargets.remove(this);
                if (listener != null) {
                    listener.onDownload(null);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        pendingTargets.add(target);
        Picasso.with(context).load(url).into(target);
    }

    interface OnDownloadImageListener {
        void onDownload(@Nullable Bitmap image);
    }
}
