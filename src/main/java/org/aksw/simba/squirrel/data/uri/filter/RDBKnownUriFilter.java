package org.aksw.simba.squirrel.data.uri.filter;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.model.MapObject;
import com.rethinkdb.net.Cursor;
import org.aksw.simba.squirrel.data.uri.CrawleableUri;
import org.aksw.simba.squirrel.data.uri.UriType;
import org.aksw.simba.squirrel.model.RDBConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;

/**
 * Created by ivan on 8/18/16.
 */
public class RDBKnownUriFilter implements KnownUriFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDBKnownUriFilter.class);

    private RDBConnector connector = null;
    private Integer recrawlEveryWeek = 60 * 60 * 24 * 7 * 1000; //in miiliseconds
    private RethinkDB r = RethinkDB.r;

    public RDBKnownUriFilter(String hostname, Integer port) {
        this.connector = new RDBConnector(hostname, port);
    }

    @Override
    public void add(CrawleableUri uri) {
        add(uri, System.currentTimeMillis());
    }

    @Override
    public void add(CrawleableUri uri, long timestamp) {
        Object result = r.db("squirrel")
                .table("knownurifilter")
                .insert(convertURITimestampToRDB(uri, timestamp))
                .run(connector.connection);
        System.out.println(result);
    }

    private MapObject convertURIToRDB(CrawleableUri uri) {
        InetAddress ipAddress = uri.getIpAddress();
        URI uriPath = uri.getUri();
        UriType uriType = uri.getType();
        return r.hashMap("uri", uriPath.toString())
                .with("ipAddress", ipAddress.toString())
                .with("type", uriType.toString());
    }

    private MapObject convertURITimestampToRDB(CrawleableUri uri, long timestamp) {
        MapObject uriMap = convertURIToRDB(uri);
        return uriMap
                .with("timestamp", timestamp);
    }

    @Override
    public boolean isUriGood(CrawleableUri uri) {
        Cursor cursor = r.db("squirrel")
                .table("knownurifilter")
                .getAll(uri.getUri().toString())
                .optArg("index", "uri")
                .g("timestamp")
                .run(connector.connection);
        if(cursor.hasNext()) {
            Object timestampRetrieved = cursor.next();
            cursor.close();
            return (System.currentTimeMillis() - (long) timestampRetrieved) < recrawlEveryWeek;
        } else {
            cursor.close();
            return false;
        }
    }

    public void open() {
        this.connector.open();
        try {
            r.dbCreate("squirrel");
        } catch (Exception e) {
            LOGGER.debug(e.toString());
        }
        r.db("squirrel").tableCreate("knownurifilter").run(this.connector.connection);
        r.db("squirrel").table("knownurifilter").indexCreate("uri").run(this.connector.connection);
        r.db("squirrel").table("knownurifilter").indexWait("uri").run(this.connector.connection);
    }

    public void close() {
        r.db("squirrel").tableDrop("knownurifilter").run(this.connector.connection);
        this.connector.close();
    }
}
