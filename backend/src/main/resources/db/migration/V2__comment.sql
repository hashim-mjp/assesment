-- Comment table (T07). Portable across H2 (local) and PostgreSQL (postgres profile).

CREATE TABLE comment (
    id UUID NOT NULL,
    ticket_id UUID NOT NULL,
    body TEXT NOT NULL,
    author VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id),
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (id)
);

CREATE INDEX idx_comment_ticket_id ON comment (ticket_id);
