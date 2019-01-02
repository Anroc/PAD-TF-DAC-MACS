package de.tuberlin.tfdacmacs.attributeauthority.user.data;

import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends Entity {

    public User(@NonNull String id) {
        super(id);
        this.attributes = new HashSet<>();
    }

    @Valid
    @NotNull
    private Set<UserAttributeKey> attributes;
}
