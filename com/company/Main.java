package com.company;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * Paraméterek:
 *  -i: Bemeneti fájl neve (Kötelező)
 *  -o: Kimeneti fájl neve (Kötelező)
 *  -p: Minta, ami alapján a program egyezést keres (Kötelező)
 *  -gi: Tömörített bemenet (Nem kötelező)
 *  -go: Tömörített kimenet (Nem kötelező)
 *
 * Tömörítés esetén a helyes fájlnév beállítása a felhasználó felelőssége!
 */

/** Main osztály, nem példányosítható, így statikus lesz minden tag, nem tartozik konkrét példányhoz */
public class Main {
    /** Input fájl neve */
    public static String input;
    /** Output fájl neve */
    public static String output;
    /** Keresendő szűrő */
    public static String pattern;
    /** Használunk-e tömörítést a bemeneten */
    public static boolean gin = false;
    /** Használunk-e tömörítést a kimeneten */
    public static boolean gout = false;
    /** Találatokat tároló lista */
    public static ArrayList<String> matches;
    /** A jelenlegi working directory */
    public static String wd = System.getProperty("user.dir");

    /** Paraméterek beolvasása a parancssorról */
    public static void getParameters(String[] args) {
        /* Kapott kódrészlet, az alap flagek felismerését végzi */
        for (int i = 0; i < args.length; i++) {
            if ((i+1 < args.length) && args[i].equals("-i")) {
                i++; input = args[i];
            } else if ((i+1 < args.length) && args[i].equals("-o")) {
                i++; output = args[i];
            } else if ((i+1 < args.length) && args[i].equals("-p")) {
                i++; pattern = args[i];
            }
        }
        /* Forrás: https://www.iit.bme.hu/system/files/uploads/module_files/java_3_io_tasks_0.pdf */
        /* Tömörítést jelző flagek, tömörítés esetén a felhasználó felelőssége a helyes fájlkiterjesztést beállítani */
        if(Arrays.asList(args).contains("-gi")) gin = true;
        if(Arrays.asList(args).contains("-go")) gout = true;
    }

    /** Végeredmény mentése a megadott kimeneti fájlba, bájtonként mentünk, tömörítést használunk ha kell */
    public static void saveResults() throws IOException {
        /* Output fájl */
        OutputStream file = new FileOutputStream(String.valueOf(Paths.get(wd, output)));
        /* Megvizsgáljuk, hogy használunk e tömörítést */
        if(gout) file = new GZIPOutputStream(file);
        /* Végigmegyünk az eltárolt sorokon */
        for(String line : matches) {
            /* Bájtonként beleírjuk a fájlba az eredményt */
            for(int i=0;i<line.length();i++) file.write((int) line.charAt(i));
        }
        /* Bezárjuk a fájlt */
        file.close();
    }

    /** Beolvassuk az input fájlban lévő adatot soronként, majd megvizsgáljuk, hogy egyezik e az adott mintával */
    public static void searchForPattern() throws IOException {
        /* Input fájl */
        InputStream file = new FileInputStream(String.valueOf(Paths.get(wd, input)));
        /* Megvizsgáljuk, hogy használunk e tömörítést */
        if(gin) file = new GZIPInputStream(file);
        /* Sort tároló sztring */
        StringBuilder line = new StringBuilder();
        /* Adat, a fájlból egy adott byte */
        int data = file.read();
        /* Ameddig van adatunk, addig olvasunk be bájtonként */
        while(data != -1) {
            /* Ha elértük a sor végét, feldolgozzuk a sort */
            if((char) data == '\n') {
                /* Ha egyezik, akkor hozzáadjuk a mentendő listához */
                if(Pattern.compile(pattern).matcher(line.toString()).find()) matches.add(line.toString());
                /* Nullázzuk a jelenleg eltárolt sort */
                line.setLength(0);
             /* Egyéb esetben, ha nem sor vége jel, akkor a karaktert egy StringBuilderhez fűzzük, ez tartalmazza az adott sort */
            }else line.append((char) data);
            /* Felülírjuk az adatot a következő bájtal */
            data = file.read();
        }
        /* Bezárjuk a fájlt */
        file.close();
    }

    /** Fő metódus */
    public static void main(String[] args) {
        /* ArrayList definiálása, egyéb esetben null pointer exception */
        matches = new ArrayList<>();
        try {
            /* Paraméterek beolvasása */
            Main.getParameters(args);
            /* Egyezés keresése */
            Main.searchForPattern();
            /* Eredmény mentése */
            Main.saveResults();
        }catch(Exception e) {
            /* Hibakezelés */
            e.printStackTrace();
            /* Hiba esetén kilépünk */
            System.exit(1);
        }
    }
}
