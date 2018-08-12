package com.luna.snake;

public class GameEvent {
	 private String accountId;
     private EventType eventType;
     private String description;
	
	public GameEvent(EventType eventType, String description) {
		this.eventType = eventType;
		this.description = description;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	
	public enum EventType{
    	die
    }
}
