package com.luna.snake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.luna.snake.SnakeEntity.State.*;

public class SnakeGameEngine {
	 static final Logger logger=   LoggerFactory.getLogger(SnakeGameEngine.class);
	 
	 private final Mark mapsMarks[];
	 private final  int refreshTime;
	 private final int mapWidth;
	 private final int mapHeight;
	 
	 private ScheduledExecutorService executorsService=Executors.newScheduledThreadPool(2);
	 private ScheduledExecutorService stateService=Executors.newScheduledThreadPool(1);
	 private ScheduledFuture<?> mapFuture;
	 private SnakeGameListener listener;
	 private Long currentVersion=0L;
	 private volatile LinkedList<VersionData>  historyVersionData=new LinkedList();
	 private volatile VersionData currentMapData=null;
     private static final int historyVersionMax=20;
     private ArrayList<Food> foods=new ArrayList<>();
     private HashMap<String,SnakeEntity> snakes=new HashMap<>();
     private int footMaxSize=10;
     private ScheduledFuture<?>  stateFuture;
     private LinkedList<GameEvent>  eventQueue=new LinkedList<>();
	public SnakeGameEngine() {
		mapWidth=400;
		mapHeight=300;
		refreshTime=200;
		mapsMarks=new Mark[mapWidth*mapHeight];
	}
	public SnakeGameEngine(int mapWidth,int mapHeight,int refreshTime){
		this.mapWidth=mapWidth;
		this.mapHeight=mapHeight;
		this.refreshTime=refreshTime;
		mapsMarks=new Mark[mapWidth* mapHeight];
	}
	
	public void start(){
		logger.info("游戏引擎启动...");
		mapFuture=executorsService.scheduleWithFixedDelay(()->{
			gameTimeStep();
		},
				refreshTime, refreshTime, TimeUnit.MILLISECONDS);
		stateFuture =stateService.scheduleWithFixedDelay(()->{
			   fireStateChange();
			   fireNoticeEvent();
		},
				1000, 1000, TimeUnit.MILLISECONDS);
	}
	private void fireStateChange(){
		GameStatistics statistics=new GameStatistics();
		statistics.setLastVersion(currentVersion);
		statistics.setOnLineCount(snakes.size());
		statistics.setRankingList(getRankingList());
		listener.statusChange(statistics);
		
	}
	
	private void  fireNoticeEvent(){
		if(listener == null)
			return;
		if(eventQueue.isEmpty())
			return;
		GameEvent[] events=eventQueue.toArray(new GameEvent[eventQueue.size()]);
		listener.noticeEvent(events);
		for(GameEvent event: events){
			eventQueue.remove(event);
		}
	}
	public void gameTimeStep(){
		try{
			build();
		}catch(Throwable e){
			logger.error("地图构建异常",e);
		}finally{
			afterBuild();
		}
	}
	
	private void build(){
		for(SnakeEntity snake: snakes.values()){
			switch(snake.getState()){
			case  inactive:
				logger.info("激活新角色：{}，当前版本:{}",snake.toString(),currentVersion);
				snake.active();
				break;
			
			case alive:
			    snake.moveStep();
			    break;
			case grow:
				snake.addToHead();
				snake.alive();
				snake.moveStep();
				break;
			case dying:
				snake.die();
				GameEvent event =new GameEvent(GameEvent.EventType.die,"角色死亡");
				event.setAccountId(snake.getAccountId);
				eventQueue.addFirst(event);
				break;
			case die:
				 break;
			case offline:
				break;
			}
		}
		ArrayList<Integer[]>  changeNodes=new ArrayList<>();
		for(SnakeEntity snake: snakes.values()){
			if(snake.isOffLine())
				continue;
			if(!snake.isDie()&&!isMapRange(snake.getHead())){
				snake.dying();
			}
			for(Integer[] node:snake.getAddNodes()){
				if(getMark(node).snakeNodes>1){
					snake.dying();
					killSnake(snake,node);
				}else if(getMark(node).footNode>0){
					digestionFood(snake,node);
				}
			}
			changeNodes.addAll(snake.getAddNodes());
			changeNodes.addAll(snake.getRemoveNodes());
		}
	}
     
     
}
