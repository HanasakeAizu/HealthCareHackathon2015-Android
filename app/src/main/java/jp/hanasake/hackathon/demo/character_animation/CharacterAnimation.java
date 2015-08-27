package jp.hanasake.hackathon.demo.character_animation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryohei Komiya on 15/08/26.
 */
public final class CharacterAnimation {
    private final Emotion emotion;
    private final int fps;
    private final Size size;
    private final List<String> frameURLs;

    @NonNull
    public Emotion getEmotion() {
        return emotion;
    }

    public int getFps() {
        return fps;
    }

    @NonNull
    public Size getSize() {
        return size;
    }

    @NonNull
    public List<String> getFrameURLs() {
        return frameURLs;
    }

    public enum Emotion {
        Delight("delight"),
        Anger("anger"),
        Sorrow("sorrow"),
        Walk("walk");

        @NonNull
        private final String name;

        Emotion(@NonNull final String name) {
            this.name = name;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @Nullable
        static Emotion build(final String name) {
            for (Emotion value : values()) {
                if (value.name.equals(name)) {
                    return value;
                }
            }
            return null;
        }
    }

    @Nullable
    static CharacterAnimation parseJson(@NonNull JSONObject json) {
        try {
            final Emotion emotion = Emotion.build(json.getString("emotion"));
            if (emotion == null) {
                return null;
            }

            final int fps = json.getInt("fps");

            final JSONObject size = json.getJSONObject("size");
            final int width = size.getInt("width");
            final int height = size.getInt("height");

            final JSONArray frames = json.getJSONArray("frames");
            final List<String> frameURLs = new ArrayList<>(frames.length());
            for (int i=0; i < frames.length(); i++) {
                frameURLs.add(frames.getString(i));
            }

            return new CharacterAnimation(emotion, fps, new Size(width, height), frameURLs);

        } catch (JSONException e) {
            return null;
        }
    }

    private CharacterAnimation(Emotion emotion, int fps, Size size, List<String> frameURLs) {
        this.emotion = emotion;
        this.fps = fps;
        this.size = size;
        this.frameURLs = frameURLs;
    }
}
