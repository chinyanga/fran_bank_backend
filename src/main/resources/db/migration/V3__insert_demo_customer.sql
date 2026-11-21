
-- ============================================================
-- FRANBANK
-- V3: Insert Development Demo Customer and Account
-- Database: H2 (development only)
-- ============================================================

-- 1. DEMO CUSTOMER

INSERT INTO users (
    first_name,
    last_name,
    email,
    email_normalized,
    phone_number,
    password_hash,
    status,
    email_verified
)
VALUES (
           'Demo',
           'Customer',
           'demo@franbank.com',
           'demo@franbank.com',
           '+27771000001',
           '$2a$12$J2EUmOMzjViP9yjGHoB9oeyfJ8rshdCJir91DXE.6H8vOCb0Ylrq2',
           'ACTIVE',
           TRUE
       );

-- 2. ASSIGN CUSTOMER ROLE

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         CROSS JOIN roles r
WHERE u.email_normalized = 'demo@novabank.com'
  AND r.name = 'CUSTOMER';

-- 3. CREATE CUSTOMER LIABILITY LEDGER ACCOUNT

INSERT INTO ledger_accounts (
    account_code,
    account_name,
    account_type,
    currency,
    status
)
VALUES (
           'FRANBANK-DEMO-CUSTOMER-USD',
           'Franbank Demo Customer Liability',
           'LIABILITY',
           'USD',
           'ACTIVE'
       );

-- 4. CREATE DEMO SAVINGS ACCOUNT

INSERT INTO accounts (
    user_id,
    ledger_account_id,
    account_number,
    account_name,
    account_type,
    currency,
    balance,
    status
)
SELECT
    u.id,
    la.id,
    '900000000001',
    'Franbank Demo Savings',
    'SAVINGS',
    'USD',
    1000.0000,
    'ACTIVE'
FROM users u
         CROSS JOIN ledger_accounts la
WHERE u.email_normalized = 'demo@franbank.com'
  AND la.account_code = 'FRANBANK-DEMO-CUSTOMER-USD';

-- 5. CREATE OPENING DEPOSIT TRANSACTION

INSERT INTO bank_transactions (
    reference,
    transaction_type,
    status,
    source_account_id,
    destination_account_id,
    amount,
    currency,
    description,
    created_by,
    completed_at
)
SELECT
    'FRANBANK-DEMO-OPENING-001',
    'DEPOSIT',
    'COMPLETED',
    NULL,
    a.id,
    1000.0000,
    'USD',
    'Franbank demo account opening balance',
    u.id,
    CURRENT_TIMESTAMP
FROM users u
         CROSS JOIN accounts a
WHERE u.email_normalized = 'demo@franbank.com'
  AND a.account_number = '900000000001';

-- 6. DEBIT BANK CASH LEDGER

INSERT INTO ledger_entries (
    transaction_id,
    ledger_account_id,
    entry_side,
    amount,
    currency,
    description
)
SELECT
    bt.id,
    la.id,
    'DEBIT',
    1000.0000,
    'USD',
    'Franbank demo opening deposit - bank cash'
FROM bank_transactions bt
         CROSS JOIN ledger_accounts la
WHERE bt.reference = 'FRANBANK-DEMO-OPENING-001'
  AND la.account_code = 'BANK-CASH-USD';

-- 7. CREDIT CUSTOMER LIABILITY LEDGER

INSERT INTO ledger_entries (
    transaction_id,
    ledger_account_id,
    entry_side,
    amount,
    currency,
    description
)
SELECT
    bt.id,
    la.id,
    'CREDIT',
    1000.0000,
    'USD',
    'Franbank demo opening deposit - customer liability'
FROM bank_transactions bt
         CROSS JOIN ledger_accounts la
WHERE bt.reference = 'FRANBANK-DEMO-OPENING-001'
  AND la.account_code = 'FRANBANK-DEMO-CUSTOMER-USD';

-- ============================================================
-- END OF FRANBANK V3 MIGRATION
-- ============================================================
