package com.luna.snake;

public class IntegralInfo {

	 private String accountId;
	 private String gameName;
	 private int dieIntegral;
	 private Long lastVersion;
		public Long getLastVersion() {
		return lastVersion;
	}
	public void setLastVersion(Long lastVersion) {
		this.lastVersion = lastVersion;
	}
		private int killIntegral=0;
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
}
