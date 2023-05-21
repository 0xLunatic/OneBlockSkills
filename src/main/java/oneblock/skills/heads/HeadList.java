package oneblock.skills.heads;

import oneblock.skills.Main;
import org.bukkit.inventory.ItemStack;

public enum HeadList {
    SPIRIT("ZTY3OTkxOGU1MmYzZjhmMmNhYmJiZWFjNmE5NzY4MWYyZjhhYTEwYzBiMmU4MTg1OTI4ODVhNGEwZTlkMjI3In19fQ==","spirit"),
    LIGHTBLUE("ZjA1MmJlMWMwNmE0YTMyNTEyOWQ2ZjQxYmI4NGYwZWExY2E2ZjlmNjllYmRmZmY0MzE2ZTc0MjQ1MWM3OWMyMSJ9fX0=","lightblue"),
    BLUE("Zjg2OGU2YTVjNGE0NDVkNjBhMzA1MGI1YmVjMWQzN2FmMWIyNTk0Mzc0NWQyZDQ3OTgwMGM4NDM2NDg4MDY1YSJ9fX0=","blue"),
    LIME("N2EyZGYzMTViNDM1ODNiMTg5NjIzMWI3N2JhZTFhNTA3ZGJkN2U0M2FkODZjMWNmYmUzYjJiOGVmMzQzMGU5ZSJ9fX0=","lime"),
    GREEN("YTI2ZWM3Y2QzYjZhZTI0OTk5NzEzN2MxYjk0ODY3YzY2ZTk3NDk5ZGEwNzFiZjUwYWRmZDM3MDM0MTMyZmEwMyJ9fX0=","green"),
    ORANGE("ZWVmMTYyZGVmODQ1YWEzZGM3ZDQ2Y2QwOGE3YmY5NWJiZGZkMzJkMzgxMjE1YWE0MWJmZmFkNTIyNDI5ODcyOCJ9fX0=","orange"),
    PINK("NGY4NTUyMmVlODE1ZDExMDU4N2ZmZmM3NDExM2Y0MTlkOTI5NTk4ZTI0NjNiOGNlOWQzOWNhYTlmYjZmZjVhYiJ9fX0=","pink"),

    JelloBucketEmpty("MTZhYjZkMDgxMmZlN2JmMzQ0NDZjMmJhY2I4ZmIwOTQ1MTMwNWRiMzE5ZDUwNDIxZDlmN2FkMzFkYzIxYmRmNiJ9fX0=", "JelloBucketEmpty"),
    JelloBucketFull("NzNlOGQ3NDZlNGJjMWZjMjlkNWUzYmQ1MzIyYjI5ODZjYmFmODZiMDY2ZmE4MzYxOGJmMjQ2ZmRmMzczIn19fQ==", "JelloBucketFull");

    private final ItemStack item;
    private final String idTag;
    private final String url;
    public String prefix = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";

    HeadList(String texture, String id){
        item = Main.createSkull(prefix + texture, id);
        idTag = id;
        url = prefix + texture;
    }
    public String getUrl(){
        return url;
    }
    public ItemStack getItemStack() {
        return item;
    }
    public String getName() {
        return idTag;
    }
}
