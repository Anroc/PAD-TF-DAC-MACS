package de.tuberlin.tfdacmacs.basics.factory;

import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeType;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttributeTestFactory {

    private final AttributeValueTestFactory attributeValueTestFactory;

    public Attribute create() {
        return new Attribute("aa.tu-berlin.de", "testAttribute", Sets.newLinkedHashSet(
                attributeValueTestFactory.createString()
        ), AttributeType.STRING);
    }
}
