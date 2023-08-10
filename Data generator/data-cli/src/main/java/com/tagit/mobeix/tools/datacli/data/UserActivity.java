package com.tagit.mobeix.tools.datacli.data;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class UserActivity {

	private String user;
	private ZonedDateTime date;
	private String activity;
	private String reference;
	private String status;
	
	private Geolocation geolocation;
	
	private ActivitySchema data; 
	
}
