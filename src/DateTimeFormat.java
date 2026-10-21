import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public final class DateTimeFormat
{
    private static final DateTimeFormatter FORMAT = DateTimeFormatter
            .ofPattern("dd-MM-uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private DateTimeFormat() {}

    public static String format(Timestamp value)
    {
        return value.toLocalDateTime().format(FORMAT);
    }

    public static LocalDateTime parse(String value)
    {
        return LocalDateTime.parse(value.trim(), FORMAT);
    }
}
