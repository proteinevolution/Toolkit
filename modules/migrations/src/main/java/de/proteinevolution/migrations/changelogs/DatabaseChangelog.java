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

    @ChangeSet(order = "010", id = "10", author = "Felix Gabler")
    public void migrateSessionID(final MongoDatabase db) {
        MongoCollection<Document> users = db.getCollection("users");
        users.find().forEach((Block<Document>) user -> {
            Bson filter = Filters.eq("_id", user.get("_id"));
            Object sessionID = user.get("sessionID");
            if (sessionID != null) {
                Bson update = Updates.set("sessionID", sessionID.toString());
                users.updateOne(filter, update);
            }
        });
    }

    @ChangeSet(order = "011", id = "11", author = "Felix Gabler")
    public void createStatisticsID(final MongoDatabase db) {
        MongoCollection<Document> statistics = db.getCollection("statistics");
        statistics.find().forEach((Block<Document>) statistic -> {
            Bson filter = Filters.eq("_id", statistic.get("_id"));
            Bson update = Updates.set("id", statistic.get("_id").toString());
            statistics.updateOne(filter, update);
        });
        statistics.createIndex(Indexes.ascending("id"), new IndexOptions().unique(true));
    }

    @ChangeSet(order = "012", id = "12", author = "Felix Gabler")
    public void changeJobEventKeysToCamel(final MongoDatabase db) {
        final Document rename = new Document();
        rename.put("job_id", "jobID");
        rename.put("internal_job", "internalJob");

        Bson filters = Filters.and(
                Filters.and(
                        Filters.exists("jobID", false)
                ),
                Filters.and(
                        Filters.exists("job_id", true)
                )
        );
        Bson update = new Document("$rename", rename);
        db.getCollection("jobevents").updateMany(filters, update);
    }

    @ChangeSet(order = "013", id = "13", author = "Felix Gabler")
    public void changeJobKeysBackToCamelCase(final MongoDatabase db) {
        final Document rename = new Document();
        rename.put("job_id", "id");
        rename.put("owner_id", "ownerID");
        rename.put("parent_id", "parentID");
        rename.put("is_public", "isPublic");
        rename.put("email_update", "emailUpdate");
        rename.put("watch_list", "watchList");
        rename.put("cluster_data", "clusterData");
        rename.put("date_created", "dateCreated");
        rename.put("date_updated", "dateUpdated");
        rename.put("date_viewed", "dateViewed");
        rename.put("date_deleted", "dateDeleted");
        rename.put("toolname_long", "toolnameLong");
        rename.put("ip_hash", "ipHash");

        Bson filters = Filters.and(
                Filters.exists("id", false),
                Filters.exists("job_id", true)
        );
        Bson update = new Document("$rename", rename);
        db.getCollection("jobs").updateMany(filters, update);
    }

    @ChangeSet(order = "014", id = "14", author = "Felix Gabler")
    public void changeJobClusterDataKeysToCamelCase(final MongoDatabase db) {
        final Document rename = new Document();
        rename.put("clusterData.sge_id", "clusterData.sgeID");
        rename.put("clusterData.started", "clusterData.dateStarted");
        rename.put("clusterData.finished", "clusterData.dateFinished");
        Bson filters = Filters.and(
                Filters.exists("clusterData.sgeID", false),
                Filters.exists("clusterData.sge_id", true)
        );
        Bson update = new Document("$rename", rename);
        db.getCollection("jobs").updateMany(filters, update);
    }

    @ChangeSet(order = "015", id = "15", author = "Felix Gabler")
    public void renameJobDeletionField(final MongoDatabase db) {
        Bson rename = Updates.rename("dateDeleted", "dateDeletionOn");
        Bson filters = Filters.and(
                Filters.exists("dateDeletionOn", false),
                Filters.exists("dateDeleted", true)
        );
        db.getCollection("jobs").updateMany(filters, rename);
    }
}
