#include <stdio.h>
#include <stdlib.h>
typedef struct _AVPacket AVPacket;



struct _AVPacket{
	int stream_index;
	//����
	int *buf;
};

//ջ�ڴ�
AVPacket packet;

void main(){
	//���ڴ�
	AVPacket *p1 = (AVPacket*)malloc(sizeof(AVPacket));
	//AVPacket p;
	//AVPacket *p1 = &p;

	//ջ�ڴ�ṹ���Ա��ֵ
	packet.stream_index = 1;
	packet.buf = malloc(sizeof(int) * 3);
	packet.buf[0] = 5;
	packet.buf[1] = 7;
	packet.buf[2] = 10;

	//�����ṹ�������
	*p1 = packet;

	printf("%d\n", p1->stream_index);

	system("pause");
}

