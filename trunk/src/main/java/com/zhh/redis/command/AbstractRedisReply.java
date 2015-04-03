package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.zhh.redis.protocol.RedisDecoderV2;

public abstract class AbstractRedisReply implements RedisReply { 
	private  Type type;
	
	public AbstractRedisReply(){
		
	}
	
	public Type getType(){
		return this.type;
	}
	
	public void setType(Type type){
		this.type = type;
	}
	public AbstractRedisReply(Type type){
		this.type = type;
	}
	public void writeCRLF(ChannelBuffer buffer){
		buffer.writeByte(RedisDecoderV2.CR_BYTE);
		buffer.writeByte(RedisDecoderV2.LF_BYTE);
	}
	
	
	
	public void writeStart(ChannelBuffer buffer){
		buffer.writeByte(type.getCode());
	}
	
public ChannelBuffer encode() {
		
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		writeStart(buffer);
	    doEncode(buffer);
		//writeCRLF(buffer);
		return buffer;
		}
	
	public abstract void  doEncode(ChannelBuffer buffer);
}
