package io.sendur.repositories;

import io.sendur.models.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends MongoRepository<Lead, String> {

    @Query("{ 'email':  {$regex: '^Not available$', $options: 'i' } }")
    List<Lead> findLeadByEmailNotAvailable();
}
