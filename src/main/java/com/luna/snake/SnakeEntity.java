package com.luna.snake;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnakeEntity {
	 static final Logger logger=   LoggerFactory.getLogger(SnakeEntity.class);
	 private Direction direction;
	 private List<Integer[]>  bodys=new Lin
			 kedList<>();
	 private Long accountId;
	 private String gameName;
	public Long getAccountId() {
		return accountId;
	}
	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	public String getGameName() {
		return gameName;
	}
	public void setGameName(String gameName) {
		this.gameName = gameName;
	}
	private ArrayList<Integer[]>  removeNodes=new ArrayList<>();
	private ArrayList<Integer[]>  addNodes=new ArrayList<>();
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
		logger.info("id:{} name:{}",accountId,gameName);
	}
	public void alive(){
		this.state=State.alive;
	}
	public void die(){
		int bodySize=bodys.sie();
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
	private int dieIntegral;
	private int killIntegral=0;
	
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
	public State state;
	public  enum State{
		inactive,alive,grow,dying,die,offline
	}
    public enum Direction{
    	up,down,left,right;
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
    private void add(int index,Integer[] point){
    	bodys.add(index,point);
    	if(engine.isMapRange(point)){
    		engine.getMark(point).snakeNodes++;
    		addNodes.add(point);
    	}
    	logger.info("添加结点 x:{},y:{}",point[1],point[0]);
    }
}
