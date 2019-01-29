package de.tuberlin.tfdacmacs.lib.attributes.data;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Attribute extends AbstractAttribute<AttributeValue> {

    protected Attribute(String authorityDomain, String name,
            Set<AttributeValue> values, AttributeType type) {
        super(authorityDomain, name, values, type);
    }
}