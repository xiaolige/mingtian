package com.luna.snake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.luna.snake.SnakeEntity.Direction;
import com.luna.snake.SnakeEntity.State;

import static com.luna.snake.SnakeEntity.State.*;

public class SnakeGameEngine {
	 static final Logger logger=   LoggerFactory.getLogger(SnakeGameEngine.class);
	 
	 private final Mark[] mapsMarks;
	 private final  int refreshTime;
	 private final int mapWidth;
	 private final int mapHeight;
	 //计算地图状态角色和食物
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
				event.setAccountId(snake.getAccountId());
				eventQueue.addFirst(event);
				break;
			case die:
				 break;
			case offline:
				break;
			default:
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
				if(getMark(node).snakeNodes>1){//撞击自身
					snake.dying();
					killSnake(snake,node);
				}else if(getMark(node).footNode>0){//吃掉食物
					digestionFood(snake,node);
				}
			}
			changeNodes.addAll(snake.getAddNodes());
			changeNodes.addAll(snake.getRemoveNodes());
		}
		
		while(foods.size()<footMaxSize){
			Food food=grantFood();
			changeNodes.add(food.point);
		}
		if(!changeNodes.isEmpty()){
			long newVersion=currentVersion+1;
			//版本归档存储
			while(historyVersionData.size()>=historyVersionMax){
				historyVersionData.removeLast();
			}
			VersionData changeData=encodeVersion(newVersion,changeNodes);
           historyVersionData.addFirst(changeData);			
           currentVersion=newVersion;
           getCurrentMapData(true);
           
           //触发版本变更事件
           if(listener!=null){
        	   try{
        		   listener.versionChange(changeData, null);
        	   }catch(Exception e){
        		   logger.error("版本变更通知失败",e);
        	   }
           }
		}
	}
	
	
	public void setListener( SnakeGameListener listener){
		this.listener=listener;
	}
	public Long getCurrentVersion(){
		return currentVersion;
	}
	public VersionData getCurrentMapData(boolean check){
		if(currentMapData==null){
			currentMapData=encodeCurrentMapData();
		}else if(check&&currentMapData.getVersion()<currentVersion){
			currentMapData=encodeCurrentMapData();
		}
		return currentMapData;
	}
	private void afterBuild(){
		for(SnakeEntity snake:snakes.values()){
			snake.flush();
		}
	}
	public List<VersionData> getVersion(Long[] versionIds){
		List<VersionData>  list=new ArrayList<>();
		VersionData[] historys=historyVersionData.toArray(new VersionData[list.size()]);
		for(VersionData historyVersion:historys){
			for(long versionId:versionIds){
			  if(historyVersion.getVersion()==versionId){
				list.add(historyVersion);
			   }
			}
		}
		return list;
	};
	public IntegralInfo getIntegeralInfoByAccountId(String accountId){
		if(!snakes.containsKey(accountId)){
			return null;
		}
		SnakeEntity snake=snakes.get(accountId);
		IntegralInfo info=new IntegralInfo();
		info.setLastVersion(currentVersion);
		info.setGameName(snake.getGameName());
		info.setAccountId(snake.getAccountId());
		info.setDieIntegral(snake.getDieIntegral());
		info.setKillIntegral(snake.getKillIntegral());
		return info;
	}
	public DrawingCommand getDrawingCommand(String accountId){
		DrawingCommand cmd=null;
		if(!snakes.containsKey(accountId)){
			return cmd;
		}
		SnakeEntity snake=snakes.get(accountId);
		Integer[] head=snake.getHead();
		if(head!=null){
			cmd= new DrawingCommand("Lime",head[1],head[0]);
		}
		return cmd;
	}
	
	public  List<IntegralInfo> getRankingList(){
		List<IntegralInfo> result=new ArrayList<>();
		List<SnakeEntity> list=new ArrayList<>();
		list.addAll(snakes.values());
		Collections.sort(list,(Comparator<SnakeEntity>)(o1,o2)->{
			return o1.getKillIntegral()-o2.getKillIntegral();
		});
		for(SnakeEntity snake:list){
			IntegralInfo integeralInfo=new IntegralInfo();
			integeralInfo.setAccountId(snake.getAccountId());
			integeralInfo.setDieIntegral(snake.getDieIntegral());
			integeralInfo.setKillIntegral(snake.getKillIntegral());
			integeralInfo.setGameName(snake.getGameName());
			integeralInfo.setLastVersion(currentVersion);
			result.add(integeralInfo);
		}
		return result;
	}
	public void doResurgence(String accountId){
		if(!snakes.containsKey(accountId)){
			this.logger.warn("角色复活失败找不到指定账户 ID:{}",accountId);
			return;
		}
		if(!snakes.get(accountId).isDie()){
			this.logger.warn("角色复活失败,必须为死亡状态 ID:{}",accountId);
			return;
		}
		int max=Math.min(mapWidth, mapHeight);
		int min=0;
		Random random=new Random();
		int startPoint=random.nextInt(max-min+1)+min;
		snakes.get(accountId).resurgence(startPoint,3);
		this.logger.info("角色复活 ID：{},出生地点:{},初始节点:{}",accountId,startPoint,3);
	}
	
	public boolean isMapRange(Integer[] point){
		return point[0]>=0&&point[0]<mapHeight&&point[1]>=0&&point[1]<mapWidth;
	}

	private Mark getMark(int index){
		if(mapsMarks[index]==null){
			mapsMarks[index]=new Mark();
		}
		return mapsMarks[index];
	}
	
	public ArrayList<Integer[]> getAllPoint(){
		ArrayList<Integer[]> allPoints=new ArrayList<>(2000);
		Integer x,y;
		for(int i=0;i<mapsMarks.length;i++){
			if(mapsMarks[i]!=null&& !mapsMarks[i].isEmpty()){
				x=i%mapWidth;
				y=i/mapWidth;
				
				allPoints.add(new Integer[]{y,x});
			}
		}
		return allPoints;
	}
	public void killSnake(SnakeEntity die,Integer[] killPoint){
		List<SnakeEntity> list=getSnakeByNode(killPoint);
		list.remove(die);
		SnakeEntity killer=null;
		for(SnakeEntity snakeEntity:list){
			if(Arrays.equals(killPoint, snakeEntity.getHead())){
				killer=snakeEntity;
			
				continue;
				
			}
		}
		if(killer!=null){
			killer.addKillIntegral();
			logger.info("{}击杀{}",killer.getGameName(),die.getGameName());
		}
	}
	private List<SnakeEntity> getSnakeByNode(Integer[] node){
		List<SnakeEntity>  result=new ArrayList<>();
		for(SnakeEntity snake:snakes.values()){
			  for(Integer[] point:snake.getBodys()){
				 	 if(Arrays.equals(point, node)) {
				       result.add(snake);
					 break;
				 }
			  }
		}
		return result;
	}
		
		
	    public VersionData encodeCurrentMapData(){
	    	  StringBuilder body=new StringBuilder();
			  StringBuilder food=new StringBuilder();
			  StringBuilder remove=new StringBuilder();
			  List<Integer[]>  allPoint=getAllPoint();
			  Mark mark;
			  for(Integer[] p:allPoint){
				  mark=getMark(p);
				  if(mark==null||mark.isEmpty()){
					  remove.append(","+p[1]+","+p[0]);
				  }else if(mark.snakeNodes>0){
					  body.append(","+p[1]+","+p[0]);
				  }else if(mark.footNode>0){
					  food.append(","+p[1]+","+p[0]);
				  }
			  }
			  List<String> cmds=new ArrayList<>();
			  List<String> cmdDatas=new ArrayList<>();
			  if(body.length()>0){
				  body.deleteCharAt(0);
				  cmds.add("Green");
				  cmdDatas.add(body.toString());
			  }
			  if(food.length()>0){
				  food.deleteCharAt(0);
				  cmds.add("Yellow");
				  cmdDatas.add(food.toString());
			  }
			  if(remove.length()>0){
				  remove.deleteCharAt(0);
				  cmds.add("Black");
				  cmdDatas.add(remove.toString());
			  }
			  VersionData vd=new VersionData(currentVersion,System.currentTimeMillis());
			  vd.setCmds(cmds.toArray(new String[cmds.size()]));
			  vd.setCmdDatas(cmdDatas.toArray(new String[cmdDatas.size()]));
			  vd.setFull(true);
			  
			  return vd;
	    }
		public VersionData encodeVersion(long version,ArrayList<Integer[]> changePoints){
			  StringBuilder body=new StringBuilder();
			  StringBuilder food=new StringBuilder();
			  StringBuilder remove=new StringBuilder();
			  
			  Mark mark;
			  for(Integer[] p:changePoints){
				  mark=getMark(p);
				  if(mark==null||mark.isEmpty()){
					  remove.append(","+p[1]+","+p[0]);
				  }else if(mark.snakeNodes>0){
					  body.append(","+p[1]+","+p[0]);
				  }else if(mark.footNode>0){
					  food.append(","+p[1]+","+p[0]);
				  }
			  }
			  List<String> cmds=new ArrayList<>();
			  List<String> cmdDatas=new ArrayList<>();
			  if(body.length()>0){
				  body.deleteCharAt(0);
				  cmds.add("Green");
				  cmdDatas.add(body.toString());
			  }
			  if(food.length()>0){
				  food.deleteCharAt(0);
				  cmds.add("Yellow");
				  cmdDatas.add(food.toString());
			  }
			  if(remove.length()>0){
				  remove.deleteCharAt(0);
				  cmds.add("Black");
				  cmdDatas.add(remove.toString());
			  }
			  VersionData vd=new VersionData(version,System.currentTimeMillis());
			  vd.setCmds(cmds.toArray(new String[cmds.size()]));
			  vd.setCmdDatas(cmdDatas.toArray(new String[cmdDatas.size()]));
			  vd.setFull(false);
			  
			  return vd;
		}
	public class Mark{
		public int snakeNodes=0;
		public int footNode=0;
		private boolean isEmpty(){
			return snakeNodes<=0&&footNode<=0;
		}
	}
	
	
	public class Food{
		private Integer[] point;
		private Integer type;
	public Food(Integer[] point,int type){
		this.point=point;
		this.type=type;
	}
	}
	
	public static interface SnakeGameListener{
		public void versionChange(VersionData changeData,VersionData currentData);
		public void statusChange(GameStatistics statistics);
		public void noticeEvent(GameEvent[] events);
	}

	public void newSnake(String accountId, String gameName) {
		
		
		int max=Math.min(mapWidth, mapHeight);
		int min=0;
		Random random=new Random();
		int startPoint=random.nextInt(max-min+1)+min;
		SnakeEntity snake=new SnakeEntity(this,accountId,startPoint,1,Direction.up);
		snake.setState(State.inactive);
		snake.setGameName(gameName);
		getMark(startPoint);
		mapsMarks[startPoint].snakeNodes++;
		snake.getBodys().add(new Integer[]{startPoint%mapWidth,startPoint/mapWidth});
		snakes.put(accountId, snake);
		
	}
	public void controlSnake(String accountId, int keyCode) {
           SnakeEntity  snake=snakes.get(accountId);
           if(37== keyCode){
              snake.setDirection(Direction.left);
           }else if(38== keyCode){
        	   snake.setDirection(Direction.up);
           }else if(39== keyCode){
        	   snake.setDirection(Direction.right);
           }else if(40== keyCode){
        	   snake.setDirection(Direction.down);
           }
	}
	
	public void digestionFood(SnakeEntity snake,Integer[] node){
		snake.add(0,node);
		
	};
	public  Food grantFood(){
		int releasePoint =-1;
		Random random=new Random();
		int start=random.nextInt(mapHeight*mapWidth-5)+4;
		int nextCount=random.nextInt(50);
		for(int i=start,n=0,m=0;i<mapsMarks.length&&m<mapsMarks.length;i++,m++){
			if(mapsMarks[i]==null||mapsMarks[i].isEmpty()){
				n++;
				releasePoint=i;
				if(n>=nextCount){
					break;
				}
			}
		}
		getMark(releasePoint);
		mapsMarks[releasePoint].footNode++;
		Integer[]  point=new Integer[]{releasePoint%mapWidth,releasePoint/mapWidth};
		Food food=new Food(point,1);
		return food;
		
	}
	public  Mark getMark(Integer[] point){
		
	     Integer index=point[1]*mapWidth+point[0];
		return getMark(index);
	}
     
     
}
