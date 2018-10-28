package de.proteinevolution.migrations.changelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DB;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

@ChangeLog
public class DatabaseChangelog {

    @ChangeSet(order = "001", id = "1", author = "Felix Gabler")
    public void makeJobIdUnique(DB db) {
        db.getCollection("jobs").createIndex("{ \"jobID\": 1 }, { unique: true }");
    }

    @ChangeSet(order = "002", id = "2", author = "Felix Gabler")
    public void changeJobKeysToSnakeCase(final MongoDatabase db) {
        final Document rename = new Document();
        rename.put("jobID", "job_id");
        rename.put("ownerID", "owner_id");
        rename.put("isPublic", "is_public");
        rename.put("emailUpdate", "email_update");
        rename.put("watchList", "watch_list");
        rename.put("clusterData", "cluster_data");
        rename.put("dateCreated", "date_created");
        rename.put("dateUpdated", "date_updated");
        rename.put("dateViewed", "date_viewed");
        rename.put("dateDeletion", "date_deleted");
        rename.put("toolnameLong", "toolname_long");
        rename.put("IPHash", "ip_hash");

        Bson filters = Filters.and(
                Filters.and(
                        Filters.exists("jobID", true)
                ),
                Filters.and(
                        Filters.exists("job_id", false)
                )
        );
        Bson update = new Document("$rename", rename);
        db.getCollection("jobs").updateMany(filters, update);
    }

}