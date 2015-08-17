package com.sap.simplecustomer.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.hybris.datahub.adapter.AdapterService;
import com.hybris.datahub.api.publication.PublicationException;
import com.hybris.datahub.domain.TargetItemMetadata;
import com.hybris.datahub.dto.item.ErrorData;
import com.hybris.datahub.dto.publication.PublicationResult;
import com.hybris.datahub.model.TargetItem;
import com.hybris.datahub.paging.DataHubPage;
import com.hybris.datahub.paging.DataHubPageable;
import com.hybris.datahub.paging.DefaultDataHubPageRequest;
import com.hybris.datahub.runtime.domain.PublicationActionStatusType;
import com.hybris.datahub.runtime.domain.TargetSystemPublication;
import com.hybris.datahub.service.PublicationActionService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.representation.Form;

public class SimpleCustomerAdapter implements AdapterService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SimpleCustomerAdapter.class);

    private final static String TARGET_SYSTEM_TYPE = "CECYaasCustomer";

    public final static String DEFAULT_BASE_URI = "https://api.yaas.io/hybris/oauth2/v2";
    public final static String CUSTOMER_BASE_URI = "https://api.yaas.io/hybris/customer/b1";
    private static String QUERY_PARAM_CLIENT_ID = "client_id";
    private static String QUERY_PARAM_CLIENT_SECRET = "client_secret";
    private static String QUERY_PARAM_GRANT_TYPE = "grant_type";
    private static String QUERY_PARAM_HYBRIS_TENANT = "hybris-tenant";
    private static String QUERY_PARAM_SCOPE = "scope";

    @Inject
    private PublicationActionService publicationActionService;

    private static final int PAGE_SIZE = 20;

    public void setPublicationActionService(PublicationActionService _publicationActionService) {
	this.publicationActionService = _publicationActionService;
    }

    public PublicationActionService getPublicationActionService() {
	return this.publicationActionService;
    }

    @Override
    public String getTargetSystemType() {
	// TODO Auto-generated method stub
	return TARGET_SYSTEM_TYPE;
    }

    @Override
    public void publish(final TargetSystemPublication targetSystemPublication, final String serverURL)
	    throws PublicationException {
	// TODO Auto-generated method stub

	List<ErrorData> errors = new ArrayList<ErrorData>();

	try {
	    String tenant = "cecenterrdspoc";
	    Form form = new Form();
	    form.add(QUERY_PARAM_CLIENT_ID, "0lwATcIhKwaP3jocPixym0RlkZtmQKdz");
	    form.add(QUERY_PARAM_CLIENT_SECRET, "tQnf9BWlWdCsybGA");
	    form.add(QUERY_PARAM_GRANT_TYPE, "client_credentials");
	    form.add(QUERY_PARAM_HYBRIS_TENANT, tenant);
	    form.add(QUERY_PARAM_SCOPE, "FULL_ACCESS");

	    ClientConfig clientConfig = new DefaultClientConfig();

	    Client client = Client.create(clientConfig);
	    WebResource resource = client.resource(DEFAULT_BASE_URI + "/token");
	    ClientResponse response =
		    resource.queryParams(form).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(
			    MediaType.APPLICATION_JSON_TYPE).
			    header("Authorization", "").post(ClientResponse.class);

	    System.out.println(response.getStatus());

	    JSONObject resp = response.getEntity(JSONObject.class);

	    String authorization = resp.getString("token_type") + " " + resp.getString("access_token");
	    System.out.println(authorization);
	    logger.info(authorization);

	    for (TargetItemMetadata itemMetadata : targetSystemPublication.getTargetSystem().getTargetItemMetadata()) {
		logger.info("Get item type");
		final Class<? extends TargetItem> targetItemType = TargetItem.getItemClass(itemMetadata.getItemType());
		int pageNumber = 0;

		List<? extends TargetItem> items;
		do {
		    final DataHubPageable pageable = new DefaultDataHubPageRequest(pageNumber, PAGE_SIZE);
		    DataHubPage<? extends TargetItem> page = publicationActionService.findByPublication(
			    targetSystemPublication.getPublicationId(), targetItemType,
			    pageable);

		    items = page.getContent();
		    logger.info("item number:" + items.size());
		    for (final TargetItem targetItem : items) {
			// process actual target item
			logger.info("Target Item Type is: " + targetItem.getType());

			if (targetItem.getType().equalsIgnoreCase("TargetSimpleCustomer")) {
			    Client customerClient = Client.create();
			    WebResource customerResource = customerClient.resource(CUSTOMER_BASE_URI + "/" + tenant
				    + "/customers");

			    JSONObject customerRequest = new JSONObject();

			    logger.info(targetItem.getFields().toString());
			    logger.info("first name is: " + String.valueOf(targetItem.getField("firstName")));
			    if (targetItem.getField("company") instanceof String) {
				if (!String.valueOf(targetItem.getField("company")).isEmpty()) {
				    customerRequest.put("company",
					    String.valueOf(targetItem.getField("company")));
				}
			    }
			    if (targetItem.getField("contactPhone") instanceof String) {
				if (!String.valueOf(targetItem.getField("contactPhone")).isEmpty()) {
				    customerRequest.put("contactPhone",

					    String.valueOf(targetItem.getField("contactPhone")));
				}
			    }
			    if (targetItem.getField("preferredLanguage") instanceof String) {
				if (!String.valueOf(targetItem.getField("preferredLanguage")).isEmpty()) {
				    customerRequest.put("preferredLanguage",
					    String.valueOf(targetItem.getField("preferredLanguage")));
				}

			    }
			    if (targetItem.getField("firstName") instanceof String) {
				if (!String.valueOf(targetItem.getField("firstName")).isEmpty()) {
				    customerRequest.put("firstName",
					    String.valueOf(targetItem.getField("firstName")));
				}
			    }
			    if (targetItem.getField("contactEmail") instanceof String) {
				if (!String.valueOf(targetItem.getField("contactEmail")).isEmpty()) {
				    customerRequest.put("contactEmail",
					    String.valueOf(targetItem.getField("contactEmail")));
				}

			    }
			    if (targetItem.getField("lastName") instanceof String) {
				if (!String.valueOf(targetItem.getField("lastName")).isEmpty()) {
				    customerRequest.put("lastName", String.valueOf(targetItem.getField("lastName")));
				}

			    }
			    if (targetItem.getField("title") instanceof String) {
				if (!String.valueOf(targetItem.getField("title")).isEmpty()) {
				    customerRequest.put("title",
					    String.valueOf(targetItem.getField("title")));
				}
			    }
			    if (targetItem.getField("middleName") instanceof String) {
				if (!String.valueOf(targetItem.getField("middleName")).isEmpty()) {
				    customerRequest.put("middleName",
					    String.valueOf(targetItem.getField("middleName")));
				}
			    }
			    if (targetItem.getField("preferredCurrency") instanceof String) {
				if (!String.valueOf(targetItem.getField("preferredCurrency")).isEmpty()) {
				    customerRequest.put("preferredCurrency",
					    String.valueOf(targetItem.getField("preferredCurrency")));
				}
			    }
			    ClientResponse customerResponse = customerResource.type(MediaType.APPLICATION_JSON).accept(
				    MediaType.APPLICATION_JSON).
				    header("Authorization", authorization).post(ClientResponse.class, customerRequest);
			    logger.info(String.valueOf(customerResponse.getStatus()));
			    JSONObject custResp = customerResponse.getEntity(JSONObject.class);
			    logger.info("Response for posting customer: " + custResp.toString());
			    if (customerResponse.getStatus() != Status.CREATED.getStatusCode()) {
				logger.info("Status is not 201. Failed to post the customer!");
				errors.add(buildPublicationError(targetItem, new Exception()));
			    }
			    else {
			    }
			}

			else if (targetItemType.getName().equalsIgnoreCase("TargetSimpleAddress")) {

			}
		    }
		    pageNumber++;
		} while (!CollectionUtils.isEmpty(items));
	    }
	} catch (Exception e) {
	    errors.add(buildPublicationError(e));
	    logger.info(e.getMessage());
	    System.out.println(e.getMessage());
	}

	final PublicationResult publicationResult = new PublicationResult();
	publicationResult.setExportErrorDatas(errors);
	if (errors.isEmpty()) {
	    publicationActionService.completeTargetSystemPublication(targetSystemPublication.getPublicationId(),
		    PublicationActionStatusType.SUCCESS);
	}
	else {
	    publicationActionService.completeTargetSystemPublication(targetSystemPublication.getPublicationId(),
		    PublicationActionStatusType.COMPLETE_W_ERRORS);
	}

    }

    private ErrorData buildPublicationError(final TargetItem targetItem, final Exception e)
    {
	final ErrorData errorData = new ErrorData();
	errorData.setCanonicalItemId(targetItem.getCanonicalItem().getId());
	errorData.setMessage(e.getMessage());
	return errorData;
    }

    private ErrorData buildPublicationError(final Exception e)
    {
	final ErrorData errorData = new ErrorData();
	errorData.setCanonicalItemId((long) 0);
	errorData.setMessage(e.getMessage());
	return errorData;
    }

}