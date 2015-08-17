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

import com.hybris.datahub.api.event.DataHubEventListener;
import com.hybris.datahub.api.event.InitiatePublicationEvent;
import com.hybris.datahub.api.event.PublicationCompletedEvent;
import com.hybris.datahub.runtime.domain.PublicationAction;
import com.hybris.datahub.runtime.domain.TargetSystemPublication;
import com.hybris.datahub.service.EventPublicationService;
import com.hybris.datahub.service.PoolActionService;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Required;

public class PublicationCompletedEventListener implements DataHubEventListener<PublicationCompletedEvent>
{
	private PoolActionService poolActionService;
	private EventPublicationService eventPublicationService;

	@Override
	public void handleEvent(final PublicationCompletedEvent event)
	{
		final PublicationAction action = poolActionService.findPublicationAction(event.getActionId());
		for (final TargetSystemPublication publication : action.getTargetSystemPublications())
		{
			if (publication.getTargetSystem().getTargetSystemName().equals("TargetSystem1"))
			{
				final InitiatePublicationEvent nextPublicationEvent = new InitiatePublicationEvent(event.getPoolId(),
						new ArrayList<String>()
						{{
								add("TargetSystem2");
							}});
				eventPublicationService.publishEvent(nextPublicationEvent);
			}
		}
	}

	@Override
	public Class<PublicationCompletedEvent> getEventClass()
	{
		return PublicationCompletedEvent.class;
	}

	@Override
	public boolean executeInTransaction()
	{
		return true;
	}

	@Required
	public void setPoolActionService(final PoolActionService poolActionService)
	{
		this.poolActionService = poolActionService;
	}

	@Required
	public void setEventPublicationService(final EventPublicationService eventPublicationService)
	{
		this.eventPublicationService = eventPublicationService;
	}
}
