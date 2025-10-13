package domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class Signal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private SignalType type;

    @Lob
    private String data;

}
