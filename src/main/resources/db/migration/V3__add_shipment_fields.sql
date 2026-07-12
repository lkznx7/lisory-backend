ALTER TABLE shipments ADD COLUMN IF NOT EXISTS melhor_envio_id VARCHAR(255);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS tracking_url VARCHAR(2048);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS carrier_code VARCHAR(100);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS service_code VARCHAR(100);
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS estimated_delivery TIMESTAMP;
ALTER TABLE shipments ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_shipments_melhor_envio_id ON shipments(melhor_envio_id);
