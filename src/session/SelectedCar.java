package session;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Administrator
 */
public class SelectedCar {

    public static int Id;
    public static String Model;
    public static String Kategorija;
    public static int Godiste;
    public static int CenaPoDanu;
    public static int Kilometraza;
    public static String Boja;
    public static String Registracija;

    // Metoda za brzo čišćenje nakon što se ugovor kreira
    public static void clearVoziloSession() {
        Id = 0;
        Model = null;
        Kategorija = null;
        Godiste = 0;
        CenaPoDanu = 0;
        Kilometraza = 0;
        Boja = null;
        Registracija = null;
    }
}
