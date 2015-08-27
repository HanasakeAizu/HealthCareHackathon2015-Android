package jp.hanasake.hackathon.demo;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import jp.hanasake.hackathon.demo.character_animation.CharacterAnimation;
import jp.hanasake.hackathon.demo.character_animation.CharacterAnimationPlayerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupPlayer(R.id.player_delight, R.string.delight, R.color.delight, CharacterAnimation.Emotion.Delight);
        setupPlayer(R.id.player_anger, R.string.anger, R.color.anger, CharacterAnimation.Emotion.Anger);
        setupPlayer(R.id.player_sorrow, R.string.sorrow, R.color.sorrow, CharacterAnimation.Emotion.Sorrow);
        setupPlayer(R.id.player_walk, R.string.walk, R.color.walk, CharacterAnimation.Emotion.Walk);
    }

    private void setupPlayer(@IdRes int playerResId, @StringRes int emotionLabelResId, int backgroundResId, final CharacterAnimation.Emotion emotion) {
        final View playerView = findViewById(playerResId);
        playerView.setBackgroundResource(backgroundResId);

        final TextView emotionLabel = (TextView)playerView.findViewById(R.id.emotion_label);
        emotionLabel.setText(emotionLabelResId);

        final CharacterAnimationPlayerView playerImageView = (CharacterAnimationPlayerView)playerView.findViewById(R.id.player_image_view);
        final View clickView = playerView.findViewById(R.id.click_view);

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickView.setVisibility(View.INVISIBLE);
                playerImageView.playAnimation(emotion, 3, true, new CharacterAnimationPlayerView.OnCompletePlayAnimationListener() {
                    @Override
                    public void onComplete(boolean isFinished) {
                        if (isFinished) {
                            clickView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }
}
