#include <stdio.h>
#include <stdlib.h>
typedef struct _AVPacket AVPacket;



struct _AVPacket{
	int stream_index;
	//数组
	int *buf;
};

//栈内存
AVPacket packet;

void main(){
	//堆内存
	AVPacket *p1 = (AVPacket*)malloc(sizeof(AVPacket));
	//AVPacket p;
	//AVPacket *p1 = &p;

	//栈内存结构体成员赋值
	packet.stream_index = 1;
	packet.buf = malloc(sizeof(int) * 3);
	packet.buf[0] = 5;
	packet.buf[1] = 7;
	packet.buf[2] = 10;

	//拷贝结构体的数据
	*p1 = packet;

	printf("%d\n", p1->stream_index);

	system("pause");
}

