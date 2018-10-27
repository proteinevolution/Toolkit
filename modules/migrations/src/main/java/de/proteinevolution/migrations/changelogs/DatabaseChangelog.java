package de.proteinevolution.migrations.changelogs;

import com.github.mongobee.;

@Changelog
public class DatabaseChangelog {

    @ChangeSet(order = "001", id = "snakeCaseJobModel", author = "Felix Gabler")
    public void changeJobKeysToSnakeCase(DB db){
        db.
    }

}
