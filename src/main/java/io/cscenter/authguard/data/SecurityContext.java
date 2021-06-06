package io.cscenter.authguard.data;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityContext {

    private CustomerEntity authenticatedCustomer;
    private Set<String> scopes;

}
