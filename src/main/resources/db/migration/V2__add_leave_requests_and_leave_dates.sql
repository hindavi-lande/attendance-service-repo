CREATE TABLE leave_requests (
    id               BIGSERIAL PRIMARY KEY,
    employee_id      BIGINT NOT NULL,
    requested_from   DATE NOT NULL,
    requested_to     DATE NOT NULL,
    reason           TEXT NOT NULL,

    status           VARCHAR(20) NOT NULL,

    manager_id       BIGINT,
    manager_comment TEXT,

    approved_at      TIMESTAMP,
    rejected_at      TIMESTAMP,

    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT chk_leave_requests_dates CHECK (requested_from <= requested_to)
);

CREATE INDEX idx_leave_requests_employee_id ON leave_requests (employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests (status);
CREATE INDEX idx_leave_requests_manager_id ON leave_requests (manager_id);

CREATE TABLE leave_request_dates (
    id               BIGSERIAL PRIMARY KEY,
    leave_request_id BIGINT NOT NULL,
    work_date        DATE NOT NULL,

    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_leave_request_dates_request
        FOREIGN KEY (leave_request_id) REFERENCES leave_requests (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_leave_request_dates_request_work_date UNIQUE (leave_request_id, work_date)
);

CREATE INDEX idx_leave_request_dates_work_date ON leave_request_dates (work_date);
