package me.shedaniel.betterloadingscreen;

import blue.endless.jankson.Comment;
import net.minecraft.util.StringUtil;

public class BetterLoadingScreenConfig {
    @Comment("\nWhether the memory usage bar should be drawn.\nDefault: true")
    public boolean rendersMemoryBar = true;
    @Comment("\nWhether the current hints on the top left should be drawn.\nDefault: true")
    public boolean rendersHint = true;
    @Comment("\nRenders the logo on the loading screen.\nDefault: true")
    public boolean rendersLogo = true;
    @Comment("\nDeclares the background color of the loading screen.\nDefault: #EF323D")
    public String backgroundColor = "#EF323D";
    @Comment("\nDeclares the bar color of the loading screen.\nDefault: #FFFFFF")
    public String barColor = "#FFFFFF";
    @Comment("\nDeclares the bar frame color of the loading screen.\nDefault: #FFFFFF")
    public String barFrameColor = "#FFFFFF";
    @Comment("\nDeclares the text color of the loading screen.\nDefault: #FFFFFF")
    public String textColor = "#FFFFFF";
    @Comment("\nDeclares the logo color of the loading screen.\nDefault: #FFFFFF")
    public String logoColor = "#FFFFFF";
    @Comment("\nDetects KubeJS to grab the color from the KubeJS config.\nDefault: true")
    public boolean detectKubeJS = true;
    @Comment("\nDetects All The Tweaks to grab the background image.\nDefault: true")
    public boolean detectAllTheTweaks = true;
    
    public static int getColor(String value, int def) {
        if (StringUtil.isNullOrEmpty(value) || value.equals("default")) {
            return def;
        }
        
        try {
            return Integer.decode(value.startsWith("#") ? value : ("#" + value));
        } catch (Exception ex) {
            ex.printStackTrace();
            return def;
        }
    }
}
