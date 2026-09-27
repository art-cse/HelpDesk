package helpdesk;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

public class TicketReportTest {
    public static void main(String[] args) throws Exception {
        TicketReportTest test = new TicketReportTest();
        test.testCalendarBoundaries();
        test.testCombinedFilters();
        test.testCsvExport();
        System.out.println("Ticket report tests passed.");
    }

    private void testCalendarBoundaries() {
        LocalDate today = LocalDate.of(2026, 9, 27);
        check(ReportPeriod.LAST_30_DAYS.getStartDate(today).equals(LocalDate.of(2026, 8, 29)),
                "30 days must include today and the previous 29 days.");
        check(ReportPeriod.LAST_60_DAYS.getStartDate(today).equals(LocalDate.of(2026, 7, 30)),
                "60 days must include today and the previous 59 days.");
        check(ReportPeriod.CURRENT_QUARTER.getStartDate(today).equals(LocalDate.of(2026, 7, 1)),
                "The current quarter must start on July 1.");
        check(ReportPeriod.CURRENT_YEAR.getStartDate(today).equals(LocalDate.of(2026, 1, 1)),
                "The current year must start on January 1.");

        for (ReportPeriod period : ReportPeriod.values()) {
            check(period.includes(today.atTime(23, 59, 59), today),
                    "All of the final calendar day must be included.");
            check(!period.includes(today.plusDays(1).atStartOfDay(), today),
                    "Future dates must not be counted.");
            LocalDate start = period.getStartDate(today);
            if (start != null) {
                check(period.includes(start.atStartOfDay(), today),
                        "The start date must be included.");
                check(!period.includes(start.minusDays(1).atTime(23, 59, 59), today),
                        "Tickets before the start date must be excluded.");
            }
        }

        LocalDate newYear = LocalDate.of(2027, 1, 1);
        check(ReportPeriod.CURRENT_QUARTER.getStartDate(newYear).equals(newYear),
                "The quarter must reset at the new year.");
        check(!ReportPeriod.CURRENT_YEAR.includes(newYear.minusDays(1).atStartOfDay(), newYear),
                "Last year's tickets must be excluded at the new year.");
        LocalDate newQuarter = LocalDate.of(2026, 10, 1);
        check(!ReportPeriod.CURRENT_QUARTER.includes(today.atStartOfDay(), newQuarter),
                "Last quarter's tickets must be excluded at the quarter boundary.");
        check(ReportPeriod.LAST_30_DAYS.getStartDate(LocalDate.of(2024, 3, 1))
                .equals(LocalDate.of(2024, 2, 1)), "Leap days must be handled correctly.");
    }

    private void testCombinedFilters() throws Exception {
        HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
        Ticket ticket = helpDesk.getTicket("T-1001");
        LocalDate created = ticket.getCreatedAt().toLocalDate();

        check(helpDesk.filterTickets("warehouse", TicketStatus.IN_PROGRESS, "A-01",
                ReportPeriod.LAST_30_DAYS, created.plusDays(29)).contains(ticket),
                "Date, search, status, and agent filters must work together.");
        check(helpDesk.filterTickets("warehouse", null, null,
                ReportPeriod.LAST_30_DAYS, created.plusDays(30)).isEmpty(),
                "A ticket must age out of the 30-day report.");
        check(helpDesk.filterTickets("warehouse", null, null,
                ReportPeriod.LAST_60_DAYS, created.plusDays(59)).contains(ticket),
                "The first day of the 60-day report must be included.");
        check(helpDesk.filterTickets("warehouse", null, null,
                ReportPeriod.LAST_60_DAYS, created.plusDays(60)).isEmpty(),
                "A ticket must age out of the 60-day report.");
        check(helpDesk.filterTickets("warehouse", TicketStatus.CLOSED, null,
                ReportPeriod.CURRENT_YEAR, created).isEmpty(),
                "A date match must not override a status mismatch.");
        check(helpDesk.filterTickets("", null, HelpDesk.UNASSIGNED_AGENT_FILTER,
                ReportPeriod.CURRENT_QUARTER, created).contains(helpDesk.getTicket("T-1004")),
                "Unassigned tickets must remain reportable.");
        check(helpDesk.filterTickets("", TicketStatus.CLOSED, null,
                ReportPeriod.CURRENT_YEAR, created).contains(helpDesk.getTicket("T-1003")),
                "Reports must count creation dates even when a ticket is closed.");
    }

    private void testCsvExport() throws Exception {
        HelpDesk helpDesk = DemoData.createHelpDeskWithSampleData();
        Ticket ticket = helpDesk.createTicket("C-RES-001", "P-101", TicketType.TECHNICAL_PROBLEM,
                "Router, \"offline\"", "Connection failed\nSecond line: caf\u00e9.");
        helpDesk.createTicket("C-RES-001", "P-101", TicketType.SERVICE_REQUEST,
                "=1+1", "Formula-like text must remain text.");
        ArrayList<Ticket> matches = helpDesk.filterTickets("", TicketStatus.OPEN,
                HelpDesk.UNASSIGNED_AGENT_FILTER, ReportPeriod.CURRENT_YEAR,
                ticket.getCreatedAt().toLocalDate());
        Path directory = Files.createTempDirectory("helpdesk-report-test-");
        File file = directory.resolve("report.csv").toFile();
        try {
            TicketCsvExporter.write(file, matches, ReportPeriod.CURRENT_YEAR,
                    ticket.getCreatedAt().toLocalDate(), "Status: Open; Agent: Unassigned");
            String csv = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            check(csv.startsWith("\uFEFF\"Period\",\"Current year\"\r\n"),
                    "CSV must identify its period and use Excel-compatible UTF-8.");
            check(csv.contains("\"Tickets created\",\"" + matches.size() + "\"\r\n"),
                    "CSV must include the matching ticket count.");
            check(csv.contains("\"Router, \"\"offline\"\"\""),
                    "CSV must escape embedded commas and quotation marks.");
            check(csv.contains("\"Connection failed\nSecond line: caf\u00e9.\""),
                    "CSV must preserve multiline descriptions and Unicode.");
            check(csv.contains("\"'=1+1\""), "Spreadsheet formulas must be exported as text.");
            check(!csv.contains("\"T-1003\""), "Excluded tickets must not appear in the CSV.");

            TicketCsvExporter.write(file, new ArrayList<Ticket>(), ReportPeriod.LAST_30_DAYS,
                    ticket.getCreatedAt().toLocalDate(), "Search: no matches");
            csv = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            check(csv.contains("\"Tickets created\",\"0\"\r\n")
                    && csv.contains("\"Ticket ID\"") && !csv.contains(ticket.getId()),
                    "An empty report must still contain its count and column headings.");

            boolean writeFailed = false;
            try {
                TicketCsvExporter.write(directory.toFile(), matches, ReportPeriod.CURRENT_YEAR,
                        ticket.getCreatedAt().toLocalDate(), "All");
            } catch (IOException exception) {
                writeFailed = true;
            }
            check(writeFailed, "Write failures must reach the GUI error handler.");
        } finally {
            Files.deleteIfExists(file.toPath());
            Files.deleteIfExists(directory);
        }
    }

    private void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
