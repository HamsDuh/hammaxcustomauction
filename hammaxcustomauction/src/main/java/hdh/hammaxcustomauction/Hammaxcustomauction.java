package hdh.hammaxcustomauction;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

public final class Hammaxcustomauction extends JavaPlugin {

    private static Hammaxcustomauction instance;
    private DBQueue dbQueue;
    private DBManager dbManager;
    private AuctionManager auctionManager;

    private static EconomyManager economyManager;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        // Plugin startup logic

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economyManager = new EconomyManager(econ);

        SQLSetup.init(this);
        SQLSetup.setupSQL();

        instance = this;

        dbQueue= new DBQueue();
        dbQueue.start();

        dbManager = new DBManager();

        this.getCommand("ah").setExecutor(new TestCommand());           //alle 3 öffnen ah UI
        this.getCommand("auktion").setExecutor(new TestCommand());
        this.getCommand("auction").setExecutor(new TestCommand());
        this.getCommand("npcopenah").setExecutor(new NpcOpenAHCommand());   //von ah-NPC ausgeführter befehl, nicht von spielern nutzbar
        this.getCommand("ahadmin").setExecutor(new AHAdminCommand());       //öffnet Team-UI
        this.getCommand("aset").setExecutor(new ASetCommand());             //befehl um alle werte zu setzen, erkennt eingabe an metadata
        getServer().getPluginManager().registerEvents(new UIListener(),this);   //listener der alle funktionen verwaltet

        auctionManager = new AuctionManager(this);         //automatische minütliche aktuallisierung unr auszahlung von auktionen
        auctionManager.start();

        SettingsManager.init(this);     //config yml bereit machen
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        auctionManager.stop();
    }

    //21.11. 2h
    //22.11. 5h 20min
    //24.11. 2h 40min   ->10h   Total
    //25.11.    30min
    //28.11.    30min
    //2.12.  5h 45min
    //3.12.  4h 15min   ->21h   Total
    //5.12.  1h
    //29.01. 2h 30min
    //30.01.    20min
    //1.2.   2h 10min   ->27h Total
    //2.2.   4h 30min
    //4.2.   1h 40min
    //15.02. 1h 30min
    //16.02.    20min   ->35h Total
    //17.02. 2h 30min
    //18.02. 5h 30min   ->43h Total
    //19.02. 3h 15min
    //20.02. 3h 40min
    //21.02. 1h  5min   ->51h Total
    //22.02. 1h 10min
    //23.02. 1h 30min
    //24.02.    50min
    //26.02. 2h 30min   ->57h Total
    //1.3.      15min
    //2.3.   1h 15min
    //22.3.     15min
    //25.3.  2h
    //26.3.  2h
    //27.3.     15min   ->63h Total
    //28.3.  1h 45min
    //29.3.  2h 30min
    //31.3.  2h 20min
    //1.4.   1h 25min   ->71h Total
    //1.4.      40min
    //2.4.   1h
    //3.4.   1h
    //4.4.   1h 50min
    //5.4.   3h
    //6.4.      30min   ->79h Total
    //7.4.   1h 30min
    //8.4.   2h 40min
    //9.4.      50min
    //10.4.  1h 30min
    //12.4.  1h
    //24.4.  2h 30min   ->89h Total
    //25.4.  3h
    //26.4.  4h
    //27.4.  2h         ->98h Total     //Total abgeholt
    //29.4.  2h 15min                   //abgeholt              ende phase
    //1.5.   2h 30min                   //eingetragen           neue phase
    //1.5.   1h 45min                   //eingetragen

    //To do:

    //groß klein schreibung                             -done
    //Debug bei seite aktuallisieren                    -done
    //Crafter                                           -done
    //kolben ohne                                       -done
    //startpreis - erhöhungswert                        -done

    //schlapfen itemname fixen                          -semi done


    //grenzwert für bid increase


    //interessante items -> benachrichtigung
    //spieler statistik


    public DBQueue getDBQueue(){
        return dbQueue;
    }       //warteliste von DB befehlen

    public DBManager getDbManager(){
        return dbManager;
    }    //einfacherer zugriff auf DB - Funktionen

    public EconomyManager getEconomyManager(){return economyManager;}       //zugriff auf economy plugin

    public static Hammaxcustomauction getInstance(){
        return instance;
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

}
