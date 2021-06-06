package io.cscenter.authguard.data.converters;

import java.util.stream.Stream;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.cscenter.shared.dto.enums.Domain;

@Converter(autoApply = true)
public class DomainConverter implements AttributeConverter<Domain, String> {

    @Override
    public String convertToDatabaseColumn(Domain domain) {
        if (domain == null) {
            return null;
        }
        return domain.toString();
    }

    @Override
    public Domain convertToEntityAttribute(String domain) {
        if (domain == null) {
            return null;
        }

        return Stream.of(Domain.values()).filter(d -> d.toString().toUpperCase().equals(domain.toUpperCase()))
                .findFirst().orElseThrow(IllegalArgumentException::new);

    }
}