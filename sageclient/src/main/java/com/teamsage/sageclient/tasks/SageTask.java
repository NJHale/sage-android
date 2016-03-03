package com.teamsage.sageclient.tasks;

public interface SageTask
{
	public byte[] runTask(long taskID, byte[] data);
}
