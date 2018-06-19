package com.dongnaoedu.dnffmpegplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.Surface;

/**
 * 视频播放的控制器
 * @author Jason
 * QQ: 1476949583
 * @date 2016年9月19日
 * @version 1.0
 */
public class JasonPlayer {

	public native void render(String input,Surface surface);
	
	public native void sound(String input,String output);
	
	
	/**
	 * 创建一个AudioTrac对象，用于播放
	 * @param nb_channels
	 * @return
	 */
	public AudioTrack createAudioTrack(int sampleRateInHz, int nb_channels){
		//固定格式的音频码流
		int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		Log.i("jason", "nb_channels:"+nb_channels);
		//声道布局
		int channelConfig;
		if(nb_channels == 1){
			channelConfig = android.media.AudioFormat.CHANNEL_OUT_MONO;
		}else if(nb_channels == 2){
			channelConfig = android.media.AudioFormat.CHANNEL_OUT_STEREO;
		}else{
			channelConfig = android.media.AudioFormat.CHANNEL_OUT_STEREO;
		}
		
		int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		
		AudioTrack audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, 
				sampleRateInHz, channelConfig, 
				audioFormat, 
				bufferSizeInBytes, AudioTrack.MODE_STREAM);
		//播放
		//audioTrack.play();
		//写入PCM
		//audioTrack.write(audioData, offsetInBytes, sizeInBytes);
		return audioTrack;
	}
	
	static{
		System.loadLibrary("avutil-54");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("postproc-53");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("avdevice-56");
		System.loadLibrary("myffmpeg");
	}
}
