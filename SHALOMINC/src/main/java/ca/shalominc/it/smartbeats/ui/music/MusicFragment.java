package ca.shalominc.it.smartbeats.ui.music;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ca.shalominc.it.smartbeats.R;

import static android.content.ContentValues.TAG;
import static java.sql.Types.NULL;

public class MusicFragment extends Fragment
{

    //Media Player
    MediaPlayer mediaPlayer;
    TextView shalomPosition, shalomDuration;
    SeekBar shalomSeekBar;
    ImageView shalomRew, shalomPlay, shalomPause, shalomFastForward, shalomStop;
    ImageView shalomVinyl;
    Animation rotateAnimation;
    Handler handler = new Handler();
    Runnable runnable;

    //Audio Manager
    AudioManager aM;
    SeekBar shalomVolume;

    //Spinner for user to select songs.
    Spinner shalomSongSpinner;
    String spinnerString;

    //Song selector
    String DBSongUrl;
    String DBSongUrlChoice;


// Timer countdown till the song auto stops.
    EditText shalomEditTextInput;
    TextView shalomTextViewCountDown;
    Button shalomButtonSet;
    Button shalomButtonStartPause;
    Button shalomButtonReset;
    CountDownTimer shalomCountDownTimer;
    boolean shalomTimerRunning;
    long shalomStartTimeInMillis;
    long shalomTimeLeftInMillis;
    long shalomEndTime;

    //Notification
    Button testNotification;

    //-------------------------------------------------------------------------------------------------------------------------------------------//
    // Functionality Starts here for OnCreateView
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        //Media Player and the options
        mediaPlayer = new MediaPlayer();
        shalomPosition = view.findViewById(R.id.shalom_timer);
        shalomDuration = view.findViewById(R.id.shalom_timer_duration);
        shalomSeekBar = view.findViewById(R.id.shalom_seekbar);
        shalomRew = view.findViewById(R.id.shalom_rewind);
        shalomPlay = view.findViewById(R.id.bt_play);
        shalomPause = view.findViewById(R.id.bt_pause);
        shalomFastForward = view.findViewById(R.id.bt_ff);
        shalomVinyl = view.findViewById(R.id.shalom_IV);
  //    shalomStop = view.findViewById(R.id.bt_stop);

        //Adjust Volumes.
        shalomVolume = view.findViewById(R.id.shalom_volume);
        aM = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int maxVol = aM.getStreamMaxVolume(aM.STREAM_MUSIC);
        int curVol = aM.getStreamVolume(aM.STREAM_MUSIC);
        shalomVolume.setMax(maxVol);
        shalomVolume.setProgress(curVol);

        // Timer Count down
        shalomEditTextInput = view.findViewById(R.id.shalom_edit_text_input);
        shalomTextViewCountDown = view.findViewById(R.id.shalom_text_view_countdown);
        shalomButtonSet = view.findViewById(R.id.shalom_button_set);
        shalomButtonStartPause = view.findViewById(R.id.shalom_button_start_pause);
        shalomButtonReset = view.findViewById(R.id.shalom_button_reset);


        //Notification

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("My notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        testNotification = view.findViewById(R.id.test_notification);
        testNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NotificationCompat.Builder builder = new NotificationCompat.Builder (getContext(), "My notification");
                builder.setContentTitle("My Title");
                builder.setContentText("This is a notification");
                builder.setSmallIcon(R.drawable.ic_baseline_chat_24);
                builder.setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
                notificationManagerCompat.notify(1, builder.build());
            }
        });

        //Spinner for user to select their songs
        shalomSongSpinner = view.findViewById(R.id.shalom_music_spinner);
        ArrayAdapter<CharSequence> sAdapter = ArrayAdapter.createFromResource(getContext(), R.array.Songs, android.R.layout.simple_spinner_item);
        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shalomSongSpinner.setAdapter(sAdapter);



        //Seekbar progress Duration check for the song.
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                shalomSeekBar.setProgress(mediaPlayer.getCurrentPosition());

                handler.postDelayed(this, 500);

            }
        };

        // Counter for the start text and end text of the music duration.
        int duration = mediaPlayer.getDuration();
        String sDuration = convertFormat(duration);
        shalomDuration.setText(sDuration);


        //Setting up Media Player
        mediaPlayer.setAudioAttributes
                (new AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                );

        rotateAnimation();  // Calling function rotateAnimation();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        DBSongUrl = "https://firebasestorage.googleapis.com/v0/b/shalominc-smartbeats.appspot.com/o/BLR%20-%20Taj.mp3?alt=media&token=e3aacbca-33a4-4368-ad0a-7ba49f2f6692";
        try
        {
            mediaPlayer.setDataSource(DBSongUrl);
            mediaPlayer.prepare();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Spinner Item selector
        shalomSongSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l)
            {
                spinnerString = shalomSongSpinner.getItemAtPosition(position).toString();
                Context context = getContext();
                switch (spinnerString)
                {
                    case "Select Your Song":
                        Toast.makeText(context, "Select a Song Below", Toast.LENGTH_LONG).show();
                        break;

                    case "ATC - All Around The World":
                        Toast.makeText(context, "ATC - All Around The World", Toast.LENGTH_LONG).show();
                        DBSongUrlChoice = "https://firebasestorage.googleapis.com/v0/b/shalominc-smartbeats.appspot.com/o/ATC%20-%20All%20Around%20The%20World.mp3?alt=media&token=41077a29-12e9-4371-b8a0-af1c7179a0d4";
                        mediaPlayer.reset();
                        try
                        {
                            mediaPlayer.setDataSource(DBSongUrlChoice);
                            mediaPlayer.prepare();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        break;

                    case "Dynoro - In My Mind":
                        Toast.makeText(context, "Dynoro - In My Mind", Toast.LENGTH_LONG).show();
                        DBSongUrlChoice = "https://firebasestorage.googleapis.com/v0/b/shalominc-smartbeats.appspot.com/o/Dynoro%20-%20In%20My%20Mind.mp3?alt=media&token=8600afad-31fb-4f7f-97b4-92e2968ff851";
                        mediaPlayer.reset();
                        try
                        {
                            mediaPlayer.setDataSource(DBSongUrlChoice);
                            mediaPlayer.prepare();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        break;

                    case "MEDUZA - Lose Control":
                        Toast.makeText(context, "MEDUZA - Lose Control", Toast.LENGTH_LONG).show();
                        DBSongUrlChoice = "https://firebasestorage.googleapis.com/v0/b/shalominc-smartbeats.appspot.com/o/MEDUZA%20-%20Lose%20Control.mp3?alt=media&token=92253d10-47c6-455b-897b-14bec7e1b923";
                        mediaPlayer.reset();
                        try
                        {
                            mediaPlayer.setDataSource(DBSongUrlChoice);
                            mediaPlayer.prepare();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        break;

                    case "Regard - Ride It":
                        Toast.makeText(context, "Regard - Ride It", Toast.LENGTH_LONG).show();
                        DBSongUrlChoice = "https://firebasestorage.googleapis.com/v0/b/shalominc-smartbeats.appspot.com/o/Regard%20-%20Ride%20It.mp3?alt=media&token=d52d0d1e-1152-4b64-9cfc-0def83505f00";
                        mediaPlayer.reset();
                        try
                        {
                            mediaPlayer.setDataSource(DBSongUrlChoice);
                            mediaPlayer.prepare();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        break;

                    case "SAINt Jhn - Roses":
                        Toast.makeText(context, "SAINt Jhn - Roses", Toast.LENGTH_LONG).show();
                        DBSongUrlChoice = "https://firebasestorage.googleapis.com/v0/b/shalominc-smartbeats.appspot.com/o/SAINt%20Jhn%20-%20Roses.mp3?alt=media&token=d077c318-e028-4ee1-a043-bc896c49dacb";
                        mediaPlayer.reset();
                        try
                        {
                            mediaPlayer.setDataSource(DBSongUrlChoice);
                            mediaPlayer.prepare();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });
        // Spinner Item selector ENDS HERE


        // Volume changer for musics
        shalomVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                aM.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        // Volume Changer for music Ends here

          //mediaPlayer = MediaPlayer.create(getContext(), R.raw.music);

        // Play button click listenenr
        shalomPlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                shalomPlay.setVisibility(View.GONE);
                shalomPause.setVisibility(View.VISIBLE);
                mediaPlayer.start();
                shalomSeekBar.setMax(mediaPlayer.getDuration());
                    handler.postDelayed(runnable, 0);
            }
        });

        // Pause button click listener
        shalomPause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                shalomPause.setVisibility(View.GONE);
                shalomPlay.setVisibility(View.VISIBLE);
                mediaPlayer.pause();
                handler.removeCallbacks(runnable);
            }
        });

        //FastForward button click Listener
        shalomFastForward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                if (mediaPlayer.isPlaying() && duration != currentPosition) {
                    currentPosition = currentPosition + 5000;
                    shalomPosition.setText(convertFormat(currentPosition));
                    mediaPlayer.seekTo(currentPosition);
                }
            }
        });

        //Rewind button click listener
        shalomRew.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                if (mediaPlayer.isPlaying() && currentPosition > 5000) {
                    currentPosition = currentPosition - 5000;
                    shalomPosition.setText(convertFormat(currentPosition));
                    mediaPlayer.seekTo(currentPosition);
                }
            }
        });

  /*      shalomStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mediaPlayer.stop();

                mediaPlayer.seekTo(0);

                handler.removeCallbacks(runnable);
            }
        });
*/

        //SeekBar change listener
        shalomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                shalomPosition.setText(convertFormat(mediaPlayer.getCurrentPosition()));
                shalomDuration.setText(convertFormat(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }});

        //Pause and play visibility
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                shalomPause.setVisibility(View.GONE);
                shalomPlay.setVisibility(View.VISIBLE);
                //mediaPlayer.seekTo(0);
            }
        });

//      ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        shalomButtonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = shalomEditTextInput.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(getContext(),R.string.field_not_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                long millisInput = Long.parseLong(input) * 60000;
                if (millisInput == 0) {
                    Toast.makeText(getContext(),R.string.enter_positive_number, Toast.LENGTH_SHORT).show();
                    return;
                }

                setTime(millisInput);
                shalomEditTextInput.setText("");
            }
        });

        shalomButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shalomTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        shalomButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

    } //OnViewCreated Ends here                     Functions next
    //----------------------------------------------------------------------------------------------------------------------------------

    private void rotateAnimation()
    {
        rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.spinimage);
        shalomVinyl.startAnimation(rotateAnimation);
    }


    private String convertFormat(int duration)
    {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

   //------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    //Setting the CountDown Timer
    private void setTime(long milliseconds) {
        shalomStartTimeInMillis = milliseconds;
        resetTimer();
    }

    private void startTimer() {
        shalomEndTime = System.currentTimeMillis() + shalomTimeLeftInMillis;
        shalomCountDownTimer = new CountDownTimer(shalomTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                shalomTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                shalomTimerRunning = false;
                updateWatchInterface();
            }
        }.start();

        shalomTimerRunning = true;
        updateWatchInterface();
    }

    private void pauseTimer() {
        shalomCountDownTimer.cancel();
        shalomTimerRunning = false;
        updateWatchInterface();
    }

    private void resetTimer() {
        shalomTimeLeftInMillis = shalomStartTimeInMillis;
        updateCountDownText();
        updateWatchInterface();
    }

    private void updateCountDownText() {
        int hours = (int) (shalomTimeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((shalomTimeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (shalomTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }

        shalomTextViewCountDown.setText(timeLeftFormatted);
    }

    @SuppressLint("SetTextI18n")
    private void updateWatchInterface() {
        if (shalomTimerRunning) {
            shalomEditTextInput.setVisibility(View.INVISIBLE);
            shalomButtonSet.setVisibility(View.INVISIBLE);
            shalomButtonReset.setVisibility(View.INVISIBLE);
            shalomButtonStartPause.setText(R.string.pause);
        } else {
            shalomEditTextInput.setVisibility(View.VISIBLE);
            shalomButtonSet.setVisibility(View.VISIBLE);
            shalomButtonStartPause.setText(R.string.start);

            if (shalomTimeLeftInMillis < 1000) {
                shalomButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                shalomButtonStartPause.setVisibility(View.VISIBLE);
            }

            if (shalomTimeLeftInMillis < shalomStartTimeInMillis) {
                shalomButtonReset.setVisibility(View.VISIBLE);
            } else {
                shalomButtonReset.setVisibility(View.INVISIBLE);
            }
        }
    }




}