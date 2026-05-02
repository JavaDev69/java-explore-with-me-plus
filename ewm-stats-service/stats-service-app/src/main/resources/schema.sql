CREATE SCHEMA IF NOT EXISTS public;

CREATE TABLE IF NOT EXISTS public.endpoint_hits (
    id BIGSERIAL PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    ip VARCHAR(45) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_endpoint_hits_app ON public.endpoint_hits(app);
CREATE INDEX IF NOT EXISTS idx_endpoint_hits_uri ON public.endpoint_hits(uri);
CREATE INDEX IF NOT EXISTS idx_endpoint_hits_timestamp ON public.endpoint_hits(timestamp);
CREATE INDEX IF NOT EXISTS idx_endpoint_hits_app_uri ON public.endpoint_hits(app, uri);
