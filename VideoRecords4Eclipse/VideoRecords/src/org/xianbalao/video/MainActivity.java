package org.xianbalao.video;

import org.xianbalao.video.record.VideoRecordActivkty;
import org.xiangbalao.videorecord.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class MainActivity extends Activity implements OnClickListener {

	private MediaController mMediaController;
	private VideoView mVideoView;
	private RelativeLayout rl;
	private Button button;

	private int RECORD = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

	}

	public void onEventMainThread(String event) {

		initView();
	}

	private void initView() {
		button = (Button) findViewById(R.id.button);

		button.setOnClickListener(this);

		mVideoView = (VideoView) findViewById(R.id.videoView);

		// mVideoView.setVideoPath(path);

		rl = (RelativeLayout) findViewById(R.id.rl);

		mMediaController = new MediaController(this);
		// = (MediaController) findViewById(R.id.mediaController);
		ViewGroup mvView = (ViewGroup) mMediaController.getParent();

		mvView.removeView(mMediaController);

		LayoutParams lpLayoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		mMediaController.setLayoutParams(lpLayoutParams);

		rl.addView(mMediaController);
		mVideoView.setMediaController(mMediaController);
		mVideoView.requestFocus();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button:

			startActivityForResult(new Intent(MainActivity.this,
					VideoRecordActivkty.class), RECORD);

			break;

		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RECORD) {
			if (resultCode == RESULT_OK) {

				String videoPath = data.getStringExtra("RECORD");
				mVideoView.setVideoPath(videoPath);
			}
		}

	}

}
