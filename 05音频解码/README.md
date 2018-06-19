### [音频解码流程](https://www.cnblogs.com/CoderTian/p/6791638.html)

* 参考
	* [NDK FFmpeg解码音频 ](https://blog.csdn.net/qfanmingyiq/article/details/72993217)
	* [iOS 音视频开发：Audio Unit播放FFmpeg解码的音频](https://www.jianshu.com/p/0d5315bb81ee)

---

* dn_audio_player.c  音频解码并将解码后数据写入文件

````
#include "com_dongnaoedu_dnffmpegplayer_JasonPlayer.h"
#include <android/log.h>
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"jason",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"jason",FORMAT,##__VA_ARGS__);

#define MAX_AUDIO_FRME_SIZE 48000 * 4

//封装格式
#include "libavformat/avformat.h"
//解码
#include "libavcodec/avcodec.h"
//缩放
#include "libswscale/swscale.h"
//重采样
#include "libswresample/swresample.h"


//根据路径播放音频
JNIEXPORT void JNICALL Java_com_dongnaoedu_dnffmpegplayer_JasonPlayer_sound
  (JNIEnv *env, jobject jobj, jstring input_jstr, jstring output_jstr){
	const char* input_cstr = (*env)->GetStringUTFChars(env,input_jstr,NULL);
	const char* output_cstr = (*env)->GetStringUTFChars(env,output_jstr,NULL);
	LOGI("%s","sound");
	//注册组件
	av_register_all();
	AVFormatContext *pFormatCtx = avformat_alloc_context();
	//打开音频文件
	if(avformat_open_input(&pFormatCtx,input_cstr,NULL,NULL) != 0){
		LOGI("%s","无法打开音频文件");
		return;
	}
	//获取输入文件信息
	if(avformat_find_stream_info(pFormatCtx,NULL) < 0){
		LOGI("%s","无法获取输入文件信息");
		return;
	}
	//获取音频流索引位置
	int i = 0, audio_stream_idx = -1;
	for(; i < pFormatCtx->nb_streams;i++){
		if(pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO){
			audio_stream_idx = i;
			break;
		}
	}

	//获取解码器
	AVCodecContext *codecCtx = pFormatCtx->streams[audio_stream_idx]->codec;
	AVCodec *codec = avcodec_find_decoder(codecCtx->codec_id);
	if(codec == NULL){
		LOGI("%s","无法获取解码器");
		return;
	}
	//打开解码器
	if(avcodec_open2(codecCtx,codec,NULL) < 0){
		LOGI("%s","无法打开解码器");
		return;
	}
	//压缩数据
	AVPacket *packet = (AVPacket *)av_malloc(sizeof(AVPacket));
	//解压缩数据
	AVFrame *frame = av_frame_alloc();
	//frame转成：16bit 44100采样率 PCM 统一音频采样格式与采样率
	SwrContext *swrCtx = swr_alloc();

	//重采样设置参数-------------start
	//输入的采样格式
	enum AVSampleFormat in_sample_fmt = codecCtx->sample_fmt;
	//输出采样格式16bit PCM
	enum AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16;
	//输入采样率
	int in_sample_rate = codecCtx->sample_rate;
	//输出采样率
	int out_sample_rate = 44100;
	//获取输入的声道布局
	//根据声道个数获取默认的声道布局（2个声道，默认立体声stereo）
	//av_get_default_channel_layout(codecCtx->channels);
	uint64_t in_ch_layout = codecCtx->channel_layout;
	//输出的声道布局（立体声）
	uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;

    /**
     * 分配swrContext 如果需要可以设置/重置 普通参数、
     * 如果需要，分配SwrContext并设置/重置常见参数。
     * Allocate SwrContext if needed and set/reset common parameters.

     * 这个函数参数s可以不需要用swr_alloc()所分配的（简单说可以传入NULL），
     * 另一方面   use swr_alloc_set_opts()可以在swr_alloc()所分配
     * 的上下文中设置数据
     *
     *
     * @param s     现有可用的Swr context ，如果没有那么为NULL
     * @param out_ch_layout  输出通道布局 (AV_CH_LAYOUT_*)
     * @param out_sample_fmt  输出采样格式 (AV_SAMPLE_FMT_*).
     * @param out_sample_rate 输出采样率(frequency in Hz)
     * @param in_ch_layout   输入声道布局(AV_CH_LAYOUT_*)
     * @param in_sample_fmt  输入采样格式 (AV_SAMPLE_FMT_*).
     * @param in_sample_rate  输入采样率（比特率） (frequency in Hz)
     * @param log_offset      日志偏移
     * @param log_ctx         日志上下文可以为空
     *
     * @see swr_init(), swr_free()
     * @return 再错误的情况下返回空,否则分配上下文
     */
	swr_alloc_set_opts(swrCtx,
		  out_ch_layout,out_sample_fmt,out_sample_rate,
		  in_ch_layout,in_sample_fmt,in_sample_rate,
		  0, NULL);
	swr_init(swrCtx);

	//输出的声道个数
	int out_channel_nb = av_get_channel_layout_nb_channels(out_ch_layout);

	//重采样设置参数-------------end

	//16bit 44100 PCM 数据
	uint8_t *out_buffer = (uint8_t *)av_malloc(MAX_AUDIO_FRME_SIZE);

	FILE *fp_pcm = fopen(output_cstr,"wb");

	int got_frame = 0,index = 0, ret;
	//不断读取压缩数据
	while(av_read_frame(pFormatCtx,packet) >= 0){
		//解码
		ret = avcodec_decode_audio4(codecCtx,frame,&got_frame,packet);

		if(ret < 0){
			LOGI("%s","解码完成");
		}
		//解码一帧成功
		if(got_frame > 0){
			LOGI("解码：%d",index++);
			swr_convert(swrCtx, &out_buffer, MAX_AUDIO_FRME_SIZE,frame->data,frame->nb_samples);
			//获取sample的size
			int out_buffer_size = av_samples_get_buffer_size(NULL, out_channel_nb,
					frame->nb_samples, out_sample_fmt, 1);
			fwrite(out_buffer,1,out_buffer_size,fp_pcm);//将解码写的数据写入文件
		}

		av_free_packet(packet);
	}

	fclose(fp_pcm);
	av_frame_free(&frame);
	av_free(out_buffer);

	swr_free(&swrCtx);
	avcodec_close(codecCtx);
	avformat_close_input(&pFormatCtx);

	(*env)->ReleaseStringUTFChars(env,input_jstr,input_cstr);
	(*env)->ReleaseStringUTFChars(env,output_jstr,output_cstr);

}

````

* dn_video_player.c

````
#include "com_dongnaoedu_dnffmpegplayer_JasonPlayer.h"
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#define LOGI(FORMAT,...) __android_log_print(ANDROID_LOG_INFO,"jason",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT,...) __android_log_print(ANDROID_LOG_ERROR,"jason",FORMAT,##__VA_ARGS__);
#include "libyuv.h"

//封装格式
#include "libavformat/avformat.h"
//解码
#include "libavcodec/avcodec.h"
//缩放
#include "libswscale/swscale.h"



JNIEXPORT void JNICALL Java_com_dongnaoedu_dnffmpegplayer_JasonPlayer_render
  (JNIEnv *env, jobject jobj, jstring input_jstr, jobject surface){
	const char* input_cstr = (*env)->GetStringUTFChars(env,input_jstr,NULL);
	//1.注册组件
	av_register_all();

	//封装格式上下文
	AVFormatContext *pFormatCtx = avformat_alloc_context();

	//2.打开输入视频文件
	if(avformat_open_input(&pFormatCtx,input_cstr,NULL,NULL) != 0){
		LOGE("%s","打开输入视频文件失败");
		return;
	}
	//3.获取视频信息
	if(avformat_find_stream_info(pFormatCtx,NULL) < 0){
		LOGE("%s","获取视频信息失败");
		return;
	}

	//视频解码，需要找到视频对应的AVStream所在pFormatCtx->streams的索引位置
	int video_stream_idx = -1;
	int i = 0;
	for(; i < pFormatCtx->nb_streams;i++){
		//根据类型判断，是否是视频流
		if(pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO){
			video_stream_idx = i;
			break;
		}
	}

	//4.获取视频解码器
	AVCodecContext *pCodeCtx = pFormatCtx->streams[video_stream_idx]->codec;
	AVCodec *pCodec = avcodec_find_decoder(pCodeCtx->codec_id);
	if(pCodec == NULL){
		LOGE("%s","无法解码");
		return;
	}

	//5.打开解码器
	if(avcodec_open2(pCodeCtx,pCodec,NULL) < 0){
		LOGE("%s","解码器无法打开");
		return;
	}

	//编码数据
	AVPacket *packet = (AVPacket *)av_malloc(sizeof(AVPacket));

	//像素数据（解码数据）
	AVFrame *yuv_frame = av_frame_alloc();
	AVFrame *rgb_frame = av_frame_alloc();

	//native绘制
	//窗体
	ANativeWindow* nativeWindow = ANativeWindow_fromSurface(env,surface);
	//绘制时的缓冲区
	ANativeWindow_Buffer outBuffer;

	int len ,got_frame, framecount = 0;
	//6.一阵一阵读取压缩的视频数据AVPacket
	while(av_read_frame(pFormatCtx,packet) >= 0){
		//解码AVPacket->AVFrame
		len = avcodec_decode_video2(pCodeCtx, yuv_frame, &got_frame, packet);

		//Zero if no frame could be decompressed
		//非零，正在解码
		if(got_frame){
			LOGI("解码%d帧",framecount++);
			//lock
			//设置缓冲区的属性（宽、高、像素格式）
			ANativeWindow_setBuffersGeometry(nativeWindow, pCodeCtx->width, pCodeCtx->height,WINDOW_FORMAT_RGBA_8888);
			ANativeWindow_lock(nativeWindow,&outBuffer,NULL);

			//设置rgb_frame的属性（像素格式、宽高）和缓冲区
			//rgb_frame缓冲区与outBuffer.bits是同一块内存
			avpicture_fill((AVPicture *)rgb_frame, outBuffer.bits, PIX_FMT_RGBA, pCodeCtx->width, pCodeCtx->height);

			//YUV->RGBA_8888
			I420ToARGB(yuv_frame->data[0],yuv_frame->linesize[0],
					yuv_frame->data[2],yuv_frame->linesize[2],
					yuv_frame->data[1],yuv_frame->linesize[1],
					rgb_frame->data[0], rgb_frame->linesize[0],
					pCodeCtx->width,pCodeCtx->height);

			//unlock
			ANativeWindow_unlockAndPost(nativeWindow);

			usleep(1000 * 16);

		}

		av_free_packet(packet);
	}

	ANativeWindow_release(nativeWindow);
	av_frame_free(&yuv_frame);
	avcodec_close(pCodeCtx);
	avformat_free_context(pFormatCtx);

	(*env)->ReleaseStringUTFChars(env,input_jstr,input_cstr);
}


````
* JasonPlayer.java

````

public class JasonPlayer {

	public native void render(String input,Surface surface);
	
	public native void sound(String input,String output);//native
	
	/**
	 * 创建一个AudioTrac对象，用于播放
	 * @return
	 */
	public AudioTrack createAudioTrack(){
		int sampleRateInHz = 44100;
		int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
		//声道布局
		int channelConfig = android.media.AudioFormat.CHANNEL_OUT_STEREO;
		
		int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		AudioTrack audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, 
				sampleRateInHz, channelConfig, 
				audioFormat, 
				bufferSizeInBytes, AudioTrack.MODE_STREAM);
		//播放
		//audioTrack.play();
		//写入PCM
		//audioTrack.write(byte[]buffer);
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

````

* MainActivity.java

````
public class MainActivity extends Activity {

	private JasonPlayer player;
	private VideoView videoView;
	private Spinner sp_video;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		videoView = (VideoView) findViewById(R.id.video_view);
		sp_video = (Spinner) findViewById(R.id.sp_video);
		player = new JasonPlayer();
		//多种格式的视频列表
		String[] videoArray = getResources().getStringArray(R.array.video_list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, 
				android.R.id.text1, videoArray);
		sp_video.setAdapter(adapter);
	}

	public void mPlay(View btn){
		//String video = sp_video.getSelectedItem().toString();
		//String input = new File(Environment.getExternalStorageDirectory(),video).getAbsolutePath();
		//Surface传入到Native函数中，用于绘制
		//Surface surface = videoView.getHolder().getSurface();
		//player.render(input, surface);
		String input = new File(Environment.getExternalStorageDirectory(),"Justin Bieber - Boyfriend.mp3").getAbsolutePath();
		String output = new File(Environment.getExternalStorageDirectory(),"Justin Bieber - Boyfriend.pcm").getAbsolutePath();
		player.sound(input, output);
	}
}

````