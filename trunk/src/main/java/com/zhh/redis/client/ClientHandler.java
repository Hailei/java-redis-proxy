package com.zhh.redis.client;


import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.zhh.redis.command.RedisReply;
import com.zhh.redis.command.RedisRequest;

public class ClientHandler extends SimpleChannelHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		
		RedisConnection connection = (RedisConnection)ctx.getChannel().getAttachment();
		RedisRequest request = connection.pollRequest();
		request.getCallback().equals((RedisReply)e.getMessage());
        
	}
	
	 @Override
     public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
                     throws Exception {
             Object p = e.getMessage();
             if(p instanceof RedisRequest){
            	 RedisRequest request = (RedisRequest)e.getMessage();
                     Channel ch = ctx.getChannel();
                     RedisConnection redisConn = (RedisConnection)ch.getAttachment();
                    redisConn.addRequest(request);
             }        
             super.writeRequested(ctx, e);
     }
	
	

	
}
