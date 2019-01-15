package de.tuberlin.tfdacmacs.client.policy;

import de.tuberlin.tfdacmacs.client.antlr.PolicyBaseListener;
import de.tuberlin.tfdacmacs.client.antlr.PolicyLexer;
import de.tuberlin.tfdacmacs.client.antlr.PolicyParser;
import de.tuberlin.tfdacmacs.client.attribute.AttributeService;
import de.tuberlin.tfdacmacs.client.attribute.exceptions.InvalidAttributeValueIdentifierException;
import de.tuberlin.tfdacmacs.client.authority.AuthorityService;
import de.tuberlin.tfdacmacs.client.authority.exception.InvalidAuthorityIdentifier;
import de.tuberlin.tfdacmacs.client.policy.exception.ThrowingErrorListener;
import de.tuberlin.tfdacmacs.crypto.pairing.data.AccessPolicyElement;
import de.tuberlin.tfdacmacs.crypto.pairing.data.AndAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.data.AttributePolicyElement;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
@RequiredArgsConstructor
public class AccessPolicyParser {

    private final AttributeService attributeService;
    private final AuthorityService authorityService;

    public DNFAccessPolicy parse(@NonNull String policy) {
        PolicyLexer policyLexer = new PolicyLexer(CharStreams.fromString(policy));
        policyLexer.removeErrorListeners();
        policyLexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        CommonTokenStream commonTokenStream = new CommonTokenStream(policyLexer);
        PolicyParser policyParser= new PolicyParser(commonTokenStream);
        policyParser.removeErrorListeners();
        policyParser.addErrorListener(ThrowingErrorListener.INSTANCE);

        ParserListener parserListener = new ParserListener(authorityService, attributeService);
        ParseTreeWalker parseTreeWalker = ParseTreeWalker.DEFAULT;
        parseTreeWalker.walk(parserListener, policyParser.policy());

        return parserListener.getDnfAccessPolicy();
    }

    @RequiredArgsConstructor
    public class ParserListener extends PolicyBaseListener {

        private final Stack<AccessPolicyElement> accessPolicyElement = new Stack<>();
        private final AuthorityService authorityService;
        private final AttributeService attributeService;

        @Override
        public void enterPolicy(PolicyParser.PolicyContext ctx) {
            accessPolicyElement.push(new DNFAccessPolicy());
        }

        public DNFAccessPolicy getDnfAccessPolicy() {
            return (DNFAccessPolicy) current();
        }

        @Override
        public void enterAnd_expression(PolicyParser.And_expressionContext ctx) {
            if(ctx.getParent() instanceof PolicyParser.Or_expressionContext) {
                accessPolicyElement.push(new AndAccessPolicy());
            }
        }

        @Override
        public void exitAnd_expression(PolicyParser.And_expressionContext ctx) {
            if(ctx.getParent() instanceof PolicyParser.Or_expressionContext) {
                AccessPolicyElement pop = accessPolicyElement.pop();
                current().put(pop);
            }
        }

        @Override
        public void enterAttr_value_id(PolicyParser.Attr_value_idContext ctx) {
            String authorityId = ctx.authorityId.getText();
            String attributeValueId = ctx.getText();

            AuthorityKey.Public authorityPublicKey = authorityService.findAuthorityPublicKey(authorityId)
                    .orElseThrow(
                            () -> new InvalidAuthorityIdentifier(authorityId)
                    );

            AttributeValueKey.Public attributeValuePublicKey = attributeService.findAttributeValuePublicKey(attributeValueId)
                    .orElseThrow(
                            () -> new InvalidAttributeValueIdentifierException(attributeValueId)
                    );

            current().put(new AttributePolicyElement(
                    authorityPublicKey,
                    attributeValuePublicKey,
                    attributeValueId)
            );
        }

        private AccessPolicyElement current() {
            return accessPolicyElement.peek();
        }
    }
}
