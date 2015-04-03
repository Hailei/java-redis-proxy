package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

public class BulkReply extends CommonRedisReply {

	private byte[] value;
	private int length;
	
	public BulkReply(byte[] value){
		this();
		this.value = value;
	}
	
	public BulkReply(){
		super(Type.BULK);
	}
	
	public void setValue(byte[] value){
		this.value = value;
	}
	
	public void setLength(int length){
		this.length = length;
	}

	public void doEncode(ChannelBuffer buffer) {
		
		buffer.writeBytes(ProtoUtil.convertIntToByteArray(value.length));
		writeCRLF(buffer);
		buffer.writeBytes(value);
		writeCRLF(buffer);
	}

	
}
