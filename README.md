# FiberNet HelpDesk

Java 17 Object-Oriented Programming university project with a Java Swing desktop interface.

The repository contains the complete source project in `helpdesk-application/`. The supplied university archive and extracted lectures are kept locally for reference but are intentionally excluded from Git because they are not application source or distribution files.

## Run the Windows application

Extract:

```text
helpdesk-application\dist\FiberNet-HelpDesk-Windows.zip
```

Then open `FiberNet HelpDesk.exe` inside the extracted `FiberNet HelpDesk` folder. Keep all extracted files together because the folder contains the application's private Java runtime.

## Run the JAR

With Java 17 or newer installed:

```powershell
java -jar .\helpdesk-application\dist\FiberNetHelpDesk.jar
```

## Build and run from source

```powershell
cd .\helpdesk-application
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Run all domain and Swing checks with:

```powershell
powershell -ExecutionPolicy Bypass -File .\test.ps1
```

See [helpdesk-application/README.md](helpdesk-application/README.md) for the complete architecture, OOP explanations, requirement checklist, packaging instructions, presentation sequence, and oral-exam questions.

