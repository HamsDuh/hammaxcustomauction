package hdh.hammaxcustomauction;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SettingsManager {

    public static int isProblematicMaterial(Material mat){

        Material[] problematicMaterials = {
          Material.HOPPER,
          Material.BEACON
        };

        for (int i = 0; i < problematicMaterials.length; i++){
            if (mat.equals(problematicMaterials[i])){
                return i;
            }
        }

        return -1;
    }

    public static Map<Enchantment, Integer> getAllowedEnchants(){
        Map<Enchantment, Integer> enchants = new HashMap<>();
        enchants.put(Enchantment.MENDING, 1);
        enchants.put(Enchantment.UNBREAKING, 4);
        enchants.put(Enchantment.SHARPNESS,6);
        enchants.put(Enchantment.PROTECTION,5);
        enchants.put(Enchantment.FORTUNE,4);
        enchants.put(Enchantment.RESPIRATION, 4);
        enchants.put(Enchantment.THORNS, 4);
        enchants.put(Enchantment.LOOTING, 4);
        enchants.put(Enchantment.FIRE_ASPECT, 3);
        enchants.put(Enchantment.EFFICIENCY, 6);

        return enchants;
    }

    public static Material[] getAllowedMats() {
        Material[] allowedMaterials = {
                Material.DRAGON_EGG,
                Material.GHAST_TEAR,
                Material.NETHERITE_INGOT,
                Material.NETHERITE_BLOCK,
                Material.NETHERITE_SWORD,
                Material.NETHERITE_PICKAXE,
                Material.NETHERITE_AXE,
                Material.NETHERITE_HOE,
                Material.NETHERITE_SHOVEL,
                Material.NETHERITE_HELMET,
                Material.NETHERITE_CHESTPLATE,
                Material.NETHERITE_LEGGINGS,
                Material.NETHERITE_BOOTS,
                Material.HOPPER,
                Material.SOUL_SAND,
                Material.BUDDING_AMETHYST,
                Material.BEACON,
                Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE,
                Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE
                //mending
        };
        return allowedMaterials;
    }

    public static String[] getGermanMaterialNames(){
        String[] names = {
                "Drachenei",
                "Ghast Träne",
                "Netheritbarren",
                "Netheritblock",
                "Netheritschwert",
                "Netheritschwert",
                "Netheritaxt",
                "Netherithacke",
                "Netheritschaufel",
                "Netherithelm",
                "Netheritharnisch",
                "Netheritbeinschutz",
                "Netheritstiefel",
                "Trichter",
                "Seelensand",
                "Amethystknospenblock",
                "Leuchtfeuer",
                "Stille Schmiedevorlage",
                "Augen Schmiedevorlage",
                "Rippen Schmiedevorlage",
                "Warthof Schmiedevorlage"
        };
        return names;
    }

    public static boolean allowedItemForStorage(ItemStack givenStack){

        Material[] allowedMaterials = getAllowedMats();

        for(Material mat : allowedMaterials){
            if(givenStack.getType() == mat){
                return true;
            }
        }

        //check enchants
        if (givenStack.hasItemMeta()){
            ItemMeta givenStackMeta = givenStack.getItemMeta();
            if (givenStackMeta != null && givenStackMeta.hasEnchants()){
                Map<Enchantment, Integer> itemEnchants = givenStack.getEnchantments();
                Map<Enchantment, Integer> allowedEnchants = getAllowedEnchants();

                for (Map.Entry<Enchantment, Integer> entry : itemEnchants.entrySet()){
                    Enchantment itemEnchant = entry.getKey();
                    int itemLevel = entry.getValue();

                    if (allowedEnchants.containsKey(itemEnchant)){
                        int requiredLevel = allowedEnchants.get(itemEnchant);

                        if (itemLevel >= requiredLevel){
                            return true;
                        }
                    }
                }
            }

            //suche fragment lore
            if (givenStackMeta != null && givenStackMeta.hasLore()){
                List<String> itemLore = givenStackMeta.getLore();
                if (itemLore != null){
                    for (String line : itemLore){
                        if (line.contains("zu einem Moderator")){
                            return true;
                        }
                    }
                }
            }

        }

        //mending buch ausnahme
        if (givenStack.hasItemMeta() && givenStack.getItemMeta() instanceof EnchantmentStorageMeta){
            EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) givenStack.getItemMeta();

            if (bookmeta.hasStoredEnchant(Enchantment.MENDING)){
                return true;
            }
        }

        return false;
    }

    public static String materialToGerman(Material mat){
        Material[] mats = getAllowedMats();
        String[] german = getGermanMaterialNames();
        for (int i = 0; i < mats.length; i++){
            if (mat.equals(mats[i])){
                return german[i];
            }
        }
        return "ERROR";
    }

    private static FileConfiguration config;

    public static void init(JavaPlugin plugin){
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public static void reload(JavaPlugin plugin){
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    private static int maxPlayerStorage = 18;
    private static int maxPlayerAuctions = 9;
    private static double minimumAuctionPrice = 500;
    private static double minimumAuctionIncrease = 0.01;
    private static double maximumAuctionIncrease = 2;
    private static int minimumDeadline = 1;
    private static int maximumDeadline = 31;
    private static double durationPrice = 5;
    private static double auctionPayoutMultiplyer = 0.99;

    private static boolean createAuctionAllowed = false;
    private static boolean addItemAllowed = false;


    private static int timeOffset = 0;

    public static boolean isAuctionCreationAllowed(){
        return config.getBoolean("createAuctionAllowed", createAuctionAllowed);
    }

    public static void setAuctionCreationAllowed(Player player, boolean to){
        if (player.isOp()) {
            config.set("createAuctionAllowed", to);
            Hammaxcustomauction.getInstance().saveConfig();
        }
    }

    public static boolean isAddItemAllowed(){
        return config.getBoolean("addItemAllowed", addItemAllowed);
    }

    public static void setAddItemAllowed(Player player, boolean to){
        if (player.isOp()){
            config.set("addItemAllowed", to);
            Hammaxcustomauction.getInstance().saveConfig();
        }
    }


    public static int getMaxPlayerStorage(){
        return config.getInt("maxPlayerStorage", maxPlayerStorage);
    }

    public static int getMaxPlayerAuctions(){
        return config.getInt("maxPlayerAuctions", maxPlayerAuctions);
    }

    public static double getMinimumAuctionPrice(){
        return config.getDouble("minimumAuctionPrice", minimumAuctionPrice);
    }

    public static double getMinimumAuctionIncrease(double givenValue){
        double multiplyer = config.getDouble("minimumAuctionIncrease", minimumAuctionIncrease);
        return givenValue * multiplyer;
    }

    public static double getMaximumAuctionIncrease(double givenValue)
    {
        double multiplyer = config.getDouble("maximumAuctionIncrease", maximumAuctionIncrease);
        return givenValue * multiplyer;
    }

    public static int getTimeOffsetInH(){return config.getInt("timeOffset", timeOffset);}

    public static long getMinimumDeadline(){
        return config.getLong("minimumDeadline",minimumDeadline);
    }

    public static int getMaximumDeadlineInDays(){return config.getInt("maximumDeadline",maximumDeadline);}

    public static double getDurationPrice(long duration)
    {
        double dp = config.getDouble("durationPrice",durationPrice);
        return (double) ConversionManager.durationToDays(ConversionManager.correctTimeOffset(duration)) * dp;
    }

    public static double alterAuctionPayout(double given){return given * config.getDouble("auctionPayoutMultiplyer", auctionPayoutMultiplyer); }

    public static int getDBCheckTimer(){return 3;}


    public static boolean problematicsMatch(ItemStack stack1, ItemStack stack2){

        //same material?
        if (!stack1.getType().equals(stack2.getType()) || stack1.getAmount() != stack2.getAmount()){
            //System.out.println("UnequalType or amount");
            return false;
        }

        ItemMeta stack1meta = stack1.hasItemMeta() ? stack1.getItemMeta() : null;
        ItemMeta stack2meta = stack2.hasItemMeta() ? stack2.getItemMeta() : null;

        //has meta?
        if (stack1meta != null || stack2meta != null  /*stack1.hasItemMeta() || stack2.hasItemMeta()*/){
            if (stack1meta == null && stack2meta == null  /*stack1.hasItemMeta() != stack2.hasItemMeta()*/){
                //System.out.println("EqualMeta");

                //wenn leer sonst auch wahr
                return true;
            } /*else if ((!stack1meta.hasDisplayName() && !stack1meta.hasLore() && !stack1meta.hasEnchants()) && (!stack2meta.hasDisplayName() && !stack2meta.hasLore() && !stack2meta.hasEnchants())) {
                System.out.println("AlternativeEqualMeta");
                return true;
            } */

            if (stack2meta == null && stack1meta != null){
                if (!stack1meta.hasDisplayName() && !stack1meta.hasLore() && !stack1meta.hasEnchants()){
                    //System.out.println("AlternativeEqualMeta");
                    return true;
                }
            }

            //ItemMeta stack1meta = stack1.getItemMeta();
            //ItemMeta stack2meta = stack2.getItemMeta();

            //check name
            if (stack1meta.hasDisplayName() || stack2meta.hasDisplayName()){
                if (stack1meta.hasDisplayName() != stack2meta.hasDisplayName()){
                    //System.out.println("UnequalName");
                    return false;
                } else if (!stack1meta.getDisplayName().equals(stack2meta.getDisplayName())) {
                    //System.out.println("WrongName");
                    return false;
                }
            }

            //check lore
            if (stack1meta.hasLore() || stack2meta.hasLore()){
                if (stack1meta.hasLore() != stack2meta.hasLore()){
                    //System.out.println("unequalLore");
                    return false;
                } else if (!Objects.equals(stack1meta.getLore(), stack2meta.getLore())  /*!stack1meta.getLore().equals(stack2meta.getLore())*/){
                    //System.out.println("WrongLore");
                    return false;
                }
            }

            if (stack1meta.hasEnchants() || stack2meta.hasEnchants()){
                if(stack1meta.hasEnchants() != stack2meta.hasEnchants()){
                    //System.out.println("unequalEnchants");
                    return false;
                } else if (!stack1meta.getEnchants().equals(stack2meta.getEnchants())) {
                    //System.out.println("WrongEnchants");
                    return false;
                }
            }
            //item has meta
        }
        //System.out.println("ReturnTrue");
        return true;
    }

}
