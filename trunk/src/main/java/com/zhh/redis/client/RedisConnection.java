package com.zhh.redis.client;

import java.util.concurrent.BlockingQueue;

import org.jboss.netty.channel.Channel;

import com.zhh.redis.command.RedisRequest;

public class RedisConnection {

	private Channel channel;
	private BlockingQueue<RedisConnectionCallback> queue;
	
	public RedisConnection(Channel channel){
		this.channel = channel;
	}
	
	public void write(RedisRequest request,RedisConnectionCallback callback){
		channel.write(request);
		queue.add(callback);
	}
}
