package org.xianbalao.video.record;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.xiangbalao.videorecord.R;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VideoRecordActivkty extends Activity implements OnClickListener {

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mMediaRecorder;
	private boolean isRecording = false;
	private Button captureButton;
	private TextView tv_recorderTime;
	private LinearLayout layout;
	private Timer timer = null;
	// 录制的视频路径
	private String outputpath;
	// 预览
	private FrameLayout preview;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATETIME:
				tv_recorderTime.setText(msg.obj.toString());
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_record);
		initView();

		initCamera(Cofig4VideoRecord.CAMERAINDEX);

		captureButton.setOnClickListener(this);
		findViewById(R.id.button_exit).setOnClickListener(this);
		findViewById(R.id.right_button).setOnClickListener(this);
	}

	/**
	 * 初始化相机
	 * 
	 * @param index
	 */
	private void initCamera(int index) {
		mCamera = getCameraInstance(index);
		if (mCamera != null) {
			Parameters p = mCamera.getParameters();
			// p.setPreviewSize(240, 160);
			p.setRotation(90);
			mCamera.setDisplayOrientation(90);
			mCamera.setParameters(p);
			mPreview = new CameraPreview(this, mCamera);
			preview.removeAllViews();
			preview.addView(mPreview);
		}

	}

	private void initView() {
		preview = (FrameLayout) findViewById(R.id.camera_preview);
		layout = (LinearLayout) findViewById(R.id.ll_timer);
		tv_recorderTime = (TextView) findViewById(R.id.tv_recorderTime);
		captureButton = (Button) findViewById(R.id.button_capture);
		findViewById(R.id.return_button).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
	}

	private class mTimerTask extends TimerTask {

		private long start_time;
		private long end_time;
		private SimpleDateFormat simpleDateFormat;

		public mTimerTask() {
			super();
			// 设置录制时间布局显示
			layout.setVisibility(View.VISIBLE);
			start_time = System.currentTimeMillis(); // 记录开始时间
			simpleDateFormat = new SimpleDateFormat("mm:ss");
		}

		@Override
		public void run() {
			// 计算从开始到现在所用的时间
			end_time = System.currentTimeMillis();
			Date date = new Date(end_time - start_time);

			// 通知主线程显示
			Message msg = new Message();
			msg.what = UPDATETIME;
			msg.obj = simpleDateFormat.format(date);

			handler.sendMessage(msg);
		}
	}

	private static final int UPDATETIME = 1;

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		VideoRecordActivkty.this.finish();
		System.exit(0);
	}

	protected void setCaptureButton() {
		// captureButton.setText(string);

		if (captureButton.isSelected()) {
			captureButton.setSelected(false);
		} else {
			captureButton.setSelected(true);
		}

	}

	/**
	 * 单例获取摄像头
	 * 
	 * @return
	 */
	public Camera getCameraInstance(int index) {
		releaseMediaRecorder();
		releaseCamera();
		Camera c = null;

		try {
			c = Camera.open(index);
		} catch (Exception e) {
			Log.i("camera", e.toString());

		}
		return c;
	}

	private boolean prepareVideoRecorder() {

		releaseMediaRecorder();
		releaseCamera();
		mCamera = getCameraInstance(Cofig4VideoRecord.CAMERAINDEX);
		Parameters param = mCamera.getParameters();
		// p.setPreviewSize(240, 160);
		param.setRotation(90);
		mCamera.setDisplayOrientation(90);
		// param.setPreviewFormat(PixelFormat.YCbCr_422_SP);
		// param.setPictureSize(213, 350);
		// param.setFlashMode(Parameters.FLASH_MODE_TORCH);
		mCamera.setParameters(param);

		mMediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		// Step 2: Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		if (Cofig4VideoRecord.CAMERAINDEX == Camera.CameraInfo.CAMERA_FACING_BACK) {
			mMediaRecorder.setOrientationHint(90);// 视频旋转90度
		} else {
			mMediaRecorder.setOrientationHint(270);// 视频旋转270度
		}

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		mMediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_480P));
		/*
		 * API Level 8以下的设置方法
		 * mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		 * mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		 * mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
		 */
		// Step 4: Set output file
		outputpath = Cofig4VideoRecord.getOutputMediaFile(
				Cofig4VideoRecord.MEDIA_TYPE_VIDEO).getAbsolutePath();
		mMediaRecorder.setOutputFile(outputpath);
		// mMediaRecorder.setVideoFrameRate(10);
		// mMediaRecorder.setVideoSize(240, 160);

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			// Log.d(TAG, "IllegalStateException preparing MediaRecorder: " +
			// e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			// Log.d(TAG, "IOException preparing MediaRecorder: " +
			// e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaRecorder(); // if you are using MediaRecorder, release it
								// first
		releaseCamera(); // release the camera immediately on pause event
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.reset(); // clear recorder configuration
			mMediaRecorder.release(); // release the recorder object
			mMediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}

	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // 释放摄像头
			mCamera = null;
		}
	}

	@Override
	protected void onDestroy() {
		// 释放资源
		super.onDestroy();
		releaseMediaRecorder();
		releaseCamera();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_capture:
			// get an image from the camera
			capture();

			break;
		case R.id.button_exit:

			// 退出应用
			VideoRecordActivkty.this.finish();
			System.exit(0);
			break;
		case R.id.right_button:

			if (Cofig4VideoRecord.CAMERAINDEX == Camera.CameraInfo.CAMERA_FACING_BACK) {
				Cofig4VideoRecord.CAMERAINDEX = Camera.CameraInfo.CAMERA_FACING_FRONT;
			} else {
				Cofig4VideoRecord.CAMERAINDEX = Camera.CameraInfo.CAMERA_FACING_BACK;
			}
			initCamera(Cofig4VideoRecord.CAMERAINDEX);

			break;

		case R.id.return_button:
			if (outputpath != null) {
				Intent it = new Intent();
				it.putExtra("RECORD", outputpath);
				setResult(Activity.RESULT_OK, it);
			} else {
				Intent it = new Intent();

				setResult(Activity.RESULT_CANCELED, it);
			}

			finish();
			break;

		case R.id.cancel:
			if (outputpath != null) {
				deleteFiles(outputpath);
			}

			break;
		default:
			break;
		}

	}

	private void capture() {
		if (isRecording) {
			timer.cancel(); // 停止录制时间显示
			timer = null;
			// stop recording and release camera
			mMediaRecorder.stop(); // stop the recording
			releaseMediaRecorder(); // release the MediaRecorder
									// object
			mCamera.lock(); // take camera access back from
							// MediaRecorder
			mCamera.stopPreview();
			mCamera.startPreview();

			// inform the user that recording has stopped
			setCaptureButton();

			isRecording = false;
		} else {
			// initialize video camera
			if (prepareVideoRecorder()) {
				// Camera is available and unlocked, MediaRecorder
				// is prepared,
				// now you can start recording

				mMediaRecorder.start();
				// inform the user that recording has started
				setCaptureButton();
				isRecording = true;
				timer = new Timer();

				timer.schedule(new mTimerTask(), 0, 1000);
			} else {
				// prepare didn't work, release the camera
				releaseMediaRecorder();
				// inform user
			}
		}
	}

	public void deleteFiles(String name) {

		File file = new File(name);
		if (file.isFile()) {
			file.delete();
			return;
		}

	}
}
