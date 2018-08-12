package com.luna.snake;

public class DrawingCommand {
     private String cmd;
     private String cmdData;
	public DrawingCommand(String cmd, Integer x, Integer y) {
		this.cmd=cmd;
		this.cmdData=x+","+y;
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
