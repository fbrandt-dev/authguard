package io.cscenter.authguard.data;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.hibernate.annotations.Type;

import io.cscenter.shared.dto.CustomerIdentifierDTO;
import io.cscenter.shared.dto.enums.Domain;
import io.cscenter.shared.helpers.Transformable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerIdentifier implements Serializable, Transformable<CustomerIdentifierDTO> {

    @Column(name = "identifier", columnDefinition = "varchar(255)", nullable = false)
    @Type(type = "uuid-char")
    private UUID identifier;

    private Domain domain;

    @Override
    public CustomerIdentifierDTO transform() {
        return CustomerIdentifierDTO.builder().domain(domain).identifier(identifier).build();
    }

}
