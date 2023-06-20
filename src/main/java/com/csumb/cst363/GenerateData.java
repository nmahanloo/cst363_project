package com.csumb.cst363;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Random;

public class GenerateData {
    public static void main(String[] args) {
        final String db_url= "jdbc:mysql://localhost:3306/pharmacies";
        final String user= "root";
        final String password= "1234";

        Random random = new Random();

       try {
           Connection conn = DriverManager.getConnection(db_url, user, password);
           if (checkDatabaseIsEmpty(conn)) {
               for (int i = 0; i < 10; i++) {
                   addpharmacy(conn, random);
               }

               for (int i = 0; i < 5; i++) {
                   adddoctor(conn, random, true);
               }
               for (int i = 0; i < 5; i++) {
                   adddoctor(conn, random, false);
               }
               for (int i = 0; i < 100; i++) {
                   addpatient(conn, random);
               }
               //for (int i = 0; i < 100; i++) {
               //adddrug(conn, random, i);
               //}
               generatedrug(conn);
               for (int i = 0; i < 100; i++) {
                   addprescription(conn, random);
               }
           }

       }catch (SQLException e){
           e.printStackTrace();
       } catch (ParseException e) {
           throw new RuntimeException(e);
       }
    }

    public static boolean checkDatabaseIsEmpty(Connection connection) throws SQLException {
        boolean empty = true;
        String sql = "SELECT PharmacyID FROM PHARMACY";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            empty = false;
        }
        sql = "SELECT PatientID FROM PATIENT";
        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        if (rs.next()) {
            empty = false;
        }
        sql = "SELECT PhysicianID FROM PHYSICIAN";
        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        if (rs.next()) {
            empty = false;
        }
        sql = "SELECT Drug_ID FROM DRUG";
        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        if (rs.next()) {
            empty = false;
        }
        sql = "SELECT RxID FROM PRESCRIPTION";
        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        if (rs.next()) {
            empty = false;
        }
        return empty;
    }

    public static void adddrug(Connection connection, Random random, int i) throws SQLException{
        String Trade_Name = trade_names[i];
        String formula = formulas[random.nextInt(formulas.length)];
        String sql = "insert into drug(trade_name, formula) values ('%s', '%s')".formatted(Trade_Name, formula);
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }
    public static void addpharmacy(Connection connection, Random random) throws SQLException{
        String firstname = Names[random.nextInt(Names.length)];
        String name = firstname+" Drugs";
        String street = random.nextInt(1000)+" "+Names[random.nextInt(Names.length)];
        String state = states[random.nextInt(states.length)];
        String city = Names[random.nextInt(Names.length)];
        int zip=random.nextInt(89099) + 10000;
        String address = "%s, %s, %s, %d".formatted(street, city, state, zip);
        String phonenumber = GetRandomPhone(random);
        String sql = "insert into pharmacy(name, address, phone) values ('%s', '%s','%s')".formatted(name, address, phonenumber);
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }
    public static void addpatient(Connection connection, Random random) throws SQLException, ParseException {
        String firstname = Names[random.nextInt(Names.length)];
        int physician_id;
        String lastname = lastnames[random.nextInt(lastnames.length)];
        int month = random.nextInt(12);
        String a = month + "/" + random.nextInt(31) + "/" + (random.nextInt(100) + 1923);
        if (month < 10) {
            a = "0" + a;
        }
        Date birthdate = new SimpleDateFormat("MM/dd/yyyy").parse(a);
        long time =  new Date().getTime() - birthdate.getTime();
        if (time > 1000L * 60L * 60L * 24L * 365L * 18L) {
            // Adult assign non-pediatrician
            physician_id = random.nextInt(5) + 6;
        } else {
            // Child assign pediatrician
            physician_id = random.nextInt(5) + 1;
        }
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(birthdate);
        String ssn = GetRandomSSN(random);
        String street = random.nextInt(1000)+" "+Names[random.nextInt(Names.length)];
        String state = states[random.nextInt(states.length)];
        String city = Names[random.nextInt(Names.length)];
        int zip=random.nextInt(89099) + 10000;
//        long phonenumber = random.nextLong(1000000000l, 9000000000l);
        String sql = "insert into patient(firstname, PhysicianID, lastname, birthdate, ssn, street, city, state, zipcode) values ('%s', %d, '%s', '%s', '%s', '%s', '%s', '%s', '%d')".formatted(firstname,  physician_id, lastname, dateString, ssn, street, city, state, zip);
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }
    public static void adddoctor(Connection connection, Random random, boolean isPediatrician) throws SQLException {
        String firstname = Names[random.nextInt(Names.length)];
        //String middlename = Names[random.nextInt(Names.length)];
        String lastname = lastnames[random.nextInt(lastnames.length)];
        String specialty;// = titles[random.nextInt(titles.length)];
        if (isPediatrician){
            specialty = "Pediatrician";
        }else {
            specialty = titles[random.nextInt(titles.length)];
        }
        //Date birthdate = new Date(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() - random.nextLong(1000L*60L*60L*24L*365L*110L));
        //String dateString = new SimpleDateFormat("yyyy-MM-dd").format(birthdate);
        String ssn = GetRandomSSN(random);
        //String address = random.nextInt(1000)+" "+Names[random.nextInt(Names.length)];
        //String state = states[random.nextInt(states.length)];
        //String city = Names[random.nextInt(Names.length)];
        int year=random.nextInt(73) + 1950;

        //long phonenumber = random.nextLong(1000000000l, 9000000000l);
        String sql = "insert into physician(firstname, lastname, ssn, specialty, firstyearofpractice) values ('%s', '%s', '%s', '%s', '%d')".formatted(firstname, lastname, ssn,specialty, year);
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    public static String GetRandomPhone(Random random) {
        StringBuilder phone = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            phone.append(random.nextInt(9));
        }
        return phone.toString();
    }

    public static String GetRandomSSN(Random random) {
        StringBuilder phone = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            phone.append(random.nextInt(9));
        }
        return phone.toString();
    }

    public static void generatedrug(Connection connection) throws SQLException {
        String sql = "INSERT INTO `drug` VALUES \n" +
                "(1,'Tylenol with Codeine','acetaminophen and codeine'),\n" +
                "(2,'Proair Proventil','albuterol aerosol'),\n" +
                "(3,'Accuneb','albuterol HFA'),\n" +
                "(4,'Fosamax','alendronate'),\n" +
                "(5,'Zyloprim','allopurinol'),\n" +
                "(6,'Xanax','alprazolam'),\n" +
                "(7,'Elavil','amitriptyline'),\n" +
                "(8,'Augmentin','amoxicillin and clavulanate K+'),\n" +
                "(9,'Amoxil','amoxicillin'),\n" +
                "(10,'Adderall XR','amphetamine and dextroamphetamine XR'),\n" +
                "(11,'Tenormin','atenolol'),\n" +
                "(12,'Lipitor','atorvastatin'),\n" +
                "(13,'Zithromax','azithromycin'),\n" +
                "(14,'Lotrel','benazepril and amlodipine'),\n" +
                "(15,'Soma','carisoprodol'),\n" +
                "(16,'Coreg','carvedilol'),\n" +
                "(17,'Omnicef','cefdinir'),\n" +
                "(18,'Celebrex','celecoxib'),\n" +
                "(19,'Keflex','cephalexin'),\n" +
                "(20,'Cipro','ciprofloxacin'),\n" +
                "(21,'Celexa','citalopram'),\n" +
                "(22,'Klonopin','clonazepam'),\n" +
                "(23,'Catapres','clonidine HCl'),\n" +
                "(24,'Plavix','clopidogrel'),\n" +
                "(25,'Premarin','conjugated estrogens'),\n" +
                "(26,'Flexeril','cyclobenzaprine'),\n" +
                "(27,'Valium','diazepam'),\n" +
                "(28,'Voltaren','diclofenac sodium'),\n" +
                "(29,'Yaz','drospirenone and ethinyl estradiol'),\n" +
                "(30,'Cymbalta','Duloxetine'),\n" +
                "(31,'Vibramycin','doxycycline hyclate'),\n" +
                "(32,'Vasotec','enalapril'),\n" +
                "(33,'Lexapro','escitalopram'),\n" +
                "(34,'Nexium','esomeprazole'),\n" +
                "(35,'Zetia','ezetimibe'),\n" +
                "(36,'Tricor','fenofibrate'),\n" +
                "(37,'Allegra','fexofenadine'),\n" +
                "(38,'Diflucan','fluconozole'),\n" +
                "(39,'Prozac','fluoxetine HCl'),\n" +
                "(40,'Advair','fluticasone and salmeterol inhaler'),\n" +
                "(41,'Flonase','fluticasone nasal spray'),\n" +
                "(42,'Folic Acid','folic acid'),\n" +
                "(43,'Lasix','furosemide'),\n" +
                "(44,'Neurontin','gabapentin'),\n" +
                "(45,'Amaryl','glimepiride'),\n" +
                "(46,'Diabeta','glyburide'),\n" +
                "(47,'Glucotrol','glipizide'),\n" +
                "(48,'Microzide','hydrochlorothiazide'),\n" +
                "(49,'Lortab','hydrocodone and acetaminophen'),\n" +
                "(50,'Motrin','ibuprophen'),\n" +
                "(51,'Lantus','insulin glargine'),\n" +
                "(52,'Imdur','isosorbide mononitrate'),\n" +
                "(53,'Prevacid','lansoprazole'),\n" +
                "(54,'Levaquin','levofloxacin'),\n" +
                "(55,'Levoxl','levothyroxine sodium'),\n" +
                "(56,'Zestoretic','lisinopril and hydrochlorothiazide'),\n" +
                "(57,'Prinivil','lisinopril'),\n" +
                "(58,'Ativan','lorazepam'),\n" +
                "(59,'Cozaar','losartan'),\n" +
                "(60,'Mevacor','lovastatin'),\n" +
                "(61,'Mobic','meloxicam'),\n" +
                "(62,'Glucophage','metformin HCl'),\n" +
                "(63,'Medrol','methylprednisone'),\n" +
                "(64,'Toprol','metoprolol succinate XL'),\n" +
                "(65,'Lopressor','metoprolol tartrate'),\n" +
                "(66,'Nasonex','mometasone'),\n" +
                "(67,'Singulair','montelukast'),\n" +
                "(68,'Naprosyn','naproxen'),\n" +
                "(69,'Prilosec','omeprazole'),\n" +
                "(70,'Percocet','oxycodone and acetaminophen'),\n" +
                "(71,'Protonix','pantoprazole'),\n" +
                "(72,'Paxil','paroxetine'),\n" +
                "(73,'Actos','pioglitazone'),\n" +
                "(74,'Klor-Con','potassium Chloride'),\n" +
                "(75,'Pravachol','pravastatin'),\n" +
                "(76,'Deltasone','prednisone'),\n" +
                "(77,'Lyrica','pregabalin'),\n" +
                "(78,'Phenergan','promethazine'),\n" +
                "(79,'Seroquel','quetiapine'),\n" +
                "(80,'Zantac','ranitidine'),\n" +
                "(81,'Crestor','rosuvastatin'),\n" +
                "(82,'Zoloft','sertraline HCl'),\n" +
                "(83,'Viagra','sildenafil HCl'),\n" +
                "(84,'Vytorin','simvastatin and ezetimibe'),\n" +
                "(85,'Zocor','simvastatin'),\n" +
                "(86,'Aldactone','spironolactone'),\n" +
                "(87,'Bactrim DS','sulfamethoxazole and trimethoprim DS'),\n" +
                "(88,'Flomax','tamsulosin'),\n" +
                "(89,'Restoril','temezepam'),\n" +
                "(90,'Topamax','topiramate'),\n" +
                "(91,'Ultram','tramadol'),\n" +
                "(92,'Aristocort','triamcinolone Ace topical'),\n" +
                "(93,'Desyrel','trazodone HCl'),\n" +
                "(94,'Dyazide','triamterene and hydrochlorothiazide'),\n" +
                "(95,'Valtrex','valaciclovir'),\n" +
                "(96,'Diovan','valsartan'),\n" +
                "(97,'Effexor XR','venlafaxine XR'),\n" +
                "(98,'Calan SR','verapamil SR'),\n" +
                "(99,'Ambien','zolpidem');";
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    public static void addprescription(Connection connection, Random random) throws SQLException, ParseException {
        int patientid = random.nextInt(100)+1;
        int drugid = random.nextInt(99)+1;
        int physicianid = random.nextInt(10)+1;
        int pharmacyid = random.nextInt(10)+1;
        int Quanity = 50 * random.nextInt(4)+50;
        double price = random.nextDouble()*100 + 10;
        int month = random.nextInt(12);
        String a = month + "/" + random.nextInt(31) + "/" + (random.nextInt(100) + 1923);
        if (month < 10) {
            a = "0" + a;
        }
        Date date = new SimpleDateFormat("MM/dd/yyyy").parse(a);

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
        String drugNameSql = "(select case when rand() > 0.5 then (select trade_name from drug where drug_id = %d) else (select formula from drug where drug_id=%d) end)".formatted(drugid, drugid);
        String sql = "insert into prescription(patientid, drug_id, physicianid, pharmacyid, drugname, quantity, rxprice, prescriptiondate, prescriptionfilldate) values ('%d', '%d', '%d', '%d', %s, '%d', '%f', '%s', '%s')".formatted(patientid, drugid, physicianid, pharmacyid, drugNameSql, Quanity, price, dateString, dateString);
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

    public static String RandomWord(Random random) {
        int length = random.nextInt(4)+8;
        boolean add_vowel = random.nextBoolean();
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (add_vowel) {
                word.append(vowels[random.nextInt(vowels.length)]);
            } else {
                word.append(consonants[random.nextInt(consonants.length)]);
            }
            add_vowel = !add_vowel;
        }
        return word.toString();
    }

    public static String[] trade_names = new String[] {
            "Acetaminophen",
            "Adderall",
            "Amitriptyline",
            "Amlodipine",
            "Amoxicillin",
            "Ativan",
            "Atorvastatin",
            "Azithromycin",
            "Benzonatate",
            "Brilinta",
            "Bunavail",
            "Buprenorphine",
            "Cephalexin",
            "Ciprofloxacin",
            "Citalopram",
            "Clindamycin",
            "Clonazepam",
            "Cyclobenzaprine",
            "Cymbalta",
            "Entresto",
            "Entyvio",
            "Farxiga",
            "Fentanyl Patch",
            "Gabapentin",
            "Gilenya",
            "Humira",
            "Hydrochlorothiazide",
            "Hydroxychloroquine",
            "Ibuprofen",
            "Imbruvica",
            "Invokana",
            "Januvia",
            "Jardiance",
            "Kevzara",
            "Lexapro",
            "Lisinopril",
            "Lofexidine",
            "Loratadine",
            "Lyrica",
            "Melatonin",
            "Meloxicam",
            "Metformin",
            "Methadone",
            "Methotrexate",
            "Metoprolol",
            "Naloxone",
            "Naltrexone",
            "Naproxen",
            "Narcan",
            "Nurtec",
            "Omeprazole",
            "Onpattro",
            "Otezla",
            "Ozempic",
            "Pantoprazole",
            "Plan B",
            "Prednisone",
            "Probuphine",
            "Rybelsus",
            "secukinumab",
            "Sublocade",
            "Tramadol",
            "Trazodone",
            "Viagra",
            "Wegovy",
            "Wellbutrin",
            "Xanax",
            "Zubsolv",
            "Dabigatran",
            "Dapagliflozin",
            "Darzalex",
            "Dayvigo",
            "Decadron",
            "Degarelix",
            "Delzicol",
            "Denosumab",
            "Depakote",
            "Descovy",
            "Desloratadine",
            "Desmopressin",
            "Desvenlafaxine",
            "Desyrel",
            "Detrol",
            "Dexamethasone",
            "Dexamethasone Intensol",
            "Dexilant",
            "Dextroamphetamine",
            "Dextromethorphan",
            "Dextrose",
            "Diacomit",
            "Diastat",
            "Diazepam",
            "Diclofenac",
            "Dicyclomine",
            "Diflucan",
            "Digoxin",
            "Dilantin",
            "Dilaudid",
            "Diltiazem",
            "Diovan",
            "Dipentum",
            "Diphenhydramine",
            "Diprivan",
            "Diprolene",
            "Divalproex",
            "Divalproex sodium",
            "Docusate Sodium",
            "Donepezil",
            "Doxazosin",
            "Doxepin",
            "Doxycycline",
            "Dulaglutide",
            "Dulcolax",
            "Duloxetine",
            "DuoNeb",
            "Duopa",
            "Dupixent",
            "Dutasteride"

    };

    public static String[] formulas = new String[]{
            "C8H9NO2", //Tylenol
            "C13H18O2", //Ibuprofen
            "C8H10N4O2", //Caffeine
            "C9H8O4" //Aspirin
    };

    public static String[] vowels = new String[]{
            "a", "e", "i", "o", "u"
    };

    public static String[] consonants = new String[]{
            "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z"
    };

    public static String[] titles = new String[]{
            "Family medicine",
            "Internal Medicine"
//            "Pediatrician",
//            "Obstetricians/gynecologist (OBGYNs)",
//            "Cardiologist",
//            "Oncologist",
//            "Gastroenterologist",
//            "Pulmonologist",
//            "Infectious disease",
//            "Nephrologist",
//            "Endocrinologist",
//            "Ophthalmologist",
//            "Otolaryngologist",
//            "Dermatologist",
//            "Psychiatrist",
//            "Neurologist",
//            "Radiologist",
//            "Anesthesiologist",
//            "Surgeon",
//            "Physician executive"
    };

    public static String[] states = new String[]{
            "AL",
            "AK",
            "AS",
            "AZ",
            "AR",
            "CA",
            "CO",
            "CT",
            "DE",
            "DC",
            "FL",
            "GA",
            "GU",
            "HI",
            "ID",
            "IL",
            "IN",
            "IA",
            "KS",
            "KY",
            "LA",
            "ME",
            "MD",
            "MA",
            "MI",
            "MN",
            "MS",
            "MO",
            "MT",
            "NE",
            "NV",
            "NH",
            "NJ",
            "NM",
            "NY",
            "NC",
            "ND",
            "MP",
            "OH",
            "OK",
            "OR",
            "PA",
            "PR",
            "RI",
            "SC",
            "SD",
            "TN",
            "TX",
            "UT",
            "VT",
            "VA",
            "VI",
            "WA",
            "WV",
            "WI",
            "WY"
    };

    public static String[] Names = new String[]{
            "Olivia", "Noah",
            "Emma", "Liam",
            "Charlotte", "Oliver",
            "Ava", "Elijah",
            "Amelia", "Leo",
            "Isabella", "Lucas",
            "Lily", "Ethan",
            "Luna", "Mateo",
            "Sophia", "James",
            "Mia", "Aiden",
            "Harper", "Asher",
            "Aurora", "Luca",
            "Grace", "Jack",
            "Violet", "Mason",
            "Ellie", "Michael",
            "Aria", "Theo",
            "Evelyn", "Benjamin",
            "Ella", "Logan",
            "Elena", "Luke",
            "Nova", "Jayden",
            "Sofia", "Ezra",
            "Abigail", "Henry",
            "Layla", "Levi",
            "Maya", "Carter",
            "Victoria", "Daniel",
            "Chloe", "Kai",
            "Gianna", "William",
            "Willow", "Jackson",
            "Athena", "Sebastian",
            "Emily", "Elias",
            "Isla", "Hudson",
            "Elizabeth", "Julian",
            "Hazel", "Alexander",
            "Ivy", "Gabriel",
            "Nora", "Maverick",
            "Scarlett", "Muhammad",
            "Avery", "Samuel",
            "Zoey", "Thomas",
            "Delilah", "Adam",
            "Hannah", "David",
            "Gabriella", "John",
            "Mila", "Josiah",
            "Emilia", "Nathan",
            "Lucy", "Waylon",
            "Madison", "Grayson",
            "Paisley", "Wyatt",
            "Kinsley", "Greyson",
            "Penelope", "Jacob",
            "Serenity", "Matthew",
            "Autumn", "Micah",
            "Ayla", "Atlas",
            "Eliana", "Isaiah",
            "Leah", "Joseph",
            "Maria", "Owen",
            "Savannah", "Anthony",
            "Sophie", "Jaxon",
            "Valentina", "Jeremiah",
            "Zoe", "Ryder",
            "Jade", "Theodore",
            "Josie", "Kingston",
            "Leilani", "Austin",
            "Aaliyah", "Cameron",
            "Addison", "Enzo",
            "Eva", "Milo",
            "Iris", "Amir",
            "Nevaeh", "Christopher",
            "Amara", "Ezekiel",
            "Brooklyn", "Joshua",
            "Eleanor", "Rowan",
            "Faith", "Adrian",
            "Madelyn", "Caleb",
            "Parker", "Charlie",
            "Adalynn", "Cooper",
            "Alice", "Dominic",
            "Aubrey", "Aaron",
            "Audrey", "Isaac",
            "Camila", "Jameson",
            "Riley", "Kayden",
            "Amaya", "King",
            "Bella", "Lincoln",
            "Daisy", "Matteo",
            "Freya", "Nolan",
            "Hailey", "Zion",
            "Jasmine", "Ace",
            "Naomi", "Dylan",
            "Oakley", "Hunter",
            "Ariella", "Miles",
            "Eden", "Weston",
            "Kehlani", "Colton",
            "Lydia", "Giovanni",
            "Myla", "Ian",
            "Sarah", "Jaxson",
            "Sienna", "Nathaniel",
            "Arya", "Silas",
            "Melody", "Xavier",
            "Princess", "Ali",
            "Quinn", "Andrew",
            "Selena", "Eli",
            "Alora", "Finn",
            "Ariana", "Landon"
    };

    public static String[] lastnames=new String[]{
            "Martin",

            "Lee",

            "Perez",


            "Thompson",

            "White",

            "Harris",

            "Sanchez",

            "Clark",

            "Ramirez",

            "Lewis",

            "Robinson",

            "Walker",

            "Young",

            "Allen",

            "King",

            "Wright",

            "Scott",

            "Torres",

            "Nguyen",

            "Hill",

            "Flores",

            "Green",

            "Adams",

            "Nelson",

            "Baker",

            "Hall",

            "Rivera",

            "Campbell",

            "Mitchell",

            "Carter",

            "Roberts"
    };
}