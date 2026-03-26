IF DB_ID(N'chn_examen') IS NULL
BEGIN
    CREATE DATABASE chn_examen;
END
GO

USE chn_examen;
GO

IF OBJECT_ID(N'dbo.Customer', N'U') IS NULL
BEGIN
    CREATE TABLE Customer (
        id INT IDENTITY(1,1) PRIMARY KEY,
        first_name NVARCHAR(100) NOT NULL,
        last_name NVARCHAR(100) NOT NULL,
        nit NVARCHAR(20) UNIQUE NOT NULL,
        date_of_birth DATE NOT NULL,
        address NVARCHAR(255),
        email NVARCHAR(100),
        phone_number NVARCHAR(20)
    );
END
GO

IF OBJECT_ID(N'dbo.Employee', N'U') IS NULL
BEGIN
    CREATE TABLE Employee (
        id INT IDENTITY(1,1) PRIMARY KEY,
        first_name NVARCHAR(100) NOT NULL,
        last_name NVARCHAR(100) NOT NULL,
        employee_code NVARCHAR(50) UNIQUE NOT NULL,
        position NVARCHAR(100)
    );
END
GO

IF OBJECT_ID(N'dbo.AppUser', N'U') IS NULL
BEGIN
    CREATE TABLE AppUser (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) UNIQUE NOT NULL,
        password NVARCHAR(255) NOT NULL,
        employee_id INT UNIQUE NOT NULL,
        CONSTRAINT fk_appuser_employee FOREIGN KEY (employee_id) REFERENCES Employee(id)
    );
END
GO

IF OBJECT_ID(N'dbo.LoanApplication', N'U') IS NULL
BEGIN
    CREATE TABLE LoanApplication (
        id INT IDENTITY(1,1) PRIMARY KEY,
        customer_id INT NOT NULL,
        requested_amount DECIMAL(15, 2) NOT NULL,
        term_in_months INT NOT NULL,
        status NVARCHAR(20) DEFAULT 'PENDING' NOT NULL,
        resolution_details NVARCHAR(MAX),
        application_date DATETIME2 DEFAULT GETDATE() NOT NULL,
        evaluated_by INT,
        CONSTRAINT chk_loanapp_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
        CONSTRAINT fk_loanapp_customer FOREIGN KEY (customer_id) REFERENCES Customer(id) ON DELETE CASCADE,
        CONSTRAINT fk_loanapp_user FOREIGN KEY (evaluated_by) REFERENCES AppUser(id)
    );
END
GO

IF OBJECT_ID(N'dbo.Loan', N'U') IS NULL
BEGIN
    CREATE TABLE Loan (
        id INT IDENTITY(1,1) PRIMARY KEY,
        loan_application_id INT UNIQUE NOT NULL,
        customer_id INT NOT NULL,
        approved_amount DECIMAL(15, 2) NOT NULL,
        outstanding_balance DECIMAL(15, 2) NOT NULL,
        payment_status NVARCHAR(20) DEFAULT 'CURRENT' NOT NULL,
        CONSTRAINT chk_loan_status CHECK (payment_status IN ('CURRENT', 'IN_ARREARS', 'PAID_OFF')),
        CONSTRAINT fk_loan_application FOREIGN KEY (loan_application_id) REFERENCES LoanApplication(id) ON DELETE CASCADE,
        CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES Customer(id)
    );
END
GO

IF OBJECT_ID(N'dbo.Payment', N'U') IS NULL
BEGIN
    CREATE TABLE Payment (
        id INT IDENTITY(1,1) PRIMARY KEY,
        loan_id INT NOT NULL,
        amount_paid DECIMAL(15, 2) NOT NULL,
        payment_date DATETIME2 DEFAULT GETDATE() NOT NULL,
        payment_method NVARCHAR(50) DEFAULT 'CASH' NOT NULL,
        registered_by INT NOT NULL,
        CONSTRAINT fk_payment_loan FOREIGN KEY (loan_id) REFERENCES Loan(id) ON DELETE CASCADE,
        CONSTRAINT fk_payment_user FOREIGN KEY (registered_by) REFERENCES AppUser(id)
    );
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM Employee
    WHERE employee_code = 'EMP-001'
)
BEGIN
    INSERT INTO Employee (first_name, last_name, employee_code, position)
    VALUES ('Ernesto', 'Juarez', 'EMP-001', 'Analista de Créditos');
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM AppUser
    WHERE username = 'admin'
)
BEGIN
    INSERT INTO AppUser (username, password, employee_id)
    SELECT
        'admin',
        '$2a$10$gZak./DjrsAlUdjsEZOuiuhj8k9Lt19tkK3bDQMMMUyTQCLxF8TdW',
        e.id
    FROM Employee e
    WHERE e.employee_code = 'EMP-001';
END
GO
