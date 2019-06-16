/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.migrations.changelogs;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.Block;
import com.mongodb.DB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.stream.Collectors;

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

    @ChangeSet(order = "003", id = "3", author = "Felix Gabler")
    public void changeJobEventLogKeysToSnakeCase(final MongoDatabase db) {
        final Bson rename = Updates.rename("internalJob", "internal_job");
        Bson filters = Filters.and(
                Filters.and(
                        Filters.exists("internalJob", true)
                ),
                Filters.and(
                        Filters.exists("internal_job", false)
                )
        );
        db.getCollection("jobevents").updateMany(filters, rename);
    }

    @ChangeSet(order = "004", id = "4", author = "Felix Gabler")
    public void changeJobClusterDataKeysToSnakeCase(final MongoDatabase db) {
        final Document rename = new Document();
        rename.put("cluster_data.sgeid", "cluster_data.sge_id");
        Bson filters = Filters.and(
                Filters.and(
                        Filters.exists("cluster_data.sgeid", true)
                ),
                Filters.and(
                        Filters.exists("cluster_data.sge_id", false)
                )
        );
        Bson update = new Document("$rename", rename);
        db.getCollection("jobs").updateMany(filters, update);
    }

    @ChangeSet(order = "005", id = "5", author = "Felix Gabler")
    public void removeCommentListFromJob(final MongoDatabase db) {
        final Document unset = new Document();
        unset.put("commentList", 1);
        Bson filters = Filters.exists("commentList", true);
        Bson update = new Document("$unset", unset);
        db.getCollection("jobs").updateMany(filters, update);
    }

    @ChangeSet(order = "006", id = "6", author = "Felix Gabler")
    public void createUserID(final MongoDatabase db) {
        MongoCollection<Document> users = db.getCollection("users");
        users.find().forEach((Block<Document>) document -> {
            Bson filter = Filters.eq("_id", document.get("_id"));
            Bson update = Updates.set("id", document.get("_id").toString());
            users.updateOne(filter, update);
        });
        users.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
    }

    @ChangeSet(order = "007", id = "7", author = "Felix Gabler")
    public void migrateJobWatchList(final MongoDatabase db) {
        MongoCollection<Document> jobs = db.getCollection("jobs");
        jobs.find().forEach((Block<Document>) job -> {
            Bson filter = Filters.eq("_id", job.get("_id"));
            List watchList = job.get("watch_list", List.class);
            Object res = watchList.stream().map(Object::toString).collect(Collectors.toList());
            Bson update = Updates.set("watch_list", res);
            jobs.updateOne(filter, update);
        });
    }

    @ChangeSet(order = "008", id = "8", author = "Felix Gabler")
    public void migrateJobOwner(final MongoDatabase db) {
        MongoCollection<Document> jobs = db.getCollection("jobs");
        jobs.find().forEach((Block<Document>) job -> {
            Bson filter = Filters.eq("_id", job.get("_id"));
            Bson update = Updates.set("owner_id", job.get("owner_id").toString());
            jobs.updateOne(filter, update);
        });
    }

    @ChangeSet(order = "009", id = "9", author = "Felix Gabler")
    public void migrateUserToken(final MongoDatabase db) {
        MongoCollection<Document> users = db.getCollection("users");
        users.find().forEach((Block<Document>) user -> {
            Bson filter = Filters.and(
                    Filters.eq("_id", user.get("_id")),
                    Filters.exists("userToken.userID", true)
            );
            Document userToken = user.get("userToken", Document.class);
            if (userToken != null) {
                Object userId = userToken.get("userID");
                if (userId != null) {
                    Bson update = Updates.set("userToken.userID", userId.toString());
                    users.updateOne(filter, update);
                }
            }
        });
    }

//    @ChangeSet(order = "004", id = "4", author = "Felix Gabler")
//    public void changeJobEventKeysToSnakeCase(final MongoDatabase db) {
//        // TODO: does not work yet because events is an array of dynamic documents
//        List<Bson> pipeline = new ArrayList<>();
//
//        final Document project = new Document();
//        project.put("job_state", "$events.$.jobState");
//        Bson $project = new Document("$cproject", project);
//        pipeline.add($project);
//
//        db.getCollection("jobevents").aggregate(pipeline);
//    }

}
