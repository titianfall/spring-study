package hello.hello_spring.domain;

import jakarta.persistence.*;

// jpa 인터페이스 + hibername 구현
// jpa가 관리하는 entity
@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // @Column(name = "username")
    private String name;

    // id Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // name Getter and Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
