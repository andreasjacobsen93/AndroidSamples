package com.duckduckgo.mobile.android.events.feedEvents;

import com.duckduckgo.mobile.android.events.Event;
import com.duckduckgo.mobile.android.objects.FeedObject;

public class FeedItemLongClickEvent extends Event {
	
	public FeedObject feedObject;

	public FeedItemLongClickEvent(FeedObject feedObject){
		this.feedObject = feedObject;
	}
	
}
