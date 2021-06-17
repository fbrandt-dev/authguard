package io.cscenter.authguard.data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import io.cscenter.shared.dto.OAuthTokenDTO;
import io.cscenter.shared.helpers.Transformable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_tokens")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthTokenEntity implements Transformable<OAuthTokenDTO> {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "identifier", columnDefinition = "VARCHAR(255)")
    @Type(type = "uuid-char")
    private UUID identifier;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private CustomerEntity customer;

    @Column(nullable = false, columnDefinition = "TEXT", unique = true)
    private String token;

    @Column(nullable = false, columnDefinition = "TEXT", unique = true)
    private String refreshToken;

    @Column(nullable = false)
    private PublicKey key;

    private Date tokenIssuedAt;

    private Date tokenValidUntil;

    private Date refreshTokenIssuedAt;

    private Date refreshTokenValidUntil;

    @Override
    public OAuthTokenDTO transform() {
        return OAuthTokenDTO.builder().access_token(token).access_token_expires_at(tokenValidUntil)
                .refresh_token(refreshToken).refresh_token_expires_at(refreshTokenValidUntil)
                .customer(customer.transform()).identifier(identifier).build();
    }

}
