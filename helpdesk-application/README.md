# FiberNet HelpDesk

FiberNet HelpDesk is a Java 17 Object-Oriented Programming university project for a fictional IT and internet service provider. It uses a traditional Java Swing desktop interface and keeps all data in memory for the current application session.

## Main functionality

- Business, official/institutional, and residential customers
- Category-specific products and treatment priorities
- Customer, product, support-agent, and ticket tables
- Customer search and filtering
- Ticket status and history tracking
- System-generated sequential ticket IDs; users never enter them manually
- Separate ticket registration and support-agent assignment
- Unassigned tickets with safe `Unassigned` display text
- Administrator, support-agent, and customer access views
- Customer ticket submission without selecting an agent or priority
- Agent My Tickets view with status updates
- Logout back to the login window
- Realistic sample data available immediately after login

The normal workflow is:

```text
Customer reports a problem
        -> system assigns a ticket ID; ticket is Open and Unassigned
Administrator assigns a support agent
        -> agent sees it in My Tickets
Agent updates or resolves the ticket
        -> customer sees the status and history
```

## Demo accounts

Authentication is intentionally simple demonstration data. It is not production security.

| Role | Username | Password | Linked record |
|---|---|---|---|
| Administrator | `admin` | `admin123` | Full HelpDesk access |
| Support agent | `agent1` | `agent123` | Arta Krasniqi (`A-01`) |
| Support agent | `agent2` | `agent123` | Blerim Hoxha (`A-02`) |
| Customer | `customer1` | `customer123` | Era Dervishi (`C-RES-001`) |
| Customer | `business1` | `business123` | Alba Logistics LLC (`C-BIZ-001`) |

## Role views

### Administrator

Uses the existing complete HelpDesk window: Overview, Customers, Tickets, Products / Services, and Support Agents. The administrator can create tickets, assign or reassign agents, update status, and manage customer/product relationships.

### Support agent

Sees only tickets assigned to the linked agent. The agent can view details/history and update ticket status, but cannot manage customers, products, agents, or unrelated tickets.

### Customer

Sees only the linked customer's account, products, tickets, current status, and history. The customer can submit a new ticket for one of their own products. The system assigns its ID, and it starts `OPEN` and `Unassigned`.

## OOP concepts demonstrated

- **Encapsulation:** private fields, controlled updates, validation, and defensive `ArrayList` copies
- **Inheritance:** abstract `Customer` with `BusinessCustomer`, `OfficialCustomer`, and `ResidentialCustomer`
- **Abstraction:** abstract customer behavior and the `Identifiable` interface
- **Runtime polymorphism:** ticket priority comes from `customer.getTreatmentPriority()` on the actual customer subtype
- **Interfaces:** `Identifiable` supports reusable ID-based registration; Swing uses `ActionListener` and `Runnable`
- **Generics:** `Registry<T extends Identifiable>` stores customers, products, agents, and tickets
- **Association:** a ticket references its customer, product, and optional support agent
- **Aggregation:** customers reference products/tickets and agents reference assigned tickets
- **Composition:** a ticket owns its `StatusChange` history
- **Exceptions and enums:** `HelpDeskException`, validation errors, roles, statuses, categories, types, and priorities

## Project structure

```text
helpdesk-application/
|-- src/main/java/helpdesk/
|   |-- Main.java, DemoData.java
|   |-- LoginFrame.java, HelpDeskFrame.java
|   |-- AgentFrame.java, CustomerFrame.java
|   |-- Customer.java and three customer subclasses
|   |-- Ticket.java, Product.java, SupportAgent.java, StatusChange.java
|   |-- HelpDesk.java, Registry.java, Identifiable.java
|   |-- UserAccount.java, UserRole.java, AuthenticationService.java
|   `-- Swing panels and dialogs
|-- src/test/java/helpdesk/
|   |-- HelpDeskTest.java
|   |-- GuiWorkflowTest.java
|   |-- RoleWorkflowTest.java
|   `-- GuiVisualReview.java
|-- dist/
|-- build.ps1, run.ps1, test.ps1, package.ps1
|-- .classpath, .project, .settings/
`-- .vscode/settings.json
```

## Run

### Windows application

Extract:

```text
dist\FiberNet-HelpDesk-Windows.zip
```

Then open `FiberNet HelpDesk.exe` inside the extracted folder. Keep the whole extracted folder together because it contains the private Java runtime.

### Runnable JAR

Java 17 or newer is required:

```powershell
java -jar .\dist\FiberNetHelpDesk.jar
```

### Run from source

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

The Swing login window is the primary application. The console fallback can still be started after building with:

```powershell
java -cp .\out helpdesk.ConsoleMain
```

## Build and test

Compile main and test sources:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Run all domain, admin GUI, and role workflow checks:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

The current suites contain 6 domain workflow groups, 33 admin GUI checks, and 44 role workflow checks.

Rebuild the runnable JAR, Windows app image, and compressed Windows distribution:

```powershell
powershell -ExecutionPolicy Bypass -File .\package.ps1
```

Final distributable files are written to `dist/`.
