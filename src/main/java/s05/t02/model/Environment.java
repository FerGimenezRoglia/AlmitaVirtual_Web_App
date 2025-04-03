package s05.t02.model;

import jakarta.persistence.*;
import lombok.*;
import s05.t02.model.enums.EnvironmentColor;
import s05.t02.model.enums.EnvironmentStatus;

@Entity
@Table(name = "environment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Environment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnvironmentColor color = EnvironmentColor.NEUTRO;

    @Column(length = 255)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnvironmentStatus status = EnvironmentStatus.REPOSA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}