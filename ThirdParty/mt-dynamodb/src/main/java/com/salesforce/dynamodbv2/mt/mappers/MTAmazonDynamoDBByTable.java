/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.dynamodbv2.mt.mappers;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.Record;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.amazonaws.services.dynamodbv2.streamsadapter.model.RecordAdapter;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.salesforce.dynamodbv2.mt.context.MTAmazonDynamoDBContextProvider;

/**
 * Allows for dividing tenants into their own tables by prefixing table names
 * with the multi-tenant context.
 *
 * The multi-tenant context is separated from the table name by the delimiter,
 * which is '.' by default.
 *
 * To use, call the static builder() method. The following parameters are
 * required ... - an AmazonDynamoDB instance - a multi-tenant context
 *
 * The following are optional arguments ... - delimiter: a String delimiter used
 * to separate the tenant identifier prefix from the table name
 * 
 * Supported: create|describe|delete Table, get|putItem, scan, query
 *
 * @author msgroi
 */
public class MTAmazonDynamoDBByTable extends MTAmazonDynamoDBBase {

	private static final Logger logger = LoggerFactory
			.getLogger(MTAmazonDynamoDBByTable.class);

	private final String delimiter;
	private final Optional<String> tablePrefix;

	public MTAmazonDynamoDBByTable(MTAmazonDynamoDBContextProvider mtContext,
			AmazonDynamoDB amazonDynamoDB, String delimiter,
			String tablePrefix) {
		this(mtContext, amazonDynamoDB, delimiter, Optional.of(tablePrefix));
	}

	private MTAmazonDynamoDBByTable(MTAmazonDynamoDBContextProvider mtContext,
			AmazonDynamoDB amazonDynamoDB, String delimiter,
			Optional<String> tablePrefix) {
		super(mtContext, amazonDynamoDB);
		this.delimiter = delimiter;
		this.tablePrefix = tablePrefix;
		logger.info("Delim: " + delimiter + ", tblPrefix: " + tablePrefix);
	}

	public CreateTableResult createTable(CreateTableRequest createTableRequest) {
		createTableRequest = createTableRequest.clone();
		createTableRequest
				.withTableName(buildPrefixedTablename(createTableRequest.getTableName()));
		logger.info("createTableRequest: " + createTableRequest);
		return getAmazonDynamoDB().createTable(createTableRequest);
	}

	public DeleteItemResult deleteItem(DeleteItemRequest deleteItemRequest) {
		deleteItemRequest = deleteItemRequest.clone();
		deleteItemRequest
				.withTableName(buildPrefixedTablename(deleteItemRequest.getTableName()));
		logger.info("deleteItemRequest: " + deleteItemRequest);
		return getAmazonDynamoDB().deleteItem(deleteItemRequest);
	}

	public DeleteTableResult deleteTable(DeleteTableRequest deleteTableRequest) {
		String virtualTableName = deleteTableRequest.getTableName();
		deleteTableRequest = deleteTableRequest.clone();
		deleteTableRequest
				.withTableName(buildPrefixedTablename(deleteTableRequest.getTableName()));
		logger.info("deleteTableRequest: " + deleteTableRequest);
		DeleteTableResult deleteTableResult = getAmazonDynamoDB()
				.deleteTable(deleteTableRequest);
		deleteTableResult.getTableDescription().setTableName(virtualTableName);
		return deleteTableResult;
	}

	public DescribeTableResult describeTable(DescribeTableRequest describeTableRequest) {
		String virtualTableName = describeTableRequest.getTableName();
		describeTableRequest = describeTableRequest.clone();
		describeTableRequest.withTableName(
				buildPrefixedTablename(describeTableRequest.getTableName()));
		logger.info("describeTableRequest: " + describeTableRequest);
		DescribeTableResult describeTableResult = getAmazonDynamoDB()
				.describeTable(describeTableRequest);
		describeTableResult.getTable().setTableName(virtualTableName);
		return describeTableResult;
	}

	public GetItemResult getItem(GetItemRequest getItemRequest) {
		getItemRequest = getItemRequest.clone();
		String prefixedTableName = buildPrefixedTablename(getItemRequest.getTableName());
		getItemRequest.withTableName(prefixedTableName);
		logger.info("getItemRequest: " + getItemRequest);
		return getAmazonDynamoDB().getItem(getItemRequest);
	}

	public PutItemResult putItem(PutItemRequest putItemRequest) {
		putItemRequest = putItemRequest.clone();
		putItemRequest
				.withTableName(buildPrefixedTablename(putItemRequest.getTableName()));
		logger.info("putItemRequest: " + putItemRequest);
		return getAmazonDynamoDB().putItem(putItemRequest);
	}

	public QueryResult query(QueryRequest queryRequest) {
		queryRequest = queryRequest.clone();
		queryRequest.withTableName(buildPrefixedTablename(queryRequest.getTableName()));
		logger.info("queryRequest: " + queryRequest);
		return getAmazonDynamoDB().query(queryRequest);
	}

	public ScanResult scan(ScanRequest scanRequest) {
		scanRequest = scanRequest.clone();
		scanRequest.withTableName(buildPrefixedTablename(scanRequest.getTableName()));
		logger.info("scanRequest: " + scanRequest);
		return getAmazonDynamoDB().scan(scanRequest);
	}

	public UpdateItemResult updateItem(UpdateItemRequest updateItemRequest) {
		updateItemRequest = updateItemRequest.clone();
		updateItemRequest
				.withTableName(buildPrefixedTablename(updateItemRequest.getTableName()));
		logger.info("updateItemRequest: " + updateItemRequest);
		return getAmazonDynamoDB().updateItem(updateItemRequest);
	}

	// TODO paging
	// TODO assumes prefix does not contain delimiter
	// TODO assumes everything that starts with prefix is in fact an MT table
	// (ok?)
	// TODO assumes context does not contain delimiter
	@Override
	public List<MTStreamDescription> listStreams(IRecordProcessorFactory factory) {
		String prefix = tablePrefix.orElse("");
		logger.info("listStreams: " + prefix);
		return listAllTables().stream() //
				.filter(n -> n.startsWith(prefix)
						&& n.indexOf(delimiter, prefix.length()) >= 0)
				// TODO  handle table not exists
				.map(n -> getAmazonDynamoDB().describeTable(n).getTable())
				.filter(d -> Optional.ofNullable(d.getStreamSpecification())
						// only include tables with streaming enabled
						.map(StreamSpecification::isStreamEnabled).orElse(false))
				.map(d -> new MTStreamDescription() //
						.withLabel(d.getTableName()) // use raw name as label
						.withArn(d.getLatestStreamArn()) //
						.withRecordProcessorFactory(newAdapter(factory,
								d.getTableName().substring(prefix.length())))) //
				.collect(Collectors.toList());
	}

	private IRecordProcessorFactory newAdapter(IRecordProcessorFactory factory,
			String tableName) {
		int idx = tableName.indexOf(delimiter);
		String tenant = tableName.substring(0, idx);
		String name = tableName.substring(idx + delimiter.length(), tableName.length());
		logger.info("newAdapter: " + name);
		return () -> new RecordProcessor(tenant, name, factory.createProcessor());
	}

	private static class RecordProcessor implements IRecordProcessor {
		private final String tenant;
		private final String tableName;
		private final IRecordProcessor processor;

		public RecordProcessor(String tenant, String tableName,
				IRecordProcessor processor) {
			this.tenant = tenant;
			this.tableName = tableName;
			this.processor = processor;
			logger.info("RecordProcessor: " + tenant + ", " + tableName);
		}

		@Override
		public void initialize(InitializationInput initializationInput) {
			processor.initialize(initializationInput);
		}

		@Override
		public void processRecords(ProcessRecordsInput processRecordsInput) {
			List<com.amazonaws.services.kinesis.model.Record> records = processRecordsInput
					.getRecords().stream().map(RecordAdapter.class::cast)
					.map(this::toMTRecord).collect(toList());
			processor.processRecords(processRecordsInput.withRecords(records));
		}

		private com.amazonaws.services.kinesis.model.Record toMTRecord(
				RecordAdapter adapter) {
			Record r = adapter.getInternalObject();
			return new RecordAdapter(new MTRecord() //
					.withAwsRegion(r.getAwsRegion()) //
					.withDynamodb(r.getDynamodb()) //
					.withEventID(r.getEventID()) //
					.withEventName(r.getEventName()) //
					.withEventSource(r.getEventSource()) //
					.withEventVersion(r.getEventVersion()) //
					.withContext(tenant) //
					.withTableName(tableName));
		}

		@Override
		public void shutdown(ShutdownInput shutdownInput) {
			logger.info("shutdown: " + shutdownInput);
			processor.shutdown(shutdownInput);
		}

	}

	public static MTAmazonDynamoDBBuilder builder() {
		return new MTAmazonDynamoDBBuilder();
	}

	public static class MTAmazonDynamoDBBuilder {

		private AmazonDynamoDB amazonDynamoDB;
		private MTAmazonDynamoDBContextProvider mtContext;
		private String delimiter;
		private Optional<String> tablePrefix;

		public MTAmazonDynamoDBBuilder withAmazonDynamoDB(AmazonDynamoDB amazonDynamoDB) {
			this.amazonDynamoDB = amazonDynamoDB;
			return this;
		}

		public MTAmazonDynamoDBBuilder withContext(
				MTAmazonDynamoDBContextProvider mtContext) {
			this.mtContext = mtContext;
			return this;
		}

		public MTAmazonDynamoDBBuilder withDelimiter(String delimiter) {
			this.delimiter = delimiter;
			return this;
		}

		public MTAmazonDynamoDBBuilder withTablePrefix(String tablePrefix) {
			this.tablePrefix = Optional.of(tablePrefix);
			return this;
		}

		public MTAmazonDynamoDBByTable build() {
			setDefaults();
			Preconditions.checkNotNull(amazonDynamoDB, "amazonDynamoDB is required");
			Preconditions.checkNotNull(mtContext, "mtContext is required");
			return new MTAmazonDynamoDBByTable(mtContext, amazonDynamoDB, delimiter,
					tablePrefix);
		}

		private void setDefaults() {
			if (delimiter == null) {
				delimiter = ".";
			}
			if (tablePrefix == null) {
				tablePrefix = Optional.empty();
			}
		}

	}

	@VisibleForTesting
	String buildPrefixedTablename(String virtualTablename) {
		setTenantIfNotExists();
		logger.info("buildPrefixedTablename: " + (tablePrefix.orElse(""))
				+ getMTContext().getContext() + delimiter + virtualTablename);
		return (tablePrefix.orElse("")) + getMTContext().getContext() + delimiter
				+ virtualTablename;
	}

	private void setTenantIfNotExists() {
		try {
			getMTContext().getContext();
		} catch (IllegalStateException ise) {
			logger.error(ise.getMessage());
			// Reset the tenant id if exists in system properties.
			String tenantId = System.getProperty("multitenant.context.key");
			getMTContext().setContext(tenantId);
			logger.info("Tenant has been set as: " + tenantId);
		}
	}

}