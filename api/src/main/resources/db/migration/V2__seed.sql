INSERT INTO teams (id, name, created_at) VALUES 
('11111111-1111-1111-1111-111111111111', 'Alpha Team', now()),
('22222222-2222-2222-2222-222222222222', 'Beta Team', now());

INSERT INTO users (id, email, name, password_hash, role, team_id, created_at) VALUES 
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'admin@relaydesk.dev', 'Alice Admin', '$2a$10$2FwCu0uNgOJmZXHKPQqei.39StFKBNTDIadwV3wzPt8uD.hm2Y6Ja', 'ROLE_ADMIN', '11111111-1111-1111-1111-111111111111', now()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'reviewer1@relaydesk.dev', 'Bob Reviewer', '$2a$10$2FwCu0uNgOJmZXHKPQqei.39StFKBNTDIadwV3wzPt8uD.hm2Y6Ja', 'ROLE_REVIEWER', '11111111-1111-1111-1111-111111111111', now()),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'reviewer2@relaydesk.dev', 'Carol Reviewer', '$2a$10$2FwCu0uNgOJmZXHKPQqei.39StFKBNTDIadwV3wzPt8uD.hm2Y6Ja', 'ROLE_REVIEWER', '22222222-2222-2222-2222-222222222222', now()),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'eng1@relaydesk.dev', 'Dave Engineer', '$2a$10$2FwCu0uNgOJmZXHKPQqei.39StFKBNTDIadwV3wzPt8uD.hm2Y6Ja', 'ROLE_ENGINEER', '11111111-1111-1111-1111-111111111111', now()),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'eng2@relaydesk.dev', 'Eve Engineer', '$2a$10$2FwCu0uNgOJmZXHKPQqei.39StFKBNTDIadwV3wzPt8uD.hm2Y6Ja', 'ROLE_ENGINEER', '22222222-2222-2222-2222-222222222222', now()),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'eng3@relaydesk.dev', 'Frank Engineer', '$2a$10$2FwCu0uNgOJmZXHKPQqei.39StFKBNTDIadwV3wzPt8uD.hm2Y6Ja', 'ROLE_ENGINEER', '11111111-1111-1111-1111-111111111111', now()),
('10101010-1010-1010-1010-101010101010', 'manager@relaydesk.dev', 'Grace Manager', '$2a$10$2FwCu0uNgOJmZXHKPQqei.39StFKBNTDIadwV3wzPt8uD.hm2Y6Ja', 'ROLE_RELEASE_MANAGER', null, now());

INSERT INTO change_requests (id, public_key, title, description, service, environment, risk_level, priority, status, rollback_plan, author_id, team_id, created_at, updated_at) VALUES
('00000000-0000-0000-0000-000000000001', 'CR-' || nextval('cr_seq'), 'Upgrade redis version', 'Upgrade redis to v7', 'cache-service', 'DEV', 'LOW', 'P2', 'DRAFT', null, 'dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', now(), now()),
('00000000-0000-0000-0000-000000000002', 'CR-' || nextval('cr_seq'), 'Fix bug in login', 'Fix bug where users cant login', 'auth-service', 'STAGING', 'HIGH', 'P1', 'SUBMITTED', null, 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-2222-2222-2222-222222222222', now(), now()),
('00000000-0000-0000-0000-000000000003', 'CR-' || nextval('cr_seq'), 'Add new payment gateway', 'Integrating stripe', 'payment-service', 'PROD', 'CRITICAL', 'P0', 'APPROVED', 'Revert commit and deploy', 'ffffffff-ffff-ffff-ffff-ffffffffffff', '11111111-1111-1111-1111-111111111111', now(), now()),
('00000000-0000-0000-0000-000000000004', 'CR-' || nextval('cr_seq'), 'Increase DB pool size', 'Setting pool size to 50', 'payment-service', 'PROD', 'LOW', 'P2', 'SHIPPED', null, 'ffffffff-ffff-ffff-ffff-ffffffffffff', '11111111-1111-1111-1111-111111111111', now(), now()),
('00000000-0000-0000-0000-000000000005', 'CR-' || nextval('cr_seq'), 'Change DNS records', 'Point to new load balancer', 'infra', 'PROD', 'CRITICAL', 'P0', 'SCHEDULED', 'Revert commit and deploy', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111', now(), now());

INSERT INTO releases (id, name, status, manager_id, shipped_at, created_at, updated_at) VALUES 
('33333333-3333-3333-3333-333333333333', 'R-26.1', 'SHIPPED', '10101010-1010-1010-1010-101010101010', now(), now(), now()),
('44444444-4444-4444-4444-444444444444', 'R-26.2', 'PLANNING', '10101010-1010-1010-1010-101010101010', null, now(), now());

INSERT INTO release_items (id, release_id, change_request_id, added_by, created_at) VALUES
('55555555-5555-5555-5555-555555555551', '33333333-3333-3333-3333-333333333333', '00000000-0000-0000-0000-000000000004', '10101010-1010-1010-1010-101010101010', now()),
('55555555-5555-5555-5555-555555555552', '44444444-4444-4444-4444-444444444444', '00000000-0000-0000-0000-000000000005', '10101010-1010-1010-1010-101010101010', now());
