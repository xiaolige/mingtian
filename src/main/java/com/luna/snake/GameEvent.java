package com.luna.snake;

public class GameEvent {
	 private Long accountId;
     private EventType eventType;
     private String description;
	
	public GameEvent(EventType eventType, String description) {
		this.eventType = eventType;
		this.description = description;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}
	
	public enum EventType{
    	die
    }
}
