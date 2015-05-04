package com.doepiccoding.voiceanalizer;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final int sampleRate = 8000;
	private AudioRecord audio;
	private int bufferSize;
	private double lastLevel = 0;
	private Thread thread;
	private static final int SAMPLE_DELAY = 75;
	private ImageView mouthImage;
    private View soundbar;
    private TextView level;
    private TextView count;
    private int c;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mouthImage = (ImageView)findViewById(R.id.mounthHolder);
		mouthImage.setKeepScreenOn(true);

        level = (TextView)findViewById(R.id.level);
        count = (TextView)findViewById(R.id.count);
        soundbar = (View)findViewById(R.id.soundbar);

        try {
			bufferSize = AudioRecord
					.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
		} catch (Exception e) {
			android.util.Log.e("TrackingFlow", "Exception", e);
		}
	}

	protected void onResume() {
		super.onResume();
		audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		audio.startRecording();
		thread = new Thread(new Runnable() {
			public void run() {
				while(thread != null && !thread.isInterrupted()){
					//Let's make the thread sleep for a the approximate sampling time
					try{Thread.sleep(SAMPLE_DELAY);}catch(InterruptedException ie){ie.printStackTrace();}
					readAudioBuffer();//After this call we can get the last value assigned to the lastLevel variable
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if(lastLevel > 0 && lastLevel <= 2){
								mouthImage.setImageResource(R.drawable.mouth4);
							}else
							if(lastLevel > 2 && lastLevel <= 3){
								mouthImage.setImageResource(R.drawable.mouth3);
							}else
							if(lastLevel > 3 && lastLevel <= 5){
								mouthImage.setImageResource(R.drawable.mouth2);
							}
							if(lastLevel > 5){
								mouthImage.setImageResource(R.drawable.mouth1);
							}

                            String level1=String.format("%.2f",lastLevel);

                            level.setText(level1);
                            c++;
                            count.setText(String.valueOf(c));

                            LayoutParams params=soundbar.getLayoutParams();
                            double l2=lastLevel*40;
                            int lastlevel1=(int) l2;
                            params.width=lastlevel1;
                            soundbar.setLayoutParams(params);

                        }
					});
				}
			}
		});
		thread.start();
	}
	
	/**
	 * Functionality that gets the sound level out of the sample
	 */
	private void readAudioBuffer() {

		try {
			short[] buffer = new short[bufferSize];

			int bufferReadResult = 1;

			if (audio != null) {

				// Sense the voice...
				bufferReadResult = audio.read(buffer, 0, bufferSize);
				double sumLevel = 0;
				for (int i = 0; i < bufferReadResult; i++) {
					sumLevel += buffer[i];
				}
				lastLevel = Math.abs((sumLevel / bufferReadResult));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		thread.interrupt();
		thread = null;
		try {
			if (audio != null) {
				audio.stop();
				audio.release();
				audio = null;
			}
		} catch (Exception e) {e.printStackTrace();}
	}
}
