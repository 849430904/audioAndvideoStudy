package com.dongnaoedu.live.params;

/**
 * 视频数据参数
 * 
 * @author Jason QQ: 1476949583
 * @date 2016年11月13日
 * @version 1.0
 */
public class VideoParam {

	private int width;
	private int height;
	private int cameraId;

	public VideoParam(int width, int height, int cameraId) {
		super();
		this.width = width;
		this.height = height;
		this.cameraId = cameraId;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

}
