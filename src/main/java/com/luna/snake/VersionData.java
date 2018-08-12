package com.luna.snake;

public class VersionData {
   private long version;
   private long time;
   private Boolean full;
   private String cmds[];
   private String cmdDatas[];
public VersionData(long version, long time) {
	this.version = version;
	this.time = time;
	this.full=false;
}
public VersionData() {
	// TODO Auto-generated constructor stub
}
public long getVersion() {
	return version;
}
public void setVersion(long version) {
	this.version = version;
}
public long getTime() {
	return time;
}
public void setTime(long time) {
	this.time = time;
}
public Boolean getFull() {
	return full;
}
public void setFull(Boolean full) {
	this.full = full;
}
public String[] getCmds() {
	return cmds;
}
public void setCmds(String[] cmds) {
	this.cmds = cmds;
}
public String[] getCmdDatas() {
	return cmdDatas;
}
public void setCmdDatas(String[] cmdDatas) {
	this.cmdDatas = cmdDatas;
}
}
