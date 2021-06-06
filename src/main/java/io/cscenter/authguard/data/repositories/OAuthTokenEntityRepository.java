package io.cscenter.authguard.data.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import io.cscenter.authguard.data.CustomerEntity;
import io.cscenter.authguard.data.OAuthTokenEntity;

public interface OAuthTokenEntityRepository extends CrudRepository<OAuthTokenEntity, UUID> {

    public Optional<OAuthTokenEntity> findByToken(final String token);

    public Optional<OAuthTokenEntity> findByRefreshToken(final String token);

    public List<OAuthTokenEntity> findByCustomer(final CustomerEntity customer);

}
