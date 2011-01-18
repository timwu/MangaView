package com.timwu.MangaView;

import java.util.Calendar;

import android.view.animation.Interpolator;

public class SimpleAnimation {
	private long startTime;
	private long duration;
	private float distance;
	private float from, to;
	private Interpolator interpolator;
	
	public SimpleAnimation(Interpolator interpolator, long duration, float from, float to) {
		this.interpolator = interpolator;
		this.duration = duration;
		this.distance = to - from;
		this.from = from;
		this.to = to;
		this.startTime = Calendar.getInstance().getTimeInMillis();
	}
	
	public float getCurrent() {
		if (isDone()) return to;
		long currentTime = Calendar.getInstance().getTimeInMillis();
		float completion = (currentTime - startTime) / (duration * 1.0f) ;
		return interpolator.getInterpolation(completion) * distance + from;
	}
	
	public boolean isDone() {
		return Calendar.getInstance().getTimeInMillis() >= startTime + duration;
	}
}
