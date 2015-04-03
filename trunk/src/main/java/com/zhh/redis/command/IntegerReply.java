package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;


public class IntegerReply extends CommonRedisReply {

	
	public IntegerReply(byte[] value){
		this();
		this.value = value;
	}
	public IntegerReply(){
		super(Type.INTEGER);
	}
	@Override
	public void doEncode(ChannelBuffer buffer) {
	
		buffer.writeBytes(value);
		writeCRLF(buffer);
	}

}
