package de.tuberlin.tfdacmacs.centralserver.user.db;

import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import lombok.NonNull;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CouchbaseRepository<User, String> {

    @Query("#{#n1ql.selectEntity} WHERE `_class` = 'de.tuberlin.tfdacmacs.centralserver.user.data.User' and authorityId = $1")
    List<User> findUsersByAuthorityId(@NonNull String authorityId);
}
