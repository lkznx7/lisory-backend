ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_payment_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_order_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS order_nsu VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS transaction_nsu VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_link VARCHAR(2048);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS expiration_date TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS authorization_code VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS installments INTEGER DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_payments_gateway_payment_id ON payments(gateway_payment_id);
