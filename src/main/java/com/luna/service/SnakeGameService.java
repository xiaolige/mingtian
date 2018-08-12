package com.luna.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import javax.xml.ws.spi.http.HttpHandler;

import org.apache.commons.beanutils.BeanUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

import com.alibaba.fastjson.JSON;
import com.luna.snake.DrawingCommand;
import com.luna.snake.GameEvent;
import com.luna.snake.GameStatistics;
import com.luna.snake.IntegralInfo;
import com.luna.snake.SnakeGameEngine;
import com.luna.snake.VersionData;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

public class SnakeGameService {

	     private SnakeGameEngine gameEngine;
	     private ChannelGroup channels;
	     public SnakeGameService(){
	    	 gameEngine=new SnakeGameEngine(70,70,500);
	    	 channels=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	     }
	    public void run(){
	    	gameEngine.start();
	    	gameEngine.setListener(new SnakeGameEngine.SnakeGameListener(){

				@Override
				public void versionChange(VersionData changeData, VersionData currentData) {
				     versionChange(changeData,currentData);
					
				}

				@Override
				public void statusChange(GameStatistics statistics) {
					statusChange(statistics);
					
				}

				@Override
				public void noticeEvent(GameEvent[] events) {
					noticeEvent(events);
					
				}
	    		
	    	});
	    	 EventLoopGroup workerGroup = new NioEventLoopGroup();
	   	     EventLoopGroup bossGroup = new NioEventLoopGroup();

	        ServerBootstrap boot = new ServerBootstrap();
	        boot.group(bossGroup, workerGroup)
	            .channel(NioServerSocketChannel.class)
	            .childHandler(new ChannelInitializer<Channel>(){
	            	
	            	@Override
	                protected void initChannel(Channel ch) throws Exception {
	                    ChannelPipeline pipeline = ch.pipeline();
	                    pipeline.addLast("http-codec",new HttpServerCodec());
	                    pipeline.addLast("aggregator",new HttpObjectAggregator(65536));
	                    pipeline.addLast("http-chunked",new ChunkedWriteHandler());
	                    pipeline.addLast("http_request",new HttpRequestHandler("/ws"));
	                    pipeline.addLast("WebSoket-protocol",new WebSocketServerProtocolHandler("/ws"));
	                    pipeline.addLast("WebSocket-reqeust",new SnakeGameHandler(channels,gameEngine));   
	            	}

	            }).option(ChannelOption.SO_BACKLOG, 120)
	            .option(ChannelOption.SO_KEEPALIVE, true);

	        try {
	        	System.out.println("SnakeGameServer 启动了,端口:2048");
	            ChannelFuture f = boot.bind(2048).sync();
	            f.channel().closeFuture().sync();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }finally{
	           bossGroup.shutdownGracefully();
	           workerGroup.shutdownGracefully();
	           System.out.println("SnakeGameServer 关闭了");
	        }

	    }
	    
	    private void sendVersionData(VersionData data){
	    	try{
	    	   VersionData copy=new VersionData();
				BeanUtils.copyProperties(data, copy);
	    	  String str=JSON.toJSONString(data);
	    	  String prefix="version\r\n";
	    	  String[] cmds,cmdDatas;
	    	  for(Channel channel:channels){
	    		  DrawingCommand cmd=gameEngine.getDrawingCommand(channel.id().asShortText());
	    		  if(cmd!=null){
	    		    cmds=Arrays.copyOf(data.getCmds(), data.getCmds().length+1);
	    		     cmds[cmds.length-1]=cmd.getCmd();
	    		     cmdDatas=Arrays.copyOf(data.getCmdDatas(), data.getCmdDatas().length+1);
	    		     cmdDatas[cmdDatas.length-1]=cmd.getCmdData();
	    		     copy.setCmds(cmds);
	    		     copy.setCmdDatas(cmdDatas);
	    		     channel.writeAndFlush(new TextWebSocketFrame(prefix)+JSON.toJSONString(copy)); 
	    		  }else{
	    			  channel.writeAndFlush(new TextWebSocketFrame(str));
	    		  }
	    	  }
	    	}catch(Exception e){
	    		
	    	}
	    }
	    private void sendEvent(GameEvent[] events){
	    	String prefix="event/r/n";
	    	for(Channel channel:channels){
	    		for(GameEvent event:events){
	    			if(event.getAccountId()==null||
	    					event.getAccountId().equals(channel.id().asShortText())){
	    				channel.writeAndFlush(new TextWebSocketFrame(prefix+JSON.toJSONString(event)));
	    			}
	    		}
	    	}
	    }
	    private void sendStatusData(GameStatistics statistics){
	    	String prefix="status\r\n";
	    	for(Channel channel: channels){
	    		IntegralInfo info= gameEngine.getIntegeralInfoByAccountId(channel.id().asShortText());
	    		statistics.setCurrent(info);
	    		channel.writeAndFlush(new TextWebSocketFrame(prefix+JSON.toJSONString(statistics)));
	    	}
	    }

	    public static void main(String[] args) {    
	    	
	       new SnakeGameService().run();
	    }

	}
