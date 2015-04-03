package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

import com.zhh.redis.client.RedisConnectionCallback;

public interface RedisRequest {

	public ChannelBuffer encode();
	
	public RedisConnectionCallback getCallback();
	
	public void setCallback(RedisConnectionCallback callback);
}
