package io.cscenter.authguard.data.repositories;

import org.springframework.data.repository.CrudRepository;

import io.cscenter.authguard.data.CustomerEntity;
import io.cscenter.authguard.data.CustomerIdentifier;
import io.cscenter.shared.dto.enums.Domain;

import java.util.Optional;

public interface CustomerEntityRepository extends CrudRepository<CustomerEntity, CustomerIdentifier> {

    Optional<CustomerEntity> findByCustomerIdentifier_DomainAndUsername(Domain domain, String username);

}