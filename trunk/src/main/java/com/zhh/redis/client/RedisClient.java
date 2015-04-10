package com.zhh.redis.client;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class RedisClient {

	private ClientBootstrap bootstrap;
	private String host;
	private int port;
	private RedisConnection conn;
	
	
	
	public RedisClient(String host,int port){
	       this.host = host;
	       this.port = port;
		  start();
	}
	
	public RedisConnection getConnection(){
		return conn;
	}
	
	
	public void   start(){
		
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool());
		bootstrap = new ClientBootstrap(factory);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("encoder", new RedisRequestEncoder());
				pipeline.addLast("decoder", new RedisRequestDecoder());
				pipeline.addLast("handler", new ClientHandler());
				return pipeline;
			}
		});
		Channel channel = bootstrap.connect(new InetSocketAddress(host,port)).awaitUninterruptibly().getChannel();
		conn = new RedisConnection(channel);
		channel.setAttachment(conn);
		 
	}
}
