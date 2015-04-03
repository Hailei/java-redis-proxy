package com.zhh.redis.command;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.zhh.redis.client.RedisConnectionCallback;
import com.zhh.redis.protocol.RedisDecoderV2;

/**
 * 
 * 
 * @author Hailei
 *
 */
public class RequestCommand implements RedisCommand,RedisRequest {

	private int argCount;
	//private List<String> args;
	private List<byte[]> args;
	private RedisConnectionCallback callback;
	
	public RequestCommand(){
		
	}

	public List<byte[]> getArgs() {
		return args;
	}

	public void setArgs(List<byte[]> args) {
		this.args = args;
	}

	public int getArgCount() {
		return argCount;
	}
	
	public void init(int argCount){
		//TODO 边缘判断
		setArgCount(argCount);
		this.args = new ArrayList<byte[]>(argCount);
	}

	public void setArgCount(int argCount) {
		this.argCount = argCount;
	}
	
	public boolean needMoreArg(){
		return this.args.size() < this.argCount;
	}
	
	public void addArg(byte[] arg){
		args.add(arg);
	}

	@Override
	public String toString() {
		return "RequestCommand [argCount=" + argCount + ", args=" + args + "]";
	}
	
	public void writeCRLF(ChannelBuffer buffer){
		buffer.writeByte(RedisDecoderV2.CR_BYTE);
		buffer.writeByte(RedisDecoderV2.LF_BYTE);
	}

	public ChannelBuffer encode() {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte((byte)'*');
		buffer.writeBytes(ProtoUtil.convertIntToByteArray(args.size()));
		writeCRLF(buffer);
		for(byte[] arg:args){
			buffer.writeByte((byte)'$');
			buffer.writeBytes(ProtoUtil.convertIntToByteArray(arg.length));
			writeCRLF(buffer);
			buffer.writeBytes(arg);
			writeCRLF(buffer);
		}
		
		return buffer;
	}

	public RedisConnectionCallback getCallback() {
		return callback;
	}

	public void setCallback(RedisConnectionCallback callback) {
         this.callback = callback;		
	}
	
	
	
}
