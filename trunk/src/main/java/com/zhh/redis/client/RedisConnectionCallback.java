package com.zhh.redis.client;

import com.zhh.redis.command.RedisReply;

public interface RedisConnectionCallback {

	
	public void handleReply(RedisReply reply);
}
