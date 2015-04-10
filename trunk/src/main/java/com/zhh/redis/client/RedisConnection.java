package com.zhh.redis.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.channel.Channel;

import com.zhh.redis.command.RedisRequest;

public class RedisConnection {

	private Channel channel;
	private BlockingQueue<RedisRequest> queue;
	
	public RedisConnection(Channel channel){
		this.channel = channel;
		queue = new LinkedBlockingQueue<RedisRequest>();
	}
	
	public void write(RedisRequest request){
		channel.write(request);
	}
	
	public void addRequest(RedisRequest request){
		queue.add(request);
	}
	
	public RedisRequest pollRequest(){
		//TODO timeout
		return queue.poll();
	}
}
