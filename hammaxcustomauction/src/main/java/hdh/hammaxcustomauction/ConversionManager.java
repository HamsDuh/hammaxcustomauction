package hdh.hammaxcustomauction;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class ConversionManager {

    //Item zum in DB speichern zu bytearray machen
    //
    public static String itemToBase64(ItemStack item){
        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //Item aus DB auslesen und zu Item machen
    //
    public static ItemStack itemFromBase64(String data){
        try{
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            return (ItemStack) dataInput.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatTime(long givenTime){
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(new java.util.Date(givenTime));
    }

    public static String formatTime2(long givenTime){
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new java.util.Date(givenTime));
    }



    public static double signToMoneyDouble(String recieved){
        double erg = 0.0;
        char[] given = recieved.toCharArray();
        boolean firstHalf = true;
        int maxDecimal = 0;
        int maxLength = 8;
        String firstString = "0";
        String secondString = "0";

        for(int i = 0; i < given.length && maxDecimal < 2 && firstString.length() + secondString.length() < maxLength; i++){
            if(String.valueOf(given[i]).matches("[0-9]")){
                if (firstHalf){
                    if (firstString == "0" && String.valueOf(given[i]) != "0") {
                        firstString = "" + given[i];
                    }else {
                        firstString = firstString + given[i];
                    }
                }else {
                    if (secondString == "0" && String.valueOf(given[i]) != "0") {
                        secondString = "" + given[i];
                    }else {
                        secondString = secondString + given[i];
                    }
                    maxDecimal++;
                }

            } else if (String.valueOf(given[i]).equals(".") || String.valueOf(given[i]).equals(",")) {
                firstHalf = false;
            }
        }

        firstString = firstString + "." + secondString;
        erg = Double.parseDouble(firstString);

        return erg;
    }

    public static long chatToDuration(String given){

        int days = 0;

        char[] givenCa = given.toCharArray();

        for(int i = 0; i < givenCa.length && days < SettingsManager.getMaximumDeadlineInDays(); i++){
            if(String.valueOf(givenCa[i]).matches("[0-9]")){
                days = days * 10 + Character.getNumericValue(givenCa[i]);
            }
        }
        if(days < 1){
            days = 1;
        } else if (days > SettingsManager.getMaximumDeadlineInDays()) {
            days = SettingsManager.getMaximumDeadlineInDays();
        }

        return (long) days * 24L * 60L * 60L * 1000L;
    }

    public static int durationToDays(long given){
        long millisOfDay = 24L * 60L *60L * 1000L;
        return (int) (given/millisOfDay);
    }

    public static long correctTimeOffset(long given){
        return (long) SettingsManager.getTimeOffsetInH() * 60L * 60L * 1000L + given;
    }

    public static int[] convertToTimeLeft(long deadline){
        long timeLeft = deadline - System.currentTimeMillis();
        if (timeLeft <= 0){
            return new int[] {0,0,0};
        }

        long totalMinutes = timeLeft / (1000L * 60L);
        long totalHours = totalMinutes / 60L;
        long totalDays = totalHours / 24L;

        int days = (int) totalDays;
        int hours = (int) (totalHours % 24);
        int minutes = (int) (totalMinutes % 60);
        return new int[] {days,hours,minutes};
    }

    public static String normaliseMaterial(String given){
        return given.toLowerCase().replace("_"," ").replace("-", " ");
    }

}
