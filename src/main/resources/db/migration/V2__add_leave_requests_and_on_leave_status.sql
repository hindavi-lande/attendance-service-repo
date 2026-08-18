ALTER TABLE attendance_entries
    ADD COLUMN attendance_status VARCHAR(32) NOT NULL DEFAULT 'CLOCKED_IN';

ALTER TABLE attendance_entries
    ALTER COLUMN clock_in_at DROP NOT NULL;

CREATE TABLE leave_requests (
    id                BIGSERIAL PRIMARY KEY,
    employee_id       BIGINT NOT NULL,
    from_date         DATE NOT NULL,
    to_date           DATE NOT NULL,
    reason            TEXT NOT NULL,
    status            VARCHAR(32) NOT NULL,
    manager_id        BIGINT,
    manager_comment   TEXT,
    submitted_at      TIMESTAMP NOT NULL DEFAULT now(),
    decided_at        TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT chk_leave_dates CHECK (to_date >= from_date)
);

CREATE INDEX idx_leave_requests_employee_id ON leave_requests (employee_id);

ALTER TABLE attendance_entries
    ALTER COLUMN attendance_status SET DEFAULT 'CLOCKED_IN';
