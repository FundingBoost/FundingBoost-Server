package kcs.funding.fundingboost.domain.security.repository;

import kcs.funding.fundingboost.domain.security.entity.token.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
