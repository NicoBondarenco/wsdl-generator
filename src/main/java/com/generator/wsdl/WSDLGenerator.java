package com.generator.wsdl;

import com.generator.wsdl.exception.WSDLGeneratorException;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJava;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

public class WSDLGenerator {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static void main(String[] args) {

        try {
            if (args.length == 0) {
                throw new WSDLGeneratorException("No parameters specified");
            }

            if (args[0].equals("-h") || args[0].equals("--help") || args[0].equals("-help")) {
                printHelp();
                if (args.length > 1 && args[1].equals("-l")) {
                    printLocales();
                }
                System.exit(0);
            }

            if (args.length < 2 || args.length % 2 != 0) {
                throw new WSDLGeneratorException("Wrong number of arguments");
            }

            HashMap<String, String> commands = new HashMap<>();
            for (int index = 0; index < args.length; index = index + 2) {
                String key = args[index];
                if (!key.startsWith("-")) {
                    throw new WSDLGeneratorException("Wrong command " + key);
                }
                commands.put(key, args[index + 1]);
            }

            Locale.setDefault(new Locale(commands.getOrDefault("-l", "en")));

            List<String> arguments = new ArrayList<>();
            arguments.add("-suppress-generated-date");
//            arguments.add("-fe");
//            arguments.add("jaxws21");
            arguments.add("-d");
            arguments.add(new File(commands.getOrDefault("-d", System.getProperty("user.dir"))).getAbsolutePath());
            if (commands.containsKey("-p")) {
                arguments.add("-p");
                arguments.add(commands.get("-p"));
            }

            if (commands.containsKey("-fe")) {
                arguments.add("-fe");
                arguments.add(commands.get("-fe"));
            }

            if (commands.containsKey("-f")) {
                arguments.add(commands.get("-f"));
            } else {
                throw new WSDLGeneratorException("WSDL file not informed");
            }

            WSDLToJava wsdlToJava = new WSDLToJava();
            wsdlToJava.setArguments(arguments.toArray(new String[arguments.size()]));
            wsdlToJava.run(new ToolContext(), new PrintStream(System.out));

        } catch (Throwable e) {
            System.out.println(ANSI_RED);
            e.printStackTrace();
            System.out.println(ANSI_RESET);
            printHelp();
        }
    }


    private static void printHelp() {
        getHelpList().forEach(System.out::println);
    }

    private static void printLocales() {
        System.out.println(ANSI_BLUE);
        System.out.println("******* AVAILABLE LANGUAGES *******");
        System.out.println(ANSI_RESET);
        Supplier<Stream<String>> supplier = getLanguagesStream();
        List<String> languages = getFormattedLanguages(supplier);
        System.out.println(getLanguagesCommand(languages));
    }

    private static List<String> getHelpList() {
        List<String> commands = new LinkedList<>();
        commands.add("\n\n");
        commands.add(ANSI_BLUE);
        commands.add("***************************************** COMMANDS HELP LIST *****************************************");
        commands.add(ANSI_RESET);
        commands.add("-h | -help | --help: lists the command list, to list the available locales, use -l after the help command. Ex.: -h -l");
        commands.add("");
        commands.add("-p <package>: the package that the WSDL classes will be generated. Ex.: -p com.my.package");
        commands.add("\t- if not specified, the default package of the wsdl file will be used");
        commands.add("");
        commands.add("-d <directory>: the directory where the WSDL classes will be generated. Ex.: -d C:/wsdl/generated");
        commands.add("\t- if not specified, the current directory will be used");
        commands.add("");
        commands.add("-l <language sufix>: the language that will be used on the WSDL classes on generation. Ex.: -l pt | -l en | -l uk");
        commands.add("\t- if not specified or not found, 'en' will be used as default");
        commands.add("");
        commands.add("-f <file location>: the location of the WSDL file (mandatory). Ex.: -f C:/wsdl/my_file.wsdl");
        commands.add("\t- if not specified, an error will be thrown");
        commands.add("");
        return commands;
    }

    private static Supplier<Stream<String>> getLanguagesStream() {
        return () -> Stream.of(Locale.getAvailableLocales())
                .filter(locale -> locale.getLanguage() != null && !locale.getLanguage().trim().isEmpty())
                .map(locale -> format("({0}) {1}", locale.getLanguage(), locale.getDisplayLanguage()))
                .distinct()
                .sorted();
    }

    private static List<String> getFormattedLanguages(Supplier<Stream<String>> supplier) {
        Integer maxLengh = supplier.get().max(Comparator.comparing(String::length)).get().length() + 2;
        LinkedList<String> languages = supplier.get().map(lang -> {
            String language = " " + lang;
            while (language.length() < maxLengh) {
                language += " ";
            }
            return language;
        }).collect(Collectors.toCollection(LinkedList::new));
        return languages;
    }

    private static String getLanguagesCommand(List<String> languages) {
        StringBuilder builder = new StringBuilder();
        builder.append("|");
        for (int index = 0; index < languages.size(); index++) {
            if (index > 0 && index % 4 == 0) {
                builder.append(System.lineSeparator()).append("|");
            }
            builder.append(languages.get(index)).append("|");
        }
        return builder.toString();
    }
}
