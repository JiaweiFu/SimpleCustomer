/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2015 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */

package com.hybris.datahub.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.datahub.api.event.DataHubEventListener;
import com.hybris.datahub.api.event.DataHubInitializationCompletedEvent;
import com.hybris.datahub.service.DataHubFeedService;
import com.hybris.datahub.validation.ValidationException;

/**
 * Creates a SAMPLE_EVENT_FEED after the initialization of the Data Hub.
 */
public class FeedCreationListener implements DataHubEventListener<DataHubInitializationCompletedEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(FeedCreationListener.class);

    private static final String EVENT_FEED_NAME = "DEFAULT_FEED";

    private DataHubFeedService feedService;

    @Override
    public void handleEvent(final DataHubInitializationCompletedEvent event)
    {
	logger.info("Checking for existing of sample event driven feed");
	if (feedService.findDataFeedByName(EVENT_FEED_NAME) == null)
	{
	    try
	    {
		logger.debug("Attempting to create sample event driven feed");
		feedService.createFeed(EVENT_FEED_NAME, SimpleCustomerEventDrivenPoolConstants.EVENT_POOL_NAME,
			"MANUAL", "MANUAL", "NAMED_POOL", "A test feed to demonstrate " +
				"event driven composition and publication");
	    } catch (final ValidationException e)
	    {
		logger.error("Error creating sample feed", e);
	    }
	}
    }

    @Override
    public Class<DataHubInitializationCompletedEvent> getEventClass()
    {
	return DataHubInitializationCompletedEvent.class;
    }

    @Override
    public boolean executeInTransaction()
    {
	return true;
    }

    @Required
    public void setFeedService(final DataHubFeedService feedService)
    {
	this.feedService = feedService;
    }
}
