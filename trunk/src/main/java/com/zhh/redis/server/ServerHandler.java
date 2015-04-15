/**
 * 
 */
package com.zhh.redis.server;


import java.io.IOException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhh.redis.client.DefaultRedisConnectionCallback;
import com.zhh.redis.client.RedisClient;
import com.zhh.redis.command.ErrorReply;
import com.zhh.redis.command.ProtoUtil;
import com.zhh.redis.command.RedisRequest;



public class ServerHandler extends SimpleChannelHandler {
	private RedisClient client;
	static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
	public ServerHandler(RedisClient client){
	  
		this.client = client;
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		//命令分发
		RedisRequest request = (RedisRequest)e.getMessage();
		Channel ch = ctx.getChannel();
		DefaultRedisConnectionCallback callback = new DefaultRedisConnectionCallback(
				ch);
		request.setCallback(callback);
        client.getConnection().write(request);
		
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		//TODO  管理channel 例如心跳 以及client list命令
		LOGGER.info(ctx.getChannel() + "had closed!");
		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Throwable cause = e.getCause();

		if (cause instanceof IOException) {
			String message = cause.getMessage();
			if (message != null && "Connection reset by peer".equals(message)) {
				LOGGER.warn("Client closed!",cause);
			} else {
				LOGGER.error("出错,关闭连接", cause);
			}
			e.getChannel().close();
		} else {
			LOGGER.error("出错", cause);
			e.getChannel().write(new ErrorReply(ProtoUtil.buildErrorReplyBytes("closed by upstream")));
		}
	}

}
