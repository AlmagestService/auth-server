package org.almagestauth.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;
    @Column(name = "role_name", unique = true)
    private String name;

    @OneToMany(mappedBy = "role")
//    @JsonBackReference
    private List<Member> members = new ArrayList<>();

    public Role(String roleName) {
        this.name = roleName;
    }
}
