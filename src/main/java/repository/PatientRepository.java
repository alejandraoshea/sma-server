package repository;

import domain.Symptoms;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import domain.SymptomType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class PatientRepository {
    private final JdbcTemplate jdbcTemplate;

    public PatientRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveSymptoms(List<Symptoms> symptomsList) {
        if (symptomsList == null || symptomsList.isEmpty()) return;

        String insertSql = "INSERT INTO symptoms (patient_id, symptom, timestamp) VALUES (?, ?, ?)";

        for (Symptoms symptom : symptomsList) {
            Long patientId = symptom.getPatientId();

            LocalDateTime timestamp;
            if (symptom.getTimestamp() != null) {
                timestamp = symptom.getTimestamp();
            } else {
                timestamp = LocalDateTime.now();
            }

            for (SymptomType type : symptom.getSymptomsSet()) {
                jdbcTemplate.update(insertSql, patientId, type.name(), Timestamp.valueOf(timestamp));
            }
        }
    }

    public List<Symptoms> findByPatientId(Long patientId) {
        String sql = "SELECT symptom_id, patient_id, symptom_type, timestamp FROM symptoms WHERE patient_id = ?";
        //** if we want to order it we can add:  "ORDER BY timestamp DESC;"

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, patientId);
        Map<LocalDateTime, Set<SymptomType>> groupedByTimestamp = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            LocalDateTime ts = ((java.sql.Timestamp) row.get("timestamp")).toLocalDateTime();
            groupedByTimestamp.computeIfAbsent(ts, k -> new HashSet<>())
                    .add(SymptomType.valueOf((String) row.get("symptom_type")));
        }

        List<Symptoms> results = new ArrayList<>();
        for (var entry : groupedByTimestamp.entrySet()) {
            results.add(new Symptoms(null, patientId, entry.getValue(), entry.getKey()));
        }

        return results;
    }


}
