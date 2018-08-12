package com.luna.snake;

import java.util.List;

public class GameStatistics {
   private Long lastVersion;
   private Integer onLineCount;
   private List<IntegralInfo> rankingList;
   private IntegralInfo  current;

public IntegralInfo getCurrent() {
	return current;
}
public void setCurrent(IntegralInfo current) {
	this.current = current;
}
public List<IntegralInfo> getRankingList() {
	return rankingList;
}
public void setRankingList(List<IntegralInfo> rankingList) {
	this.rankingList = rankingList;
}
public Long getLastVersion() {
	return lastVersion;
}
public void setLastVersion(Long lastVersion) {
	this.lastVersion = lastVersion;
}
public Integer getOnLineCount() {
	return onLineCount;
}
public void setOnLineCount(Integer onLineCount) {
	this.onLineCount = onLineCount;
}
}
