---
layout: default
class: Analyzer
title: ee
summary: The name of the highest execution environment found in the current JAR
---
layout: default

	public String _ee(String args[]) {
		return getHighestEE().getEE();
	}

