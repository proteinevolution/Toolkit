package de.proteinevolution.migrations.changelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.DB;

@ChangeLog
public class DatabaseChangelog {

    @ChangeSet(order = "001", id = "1", author = "Felix Gabler")
    public void makeJobIdUnique(DB db) {
        db.getCollection("jobs").createIndex("{ \"jobID\": 1 }, { unique: true }");
    }

//    @ChangeSet(order = "002", id = "2", author = "Felix Gabler")
//    public void changeJobKeysToSnakeCase(DB db) {
//        // TODO: does not work yet
//        db.command("db.jobs.update ( {}, { $rename : { \"dateViewed\" : \"date_viewed\" }} );");
//    }

}
