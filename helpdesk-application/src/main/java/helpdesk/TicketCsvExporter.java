package helpdesk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TicketCsvExporter {
    private TicketCsvExporter() {
    }

    public static void write(File file, ArrayList<Ticket> tickets, ReportPeriod period,
            LocalDate reportDate, String filters) throws IOException {
        LocalDate startDate = period.getStartDate(reportDate);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write('\uFEFF'); // Helps Excel recognize UTF-8 names and descriptions.
            writeRow(writer, new String[] { "Period", period.toString() });
            writeRow(writer, new String[] { "From (inclusive)",
                    startDate == null ? "All time" : startDate.toString() });
            writeRow(writer, new String[] { "Through (inclusive)", reportDate.toString() });
            writeRow(writer, new String[] { "Filters", filters });
            writeRow(writer, new String[] { "Tickets created", String.valueOf(tickets.size()) });
            writer.write("\r\n");
            writeRow(writer, new String[] { "Ticket ID", "Customer ID", "Customer",
                    "Customer category", "Product / Service", "Type", "Subject",
                    "Description", "Priority", "Assigned Agent", "Status", "Created Date" });

            for (Ticket ticket : tickets) {
                writeRow(writer, new String[] { ticket.getId(), ticket.getCustomer().getId(),
                        ticket.getCustomer().getName(), ticket.getCustomer().getCategory().toString(),
                        ticket.getProduct().getName(), ticket.getType().toString(),
                        ticket.getTitle(), ticket.getDescription(), ticket.getPriority().toString(),
                        ticket.getResponsibleAgentName(), ticket.getStatus().toString(),
                        ticket.getCreatedAt().format(dateFormat) });
            }
        }
    }

    private static void writeRow(BufferedWriter writer, String[] fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            String value = fields[i];
            String trimmed = value.stripLeading();
            if (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0) {
                value = "'" + value; // Keep spreadsheet formulas in user text as plain text.
            }
            writer.write('"');
            writer.write(value.replace("\"", "\"\""));
            writer.write('"');
        }
        writer.write("\r\n");
    }
}
