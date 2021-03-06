package completely;

import completely.data.SampleRecord;
import completely.text.analyze.SampleAnalyzer;

import java.io.Console;
import java.util.Locale;

public final class AutocompleteEngineSample
{
    @SuppressWarnings("checkstyle:leftcurly")
    private AutocompleteEngineSample() { }

    public static void main(String[] args)
    {
        AutocompleteEngine<SampleRecord> engine = new AutocompleteEngine.Builder<SampleRecord>()
            .setIndex(new SampleAdapter())
            .setAnalyzer(new SampleAnalyzer())
            .build();

        for (String country : Locale.getISOCountries())
        {
            Locale locale = new Locale("", country);
            engine.add(new SampleRecord(locale.getDisplayCountry()));
        }

        Console console = System.console();
        while (true)
        {
            String input = console.readLine("Query: ");
            for (SampleRecord record : engine.search(input))
            {
                console.printf("- %s%n", record.getName());
            }
        }
    }
}
