package com.luna.snake;

public class DrawingCommand {
     private String cmd;
     private String cmdData;
     private String color;
     private Integer x,y;
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public DrawingCommand(String color, Integer x, Integer y) {
		this.color=color;
		this.x=x;
		this.y=y;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getCmdData() {
		return cmdData;
	}
	public void setCmdData(String cmdData) {
		this.cmdData = cmdData;
	}
}
