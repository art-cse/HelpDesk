# FiberNet HelpDesk

FiberNet HelpDesk is a Java 17 Swing application made for a university Object-Oriented Programming project. It represents an IT and internet provider that supports business, institutional, and residential customers. Application data is stored in memory and realistic sample data is loaded at startup.

## Main features

- Login views for administrators, support agents, and customers
- Business, official/institutional, and residential customer types
- Automatic customer and ticket IDs
- Products and services assigned to customers
- Customer search and category/product filters
- Ticket registration with category-based priority
- Separate support-agent assignment and reassignment
- Ticket status updates and complete status history
- Customer and agent views limited to their own records

## OOP concepts

- `Customer` is an abstract superclass of `BusinessCustomer`, `OfficialCustomer`, and `ResidentialCustomer`.
- Overridden methods provide different priorities and support policies at runtime.
- Private fields and controlled methods provide encapsulation.
- `Identifiable` defines a common interface for stored objects.
- `Registry<T extends Identifiable>` demonstrates a simple generic class.
- Enums represent customer categories, ticket types, priorities, statuses, roles, and product types.
- `HelpDeskException` handles invalid business operations.
- Tickets are associated with customers, products, and support agents.
- Each ticket contains its own collection of `StatusChange` history entries.

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Administrator | `admin` | `admin123` |
| Support agent | `agent1` | `agent123` |
| Support agent | `agent2` | `agent123` |
| Residential customer | `customer1` | `customer123` |
| Business customer | `business1` | `business123` |

## Run from source

Open PowerShell in the repository and run:

```powershell
cd .\helpdesk-application
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

The Swing login window is the main application entry point.

## Build and test

Java 17 or newer is required.

Compile the source and tests:

```powershell
cd .\helpdesk-application
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Run the domain and role tests:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

Create the runnable JAR and Windows application:

```powershell
powershell -ExecutionPolicy Bypass -File .\package.ps1
```

Generated files are written to `helpdesk-application\dist\`. They are not committed because they can be recreated with the packaging script.

After packaging, run the JAR with:

```powershell
java -jar .\dist\FiberNetHelpDesk.jar
```

The Windows application is created at:

```text
dist\FiberNet HelpDesk\FiberNet HelpDesk.exe
```
