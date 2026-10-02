CREATE TABLE settlement_transactions (
    id uuid PRIMARY KEY,
    idempotency_key varchar(128) NOT NULL UNIQUE,
    debtor_iban varchar(255) NOT NULL,
    creditor_iban varchar(255) NOT NULL,
    amount numeric(19,4) NOT NULL CHECK (amount > 0),
    currency varchar(3) NOT NULL,
    status varchar(255) NOT NULL,
    created_at timestamp with time zone NOT NULL
);
CREATE TABLE outbox_events (
    id uuid PRIMARY KEY,
    aggregate_id uuid NOT NULL,
    aggregate_type varchar(255) NOT NULL,
    event_type varchar(255) NOT NULL,
    payload text NOT NULL,
    processed boolean NOT NULL DEFAULT false,
    created_at timestamp with time zone NOT NULL
);
CREATE INDEX idx_outbox_unprocessed ON outbox_events (processed, created_at);
