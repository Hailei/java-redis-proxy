/**
 * 
 */
package com.zhh.redis.server;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhh.redis.command.BulkReply;
import com.zhh.redis.command.ErrorReply;
import com.zhh.redis.command.ProtoUtil;
import com.zhh.redis.command.RequestCommand;
import com.zhh.redis.command.ShutDownCommand;
import com.zhh.redis.command.StatusReply;



public class ServerHandler extends SimpleChannelHandler {
	static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
	public ServerHandler(String engine){
	  
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		//命令分发
		//TODO  分发的逻辑可以抽取出来以便使用多种transport
		Object command = e.getMessage();

		
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
