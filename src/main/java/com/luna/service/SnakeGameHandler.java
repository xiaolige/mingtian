package com.luna.service;

import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.luna.snake.SnakeGameEngine;
import com.luna.snake.VersionData;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class SnakeGameHandler  extends 
        SimpleChannelInboundHandler<TextWebSocketFrame>{
	static final Logger logger=LoggerFactory.getLogger(SnakeGameHandler.class);
	private final ChannelGroup channels;
	private final SnakeGameEngine gameEngine;

	public SnakeGameHandler(ChannelGroup channels, SnakeGameEngine gameEngine) {
		this.channels = channels;
		this.gameEngine = gameEngine;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		Channel incoming=ctx.channel();
		logger.debug("�������� ��ַ��{}��id{},�ı�:{}",incoming.remoteAddress(),incoming.id().asShortText(),msg.text());
		String cmdText=msg.text().trim();
		int splitTindex;
		if((splitTindex=cmdText.indexOf(":"))<0){
			logger.error("�쳣ָ��:{}",cmdText);
			return;
		}
		String cmd=cmdText.substring(0, splitTindex);
		String cmdData=cmdText.substring(splitTindex+1);
		if(cmd.equals("JOIN")){
			gameEngine.newSnake(incoming.id().asShortText(),cmdData);
		}else if(cmd.equals("CONTROL")){
			gameEngine.controlSnake(incoming.id().asShortText(),Integer.parseInt(cmdData));
		}else if(cmd.equals("FULL")){
			String fullData=JSON.toJSONString(gameEngine.getCurrentMapData(false));
			fullData="version\r\n"+fullData;
			incoming.writeAndFlush(new TextWebSocketFrame(fullData));
		}else if(cmd.equals("QUANTITATIVE")){
			String[] vTexts=cmdData.split(",");
			Long versions[]=new Long[vTexts.length];
			for(int i=0;i<vTexts.length;i++){
				versions[i]=Long.parseLong(vTexts[i]);
			}
			for(VersionData s:gameEngine.getVersion(versions)){
				incoming.writeAndFlush(new TextWebSocketFrame("version/r/n"+JSON.toJSONString(s)));
			}
			
		}else if(cmd.equals("RESURGENCE")){
			gameEngine.doResurgence(incoming.id().asShortText());
		}
		
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel incoming=ctx.channel();
		logger.info("client:"+incoming.remoteAddress()+"����");
	
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Channel incoming=ctx.channel();
		logger.info("client:"+incoming.remoteAddress()+"�쳣",cause);
		ctx.close();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		Channel incoming=ctx.channel();
		logger.info("[SERVER] -"+incoming.remoteAddress()+"����");
		channels.add(incoming);
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		Channel incoming=ctx.channel();
		logger.info("client:"+incoming.remoteAddress()+"�뿪");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel incoming=ctx.channel();
		logger.info("client:"+incoming.remoteAddress()+"����");
	}


	
	
}
