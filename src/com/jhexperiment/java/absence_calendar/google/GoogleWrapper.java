package com.jhexperiment.java.absence_calendar.google;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import com.google.gdata.client.appsforyourdomain.AppsGroupsService;
import com.google.gdata.data.appsforyourdomain.generic.GenericEntry;
import com.google.gdata.data.appsforyourdomain.generic.GenericFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * Wrapper enum to access Google Apps domain.
 * 
 * @author jhxmonkey
 *
 */
public enum GoogleWrapper {
	INSTANCE;
	
	/**
	 * Get the list of groups for a Google Apps domain.
	 * @param googleProps Properties containing Google Apps connection information.
	 * @return A list of Group IDs/Emails
	 * @throws AuthenticationException
	 * @throws ServiceException
	 * @throws IOException
	 */
	public ArrayList<String> getGroupList(Properties googleProps) 
			throws AuthenticationException, ServiceException, IOException {
		
		ArrayList<String> groupList = new ArrayList<String>();
	    String adminEmail = googleProps.getProperty("adminEmail");
        String adminPassword = googleProps.getProperty("adminPassword");
        String domain = googleProps.getProperty("domain");
        String appName = googleProps.getProperty("appName");
		AppsGroupsService groupService = new AppsGroupsService(adminEmail, adminPassword, domain, appName);
		GenericFeed groupsFeed = null;
		Iterator<GenericEntry> groupsEntryIterator = null;
		
		groupsFeed = groupService.retrieveAllGroups();
		groupsEntryIterator = groupsFeed.getEntries().iterator();
		
		while (groupsEntryIterator.hasNext()) {
		  groupList.add(groupsEntryIterator.next().getProperty(AppsGroupsService.APPS_PROP_GROUP_ID));
		}
		return groupList;
	}
	
	/**
	 * Gets the list of users for a given group ID/Name.
	 * 
	 * @param groupId ID of Google Apps domain group.
	 * @param googleProps Properties containing Google Apps connection information.
	 * @return A list of User IDs/Emails
	 * @throws FileNotFoundException
	 * @throws AuthenticationException
	 * @throws ServiceException
	 * @throws IOException
	 */
	public ArrayList<String> getUserList(String groupId, Properties googleProps) 
			throws FileNotFoundException, AuthenticationException, ServiceException, IOException {
		
		ArrayList<String> userList = new ArrayList<String>();
		String adminEmail = googleProps.getProperty("adminEmail");
        String adminPassword = googleProps.getProperty("adminPassword");
        String domain = googleProps.getProperty("domain");
        String appName = googleProps.getProperty("appName");        
		AppsGroupsService groupService = new AppsGroupsService(adminEmail, adminPassword, domain, appName);
		GenericFeed groupsFeed = null;
		Iterator<GenericEntry> groupsEntryIterator = null;
		
		groupsFeed = groupService.retrieveAllMembers(groupId);
	    groupsEntryIterator = groupsFeed.getEntries().iterator();
	    
	    while (groupsEntryIterator.hasNext()) {
	    	userList.add(groupsEntryIterator.next().getProperty(AppsGroupsService.APPS_PROP_GROUP_MEMBER_ID));
	    }
	    
		return userList;
	}
}
