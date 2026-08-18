CREATE TABLE attendance_entries (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL,
    work_date     DATE NOT NULL,
    clock_in_at   TIMESTAMP NOT NULL,
    clock_out_at  TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT uq_attendance_entries_employee_work_date UNIQUE (employee_id, work_date)
);

CREATE INDEX idx_attendance_entries_employee_id ON attendance_entries (employee_id);
