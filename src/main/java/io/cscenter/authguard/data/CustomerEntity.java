package io.cscenter.authguard.data;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.cscenter.shared.dto.CustomerIdentifierDTO;
import io.cscenter.shared.helpers.Transformable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEntity implements Transformable<CustomerIdentifierDTO> {

    @EmbeddedId
    private CustomerIdentifier customerIdentifier;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    private String password;

    @CreationTimestamp
    private Timestamp created_at;

    @UpdateTimestamp
    private Timestamp updated_at;

    @Override
    public CustomerIdentifierDTO transform() {
        return this.customerIdentifier.transform();
    }

}
