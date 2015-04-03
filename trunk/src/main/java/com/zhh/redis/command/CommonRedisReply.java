package com.zhh.redis.command;

public abstract class CommonRedisReply  extends AbstractRedisReply {
	protected byte[] value;
	public  CommonRedisReply(Type type){
		super(type);
	}
	public void setValue(byte[] value){
		this.value = value;
	}
	
	
}
