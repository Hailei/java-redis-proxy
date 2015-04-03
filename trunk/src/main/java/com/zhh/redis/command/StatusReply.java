package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

import com.zhh.redis.command.RedisReply.Type;

public class StatusReply extends CommonRedisReply {

//	public static final StatusReply OK = new StatusReply("OK");
//	private String message;
//	
//	public StatusReply(String message){
//		super(Type.STATUS);
//		this.message = message;
//	}
	
	public StatusReply(){
		super(Type.STATUS);
	}
	
	public StatusReply(byte[] value){
		this();
		this.value = value;
	}

	public void doEncode(ChannelBuffer buffer) {
		buffer.writeBytes(value);
		writeCRLF(buffer);
	}
	
	
	
}
