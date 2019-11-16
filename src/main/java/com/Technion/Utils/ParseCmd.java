package com.Technion.Utils;

import org.apache.commons.cli.*;

public class ParseCmd {

    public static CommandLine parse (Options options, String... args){

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return null;
        }
        return cmd;
    }
}
