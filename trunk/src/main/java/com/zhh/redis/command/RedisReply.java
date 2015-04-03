package com.zhh.redis.command;

import org.jboss.netty.buffer.ChannelBuffer;

public interface RedisReply {

	public enum Type{
		ERROR((byte)'-'),
		STATUS((byte)'+'),
		BULK((byte)'$'),
		INTEGER((byte)':'),
		ARRAY((byte)'*');
		private byte code;
		private Type(byte code){
			this.code = code;
		}
		public byte getCode(){
			return code;
		}
	}
	public Type getType();
	public void setType(Type type);
	public ChannelBuffer encode();
}
