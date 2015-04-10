/**
 * 
 */
package com.zhh.redis.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhh.redis.client.RedisClient;
import com.zhh.redis.protocol.RedisDecoderV2;
import com.zhh.redis.protocol.RedisEncoder;


public class RedisServer {
	static final Logger LOGGER = LoggerFactory.getLogger(RedisServer.class);
	private ServerBootstrap bootstrap;
	/**
	 * @param port
	 */
	public RedisServer() {

		ChannelFactory factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		bootstrap = new ServerBootstrap(factory);
		bootstrap.setOption("child.tcpNoDelay", true);
		//bootstrap.setOption("child.keepAlive", true);
	}

	public void run(int port) {
		//这两个handler不涉及共享变量，所以可以所有的channel都可以共享
		final RedisEncoder redisEncoder = new RedisEncoder();
		RedisClient client = new RedisClient("127.0.0.1", 6379);
		final ServerHandler serverHandler = new ServerHandler(client);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new RedisDecoderV2());
				pipeline.addLast("handler", serverHandler);
				pipeline.addLast("encoder", redisEncoder);
				return pipeline;
			}
		});

		// TODO 暂时bind  anylocal  0.0.0.0 多网卡可以指定ip 
		bootstrap.bind(new InetSocketAddress(port));
//		new Thread(new Runnable() {
//			
//			public void run() {
//				while(true){
//					LOGGER.info(operator.getStats());
//					try {
//						Thread.sleep(30000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
	}

	public static void main(String[] args) {
		try {
		    if(args.length < 1){
		        System.out.println("Usage: start.sh  engine port");
		    }
		    int port = Integer.parseInt(args[0]);
			RedisServer server = new RedisServer();
	     	server.run(port);
			LOGGER.info("store server is started,listening port:" + port);
		} catch (Exception e) {
			LOGGER.error("error:",e);
			System.exit(1);
		}
	}
}
