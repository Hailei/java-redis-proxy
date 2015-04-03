package com.zhh.redis.command;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;

public  class ArrayReply extends AbstractRedisReply {
   protected List<RedisReply> arry  = new ArrayList<RedisReply>();
   private int count;
   
   public ArrayReply(int count){
	   this.count = count;
   }
   public void  doEncode(ChannelBuffer buffer){
	   
	   buffer.writeBytes(ProtoUtil.convertIntToByteArray(arry.size()));
	   writeCRLF(buffer);
	   for(RedisReply reply:arry){
		   ChannelBuffer replyBuffer = reply.encode();
		   buffer.writeBytes(replyBuffer);
	   }
   }
   
   public void setCount(int count){
	   this.count = count;
   }
   
   public void addReply(RedisReply reply){
	   arry.add(reply);
   }
   
   public boolean complete(){
	   return arry.size() == count;
   }
   
}
