# FiberNet HelpDesk

Java 17 Object-Oriented Programming university project with a Java Swing desktop interface.

## 1. What was built

FiberNet HelpDesk is a fictional IT and internet service provider application. Its primary interface is a traditional Windows desktop GUI built with standard Java Swing. It supports:

- business, official/institutional, and residential customers;
- customer registration and editing;
- category-specific products and services;
- product assignment to customers;
- technical problems, service requests, and complaints;
- category-based treatment priority;
- support-agent assignment and reassignment;
- ticket status updates and complete status history;
- customer history;
- customer and ticket search/filtering;
- overview, customer, ticket, product, and support-agent tables.

The application loads realistic demonstration data at startup. Data is stored in memory for the current session because the supplied specification does not require a database or file persistence.

The implementation deliberately stays at university OOP level. It uses ordinary classes, inheritance, abstract methods, interfaces, `ArrayList`, a simple bounded generic class, enums, checked exceptions, Swing forms, and straightforward loops. It has no framework, database, web API, reflection, concurrency, or enterprise architecture.

## 2. Starting the finished application

### Simplest method on Windows

Open this file:

```text
dist\FiberNet HelpDesk\FiberNet HelpDesk.exe
```

The packaged application contains its own Java runtime, so the presentation computer does not need a separate Java installation.

### Runnable JAR

With Java 17 or newer installed:

```powershell
java -jar .\dist\FiberNetHelpDesk.jar
```

### Build and run from source

From the `helpdesk-application` directory:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

`Main` loads the system look and feel, creates the sample HelpDesk, and opens `HelpDeskFrame`. The GUI is the primary interface. The earlier console interface remains available only as an optional fallback:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp .\out helpdesk.ConsoleMain
```

## 3. Using the GUI

The main window has a normal menu bar and five tabs:

- **Overview:** counts and the active-ticket priority queue.
- **Customers:** search by identifying information, filter by category/product, add or edit customers, assign products, and view history.
- **Tickets:** search/filter, create tickets, assign agents, update status, and view details or history.
- **Products / Services:** inspect the catalog and assign an eligible product to a customer.
- **Support Agents:** inspect agents and view the tickets assigned to a selected agent.

Select a table row before using an action such as Edit, Assign Agent, Update Status, or View History. Invalid actions show a standard error dialog and leave the data unchanged.

## 4. Project structure

```text
helpdesk-application/
|-- src/main/java/helpdesk/
|   |-- Main.java, ConsoleMain.java, DemoData.java
|   |-- HelpDesk.java, Registry.java, Identifiable.java
|   |-- Customer.java
|   |-- BusinessCustomer.java
|   |-- OfficialCustomer.java
|   |-- ResidentialCustomer.java
|   |-- Product.java, SupportAgent.java, Ticket.java, StatusChange.java
|   |-- CustomerCategory.java, ProductType.java
|   |-- TicketType.java, TicketStatus.java, TreatmentPriority.java
|   |-- HelpDeskException.java
|   |-- HelpDeskFrame.java, DashboardPanel.java
|   |-- CustomersPanel.java, TicketsPanel.java
|   |-- ProductsPanel.java, SupportAgentsPanel.java
|   |-- CustomerDialog.java, TicketDialog.java
|   |-- AssignProductDialog.java, TextDialog.java
|   |-- ReadOnlyTableModel.java, GuiUtil.java
|   `-- ConsoleApp.java
|-- src/test/java/helpdesk/
|   |-- HelpDeskTest.java
|   |-- GuiWorkflowTest.java
|   `-- GuiVisualReview.java
|-- dist/                       runnable JAR and Windows application
|-- build.ps1, run.ps1, test.ps1, package.ps1
|-- .classpath, .project        Eclipse configuration
`-- .vscode/settings.json       VS Code Java configuration
```

The extracted lectures and original project specification are outside this application directory in `../course-material/`. The original `fwd.zip` remains separate and unchanged.

## 5. Final class architecture

```text
Identifiable (interface)
|-- Customer (abstract)
|   |-- BusinessCustomer
|   |-- OfficialCustomer
|   `-- ResidentialCustomer
|-- Product
|-- SupportAgent
`-- Ticket

Registry<T extends Identifiable>
|-- Registry<Customer>
|-- Registry<Product>
|-- Registry<SupportAgent>
`-- Registry<Ticket>

HelpDesk
|-- owns the four Registry objects
|-- coordinates registration, lookup, search, and filtering
`-- maintains customer/product/ticket/agent relationships

Main -> DemoData -> HelpDesk -> domain objects
Main -> HelpDeskFrame
HelpDeskFrame
|-- DashboardPanel
|-- CustomersPanel -> CustomerDialog / AssignProductDialog / TextDialog
|-- TicketsPanel   -> TicketDialog / TextDialog
|-- ProductsPanel -> AssignProductDialog
`-- SupportAgentsPanel -> TextDialog
```

`Main` only performs application startup. GUI classes handle presentation and input. `HelpDesk` coordinates use cases. Domain objects contain their own state, validation, and behavior.

## 6. Inheritance

The main domain hierarchy is:

```text
Customer (abstract)
|-- BusinessCustomer
|-- OfficialCustomer
`-- ResidentialCustomer
```

This is a genuine is-a relationship. All subtypes inherit ID, contact details, products, ticket history, common validation, and search behavior. Each subtype overrides category, treatment priority, support policy, category information, and category-information updates.

`HelpDeskException` extends Java's `Exception`. GUI classes also naturally inherit Swing components: `HelpDeskFrame` extends `JFrame`, the tab classes extend `JPanel`, and the form classes extend `JDialog`.

## 7. Runtime polymorphism

Collections and variables use the abstract type `Customer`, while their objects are concrete customer subclasses. The most important dynamic dispatch occurs in `HelpDesk.createTicket`:

```java
TreatmentPriority priority = customer.getTreatmentPriority();
```

Java chooses the method override from the actual object at runtime:

- `OfficialCustomer` returns `URGENT`.
- `BusinessCustomer` returns `HIGH`.
- `ResidentialCustomer` returns `STANDARD`.

The result changes the ticket's real priority and its position in the priority queue. The same runtime behavior is used by `getCategory()`, `getSupportPolicy()`, `getCategorySpecificInformation()`, and `updateCategoryInformation()` without type tests or duplicated switches.

Interface-based polymorphism also happens in `Registry<T>` when it calls `getId()` on customers, products, agents, and tickets through the same `Identifiable` contract.

## 8. Abstraction

`Customer` is abstract because a category-less generic customer would be incomplete. It stores the shared state and implements common operations while requiring subclasses to supply category-specific behavior.

`Identifiable` is a small abstraction containing only `getId()`. It describes the minimum capability needed by the generic registry.

The GUI also abstracts reusable presentation details modestly: `ReadOnlyTableModel` prevents editing directly inside tables, while actual changes go through validated dialogs and HelpDesk methods.

## 9. Interfaces and why they are used

- **`Identifiable`:** implemented by `Customer`, `Product`, `SupportAgent`, and `Ticket`. It lets one generic `Registry` perform ID lookup and duplicate validation for unrelated entity types.
- **`ActionListener`:** the standard Swing interface used by menu items and buttons so an action runs when the user clicks a control.
- **`Runnable`:** used for the Swing startup task and a small refresh callback shared by panels.

`Identifiable` is the project's own interface. It has four implementations and a real client, so it is not an artificial interface added merely to claim the concept.

## 10. Object relationships

### Association

A `Ticket` is associated with one `Customer`, one `Product`, and one responsible `SupportAgent`. These objects exist independently and are connected to collaborate on a support case.

### Aggregation

A `Customer` aggregates subscribed `Product` references. A product exists independently in the FiberNet catalog and may be assigned to several customers. A customer also aggregates ticket-history references, and a support agent aggregates assigned-ticket references.

### Composition

A `Ticket` creates and owns its `StatusChange` objects. A status-history entry has no independent purpose outside its ticket, and external code cannot insert entries directly. `HelpDesk` also creates and owns its four registry objects for its entire lifetime.

## 11. Generics and collections

The explicit generic class is:

```java
public class Registry<T extends Identifiable>
```

The bound guarantees that every stored object provides `getId()`. The same implementation is reused for `Customer`, `Product`, `SupportAgent`, and `Ticket` registries.

Typed collections include `ArrayList<Customer>`, `ArrayList<Product>`, `ArrayList<Ticket>`, `ArrayList<StatusChange>`, and `ArrayList<CustomerCategory>`. Typed Swing models such as `JComboBox<Customer>` are also used. The code uses ordinary and enhanced `for` loops instead of stream-heavy expressions.

## 12. Encapsulation and validation

- Domain fields are private.
- Stable identities are final and have no setter.
- Collection getters return new `ArrayList` copies.
- A ticket status changes only through `updateStatus`, never through an unrestricted setter.
- Customer changes go through validated update methods.
- Agent reassignment updates the ticket and both agents' assigned-ticket collections together.
- Required text, email form, prices, duplicate IDs, category eligibility, and object existence are validated.

This prevents GUI code from putting objects into impossible or inconsistent states.

## 13. Exception handling

`HelpDeskException` is a checked custom exception for expected business-rule failures, including:

- duplicate IDs;
- missing customers, products, agents, or tickets;
- assigning a product to an ineligible category;
- assigning the same product twice;
- opening a ticket for a product the customer does not own;
- repeated agent assignment;
- invalid or repeated ticket-status transitions.

Constructors and update methods use `IllegalArgumentException` for invalid basic values such as blank required fields or a malformed email. Swing actions catch these exceptions and show a readable `JOptionPane` error instead of closing the application.

Allowed status paths are intentionally clear:

```text
Open -> In progress
In progress -> Waiting for customer OR Resolved
Waiting for customer -> In progress OR Resolved
Resolved -> In progress OR Closed
Closed -> no further transition
```

Each successful transition adds a `StatusChange` containing the previous status, new status, date/time, and note.

## 14. SOLID principles at course level

- **Single Responsibility:** entities manage their own state; `HelpDesk` coordinates use cases; each panel manages one GUI area; dialogs gather one kind of input; `DemoData` creates sample data; `Registry` performs registration and lookup.
- **Open/Closed:** a new customer subtype can define its policy and priority through overrides without changing ticket creation.
- **Liskov Substitution:** every customer subtype can be used wherever `Customer` is expected, including the same collections, search, product assignment, and ticket creation.
- **Interface Segregation:** `Identifiable` contains only the single operation its registry client needs.
- **Dependency Inversion:** `Registry` depends on the `Identifiable` abstraction, and HelpDesk customer operations use the abstract `Customer` type.

SOLID is used only where it clarifies the assignment. There is no repository layer, dependency-injection container, or unnecessary design-pattern hierarchy.

## 15. HelpDesk requirement checklist

### Original common and Phase I requirements

- [x] The problem is modeled with organized classes and objects.
- [x] Polymorphism is used where customer behavior differs.
- [x] Register customer problems as `Ticket` objects.
- [x] Assign a responsible support person at creation and reassign later.
- [x] View all registered problems and their current status.
- [x] View problems assigned to a selected support person.
- [x] Update a ticket's status through validated transitions.
- [x] Retain the full status progress/history.

### Original Phase II requirements

- [x] Business customers are represented by `BusinessCustomer`.
- [x] Official/institutional customers are represented by `OfficialCustomer`.
- [x] Residential customers are represented by `ResidentialCustomer`.
- [x] Categories receive different support policy and treatment priority.
- [x] Products declare which customer categories are eligible.
- [x] Official, business, and residential tickets receive urgent, high, and standard priority.
- [x] Complaint/request/problem history is retained for every customer.
- [x] Customers can be listed/filtered by category.
- [x] Customers can be listed/filtered by product.
- [x] Customers can be searched using ID, name, email, phone, address, and category-specific identification.

### Requested desktop application functionality

- [x] Customer registration and editing dialogs.
- [x] Products/services associated with customers.
- [x] Ticket creation form with customer, product, type, agent, title, and description.
- [x] All-ticket table with priority, agent, status, and creation time.
- [x] Agent-specific ticket view.
- [x] Ticket details and history dialogs.
- [x] Search plus category/product/status/agent filters.
- [x] Traditional menu bar, tabbed desktop layout, standard controls, and system look and feel.
- [x] Realistic sample data for all three categories.
- [x] GUI is the primary entry point; optional console entry remains separate.
- [x] Runnable JAR and self-contained Windows app image in `dist`.

## 16. Build, test, and package

### Requirements for source development

- JDK 17 or newer
- PowerShell for the included scripts

No external libraries are required.

### Compile

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

### Run all automated checks

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

The test script runs five domain workflow groups and 30 Swing GUI checks. The GUI checks verify the frame, menus, tabs, table contents, searches, filters, refresh behavior, customer editing, ticket creation, product assignment, agent reassignment, status history, Save actions, and Cancel actions.

Expected final messages include:

```text
All 5 HelpDesk workflow tests passed.
All 30 Swing GUI checks passed.
```

### Rebuild the distributable files

`jpackage` is included in a full JDK:

```powershell
powershell -ExecutionPolicy Bypass -File .\package.ps1
```

This runs all tests first, then produces:

```text
dist\FiberNetHelpDesk.jar
dist\FiberNet HelpDesk\FiberNet HelpDesk.exe
```

### Manual PowerShell compilation

```powershell
New-Item -ItemType Directory -Path .\out -Force
$sources = Get-ChildItem .\src\main\java, .\src\test\java -Filter *.java -Recurse
javac --release 17 -encoding UTF-8 -d .\out $sources.FullName
java -cp .\out helpdesk.HelpDeskTest
java -cp .\out helpdesk.GuiWorkflowTest
java -cp .\out helpdesk.Main
```

### VS Code and Eclipse

Open `helpdesk-application` as the VS Code folder and run `helpdesk.Main`, or import it in Eclipse with **File -> Import -> General -> Existing Projects into Workspace**. The included settings use Java 17 source compatibility.

## 17. Verification performed

Before packaging, the project was:

- compiled with Java 17 compatibility;
- run through all five domain workflow groups;
- run through all 30 automated Swing checks;
- visually inspected using captures of all five tabs and the important dialogs;
- checked for clipped labels, unusable controls, empty demo screens, and inconsistent layout;
- packaged as both a JAR and Windows application;
- launched from the packaged JAR;
- launched from the packaged Windows executable.

Both packaged launch tests remained active normally and were then closed by the test process.

## 18. Recommended 5-10 minute presentation

1. **Introduction (30 seconds):** explain that FiberNet supports three customer categories and that the application uses in-memory demo data.
2. **Overview tab (45 seconds):** point out totals and the priority-ordered active queue. Compare urgent official, high business, and standard residential tickets.
3. **Class architecture (1 minute):** show abstract `Customer`, the three subclasses, `Identifiable`, and `Registry<T extends Identifiable>`.
4. **Customers tab (1 minute):** search for `INST-4402`, filter by category/product, then add or edit a customer.
5. **Product relationship (45 seconds):** assign an eligible product and explain aggregation plus category validation.
6. **Tickets tab (1.5 minutes):** create a ticket for a customer's assigned product, select its agent, and explain runtime priority calculation.
7. **Workflow and history (1 minute):** update the ticket to In progress and open its history. Explain `Ticket` composition with `StatusChange`.
8. **Agents tab (30 seconds):** select an agent and display assigned tickets; mention consistent reassignment.
9. **Tests (45 seconds):** run `test.ps1` and show the five domain groups plus 30 GUI checks.
10. **Closing (30 seconds):** summarize encapsulation, inheritance, abstraction, polymorphism, interfaces, generics, relationships, and exceptions.

For a predictable live demo, create a residential customer, assign `Home Fiber 300`, create a technical ticket, assign Arta, move it to In progress, and view its history.

## 19. Likely professor questions

### Why is `Customer` abstract?

A generic customer would have no category, policy, or priority. The abstract class holds shared fields and behavior while forcing each real category to provide its differences.

### Why not use only one Customer class and a large switch?

The categories differ in behavior and identifying data, not only in a label. Subclasses keep those differences inside the correct objects and allow dynamic dispatch.

### Where exactly does runtime polymorphism happen?

`HelpDesk.createTicket` holds a `Customer` reference and calls `getTreatmentPriority()`. Java selects the concrete override at runtime. The result changes the ticket queue, so it is real behavior, not decorative inheritance.

### What is the difference between the abstract class and interface?

`Customer` has fields, a constructor, implemented methods, and abstract methods. `Identifiable` is only a small contract that unrelated classes can implement.

### Why does `Identifiable` exist?

The one generic registry needs an ID from four unrelated entity classes. The interface lets it call `getId()` safely without knowing the concrete class.

### Explain `T extends Identifiable`.

It is a bounded generic type parameter. The compiler allows only identifiable objects in the registry and therefore knows that every `T` has `getId()`.

### Why is a Ticket-to-Customer link association, not inheritance?

A ticket is not a kind of customer. The two independent objects collaborate, so the ticket stores a customer reference.

### Why are Products aggregated by Customer?

The product belongs to the shared catalog and exists without a particular customer. Customers hold references to subscribed products; they do not own the products' lifetime.

### Why is StatusChange composition?

The ticket creates and owns each status entry. An entry describes one ticket's internal history and has no useful independent lifetime.

### How is encapsulation protected when returning lists?

Getters return new `ArrayList` objects. A caller can change the returned copy without changing the private collection inside the entity.

### Why is there no public setStatus method?

An unrestricted setter could skip validation and history. `updateStatus` checks the allowed transition first and creates the history entry only after success.

### How does agent reassignment stay consistent?

`HelpDesk.assignAgentToTicket` removes the ticket from the previous agent, changes the ticket's responsible-agent reference, and adds it to the new agent.

### Why does the GUI call HelpDesk instead of changing objects directly?

`HelpDesk` keeps multi-object operations and rules in one place. The GUI only collects input, calls the operation, refreshes tables, and reports errors.

### Why use Swing?

Swing is included with Java, needs no framework, and demonstrates an ordinary desktop GUI with forms, buttons, tables, dialogs, and event listeners. It is appropriate for the requested traditional university desktop application.

### Why is the data not saved after exit?

Persistence was not part of the supplied requirement. In-memory `ArrayList` storage keeps the assignment focused on the OOP concepts being graded.

### Could a new customer category be added?

Yes. A new subclass would implement the abstract customer methods. The category enum and GUI category choices would also need one new value, which is a reasonable small change for this course project.

## 20. Parts that may be slightly beyond the lectures

The domain model stays within the supplied lecture topics. A few supporting features may be newer than the earliest lectures:

- Swing's event-dispatch startup with `SwingUtilities.invokeLater`;
- anonymous `ActionListener` objects for button/menu events;
- `JTable` models and modal `JDialog` forms;
- `LocalDateTime` for readable ticket-history timestamps;
- `jpackage` for the self-contained Windows distribution.

These are standard library features used directly and can be explained simply. They do not add advanced domain architecture, third-party frameworks, concurrency logic, streams, lambdas, reflection, annotations, or design-pattern machinery.

