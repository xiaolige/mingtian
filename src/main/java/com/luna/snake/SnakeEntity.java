package com.luna.snake;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeEntity {
	 static final Logger logger=   LoggerFactory.getLogger(SnakeEntity.class);
	 
	 private List<Integer[]>  bodys=new LinkedList<>();
	 private ArrayList<Integer[]>  removeNodes=new ArrayList<>();
		private ArrayList<Integer[]>  addNodes=new ArrayList<>();
	 private String accountId;
	 private String gameName;
	 private Direction direction;
	 private SnakeGameEngine engine;
	 private int startPoint;
	 private int initBodySize;
	 
	 private int dieIntegral;
		private int killIntegral=0;
		 public State state;
	 public SnakeEntity(SnakeGameEngine engine,String accountId,int startPoint,int initBodySize,Direction direction){
		 this.engine=engine;
		 this.accountId=accountId;
		 this.direction=direction;
		 state=State.inactive;
		 this.startPoint=startPoint;
		 this.initBodySize=initBodySize;
	 }
	
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getGameName() {
		return gameName;
	}
	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	
	public ArrayList<Integer[]> getAddNodes() {
		return addNodes;
	}
	public void setAddNodes(ArrayList<Integer[]> addNodes) {
		this.addNodes = addNodes;
	}
	public int addKillIntegral(){
		return ++killIntegral;
	}
	public void grow(){
		this.state=State.grow;
		logger.info("增长了id:{} name:{}",accountId,gameName);
	}
	
	public void active(){
		this.state=State.alive;
		logger.info("激活了id:{} name:{}",accountId,gameName);
	}
	
	public void alive(){
		this.state=State.alive;
	}
	public void die(){
		int bodySize=bodys.size();
		for(int i=0;i<bodys.size();i++){
			removeToTail();
		}
		this.state=State.die;
		dieIntegral++;
		logger.info("清除死亡角色id:{},name:{}",accountId,gameName);
	}
	public void dying(){
		this.state=State.dying;
		logger.info("角色死亡id:{},name:{}",accountId,gameName);
	}
	public void offline(){
		this.state=State.offline;
		logger.info("角色离线id:{},name:{}",accountId,gameName);
	}
	
	public ArrayList<Integer[]> getRemoveNodes() {
		return removeNodes;
	}
	public void setRemoveNodes(ArrayList<Integer[]> removeNodes) {
		this.removeNodes = removeNodes;
	}
	public int getDieIntegral() {
		return dieIntegral;
	}
	public void setDieIntegral(int dieIntegral) {
		this.dieIntegral = dieIntegral;
	}
	public int getKillIntegral() {
		return killIntegral;
	}
	public void setKillIntegral(int killIntegral) {
		this.killIntegral = killIntegral;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	
	public  enum State{
		inactive,alive,grow,dying,die,offline,active
	}
    public enum Direction{
    	up,down,left,right;
    }
    public void resurgence(int startPoint,int initBodySize){
    	if(!isDie()){
    		throw new RuntimeException("未达到复活条件，角色必须为死亡状态");
    	}
    	state=State.inactive;
    	this.startPoint=startPoint;
    	this.initBodySize=initBodySize;
    }
    public void setDirection(Direction direction){
    	if(this.direction==Direction.up&&direction==Direction.down){
    		return;
    	};
    	if(this.direction==Direction.down&&direction==Direction.up){
    		return;
    	};
    	if(this.direction==Direction.left&&direction==Direction.right){
    		return;
    	};
    	if(this.direction==Direction.right&&direction==Direction.left){
    		return;
    	};
    	logger.debug("改变snake方向 ID:{},当前方向:{}",accountId,direction.toString());
    	this.direction=direction;
    }
    public void moveStep(){
    	addToHead();
    	removeToTail();
    }
    public void removeToTail(){
    	Integer[] node=bodys.remove(bodys.size()-1);
    	if(engine.isMapRange(node)){
    		removeNodes.add(node);
    		engine.getMark(node).snakeNodes--;
    		logger.info("删除结点 x:{},y:{}",node[1],node[0]);
    	}
    	
    }
    public void addToHead(){
    	Integer[] first = bodys.get(0);
    	Integer[]  newFirst;
    	switch(direction){
    	case up:
    		newFirst= new Integer[]{first[0]-1,first[1]};
    		break;
    	case down:
    		newFirst= new Integer[]{first[0]+1,first[1]};
    		break;
    	case left:
    		newFirst= new Integer[]{first[0],first[1]-1};
    		break;
    	case right:
    		newFirst= new Integer[]{first[0],first[1]+1};
    		break;
    	default:
    		throw new RuntimeException("direction must not null");
    	
    	}
    	add(0,newFirst);
    }
    public List<Integer[]> getBodys() {
		return bodys;
	}

	public void setBodys(List<Integer[]> bodys) {
		this.bodys = bodys;
	}

	public SnakeGameEngine getEngine() {
		return engine;
	}

	public void setEngine(SnakeGameEngine engine) {
		this.engine = engine;
	}

	public Direction getDirection() {
		return direction;
	}

	public void add(int index,Integer[] point){
    	bodys.add(index,point);
    	if(engine.isMapRange(point)){
    		engine.getMark(point).snakeNodes++;
    		addNodes.add(point);
    	}
    	logger.info("添加结点 x:{},y:{}",point[1],point[0]);
    }
    public Boolean isDie(){
    	return this.state== State.die;
    }
    public Boolean isOffLine(){
    	return this.state==State.offline;
    }
    public void flush(){
    	addNodes.clear();
    	removeNodes.clear();
    }
    public Integer[] getHead(){
    	return this.bodys.get(0);
    }
}
