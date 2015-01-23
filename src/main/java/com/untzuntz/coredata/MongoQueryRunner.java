package com.untzuntz.coredata;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.untzuntz.coredata.anno.DBTableMap;
import com.untzuntz.coredata.exceptions.FailedRequestException;
import com.untzuntz.coredata.exceptions.FieldSetException;
import com.untzuntz.coredata.exceptions.UnknownPrimaryKeyException;
import com.untzuntz.ustack.data.MongoDB;

public class MongoQueryRunner {

    static Logger logger = Logger.getLogger(MongoQueryRunner.class);

	public static <T> List<T> runListQuery(Class<T> clazz, SearchFilters filters, OrderBy orderBy, PagingSupport paging) throws FailedRequestException, UnknownPrimaryKeyException, UnhandledException
	{
		return runListQuery(clazz, filters, orderBy, paging, null);
	}	

	public static <T> List<T> runListQuery(Class<T> clazz, SearchFilters filters, OrderBy orderBy, PagingSupport paging, DBObject additionalSearch) throws FailedRequestException, UnknownPrimaryKeyException, UnhandledException
	{
		return runListQuery(clazz, filters, orderBy, paging, additionalSearch, null);
	}
	
	public static <T> int count(Class<T> clazz, SearchFilters filters, DBObject additionalSearch) throws FailedRequestException
	{
		DBTableMap tbl = clazz.getAnnotation(DBTableMap.class);
		if (tbl == null)
			throw new FailedRequestException("Cannot persist or grab class from data source : " + clazz.getName());

		// search parameters
		DBObject searchObj = null;
		if (filters != null)
			searchObj = filters.getMongoSearchObject();
		else
			searchObj = new BasicDBObject();
		
		if (additionalSearch != null)
			searchObj.putAll(additionalSearch);

		String db = DataMgr.getDb(tbl);
		String colName = tbl.dbTable();
		DBCollection col = MongoDB.getCollection(db, colName);
		DBCursor cur = col.find(searchObj);
		int count = cur.count();
		
		logger.info(String.format("[%s.%s] => %s | Count: %d", db, colName, searchObj, count));
		
		return count;
	}
	
    /**
     * Executes a query against the MongoDB database for the request class and collection
     * 
     * @param clazz
     * @param filters
     * @param orderBy
     * @param paging
     * @return
     * @throws FailedRequestException
     * @throws UnknownPrimaryKeyException
     * @throws UnhandledException 
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws FieldSetException
     * @throws NoSuchFieldException
     */
	public static <T> List<T> runListQuery(Class<T> clazz, SearchFilters filters, OrderBy orderBy, PagingSupport paging, DBObject additionalSearch, ExportFormat exportFormat) throws FailedRequestException, UnknownPrimaryKeyException, UnhandledException
	{
		DBTableMap tbl = clazz.getAnnotation(DBTableMap.class);
		if (tbl == null)
			throw new FailedRequestException("Cannot persist or grab class from data source : " + clazz.getName());
		
		List<T> ret = new ArrayList<T>();
		
		// search parameters
		DBObject searchObj = null;
		if (filters != null)
			searchObj = filters.getMongoSearchObject();
		else
			searchObj = new BasicDBObject();
		
		if (additionalSearch != null)
			searchObj.putAll(additionalSearch);
		
		// paging and limits
		int skip = 0;
		int limit = -1;
		if (paging != null)
		{
			skip = (paging.getPage() - 1) * paging.getItemsPerPage();
			limit = paging.getItemsPerPage(); 
		}

		// sorting parameters
		DBObject orderByObj = null;
		if (orderBy != null) {
			orderByObj = new BasicDBObject();
			orderByObj.put(orderBy.getFieldName(), orderBy.getDirection().getOrderInt());
		}

		logger.info(String.format("%s | Search [%s] | Sort [%s] | Skip %d | Limit %d", clazz.getSimpleName(), searchObj, orderByObj, skip, limit));
		
		// run the actual query
		DBCollection col = MongoDB.getCollection(DataMgr.getDb(tbl), tbl.dbTable());
		DBCursor cur = col.find(searchObj);
		cur.sort(orderByObj);
		cur.skip(skip);
		if (limit != -1)
			cur.limit(limit);

		// setup result paging
		if (paging != null)
			paging.setTotal(new Long(cur.count()));
		
		if (exportFormat != null)
			exportFormat.output(cur);
		else
		{
			while (cur.hasNext())
				ret.add(DataMgr.getObjectFromDBObject(clazz, cur.next()));
		}

		return ret;
	}
	
	/**
	 * Searches for a single object based on the provided search filters
	 * 
	 * @param clazz
	 * @param filters
	 * @return
	 * @throws FailedRequestException
	 * @throws UnknownPrimaryKeyException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws FieldSetException
	 * @throws NoSuchFieldException
	 */
	public static <T> T runQuery(Class<T> clazz, SearchFilters filters) throws FailedRequestException, UnknownPrimaryKeyException
	{
		DBTableMap tbl = clazz.getAnnotation(DBTableMap.class);
		if (tbl == null)
			throw new FailedRequestException("Cannot persist or grab class from data source : " + clazz.getName());

		// search parameters
		DBObject searchObj = null;
		if (filters != null)
			searchObj = filters.getMongoSearchObject();
		else
			searchObj = new BasicDBObject();

		// run the actual query
		DBCollection col = MongoDB.getCollection(DataMgr.getDb(tbl), tbl.dbTable());
		DBObject ret = col.findOne(searchObj);
		return DataMgr.getObjectFromDBObject(clazz, ret);
	}
	
	
}