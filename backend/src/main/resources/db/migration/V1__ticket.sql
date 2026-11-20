-- Ticket table (comments deferred to V2 / T07).
-- Portable across H2 (local) and PostgreSQL (postgres profile).

CREATE TABLE ticket (
    id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL,
    assignee VARCHAR(120),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_ticket PRIMARY KEY (id)
);

CREATE INDEX idx_ticket_status ON ticket (status);
CREATE INDEX idx_ticket_created_at ON ticket (created_at);
CREATE INDEX idx_ticket_title ON ticket (title);
