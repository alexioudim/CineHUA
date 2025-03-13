package gr.dit.hua.CineHua.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long role_id;
    private String name;

    @OneToMany(mappedBy = "role")
    private List<User> users;
}
