package com.zhh.redis.client;


import org.jboss.netty.channel.Channel;

import com.zhh.redis.command.RedisReply;

public class DefaultRedisConnectionCallback  implements  RedisConnectionCallback {

	private Channel channel;
	
	
	public DefaultRedisConnectionCallback(Channel channel){
		this.channel = channel;
	}
	public void handleReply(RedisReply reply) {
		channel.write(reply);
		
	}

}
