-- ============================================================
-- TIES - Aerial Mapping System
-- PostgreSQL Initial Schema
-- ============================================================

-- Devices table (drones or phones acting as drones)
CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_identifier VARCHAR(255) UNIQUE NOT NULL,
    device_type VARCHAR(50) NOT NULL,
    registered_at TIMESTAMP DEFAULT NOW(),
    last_seen_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'ACTIVE'
);

-- Mission sessions table
CREATE TABLE IF NOT EXISTS mission_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_identifier VARCHAR(255) UNIQUE NOT NULL,
    device_id UUID REFERENCES devices(id),
    started_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP,
    status VARCHAR(50) DEFAULT 'SESSION_INITIATED',
    total_blocks INTEGER DEFAULT 0
);

-- Capture blocks table
CREATE TABLE IF NOT EXISTS capture_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    block_identifier VARCHAR(255) UNIQUE NOT NULL,
    session_id UUID REFERENCES mission_sessions(id),
    device_id UUID REFERENCES devices(id),
    captured_at TIMESTAMP NOT NULL,
    frame_count INTEGER NOT NULL,
    quality_score DECIMAL(3,2),
    spatial_label VARCHAR(255),
    transmission_status VARCHAR(50) DEFAULT 'PENDING',
    received_at TIMESTAMP DEFAULT NOW(),
    checksum VARCHAR(255)
);

-- Frame metadata table
CREATE TABLE IF NOT EXISTS frame_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    block_id UUID REFERENCES capture_blocks(id),
    frame_index INTEGER NOT NULL,
    captured_at TIMESTAMP NOT NULL,
    camera_orientation JSONB,
    exposure_params JSONB,
    quality_flag BOOLEAN DEFAULT TRUE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_capture_blocks_session ON capture_blocks(session_id);
CREATE INDEX IF NOT EXISTS idx_capture_blocks_status ON capture_blocks(transmission_status);
CREATE INDEX IF NOT EXISTS idx_frame_metadata_block ON frame_metadata(block_id);
CREATE INDEX IF NOT EXISTS idx_devices_identifier ON devices(device_identifier);

-- Insert default device for prototype phone
INSERT INTO devices (device_identifier, device_type, status)
VALUES ('PIXEL5-PROTOTYPE-001', 'MOBILE_PROTOTYPE', 'ACTIVE')
ON CONFLICT (device_identifier) DO NOTHING;
