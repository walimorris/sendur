package io.sendur.repository;

import com.mongodb.lang.NonNull;
import io.sendur.domain.lead.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends MongoRepository<Lead, String> {

    @Query("{ 'email':  {$regex: '^Not available$', $options: 'i' } }")
    List<Lead> findLeadByEmailNotAvailable();

    /**
     * Find lead by business name.
     *
     * @param businessName business name to search
     *
     * @return {@link Lead}
     */
    Lead findLeadByBusinessName(@NonNull String businessName);

    /**
     * Find leads by city.
     *
     * @param city city to search
     *
     * @return {@link List<Lead> leads}
     */
    List<Lead> findLeadsByCity(@NonNull String city);
}
