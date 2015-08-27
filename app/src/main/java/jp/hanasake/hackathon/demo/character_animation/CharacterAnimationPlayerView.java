package jp.hanasake.hackathon.demo.character_animation;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ryohei Komiya on 15/08/26.
 */
public final class CharacterAnimationPlayerView extends ImageView {
    private boolean isPlaying = false;

    public CharacterAnimationPlayerView(Context context) {
        super(context);
    }

    public CharacterAnimationPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CharacterAnimationPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CharacterAnimationPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }



    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * アニメーションを再生する
     *
     * @param emotion アニメーションの種類
     */
    public void playAnimation(@NonNull CharacterAnimation.Emotion emotion) {
        playAnimation(emotion, 1, false, null);
    }

    /**
     * アニメーションを再生する
     *
     * @param emotion アニメーションの種類
     * @param repeatCount アニメーションを繰り返し再生する回数
     * @param fillAfter アニメーション終了後に最終フレームの画像を表示し続ける場合はtrueを、そうでない場合はfalseを設定する。
     */
    public void playAnimation(@NonNull CharacterAnimation.Emotion emotion, final int repeatCount, final boolean fillAfter) {
        playAnimation(emotion, repeatCount, fillAfter, null);
    }

    /**
     * アニメーションを再生する
     *
     * @param emotion アニメーションの種類
     * @param repeatCount アニメーションを繰り返し再生する回数
     * @param fillAfter アニメーション終了後に最終フレームの画像を表示し続ける場合はtrueを、そうでない場合はfalseを設定する。
     * @param listener アニメーションの再生完了後のコールバック
     */
    public void playAnimation(@NonNull CharacterAnimation.Emotion emotion, final int repeatCount, final boolean fillAfter, @Nullable final OnCompletePlayAnimationListener listener) {
        if (isPlaying) {
            if (listener != null) {
                listener.onComplete(false);
            }
            return;
        }
        isPlaying = true;

        final Context context = getContext().getApplicationContext();
        final Handler handler = new Handler();
        final WeakReference<CharacterAnimationPlayerView> weakSelf = new WeakReference<>(this);

        CharacterAnimationDownloader.getInstance(context).downloadAnimation(emotion, new CharacterAnimationDownloader.OnDownloadAnimationListener() {
            @Override
            public void onDownload(@Nullable final CharacterAnimation animation) {
                if (animation == null) {
                    if (listener != null) {
                        listener.onComplete(false);
                    }
                    return;
                }

                final SortedMap<Integer, Bitmap> indexedImages = Collections.synchronizedSortedMap(new TreeMap<Integer, Bitmap>());

                final CountDownLatch countDownLatch = new CountDownLatch(animation.getFrameURLs().size());
                for (int i = 0; i < animation.getFrameURLs().size(); i++) {
                    final String url = animation.getFrameURLs().get(i);

                    final int index = i;
                    SimpleImageCache.getInstance(context).downloadImage(url, new SimpleImageCache.OnDownloadImageListener() {
                        @Override
                        public void onDownload(@Nullable Bitmap image) {
                            indexedImages.put(index, image);
                            countDownLatch.countDown();
                        }
                    });
                }


                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(@NonNull Void... params) {
                        try {
                            return countDownLatch.await(1, TimeUnit.MINUTES);
                        } catch (InterruptedException e) {
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(@NonNull Boolean result) {
                        if (!result) {
                            if (listener != null) {
                                listener.onComplete(false);
                            }
                            isPlaying = false;
                            return;
                        }

                        final Collection<Bitmap> images = indexedImages.values();

                        final long duration = playFrameAnimation(weakSelf.get(), images, animation.getFps(), repeatCount);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!fillAfter && weakSelf.get() != null) {
                                    weakSelf.get().setImageBitmap(null);
                                }

                                if (listener != null) {
                                    listener.onComplete(true);
                                }
                                isPlaying = false;
                            }
                        }, duration);
                    }

                }.execute();
            }
        });
    }

    public interface OnCompletePlayAnimationListener {
        void onComplete(boolean isFinished);
    }



    private static long playFrameAnimation(@NonNull ImageView target, @NonNull Collection<Bitmap> images, int fps, int repeatCount) {
        final AnimationDrawable animation = new AnimationDrawable();

        final int frameDuration = (int)(1000F / fps);
        for (int i=0; i < repeatCount; i++) {
            for (Bitmap image : images) {
                animation.addFrame(new BitmapDrawable(image), frameDuration);
            }
        }

        target.setImageDrawable(animation);
        animation.start();

        return (long)frameDuration * images.size() * repeatCount;
    }
}
